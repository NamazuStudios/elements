import { useState, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ShieldCheck } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';

// Google's official, publicly-documented reCAPTCHA v2 test key pair -- always renders and verifies as
// solved, so it never provides real protection. Intended purely to let someone try the CAPTCHA flow
// without first registering a real site. The widget itself displays "for testing purposes only" while
// these are active, so there's no need for extra in-app warnings on top of that.
const RECAPTCHA_DEMO_SITE_KEY = '6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI';
const RECAPTCHA_DEMO_SECRET_KEY = '6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe';

interface CaptchaConfiguration {
  id?: string;
  provider: 'RECAPTCHA';
  siteKey: string;
  secretKey?: string | null;
  enabled: boolean;
}

const QUERY_KEY = ['/api/rest/captcha_configuration'];

const EMPTY_CONFIGURATION: CaptchaConfiguration = {
  provider: 'RECAPTCHA',
  siteKey: '',
  secretKey: '',
  enabled: false,
};

export default function CaptchaConfiguration() {
  const { toast } = useToast();
  const [form, setForm] = useState<CaptchaConfiguration>(EMPTY_CONFIGURATION);
  const [secretKeyInput, setSecretKeyInput] = useState('');

  const { data, isLoading } = useQuery<CaptchaConfiguration | null>({
    queryKey: QUERY_KEY,
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/rest/captcha_configuration');
      if (response.status === 404) {
        return null;
      }
      return await response.json();
    },
    retry: false,
  });

  useEffect(() => {
    if (data) {
      setForm({ ...data, secretKey: '' });
      setSecretKeyInput('');
    }
  }, [data]);

  const saveMutation = useMutation({
    mutationFn: async () => {
      const body = {
        provider: form.provider,
        siteKey: form.siteKey,
        secretKey: secretKeyInput || undefined,
        enabled: form.enabled,
      };
      const response = await apiRequest('PUT', '/api/rest/captcha_configuration', body);
      return await response.json();
    },
    onSuccess: (saved: CaptchaConfiguration) => {
      queryClient.setQueryData(QUERY_KEY, saved);
      setSecretKeyInput('');
      toast({ title: 'CAPTCHA configuration saved' });
    },
    onError: (error: Error) => {
      toast({
        title: 'Failed to save CAPTCHA configuration',
        description: error.message,
        variant: 'destructive',
      });
    },
  });

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();

    if (!form.siteKey.trim()) {
      toast({ title: 'Site key is required', variant: 'destructive' });
      return;
    }

    if (!data && !secretKeyInput.trim()) {
      toast({ title: 'Secret key is required when configuring CAPTCHA for the first time', variant: 'destructive' });
      return;
    }

    saveMutation.mutate();
  };

  return (
    <div className="p-6 space-y-6 max-w-2xl">
      <div className="flex items-center gap-3">
        <ShieldCheck className="w-6 h-6 text-muted-foreground" />
        <h1 className="text-2xl font-semibold">CAPTCHA</h1>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>System-Wide CAPTCHA Configuration</CardTitle>
          <CardDescription>
            Configures the CAPTCHA provider used to gate SUPERUSER logins on the admin login form, and backs the
            general-purpose CAPTCHA verification API any Elements-backed frontend can call to gate its own
            actions (e.g. signup).
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <p className="text-sm text-muted-foreground">Loading...</p>
          ) : (
            <form onSubmit={handleSave} className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label htmlFor="captcha-enabled">Enabled</Label>
                  <p className="text-sm text-muted-foreground">
                    Enforce CAPTCHA verification for SUPERUSER logins on the admin panel.
                  </p>
                </div>
                <Switch
                  id="captcha-enabled"
                  checked={form.enabled}
                  onCheckedChange={(checked) => setForm({ ...form, enabled: checked })}
                  data-testid="switch-captcha-enabled"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="captcha-provider">Provider</Label>
                <Select value={form.provider} onValueChange={(v) => setForm({ ...form, provider: v as 'RECAPTCHA' })}>
                  <SelectTrigger id="captcha-provider" data-testid="select-captcha-provider">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="RECAPTCHA">reCAPTCHA</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="captcha-site-key">Site Key</Label>
                <Input
                  id="captcha-site-key"
                  value={form.siteKey}
                  onChange={(e) => setForm({ ...form, siteKey: e.target.value })}
                  placeholder="Public site key"
                  data-testid="input-captcha-site-key"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="captcha-secret-key">Secret Key</Label>
                <Input
                  id="captcha-secret-key"
                  type="password"
                  value={secretKeyInput}
                  onChange={(e) => setSecretKeyInput(e.target.value)}
                  placeholder={data ? 'Leave blank to keep the existing secret unchanged' : 'Private secret key'}
                  data-testid="input-captcha-secret-key"
                />
              </div>

              <div className="flex items-center gap-3 flex-wrap">
                <Button type="submit" disabled={saveMutation.isPending} data-testid="button-save-captcha-configuration">
                  {saveMutation.isPending ? 'Saving...' : 'Save'}
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    setForm({ ...form, provider: 'RECAPTCHA', siteKey: RECAPTCHA_DEMO_SITE_KEY });
                    setSecretKeyInput(RECAPTCHA_DEMO_SECRET_KEY);
                  }}
                  data-testid="button-use-demo-keys"
                >
                  Use test keys
                </Button>
              </div>
              <p className="text-xs text-muted-foreground">
                Fills in Google's public reCAPTCHA test key pair so you can try the flow without registering
                your own site. The widget always verifies as solved. Do not use them outside of testing.
              </p>
            </form>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
