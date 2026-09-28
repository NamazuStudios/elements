import { useState, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import QRCode from 'qrcode';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';

interface TotpEnrollment {
  secret: string;
  otpAuthUri: string;
}

const STATUS_QUERY_KEY = ['/api/rest/totp'];

export function TwoFactorAuthCard() {
  const { toast } = useToast();
  const [enrollment, setEnrollment] = useState<TotpEnrollment | null>(null);
  const [qrDataUrl, setQrDataUrl] = useState<string | null>(null);
  const [confirmCode, setConfirmCode] = useState('');
  const [recoveryCodes, setRecoveryCodes] = useState<string[] | null>(null);

  const { data: isEnrolled, isLoading } = useQuery<boolean>({
    queryKey: STATUS_QUERY_KEY,
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/rest/totp');
      return await response.json();
    },
  });

  useEffect(() => {
    if (enrollment?.otpAuthUri) {
      QRCode.toDataURL(enrollment.otpAuthUri)
        .then(setQrDataUrl)
        .catch(() => setQrDataUrl(null));
    } else {
      setQrDataUrl(null);
    }
  }, [enrollment]);

  const beginEnrollMutation = useMutation({
    mutationFn: async () => {
      const response = await apiRequest('POST', '/api/rest/totp/enroll');
      return (await response.json()) as TotpEnrollment;
    },
    onSuccess: setEnrollment,
    onError: (error: Error) => {
      toast({ title: 'Failed to begin enrollment', description: error.message, variant: 'destructive' });
    },
  });

  const confirmEnrollMutation = useMutation({
    mutationFn: async () => {
      const response = await apiRequest('POST', '/api/rest/totp/enroll/confirm', { code: confirmCode.trim() });
      return await response.json();
    },
    onSuccess: (data: { codes: string[] }) => {
      setRecoveryCodes(data.codes);
      setEnrollment(null);
      setConfirmCode('');
      queryClient.setQueryData(STATUS_QUERY_KEY, true);
      toast({ title: 'Two-factor authentication enabled' });
    },
    onError: (error: Error) => {
      toast({ title: 'Invalid code', description: error.message, variant: 'destructive' });
    },
  });

  const disableMutation = useMutation({
    mutationFn: async () => {
      await apiRequest('DELETE', '/api/rest/totp');
    },
    onSuccess: () => {
      queryClient.setQueryData(STATUS_QUERY_KEY, false);
      setRecoveryCodes(null);
      toast({ title: 'Two-factor authentication disabled' });
    },
    onError: (error: Error) => {
      toast({ title: 'Failed to disable', description: error.message, variant: 'destructive' });
    },
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>Two-Factor Authentication</CardTitle>
        <CardDescription>
          Require a code from an authenticator app (Google Authenticator, Authy, etc.) in addition to your
          password when signing in.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {isLoading ? (
          <p className="text-sm text-muted-foreground">Loading...</p>
        ) : recoveryCodes ? (
          <div className="space-y-3">
            <p className="text-sm font-medium">
              Save these recovery codes somewhere safe. Each one can be used once, in place of a code from
              your app, if you lose access to your device. They will not be shown again.
            </p>
            <div className="grid grid-cols-2 gap-2 rounded-md border p-4 font-mono text-sm">
              {recoveryCodes.map((code) => (
                <span key={code} data-testid="text-recovery-code">{code}</span>
              ))}
            </div>
            <Button onClick={() => setRecoveryCodes(null)} data-testid="button-recovery-codes-done">
              Done
            </Button>
          </div>
        ) : isEnrolled ? (
          <div className="flex items-center justify-between">
            <p className="text-sm text-muted-foreground">Two-factor authentication is currently enabled.</p>
            <Button
              variant="destructive"
              onClick={() => disableMutation.mutate()}
              disabled={disableMutation.isPending}
              data-testid="button-disable-totp"
            >
              Disable
            </Button>
          </div>
        ) : enrollment ? (
          <div className="space-y-4">
            {qrDataUrl && (
              <img src={qrDataUrl} alt="TOTP QR code" className="w-48 h-48" data-testid="img-totp-qr" />
            )}
            <div className="space-y-1">
              <Label>Can't scan the code?</Label>
              <p className="font-mono text-sm break-all" data-testid="text-totp-secret">{enrollment.secret}</p>
              <p className="text-xs text-muted-foreground">Enter this manually into your authenticator app.</p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="confirm-code">Enter the code from your app to confirm</Label>
              <Input
                id="confirm-code"
                value={confirmCode}
                onChange={(e) => setConfirmCode(e.target.value)}
                placeholder="123456"
                data-testid="input-confirm-code"
              />
            </div>
            <div className="flex gap-2">
              <Button
                onClick={() => confirmEnrollMutation.mutate()}
                disabled={confirmEnrollMutation.isPending || !confirmCode.trim()}
                data-testid="button-confirm-enroll"
              >
                Confirm
              </Button>
              <Button variant="ghost" onClick={() => setEnrollment(null)} data-testid="button-cancel-enroll">
                Cancel
              </Button>
            </div>
          </div>
        ) : (
          <div className="flex items-center justify-between">
            <p className="text-sm text-muted-foreground">Two-factor authentication is not enabled.</p>
            <Button
              onClick={() => beginEnrollMutation.mutate()}
              disabled={beginEnrollMutation.isPending}
              data-testid="button-begin-enroll"
            >
              Enable
            </Button>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
