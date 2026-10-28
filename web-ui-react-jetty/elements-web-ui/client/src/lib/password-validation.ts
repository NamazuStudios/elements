/**
 * Client-side validation for the self-service change-password form (#154). Deliberately minimal: the
 * authoritative password policy is deploy-time configurable on the server (PasswordPolicyValidator),
 * so policy failures are surfaced from the server's response verbatim rather than duplicated (and
 * drifted from) here. Only the checks that are independent of the configured policy are made here.
 */

export const MIN_PASSWORD_LENGTH = 4;

export interface PasswordChangeFields {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export type PasswordChangeErrors = Partial<Record<keyof PasswordChangeFields, string>>;

/**
 * Validates a change-password submission. Returns a map of field -> error message; empty when the
 * submission may be sent to the server.
 */
export function validatePasswordChange(fields: PasswordChangeFields): PasswordChangeErrors {
  const errors: PasswordChangeErrors = {};

  if (!fields.currentPassword) {
    errors.currentPassword = 'Enter your current password.';
  }

  if (!fields.newPassword) {
    errors.newPassword = 'Enter a new password.';
  } else if (fields.newPassword.length < MIN_PASSWORD_LENGTH) {
    errors.newPassword = `Password must be at least ${MIN_PASSWORD_LENGTH} characters.`;
  } else if (fields.newPassword === fields.currentPassword) {
    errors.newPassword = 'The new password must be different from the current password.';
  }

  if (!fields.confirmPassword) {
    errors.confirmPassword = 'Confirm the new password.';
  } else if (fields.newPassword && fields.confirmPassword !== fields.newPassword) {
    errors.confirmPassword = 'Passwords do not match.';
  }

  return errors;
}

/**
 * True when the form has no validation errors and may be submitted.
 */
export function isValidPasswordChange(errors: PasswordChangeErrors): boolean {
  return Object.keys(errors).length === 0;
}
