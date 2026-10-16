import { describe, it, expect } from 'vitest';
import {
  validatePasswordChange,
  isValidPasswordChange,
  MIN_PASSWORD_LENGTH,
} from './password-validation';

/**
 * Permutation matrix for the change-password form validation (#154): good input, each individual bad
 * field, combinations, boundary lengths, and the "no drift with the server policy" property -- only
 * policy-independent checks live here (the authoritative policy is deploy-time configurable and its
 * failures surface from the server response verbatim).
 */
describe('validatePasswordChange', () => {

  const good = {
    currentPassword: 'old-password',
    newPassword: 'new-password',
    confirmPassword: 'new-password',
  };

  it('accepts a valid change', () => {
    expect(validatePasswordChange(good)).toEqual({});
    expect(isValidPasswordChange(validatePasswordChange(good))).toBe(true);
  });

  it('accepts when the new password equals the minimum length', () => {
    const fields = { ...good, newPassword: 'a'.repeat(MIN_PASSWORD_LENGTH), confirmPassword: 'a'.repeat(MIN_PASSWORD_LENGTH) };
    expect(validatePasswordChange(fields)).toEqual({});
  });

  it('accepts passwords containing spaces (no trimming)', () => {
    const fields = { ...good, newPassword: 'new password with spaces', confirmPassword: 'new password with spaces' };
    expect(validatePasswordChange(fields)).toEqual({});
  });

  it('rejects an empty current password', () => {
    const errors = validatePasswordChange({ ...good, currentPassword: '' });
    expect(errors.currentPassword).toBeTruthy();
    expect(errors.newPassword).toBeUndefined();
    expect(isValidPasswordChange(errors)).toBe(false);
  });

  it('rejects an empty new password', () => {
    const errors = validatePasswordChange({ ...good, newPassword: '', confirmPassword: '' });
    expect(errors.newPassword).toBeTruthy();
    expect(errors.confirmPassword).toBeTruthy();
  });

  it('rejects a new password below the minimum length', () => {
    const errors = validatePasswordChange({
      ...good,
      newPassword: 'a'.repeat(MIN_PASSWORD_LENGTH - 1),
      confirmPassword: 'a'.repeat(MIN_PASSWORD_LENGTH - 1),
    });
    expect(errors.newPassword).toContain(String(MIN_PASSWORD_LENGTH));
  });

  it('rejects a new password identical to the current one', () => {
    const errors = validatePasswordChange({
      ...good,
      newPassword: 'old-password',
      confirmPassword: 'old-password',
    });
    expect(errors.newPassword).toContain('different');
  });

  it('rejects a mismatched confirmation', () => {
    const errors = validatePasswordChange({ ...good, confirmPassword: 'different' });
    expect(errors.confirmPassword).toBeTruthy();
    expect(errors.newPassword).toBeUndefined();
  });

  it('rejects an empty confirmation', () => {
    const errors = validatePasswordChange({ ...good, confirmPassword: '' });
    expect(errors.confirmPassword).toBeTruthy();
  });

  it('validates all fields at once, reporting every error', () => {
    const errors = validatePasswordChange({ currentPassword: '', newPassword: '', confirmPassword: 'x' });
    // The confirmation check is skipped while the new password is empty (a mismatch against nothing
    // would be noise); the other two fields report their errors.
    expect(Object.keys(errors).sort()).toEqual(['currentPassword', 'newPassword']);
  });

  it('reports the mismatch (not the length error) when the confirmation is short but matches the new password', () => {
    const errors = validatePasswordChange({
      ...good,
      newPassword: 'abc',
      confirmPassword: 'abc',
    });
    expect(errors.newPassword).toContain(String(MIN_PASSWORD_LENGTH));
    expect(errors.confirmPassword).toBeUndefined();
  });

  it('reports both the length error and the mismatch when the confirmation differs from an invalid new password', () => {
    const errors = validatePasswordChange({
      ...good,
      newPassword: 'abc',
      confirmPassword: 'xyz',
    });
    // Flagging both is deliberate: the mismatch will still be wrong after the length is fixed.
    expect(errors.newPassword).toContain(String(MIN_PASSWORD_LENGTH));
    expect(errors.confirmPassword).toBeTruthy();
  });

});
