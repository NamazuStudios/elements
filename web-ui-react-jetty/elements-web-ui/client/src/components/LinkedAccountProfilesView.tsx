import { type UseFormReturn } from 'react-hook-form';
import { FormField, FormItem, FormLabel } from '@/components/ui/form';
import { Card, CardContent } from '@/components/ui/card';

interface LinkedAccountProfilesViewProps {
  form: UseFormReturn<any>;
  fieldName: string;
  description?: string;
}

// Read-only breakout of User.linkedAccountProfiles — a Map<scheme name, Map<claim name, value>> populated
// server-side from each linked OIDC provider's most recent login. Rendered as one card per scheme instead of
// the generic JSON textarea other object/map fields get, since this is meant to be scanned, not edited.
export function LinkedAccountProfilesView({ form, fieldName, description }: LinkedAccountProfilesViewProps) {
  return (
    <FormField
      control={form.control}
      name={fieldName}
      render={({ field: formField }) => {
        let profiles: Record<string, Record<string, string>> = {};

        try {
          profiles = formField.value ? JSON.parse(formField.value) : {};
        } catch {
          profiles = {};
        }

        const schemeNames = Object.keys(profiles);

        return (
          <FormItem data-testid="form-field-linkedAccountProfiles">
            <FormLabel>Linked Account Profiles</FormLabel>
            {description && (
              <p className="text-sm text-muted-foreground">{description}</p>
            )}
            {schemeNames.length === 0 ? (
              <p className="text-sm text-muted-foreground italic" data-testid="text-no-linked-profiles">
                No linked OIDC provider profile data captured yet.
              </p>
            ) : (
              <div className="space-y-3">
                {schemeNames.map((schemeName) => {
                  const claims = profiles[schemeName] || {};
                  const claimNames = Object.keys(claims);
                  return (
                    <Card key={schemeName} data-testid={`card-linked-profile-${schemeName}`}>
                      <CardContent className="pt-4">
                        <div className="font-medium mb-2">{schemeName}</div>
                        {claimNames.length === 0 ? (
                          <p className="text-sm text-muted-foreground italic">No profile claims reported.</p>
                        ) : (
                          <dl className="grid grid-cols-[max-content_1fr] gap-x-4 gap-y-1 text-sm">
                            {claimNames.map((claimName) => (
                              <div key={claimName} className="contents">
                                <dt className="text-muted-foreground">{claimName}</dt>
                                <dd className="font-mono break-all">{claims[claimName]}</dd>
                              </div>
                            ))}
                          </dl>
                        )}
                      </CardContent>
                    </Card>
                  );
                })}
              </div>
            )}
          </FormItem>
        );
      }}
    />
  );
}
