import { describe, it, expect } from 'vitest';
import {
  describeStatus,
  isActionable,
  validateInviteEmail,
  normalizeInviteEmail,
  parseExpiryHours,
  buildIssueRequest,
  formatTimestamp,
  expiryUrgency,
  issuerLabel,
  type SuperUserInvite,
} from './superuser-invites';

/**
 * Permutation matrix for the superuser-invite admin screen's pure logic (#91): status derivation
 * (including the lazy-TTL case where the server still reports PENDING past expiry), email
 * validation/normalization, expiry-hours parsing, the issue-request assembly, timestamp
 * formatting, and urgency bucketing. Redemption is out of scope here -- the invitee performs it;
 * the admin screen only issues, lists, and revokes.
 */
const HOUR = 60 * 60 * 1000;

describe('describeStatus', () => {
  it('passes through non-pending statuses verbatim', () => {
    expect(describeStatus({ status: 'REDEEMED', expiry: futureMs() })).toBe('REDEEMED');
    expect(describeStatus({ status: 'REVOKED', expiry: futureMs() })).toBe('REVOKED');
  });

  it('reports PENDING while unexpired', () => {
    const now = Date.now();
    expect(describeStatus({ status: 'PENDING', expiry: now + HOUR }, now)).toBe('PENDING');
  });

  it('reports EXPIRED for a PENDING invite past expiry (lazy TTL)', () => {
    const now = Date.now();
    expect(describeStatus({ status: 'PENDING', expiry: now - 1 }, now)).toBe('EXPIRED');
  });

  it('treats expiry exactly at now as expired', () => {
    const now = Date.now();
    expect(describeStatus({ status: 'PENDING', expiry: now }, now)).toBe('EXPIRED');
  });

  it('tolerates a missing expiry on a pending invite', () => {
    expect(describeStatus({ status: 'PENDING', expiry: null })).toBe('PENDING');
    expect(describeStatus({ status: 'PENDING', expiry: undefined })).toBe('PENDING');
  });

  it('accepts ISO strings and epoch millis for expiry', () => {
    const iso = new Date(Date.now() + HOUR).toISOString();
    expect(describeStatus({ status: 'PENDING', expiry: iso })).toBe('PENDING');
    expect(describeStatus({ status: 'PENDING', expiry: Date.now() + HOUR })).toBe('PENDING');
  });
});

describe('isActionable', () => {
  it('is true only for unexpired pending invites', () => {
    const now = Date.now();
    expect(isActionable({ status: 'PENDING', expiry: now + HOUR }, now)).toBe(true);
    expect(isActionable({ status: 'PENDING', expiry: now - HOUR }, now)).toBe(false);
    expect(isActionable({ status: 'REDEEMED', expiry: now + HOUR }, now)).toBe(false);
    expect(isActionable({ status: 'REVOKED', expiry: now + HOUR }, now)).toBe(false);
  });
});

describe('validateInviteEmail', () => {
  it('accepts ordinary addresses', () => {
    expect(validateInviteEmail('admin@example.com')).toBeNull();
  });

  it('requires an address', () => {
    expect(validateInviteEmail('')).toBe('An email address is required.');
    expect(validateInviteEmail('   ')).toBe('An email address is required.');
    expect(validateInviteEmail(null)).toBe('An email address is required.');
    expect(validateInviteEmail(undefined)).toBe('An email address is required.');
  });

  it('rejects obviously malformed input', () => {
    expect(validateInviteEmail('not-an-email')).not.toBeNull();
    expect(validateInviteEmail('two@at@signs')).not.toBeNull();
    expect(validateInviteEmail('spaces in@address')).not.toBeNull();
    expect(validateInviteEmail('@no-local-part')).not.toBeNull();
    expect(validateInviteEmail('no-domain@')).not.toBeNull();
  });

  it('accepts addresses with plus addressing and subdomains', () => {
    expect(validateInviteEmail('first.last+tag@mail.example.com')).toBeNull();
  });
});

describe('normalizeInviteEmail', () => {
  it('trims and lowercases, mirroring the server', () => {
    expect(normalizeInviteEmail('  Admin@Example.COM  ')).toBe('admin@example.com');
  });

  it('tolerates null/undefined', () => {
    expect(normalizeInviteEmail(null)).toBe('');
    expect(normalizeInviteEmail(undefined)).toBe('');
  });
});

