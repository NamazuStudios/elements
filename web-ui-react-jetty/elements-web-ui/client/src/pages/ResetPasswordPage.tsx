import { useState, useEffect } from 'react';
import { Link, useLocation } from 'wouter';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Eye, EyeOff } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import logoPath from '@assets/elements-logo-square (1)_1760052619243.png';

export default function ResetPasswordPage() {
  const [, setLocation] = useLocation();
  const { toast } = useToast();

  const [token, setToken] = useState<string | null>(null);
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const t = params.get('token');
    setToken(t);
  }, []);

  const passwordMismatch = confirmPassword.length > 0 && password !== confirmPassword;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !password || password !== confirmPassword) return;

    setIsLoading(true);
    setError(null);
    try {
      const response = await fetch('/api/proxy/api/rest/user/password/reset/complete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, password }),
      });

      if (response.ok) {
        toast({ title: 'Password updated', description: 'You can now sign in with your new password.' });
        setLocation('/login');
      } else {
        const data = await response.json().catch(() => ({}));
        setError(data?.message || 'Invalid or expired reset token. Please request a new reset link.');
      }
    } catch {
      setError('An unexpected error occurred. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  if (token === null && typeof window !== 'undefined') {
    const params = new URLSearchParams(window.location.search);
    if (!params.get('token')) {
      return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-background p-4">
          <Card className="w-full max-w-md">
            <CardHeader className="space-y-3">
              <div className="flex items-center justify-center mb-2">
                <div className="flex items-center justify-center w-32 h-32">
                  <img src={logoPath} alt="Namazu Elements" className="w-full h-full" />
                </div>
              </div>
              <CardTitle className="text-2xl text-center">Invalid Link</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <p className="text-sm text-center text-muted-foreground">
                This reset link is invalid or missing a token.
              </p>
              <div className="text-center">
                <Link to="/forgot-password" className="text-sm text-primary hover:underline">
                  Request a new reset link
                </Link>
              </div>
            </CardContent>
          </Card>
        </div>
      );
    }
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-3">
          <div className="flex items-center justify-center mb-2">
            <div className="flex items-center justify-center w-32 h-32">
              <img src={logoPath} alt="Namazu Elements" className="w-full h-full" />
            </div>
          </div>
          <CardTitle className="text-2xl text-center">Set New Password</CardTitle>
          <CardDescription className="text-center">
            Choose a new password for your account.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="password">New password</Label>
              <div className="relative">
                <Input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  placeholder="At least 8 characters"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pr-12"
                  autoComplete="new-password"
                  minLength={8}
                  required
                />
                <div className="absolute inset-y-0 right-0 flex items-center pr-1">
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => setShowPassword(!showPassword)}
                  >
                    {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </Button>
                </div>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="confirm-password">Confirm password</Label>
              <div className="relative">
                <Input
                  id="confirm-password"
                  type={showConfirm ? 'text' : 'password'}
                  placeholder="Re-enter your new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className={`pr-12 ${passwordMismatch ? 'border-destructive' : ''}`}
                  autoComplete="new-password"
                  required
                />
                <div className="absolute inset-y-0 right-0 flex items-center pr-1">
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => setShowConfirm(!showConfirm)}
                  >
                    {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                  </Button>
                </div>
              </div>
              {passwordMismatch && (
                <p className="text-xs text-destructive">Passwords do not match.</p>
              )}
            </div>

            {error && (
              <p className="text-sm text-destructive">{error}</p>
            )}

            <Button
              type="submit"
              className="w-full"
              disabled={isLoading || passwordMismatch || !password || !confirmPassword}
            >
              {isLoading ? 'Updating...' : 'Update Password'}
            </Button>

            <div className="text-center text-sm text-muted-foreground">
              <Link to="/login" className="text-primary hover:underline">
                Back to sign in
              </Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
