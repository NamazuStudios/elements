import { useQuery, useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Fingerprint } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';

interface TotpConfiguration {
  id?: string;
  enabled: boolean;
}

const QUERY_KEY = ['/api/rest/totp_configuration'];

export default function TotpConfiguration() {
  const { toast } = useToast();

  const { data, isLoading } = useQuery<TotpConfiguration>({
    queryKey: QUERY_KEY,
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/rest/totp_configuration');
      return await response.json();
    },
  });

  const saveMutation = useMutation({
    mutationFn: async (enabled: boolean) => {
      const response = await apiRequest('PUT', '/api/rest/totp_configuration', { enabled });
      return await response.json();
    },
    onSuccess: (saved: TotpConfiguration) => {
      queryClient.setQueryData(QUERY_KEY, saved);
      toast({ title: 'TOTP configuration saved' });
    },
    onError: (error: Error) => {
      toast({
        title: 'Failed to save TOTP configuration',
        description: error.message,
        variant: 'destructive',
      });
    },
  });

  return (
    <div className="p-6 space-y-6 max-w-2xl">
      <div className="flex items-center gap-3">
        <Fingerprint className="w-6 h-6 text-muted-foreground" />
        <h1 className="text-2xl font-semibold">Two-Factor Auth</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>System-Wide TOTP Configuration</CardTitle>
          <CardDescription>
            Master switch for TOTP (authenticator app) two-factor authentication. Enrollment is always
            per-account and opt-in -- this only controls whether enrolled accounts are actually challenged
            for a code at login. Off by default.
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p className="text-sm text-muted-foreground">Loading...</p>
          ) : (
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label htmlFor="totp-enabled">Enabled</Label>
                <p className="text-sm text-muted-foreground">
                  Enforce TOTP codes at login for any account that has completed enrollment.
                </p>
              </div>
              <Switch
                id="totp-enabled"
                checked={data?.enabled ?? false}
                onCheckedChange={(checked) => saveMutation.mutate(checked)}
                disabled={saveMutation.isPending}
                data-testid="switch-totp-enabled"
              />
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
