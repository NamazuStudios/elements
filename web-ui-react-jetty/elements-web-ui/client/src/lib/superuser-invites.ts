/**
 * Pure mapping/validation logic for the superuser-invite admin screen (#91), kept out of the
 * component so it can be unit-tested exhaustively. The wire shapes mirror the REST surface:
 *
 * - `POST /api/rest/invite/superuser` issues an invite ({@link CreateSuperUserInviteRequest}:
 *   `email`, optional `expiryHours`) and returns a `SuperUserInvite`.
 * - `GET  /api/rest/invite/superuser` lists invites (newest first, `Pagination` envelope).
 * - `DELETE /api/rest/invite/superuser/{token}` revokes a pending invite.
 * - Redemption is NOT performed here: the invitee signs in and redeems the token themselves
 *   (`POST /api/rest/invite/superuser/redeem`), which is what elevates their account.
 */

export type InviteStatus = 'PENDING' | 'REDEEMED' | 'REVOKED' | 'EXPIRED';

export interface SuperUserInvite {
  token: string;
  invitedEmail: string;
  invitedBy?: { id?: string; name?: string } | null;
  expiry?: string | number | null;
  createdAt?: string | number | null;
  redeemedAt?: string | number | null;
  revokedAt?: string | number | null;
  status: InviteStatus;
}

/**
 * The display status of an invite. A PENDING invite whose expiry has passed displays as Expired
 * even if the server still reports PENDING (MongoDB's TTL index cleans up lazily).
 */
export function describeStatus(invite: Pick<SuperUserInvite, 'status' | 'expiry'>, now: number = Date.now()): InviteStatus {
  if (invite.status !== 'PENDING') return invite.status;
  const expiry = toMillis(invite.expiry);
  if (expiry !== null && expiry <= now) return 'EXPIRED';
  return 'PENDING';
}

/**
 * Whether an invite can still be redeemed: PENDING (or server-reported pending) and unexpired.
 * Used to enable/disable the revoke action.
 */
export function isActionable(invite: Pick<SuperUserInvite, 'status' | 'expiry'>, now: number = Date.now()): boolean {
  return describeStatus(invite, now) === 'PENDING';
}

/**
 * Validates the invitee email as typed into the issue form. Returns an error message, or null if
 * the address is acceptable. Normalization (trim + lowercase) is applied server-side too, but the
 * form surfaces it early.
 */
export function validateInviteEmail(raw: string | undefined | null): string | null {
  const email = (raw ?? '').trim();
  if (!email) return 'An email address is required.';
  // Deliberately minimal: a full RFC 5322 grammar is overkill for a form; the server re-validates
  // with Jakarta @Email. This catches the obvious typos and empty submits.
  if (!/^[^\s@]+@[^\s@]+$/.test(email)) return 'Enter a valid email address.';
  return null;
}

/**
 * Normalizes the invitee email the way the server does (trim + lowercase) so the form can echo
 * exactly what will be stored.
 */
export function normalizeInviteEmail(raw: string | undefined | null): string {
  return (raw ?? '').trim().toLowerCase();
}

/**
 * Parses the optional expiry-hours field as typed into the form. Blank means "use the server
 * default". Returns an error message for non-positive-integer input, or null with the parsed
 * value (undefined when blank).
 */
export function parseExpiryHours(
  raw: string | undefined | null,
): { error: string | null; expiryHours: number | undefined } {
  const trimmed = (raw ?? '').trim();
  if (!trimmed) return { error: null, expiryHours: undefined };
  if (!/^\d+$/.test(trimmed)) return { error: 'Expiry must be a whole number of hours.', expiryHours: undefined };
  const hours = Number(trimmed);
  if (hours <= 0) return { error: 'Expiry must be at least one hour.', expiryHours: undefined };
  return { error: null, expiryHours: hours };
}

/**
 * Builds the `POST /api/rest/invite/superuser` body from the form state. Returns an error message
 * instead of a request when validation fails (checked as `typeof result === 'string'` is NOT how
 * this is consumed -- use the discriminated shape).
 */
export function buildIssueRequest(
  emailRaw: string | undefined | null,
  expiryHoursRaw: string | undefined | null,
): { error: string | null; request: { email: string; expiryHours?: number } | null } {
  const emailError = validateInviteEmail(emailRaw);
  if (emailError) return { error: emailError, request: null };

  const hours = parseExpiryHours(expiryHoursRaw);
  if (hours.error) return { error: hours.error, request: null };

  return {
    error: null,
    request: {
      email: normalizeInviteEmail(emailRaw),
      ...(hours.expiryHours !== undefined ? { expiryHours: hours.expiryHours } : {}),
    },
  };
}

/**
 * Human-readable timestamp for the table, tolerant of null and of ms-epoch vs ISO strings.
 */
export function formatTimestamp(ts: string | number | null | undefined): string {
  const ms = toMillis(ts);
  if (ms === null) return '—';
  return new Date(ms).toLocaleString();
}

/**
 * Coarse urgency bucket for a pending invite's remaining time, driving the badge color.
 */
export function expiryUrgency(
  invite: Pick<SuperUserInvite, 'status' | 'expiry'>,
  now: number = Date.now(),
): 'none' | 'expired' | 'critical' | 'warning' | 'ok' {
  const status = describeStatus(invite, now);

  // Terminal (redeemed/revoked) invites have no live deadline to be urgent about. A pending invite
  // past expiry has already been bucketed as EXPIRED by describeStatus.
  if (status === 'REDEEMED' || status === 'REVOKED') return 'none';
  if (status === 'EXPIRED') return 'expired';

  const expiry = toMillis(invite.expiry);
  if (expiry === null) return 'ok';
  const remaining = expiry - now;
  if (remaining <= 0) return 'expired';
  if (remaining < 60 * 60 * 1000) return 'critical';
  if (remaining < 24 * 60 * 60 * 1000) return 'warning';
  return 'ok';
}

/**
 * The issuer's display label: name if present, else id, else a placeholder.
 */
export function issuerLabel(invite: Pick<SuperUserInvite, 'invitedBy'>): string {
  const by = invite.invitedBy;
  if (by?.name) return by.name;
  if (by?.id) return by.id;
  return '—';
}

function toMillis(ts: string | number | null | undefined): number | null {
  if (ts === null || ts === undefined || ts === '') return null;
  if (typeof ts === 'number') return ts;
  const parsed = Date.parse(ts);
  return Number.isNaN(parsed) ? null : parsed;
}
