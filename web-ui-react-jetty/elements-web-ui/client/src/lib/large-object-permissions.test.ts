import { describe, it, expect } from 'vitest';
import {
  parseSubjectList,
  formatSubjectList,
  toEditableSubjects,
  toSubjectRequest,
  toUpdateRequest,
  describeVisibility,
  type AccessPermissions,
} from './large-object-permissions';

/**
 * Permutation matrix for the Large Object permissions editor's pure logic (#157): reading stored
 * permissions into the editable form (including the nobody/no-operation cases), writing the form
 * back to the wire shape (parsing, trimming, dedupe, empty-becomes-nobody), the full update-request
 * assembly (mime type carried through), and the visibility summary. Wire-shape fidelity matters
 * here: the server treats a non-wildcard subject list with no entries as "nobody may operate".
 */
describe('parseSubjectList', () => {
  it('parses comma-separated ids', () => {
    expect(parseSubjectList('a,b,c')).toEqual(['a', 'b', 'c']);
  });

  it('trims whitespace around ids and between commas', () => {
    expect(parseSubjectList(' a ,  b ,c  ')).toEqual(['a', 'b', 'c']);
  });

  it('drops empty entries (trailing/double commas)', () => {
    expect(parseSubjectList('a,,b,')).toEqual(['a', 'b']);
    expect(parseSubjectList(',,,')).toEqual([]);
  });

  it('de-duplicates, first occurrence wins', () => {
    expect(parseSubjectList('a,b,a')).toEqual(['a', 'b']);
  });

  it('tolerates null/undefined/empty input', () => {
    expect(parseSubjectList(null)).toEqual([]);
    expect(parseSubjectList(undefined)).toEqual([]);
    expect(parseSubjectList('')).toEqual([]);
  });
});

describe('formatSubjectList', () => {
  it('joins with a comma and space', () => {
    expect(formatSubjectList(['a', 'b'])).toBe('a, b');
  });

  it('tolerates null/undefined', () => {
    expect(formatSubjectList(null)).toBe('');
    expect(formatSubjectList(undefined)).toBe('');
  });
});

describe('toEditableSubjects', () => {
  it('reads users/profiles/minimumLevel into the raw form state', () => {
    const permissions: AccessPermissions = {
      read: {
        wildcard: true,
        users: [{ id: 'u1' }, { id: 'u2' }],
        profiles: [{ id: 'p1' }],
        minimumLevel: 'USER',
      },
    };

    expect(toEditableSubjects(permissions, 'read')).toEqual({
      wildcard: true,
      userIdsRaw: 'u1, u2',
      profileIdsRaw: 'p1',
      minimumLevel: 'USER',
    });
  });

  it('treats a missing operation as nobody (not wildcard)', () => {
    const edit = toEditableSubjects({}, 'write');
    expect(edit.wildcard).toBe(false);
    expect(edit.userIdsRaw).toBe('');
    expect(edit.profileIdsRaw).toBe('');
  });

  it('tolerates null permissions and null subject fields', () => {
    expect(toEditableSubjects(null, 'read').wildcard).toBe(false);
    expect(toEditableSubjects({ read: null }, 'read').wildcard).toBe(false);
    expect(toEditableSubjects({ read: { wildcard: false } }, 'read').userIdsRaw).toBe('');
  });
});

describe('toSubjectRequest', () => {
  it('round-trips the wildcard flag and parsed id lists', () => {
    const request = toSubjectRequest({
      wildcard: false,
      userIdsRaw: 'u1, u2, u2',
      profileIdsRaw: ' p1 ,',
    });

    expect(request).toEqual({
      wildcard: false,
      userIds: ['u1', 'u2'],
      profileIds: ['p1'],
    });
  });

  it('yields an empty (nobody) request when non-wildcard with no subjects', () => {
    const request = toSubjectRequest({ wildcard: false, userIdsRaw: '', profileIdsRaw: '' });
    expect(request).toEqual({ wildcard: false, userIds: [], profileIds: [] });
  });

  it('keeps the id lists alongside a wildcard (server ignores them, but the shape stays valid)', () => {
    const request = toSubjectRequest({ wildcard: true, userIdsRaw: 'u1', profileIdsRaw: '' });
    expect(request.wildcard).toBe(true);
    expect(request.userIds).toEqual(['u1']);
  });
});

describe('toUpdateRequest', () => {
  it('carries the object mime type and all three operations through', () => {
    const request = toUpdateRequest(
      { mimeType: 'application/json' },
      {
        read: { wildcard: true, userIdsRaw: '', profileIdsRaw: '' },
        write: { wildcard: false, userIdsRaw: 'u1', profileIdsRaw: '' },
        delete: { wildcard: false, userIdsRaw: '', profileIdsRaw: 'p1' },
      },
    );

    expect(request).toEqual({
      mimeType: 'application/json',
      read: { wildcard: true, userIds: [], profileIds: [] },
      write: { wildcard: false, userIds: ['u1'], profileIds: [] },
      delete: { wildcard: false, userIds: [], profileIds: ['p1'] },
    });
  });

  it('normalizes a null mime type (the update replaces it wholesale)', () => {
    const request = toUpdateRequest(
      { mimeType: null },
      {
        read: { wildcard: false, userIdsRaw: '', profileIdsRaw: '' },
        write: { wildcard: false, userIdsRaw: '', profileIdsRaw: '' },
        delete: { wildcard: false, userIdsRaw: '', profileIdsRaw: '' },
      },
    );
    expect(request.mimeType).toBeNull();
  });
});

describe('describeVisibility', () => {
  const base = { userIdsRaw: '', profileIdsRaw: '' };

  it('wildcard is public regardless of listed subjects', () => {
    expect(describeVisibility({ ...base, wildcard: true })).toBe('Public');
    expect(describeVisibility({ ...base, wildcard: true, userIdsRaw: 'u1' })).toBe('Public');
  });

  it('non-wildcard with subjects is restricted', () => {
    expect(describeVisibility({ ...base, userIdsRaw: 'u1' })).toBe('Restricted');
    expect(describeVisibility({ ...base, profileIdsRaw: 'p1' })).toBe('Restricted');
    expect(describeVisibility({ ...base, userIdsRaw: 'u1', profileIdsRaw: 'p1' })).toBe('Restricted');
  });

  it('non-wildcard with no subjects is no access', () => {
    expect(describeVisibility({ ...base, wildcard: false })).toBe('No access');
  });
});