describe('parseExpiryHours', () => {
  it('treats blank as server default', () => {
    expect(parseExpiryHours('')).toEqual({ error: null, expiryHours: undefined });
    expect(parseExpiryHours('   ')).toEqual({ error: null, expiryHours: undefined });
    expect(parseExpiryHours(null)).toEqual({ error: null, expiryHours: undefined });
  });

  it('parses positive integers', () => {
    expect(parseExpiryHours('24')).toEqual({ error: null, expiryHours: 24 });
    expect(parseExpiryHours(' 1 ')).toEqual({ error: null, expiryHours: 1 });
  });

  it('rejects non-numeric and non-integer input', () => {
    expect(parseExpiryHours('abc')!.error).not.toBeNull();
    expect(parseExpiryHours('1.5')!.error).not.toBeNull();
    expect(parseExpiryHours('-4')!.error).not.toBeNull();
  });

  it('rejects zero', () => {
    expect(parseExpiryHours('0')!.error).not.toBeNull();
  });
});

describe('buildIssueRequest', () => {
  it('assembles the POST body with a normalized email and no expiryHours when blank', () => {
    const { error, request } = buildIssueRequest('  Admin@Example.COM  ', '');
    expect(error).toBeNull();
    expect(request).toEqual({ email: 'admin@example.com' });
    expect('expiryHours' in (request ?? {})).toBe(false);
  });

  it('carries explicit expiry hours through', () => {
    const { error, request } = buildIssueRequest('admin@example.com', '48');
    expect(error).toBeNull();
    expect(request).toEqual({ email: 'admin@example.com', expiryHours: 48 });
  });

  it('returns the email error before considering hours', () => {
    const { error, request } = buildIssueRequest('nope', 'abc');
    expect(request).toBeNull();
    expect(error).toBe('Enter a valid email address.');
  });

  it('returns the hours error when the email is fine', () => {
    const { error, request } = buildIssueRequest('admin@example.com', '1.5');
    expect(request).toBeNull();
    expect(error).not.toBeNull();
  });
});

describe('formatTimestamp', () => {
  it('renders a dash for absent timestamps', () => {
    expect(formatTimestamp(null)).toBe('—');
    expect(formatTimestamp(undefined)).toBe('—');
    expect(formatTimestamp('')).toBe('—');
  });

  it('renders epoch millis and ISO strings without throwing', () => {
    // Deliberately no text assertion on epoch 0: toLocaleString output is timezone-dependent.
    expect(formatTimestamp(0)).not.toBe('—');
    expect(formatTimestamp('2026-01-01T00:00:00Z')).not.toBe('—');
  });

  it('renders unparseable strings as a dash rather than Invalid Date', () => {
    expect(formatTimestamp('garbage')).toBe('—');
  });
});

describe('expiryUrgency', () => {
  it('is none for terminal statuses regardless of expiry', () => {
    const now = Date.now();
    expect(expiryUrgency({ status: 'REDEEMED', expiry: now - HOUR }, now)).toBe('none');
    expect(expiryUrgency({ status: 'REVOKED', expiry: now + HOUR }, now)).toBe('none');
  });

  it('buckets remaining time', () => {
    const now = Date.now();
    expect(expiryUrgency({ status: 'PENDING', expiry: now - 1 }, now)).toBe('expired');
    expect(expiryUrgency({ status: 'PENDING', expiry: now + 30 * 60 * 1000 }, now)).toBe('critical');
    expect(expiryUrgency({ status: 'PENDING', expiry: now + 6 * HOUR }, now)).toBe('warning');
    expect(expiryUrgency({ status: 'PENDING', expiry: now + 3 * 24 * HOUR }, now)).toBe('ok');
  });

  it('treats a missing expiry as ok (server-managed lifetime)', () => {
    expect(expiryUrgency({ status: 'PENDING', expiry: null })).toBe('ok');
  });
});

describe('issuerLabel', () => {
  it('prefers the issuer name, then id, then a dash', () => {
    expect(issuerLabel({ invitedBy: { id: 'u1', name: 'Keith' } })).toBe('Keith');
    expect(issuerLabel({ invitedBy: { id: 'u1' } })).toBe('u1');
    expect(issuerLabel({ invitedBy: null })).toBe('—');
    expect(issuerLabel({ invitedBy: {} })).toBe('—');
  });
});

function futureMs(): number {
  return Date.now() + HOUR;
}
