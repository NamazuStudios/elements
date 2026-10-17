/**
 * Pure mapping/parsing logic for the Large Object permissions editor (#157), kept out of the
 * component so it can be unit-tested exhaustively. The wire shapes mirror the platform's
 * `AccessPermissions`/`Subjects` model and the `SubjectRequest` update surface:
 *
 * - An object's permissions are per-operation (`read`/`write`/`delete`) `Subjects`: a `wildcard`
 *   flag (public access), the permitted users, the permitted profiles, and an optional minimum
 *   access level.
 * - Updates are sent as `SubjectRequest`s: `wildcard`, `userIds`, `profileIds`. The minimum level
 *   is not part of the REST update surface, so it is display-only in the editor.
 */

export type Operation = 'read' | 'write' | 'delete';

export const OPERATIONS: Operation[] = ['read', 'write', 'delete'];

export interface Subject {
  id: string;
}

export interface Subjects {
  wildcard: boolean;
  users: Subject[];
  profiles: Subject[];
  minimumLevel?: string | null;
}

export interface AccessPermissions {
  read?: Subjects | null;
  write?: Subjects | null;
  delete?: Subjects | null;
}

/** The editable form state for one operation's subject list. */
export interface EditableSubjects {
  wildcard: boolean;
  /** Raw, comma-separated user ids as typed into the form. */
  userIdsRaw: string;
  /** Raw, comma-separated profile ids as typed into the form. */
  profileIdsRaw: string;
  /** Display-only: the minimum access level, which the REST surface cannot change. */
  minimumLevel?: string | null;
}

/**
 * Parses a raw comma-separated id list as typed into the form: splits on commas, trims whitespace,
 * drops empty entries, and de-duplicates (first occurrence wins).
 */
export function parseSubjectList(raw: string | undefined | null): string[] {
  if (!raw) return [];
  const seen = new Set<string>();
  for (const part of raw.split(',')) {
    const trimmed = part.trim();
    if (trimmed && !seen.has(trimmed)) {
      seen.add(trimmed);
    }
  }
  return Array.from(seen);
}

/**
 * Formats a list of ids back into the comma-separated form shown in the editor.
 */
export function formatSubjectList(ids: string[] | undefined | null): string {
  return (ids ?? []).join(', ');
}

/**
 * Reads an object's stored subjects for one operation into the editable form state. Tolerates a
 * missing operation (treated as "no access" -- not wildcard, no subjects), matching the server's
 * `Subjects.nobody()` default.
 */
export function toEditableSubjects(permissions: AccessPermissions | undefined | null, operation: Operation): EditableSubjects {
  const subjects = permissions?.[operation];
  return {
    wildcard: subjects?.wildcard ?? false,
    userIdsRaw: formatSubjectList(subjects?.users?.map(u => u.id)),
    profileIdsRaw: formatSubjectList(subjects?.profiles?.map(p => p.id)),
    minimumLevel: subjects?.minimumLevel ?? null,
  };
}

/**
 * Builds the wire-shape {@link SubjectRequest} for one operation from the form state. Note the
 * server treats an empty, non-wildcard subject list as "nobody may perform this operation".
 */
export function toSubjectRequest(edit: EditableSubjects): SubjectRequest {
  return {
    wildcard: edit.wildcard,
    userIds: parseSubjectList(edit.userIdsRaw),
    profileIds: parseSubjectList(edit.profileIdsRaw),
  };
}

export interface SubjectRequest {
  wildcard: boolean;
  userIds: string[];
  profileIds: string[];
}

export interface UpdateLargeObjectPermissionsRequest {
  mimeType: string | null;
  read: SubjectRequest;
  write: SubjectRequest;
  delete: SubjectRequest;
}

/**
 * Builds the full `PUT /api/rest/large_object/{id}` body from the edited form state. The object's
 * current mime type is carried through (the update replaces it wholesale otherwise).
 */
export function toUpdateRequest(
  largeObject: { mimeType: string | null },
  edits: Record<Operation, EditableSubjects>,
): UpdateLargeObjectPermissionsRequest {
  return {
    mimeType: largeObject.mimeType ?? null,
    read: toSubjectRequest(edits.read),
    write: toSubjectRequest(edits.write),
    delete: toSubjectRequest(edits.delete),
  };
}

/**
 * Human-readable visibility for one operation's subjects: Public (wildcard), Restricted (specific
 * subjects, possibly none), or No access (wildcard off and no subjects at all).
 */
export function describeVisibility(edit: EditableSubjects): string {
  if (edit.wildcard) return 'Public';
  if (parseSubjectList(edit.userIdsRaw).length > 0 || parseSubjectList(edit.profileIdsRaw).length > 0) {
    return 'Restricted';
  }
  return 'No access';
}
