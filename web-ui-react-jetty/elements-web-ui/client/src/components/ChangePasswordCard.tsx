import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { KeyRound, Loader2 } from 'lucide-react';
import { apiRequest, queryClient } from '@/lib/queryClient';
import { apiClient } from '@/lib/api-client';
import {
  validatePasswordChange,
  isValidPasswordChange,
  type PasswordChangeErrors,
} from '@/lib/password-validation';

/**
 * Self-service password rotation (#154): lets the signed-in user change their own password against the
 * existing PUT /api/rest/user/{userId}/password endpoint. The endpoint revokes the caller's sessions and
 * issues a fresh one, so on success the returned sessionSecret is adopted immediately -- mirroring the
 * login flow -- leaving the user signed in under the new credential.
 */
export function ChangePasswordCard() {
  const { toast } = useToast();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [errors, setErrors] = useState<PasswordChangeErrors>({});

  const { data: currentUser, isLoading: isLoadingUser } = useQuery({
    queryKey: ['/api/rest/user/me'],
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/rest/user/me');
      return (await response.json()) as { id: string };
    },
  });

  const changePasswordMutation = useMutation({
    mutationFn: async () => {
      const response = await apiRequest(
        'PUT',
        `/api/rest/user/${encodeURIComponent(currentUser!.id)}/password`,
        { oldPassword: currentPassword, newPassword },
      );
      return (await response.json()) as { sessionSecret?: string };
    },
    onSuccess: (data) => {
      // The server revokes the caller's sessions and issues a fresh one; adopt the new secret so the
      // user stays signed in under the rotated credential (same as login).
      if (data.sessionSecret) {
        apiClient.setSessionToken(data.sessionSecret);
      }
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      setErrors({});
      queryClient.invalidateQueries({ queryKey: ['/api/rest/user/me'] });
      toast({ title: 'Password changed', description: 'Your password has been updated.' });
    },
    onError: (error: Error) => {
      // Server-side policy failures arrive as the response's message (the deploy-time-configured
      // policy description) -- surface it verbatim rather than duplicating the policy client-side.
      toast({ title: 'Failed to change password', description: error.message, variant: 'destructive' });
    },
  });

  const submit = () => {
    const validationErrors = validatePasswordChange({ currentPassword, newPassword, confirmPassword });
    setErrors(validationErrors);
    if (isValidPasswordChange(validationErrors)) {
      changePasswordMutation.mutate();
    }
  };

  const fieldError = (message?: string) =>
    message ? <p className="text-sm text-destructive">{message}</p> : null;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <KeyRound className="h-5 w-5" />
          Change Password
        </CardTitle>
        <CardDescription>
          Change the password you sign in with. Your other sessions will be signed out.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="current-password">Current password</Label>
          <Input
            id="current-password"
            type="password"
            autoComplete="current-password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            data-testid="input-current-password"
          />
          {fieldError(errors.currentPassword)}
        </div>
        <div className="space-y-2">
          <Label htmlFor="new-password">New password</Label>
          <Input
            id="new-password"
            type="password"
            autoComplete="new-password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            data-testid="input-new-password"
          />
          {fieldError(errors.newPassword)}
        </div>
        <div className="space-y-2">
          <Label htmlFor="confirm-password">Confirm new password</Label>
          <Input
            id="confirm-password"
            type="password"
            autoComplete="new-password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            data-testid="input-confirm-password"
          />
          {fieldError(errors.confirmPassword)}
        </div>
        <Button
          onClick={submit}
          disabled={isLoadingUser || !currentUser || changePasswordMutation.isPending}
          data-testid="button-change-password"
        >
          {changePasswordMutation.isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
          Change password
        </Button>
      </CardContent>
    </Card>
  );
}
