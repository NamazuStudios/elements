import { useState, useEffect, useRef } from 'react';
import { Link } from 'wouter';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Checkbox } from '@/components/ui/checkbox';
import { Eye, EyeOff } from 'lucide-react';
import { useAuth } from '@/contexts/AuthContext';
import { useToast } from '@/hooks/use-toast';
import { apiClient, MfaRequiredError } from '@/lib/api-client';
import logoPath from '@assets/elements-logo-square (1)_1760052619243.png';

const RECAPTCHA_SCRIPT_URL = 'https://www.google.com/recaptcha/api.js?render=explicit';

let recaptchaScriptPromise: Promise<void> | null = null;

function loadRecaptchaScript(): Promise<void> {
  if ((window as any).grecaptcha?.render) {
    return Promise.resolve();
  }
  if (!recaptchaScriptPromise) {
    recaptchaScriptPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = RECAPTCHA_SCRIPT_URL;
      script.async = true;
      script.defer = true;
      script.onload = () => {
        // The <script> tag's onload only means the file downloaded -- grecaptcha does further async
        // bootstrapping before .render() actually exists. grecaptcha.ready() queues the callback until
        // the full API (including render) is genuinely available.
        (window as any).grecaptcha.ready(() => resolve());
      };
      script.onerror = () => reject(new Error('Failed to load reCAPTCHA script'));
      document.head.appendChild(script);
    });
  }
  return recaptchaScriptPromise;
}

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [urlSessionExpired, setUrlSessionExpired] = useState(false);
  const [captchaConfig, setCaptchaConfig] = useState<{ enabled: boolean; siteKey?: string }>({ enabled: false });
  const [captchaToken, setCaptchaToken] = useState<string | null>(null);
  const [captchaLoadError, setCaptchaLoadError] = useState<string | null>(null);
  const captchaContainerRef = useRef<HTMLDivElement>(null);
  const captchaWidgetId = useRef<number | null>(null);
  const [oidcProviders, setOidcProviders] = useState<{ id: string; name: string; displayName?: string; iconUrl?: string }[]>([]);
  const [oidcLoadingProvider, setOidcLoadingProvider] = useState<string | null>(null);
  const [mfaChallengeId, setMfaChallengeId] = useState<string | null>(null);
  const [mfaCode, setMfaCode] = useState('');
  const { login, completeMfaLogin, loginWithOidcProvider, sessionExpired: authSessionExpired } = useAuth();
  const { toast } = useToast();
  // The MFA screen's "Back" button and the login form's "Sign In" button both sit in the same
  // primary-action slot, so a click landing right as the screen swaps back to the login form can
  // register as a second, genuine click on "Sign In" (confirmed via a trusted DOM submit event in
  // testing). Ignore any submit that follows a "Back" click within this window.
  const mfaBackClickedAtRef = useRef(0);

  // Fetch the public CAPTCHA bootstrap configuration on mount. Best-effort: if this fails, the login form
  // simply proceeds without a CAPTCHA gate (the server enforces the requirement independently, if enabled).
  useEffect(() => {
    apiClient.getCaptchaPublicConfig()
      .then(setCaptchaConfig)
      .catch(() => setCaptchaConfig({ enabled: false }));
  }, []);

  // Render the reCAPTCHA widget once we know it's enabled and have a site key.
  useEffect(() => {
    if (!captchaConfig.enabled || !captchaConfig.siteKey || !captchaContainerRef.current) {
      return;
    }

    let cancelled = false;
    setCaptchaLoadError(null);

    loadRecaptchaScript()
      .then(() => {
        if (cancelled || !captchaContainerRef.current || captchaWidgetId.current !== null) {
          return;
        }
        captchaWidgetId.current = (window as any).grecaptcha.render(captchaContainerRef.current, {
          sitekey: captchaConfig.siteKey,
          callback: (token: string) => setCaptchaToken(token),
          'expired-callback': () => setCaptchaToken(null),
        });
      })
      .catch((error) => {
        console.error('[LOGIN] Failed to load CAPTCHA widget:', error);
        if (!cancelled) {
          setCaptchaLoadError('CAPTCHA failed to load. Check your connection or browser extensions (ad blockers can block it), then reload the page.');
        }
      });

    return () => {
      cancelled = true;
    };
  }, [captchaConfig.enabled, captchaConfig.siteKey]);

  // Load the list of OIDC providers offered for admin-panel login, if any. No error toast on failure --
  // this endpoint requires no auth, so a failure here just means username/password remains the only option.
  useEffect(() => {
    apiClient.getOidcAdminLoginProviders()
      .then(setOidcProviders)
      .catch(() => setOidcProviders([]));
  }, []);

  const handleOidcLogin = async (provider: { id: string; name: string; displayName?: string; iconUrl?: string }) => {
    setOidcLoadingProvider(provider.id);
    try {
      await loginWithOidcProvider(provider.name, rememberMe);
      toast({
        title: 'Access Granted',
        description: 'Welcome to the Elements Admin Dashboard',
      });
    } catch (error) {
      toast({
        title: 'Authentication Failed',
        description: error instanceof Error ? error.message : 'OIDC login failed',
        variant: 'destructive',
      });
    } finally {
      setOidcLoadingProvider(null);
    }
  };

  // Check for session expiration query parameter (from 403 redirect)
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get('expired') === 'true') {
      setUrlSessionExpired(true);
      // Clean up URL by removing the query parameter
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);
  
  // Show session expired message from either URL param or auth context
  const sessionExpired = urlSessionExpired || authSessionExpired;
  
  // Show toast notification when session has expired
  useEffect(() => {
    if (sessionExpired) {
      toast({
        title: 'Session Expired',
        description: 'Your session has expired due to inactivity. Please log in again to continue.',
        variant: 'destructive',
      });
    }
  }, [sessionExpired, toast]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (Date.now() - mfaBackClickedAtRef.current < 500) {
      return;
    }
    if (!username.trim() || !password.trim()) {
      toast({
        title: 'Error',
        description: 'Please enter both username and password',
        variant: 'destructive',
      });
      return;
    }

    if (captchaConfig.enabled && !captchaToken) {
      toast({
        title: 'Error',
        description: 'Please complete the CAPTCHA challenge',
        variant: 'destructive',
      });
      return;
    }

    setIsLoading(true);
    try {
      await login(username, password, rememberMe, captchaToken ?? undefined);
      toast({
        title: 'Access Granted',
        description: 'Welcome to the Elements Admin Dashboard',
      });
    } catch (error) {
      if (error instanceof MfaRequiredError) {
        // Don't hold the plaintext password in state any longer than it takes to make this one
        // request. It also means that if anything -- a stray re-render, a password manager's
        // autofill/auto-submit, whatever -- ever re-invokes handleLogin while the MFA screen is
        // showing, the empty-field guard above rejects it instead of silently re-submitting.
        setPassword('');
        setMfaChallengeId(error.challengeId);
        return;
      }
      toast({
        title: 'Authentication Failed',
        description: error instanceof Error ? error.message : 'Invalid credentials',
        variant: 'destructive',
      });
      // A failed attempt invalidates the solved CAPTCHA (single-use); reset so the widget can be re-solved.
      if (captchaConfig.enabled && (window as any).grecaptcha?.reset && captchaWidgetId.current !== null) {
        (window as any).grecaptcha.reset(captchaWidgetId.current);
      }
      setCaptchaToken(null);
    } finally {
      setIsLoading(false);
    }
  };

  const handleMfaSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!mfaChallengeId || !mfaCode.trim()) {
      return;
    }

    setIsLoading(true);
    try {
      await completeMfaLogin(mfaChallengeId, mfaCode.trim(), rememberMe);
      toast({
        title: 'Access Granted',
        description: 'Welcome to the Elements Admin Dashboard',
      });
    } catch (error) {
      toast({
        title: 'Verification Failed',
        description: error instanceof Error ? error.message : 'Invalid or expired code',
        variant: 'destructive',
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-3">
          <div className="flex items-center justify-center mb-2">
            <div className="flex items-center justify-center w-32 h-32">
              <img src={logoPath} alt="Namazu Elements" className="w-full h-full" />
            </div>
          </div>
          <CardTitle className="text-2xl text-center">Namazu Elements</CardTitle>
          <CardDescription className="text-center">
            Sign in to access the admin dashboard
          </CardDescription>
        </CardHeader>
        <CardContent>
          {mfaChallengeId ? (
            <form onSubmit={handleMfaSubmit} className="space-y-4">
              <p className="text-sm text-muted-foreground">
                Enter the code from your authenticator app, or one of your recovery codes.
              </p>

              <div className="space-y-2">
                <Label htmlFor="mfa-code">Authentication Code</Label>
                <Input
                  id="mfa-code"
                  data-testid="input-mfa-code"
                  type="text"
                  placeholder="123456 or recovery code"
                  value={mfaCode}
                  onChange={(e) => setMfaCode(e.target.value)}
                  autoComplete="one-time-code"
                  autoFocus
                />
              </div>

              <Button
                type="submit"
                className="w-full"
                disabled={isLoading}
                data-testid="button-mfa-submit"
              >
                {isLoading ? 'Verifying...' : 'Verify'}
              </Button>

              <Button
                type="button"
                variant="ghost"
                className="w-full"
                onClick={() => {
                  mfaBackClickedAtRef.current = Date.now();
                  setMfaChallengeId(null);
                  setMfaCode('');
                  // Clear the credentials too -- password is already cleared once the MFA screen
                  // is reached, but wipe username as well so the login form comes back genuinely
                  // blank rather than pre-filled with the previous attempt's values.
                  setUsername('');
                  setPassword('');
                }}
                data-testid="button-mfa-back"
              >
                Back
              </Button>
            </form>
          ) : (
          <>
          <form onSubmit={handleLogin} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">Username</Label>
              <Input
                id="username"
                data-testid="input-username"
                type="text"
                placeholder="Enter your username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <div className="relative">
                <Input
                  id="password"
                  data-testid="input-password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pr-12"
                  autoComplete="current-password"
                />
                <div className="absolute inset-y-0 right-0 flex items-center pr-1">
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => setShowPassword(!showPassword)}
                    data-testid="button-toggle-password"
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </Button>
                </div>
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="remember-me"
                checked={rememberMe}
                onCheckedChange={(checked) => setRememberMe(checked as boolean)}
                data-testid="checkbox-remember-me"
              />
              <Label
                htmlFor="remember-me"
                className="text-sm font-normal cursor-pointer"
              >
                Remember me on this device
              </Label>
            </div>

            {captchaConfig.enabled && (
              <div className="space-y-2">
                <div ref={captchaContainerRef} data-testid="captcha-widget" />
                {captchaLoadError && (
                  <p className="text-sm text-destructive" data-testid="text-captcha-load-error">
                    {captchaLoadError}
                  </p>
                )}
              </div>
            )}

            <Button
              type="submit"
              className="w-full"
              disabled={isLoading}
              data-testid="button-login"
            >
              {isLoading ? 'Signing in...' : 'Sign In'}
            </Button>

            <div className="text-center text-sm text-muted-foreground">
              <Link to="/forgot-password" className="hover:underline">
                Forgot your password?
              </Link>
            </div>
          </form>

          {oidcProviders.length > 0 && (
            <div className="mt-6 space-y-3">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <span className="w-full border-t" />
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span className="bg-card px-2 text-muted-foreground">Or continue with</span>
                </div>
              </div>

              {oidcProviders.map((provider) => (
                <Button
                  key={provider.id}
                  type="button"
                  variant="outline"
                  className="w-full justify-center gap-2"
                  disabled={oidcLoadingProvider !== null}
                  onClick={() => handleOidcLogin(provider)}
                  data-testid={`button-oidc-login-${provider.name}`}
                >
                  {provider.iconUrl && (
                    <img src={provider.iconUrl} alt="" className="w-4 h-4" aria-hidden="true" />
                  )}
                  {oidcLoadingProvider === provider.id
                    ? 'Signing in...'
                    : `Sign in with ${provider.displayName || provider.name}`}
                </Button>
              ))}
            </div>
          )}
          </>
          )}
        </CardContent>
      </Card>
      
      <p className="text-xs text-muted-foreground text-center mt-4 max-w-md">
        This system uses cookies to maintain your session and ensure security.
        By signing in, you consent to this use.
      </p>
    </div>
  );
}
