import { useEffect, useState } from 'react';
import { type UseFormReturn } from 'react-hook-form';
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { getApiConfig } from '@/lib/config';

interface OidcRedirectUriFieldProps {
  form: UseFormReturn<any>;
  fieldName: string;
  description?: string;
}

function buildBuiltInRedirectUri(baseUrl: string, provider: string): string {
  const trimmedBase = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
  return `${trimmedBase}/oidc/${provider || '{provider}'}/callback`;
}

// Mirrors the server-side default in SuperUserOidcProviderConfigurationService: leaving redirectUri blank makes
// the server fill in its own built-in callback URI (`{API_OUTSIDE_URL}/oidc/{provider}/callback`). This checkbox
// is a convenience for previewing/toggling that value client-side; it never changes what actually gets submitted
// when unchecked with a blank field — the server still computes the same default in that case.
export function OidcRedirectUriField({ form, fieldName, description }: OidcRedirectUriFieldProps) {
  const [baseUrl, setBaseUrl] = useState<string | null>(null);
  const [initialized, setInitialized] = useState(false);
  const [useBuiltIn, setUseBuiltIn] = useState(true);

  const provider = form.watch('provider');
  const redirectUri = form.watch(fieldName);

  useEffect(() => {
    getApiConfig()
      .then((config) => setBaseUrl(config.baseUrl))
      .catch(() => setBaseUrl(null));
  }, []);

  // Determine the initial checkbox state once, from whatever value the form started with (blank on create,
  // or the existing stored value on edit) — after that, the checkbox is fully user-driven.
  useEffect(() => {
    if (initialized || baseUrl === null) return;
    const builtIn = buildBuiltInRedirectUri(baseUrl, provider);
    setUseBuiltIn(!redirectUri || redirectUri === builtIn);
    setInitialized(true);
  }, [initialized, baseUrl, provider, redirectUri]);

  // While "use built-in" is checked, keep the field's value in sync with the computed URL (e.g. as the admin
  // types the provider slug).
  useEffect(() => {
    if (!initialized || !useBuiltIn || baseUrl === null) return;
    form.setValue(fieldName, buildBuiltInRedirectUri(baseUrl, provider), {
      shouldDirty: true,
      shouldValidate: true,
    });
  }, [initialized, useBuiltIn, baseUrl, provider, fieldName, form]);

  const builtInPreview = baseUrl !== null ? buildBuiltInRedirectUri(baseUrl, provider) : null;

  return (
    <FormField
      control={form.control}
      name={fieldName}
      render={({ field: formField }) => (
        <FormItem data-testid="form-field-redirectUri">
          <FormLabel>Redirect URI</FormLabel>
          <div className="flex items-center gap-2">
            <Checkbox
              id="oidc-use-builtin-redirect"
              checked={useBuiltIn}
              onCheckedChange={(checked) => setUseBuiltIn(checked === true)}
              data-testid="checkbox-use-builtin-redirect"
            />
            <label
              htmlFor="oidc-use-builtin-redirect"
              className="text-sm font-normal cursor-pointer text-muted-foreground"
            >
              Use built-in Elements redirect
            </label>
          </div>
          <FormControl>
            <Input
              {...formField}
              value={formField.value ?? ''}
              disabled={useBuiltIn}
              placeholder={builtInPreview ?? 'https://your-app.example.com/oidc/twitch/callback'}
              data-testid="input-redirectUri"
            />
          </FormControl>
          {description && (
            <p className="text-sm text-muted-foreground">{description}</p>
          )}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}