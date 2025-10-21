import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Plus, Trash2, Search } from 'lucide-react';
import { TagsInput } from '@/components/TagsInput';
import { MetadataEditor } from '@/components/MetadataEditor';
import { ResourceSearchDialog } from '@/components/ResourceSearchDialog';

type ConfigurationType = 
  | 'Facebook'
  | 'Firebase'
  | 'GooglePlay'
  | 'iOS'
  | 'Matchmaking'
  | null;

interface ApplicationConfigurationEditorProps {
  value: any;
  onChange: (value: any) => void;
  configurationType: ConfigurationType;
  onChangeType: (type: ConfigurationType) => void;
}

export function ApplicationConfigurationEditor({
  value = {},
  onChange,
  configurationType,
  onChangeType,
}: ApplicationConfigurationEditorProps) {
  const handleFieldChange = (fieldName: string, fieldValue: any) => {
    onChange({ ...value, [fieldName]: fieldValue });
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Application Configuration</CardTitle>
        <CardDescription>Platform-specific configuration for this application</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <Label>Configuration Type</Label>
          <Select value={configurationType || ''} onValueChange={(v) => onChangeType(v as ConfigurationType)}>
            <SelectTrigger data-testid="select-configuration-type">
              <SelectValue placeholder="Select configuration type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="Facebook">Facebook</SelectItem>
              <SelectItem value="Firebase">Firebase</SelectItem>
              <SelectItem value="GooglePlay">Google Play</SelectItem>
              <SelectItem value="iOS">iOS</SelectItem>
              <SelectItem value="Matchmaking">Matchmaking</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {configurationType === 'Facebook' && (
          <FacebookConfigFields value={value} onChange={handleFieldChange} />
        )}

        {configurationType === 'Firebase' && (
          <FirebaseConfigFields value={value} onChange={handleFieldChange} />
        )}

        {configurationType === 'GooglePlay' && (
          <GooglePlayConfigFields value={value} onChange={handleFieldChange} />
        )}

        {configurationType === 'iOS' && (
          <IOSConfigFields value={value} onChange={handleFieldChange} />
        )}

        {configurationType === 'Matchmaking' && (
          <MatchmakingConfigFields value={value} onChange={handleFieldChange} />
        )}
      </CardContent>
    </Card>
  );
}

function CommonConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
  return (
    <>
      <div className="space-y-2">
        <Label htmlFor="config-name">Name</Label>
        <Input
          id="config-name"
          value={value.name || ''}
          onChange={(e) => onChange('name', e.target.value)}
          placeholder="Configuration name"
          data-testid="input-config-name"
        />
        <p className="text-sm text-muted-foreground">Optional name for this configuration</p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="config-description">Description</Label>
        <Textarea
          id="config-description"
          value={value.description || ''}
          onChange={(e) => onChange('description', e.target.value)}
          placeholder="Configuration description"
          rows={2}
          data-testid="textarea-config-description"
        />
        <p className="text-sm text-muted-foreground">Optional description for this configuration</p>
      </div>
    </>
  );
}

function FacebookConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
  return (
    <>
      <CommonConfigFields value={value} onChange={onChange} />
      
      <div className="space-y-2">
        <Label htmlFor="applicationId">
          Application ID {!value.applicationId && <span className="text-destructive">*</span>}
        </Label>
        <Input
          id="applicationId"
          value={value.applicationId || ''}
          onChange={(e) => onChange('applicationId', e.target.value)}
          placeholder="Facebook App ID"
          data-testid="input-applicationId"
        />
        <p className="text-sm text-muted-foreground">The AppID as it appears in the Facebook Developer Console</p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="applicationSecret">
          Application Secret {!value.applicationSecret && <span className="text-destructive">*</span>}
        </Label>
        <Input
          id="applicationSecret"
          type="password"
          value={value.applicationSecret || ''}
          onChange={(e) => onChange('applicationSecret', e.target.value)}
          placeholder="Facebook App Secret"
          data-testid="input-applicationSecret"
        />
        <p className="text-sm text-muted-foreground">The App Secret as it appears in the Facebook Developer Console</p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="builtinApplicationPermissions">Permissions</Label>
        <TagsInput
          value={value.builtinApplicationPermissions || []}
          onChange={(tags) => onChange('builtinApplicationPermissions', tags)}
          placeholder="Add permission (e.g., email, public_profile)"
          data-testid="tags-builtinApplicationPermissions"
        />
        <p className="text-sm text-muted-foreground">Built-in permissions connected clients will need to request</p>
      </div>
    </>
  );
}

function FirebaseConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
  return (
    <>
      <CommonConfigFields value={value} onChange={onChange} />
      
      <div className="space-y-2">
        <Label htmlFor="projectId">
          Project ID {!value.projectId && <span className="text-destructive">*</span>}
        </Label>
        <Input
          id="projectId"
          value={value.projectId || ''}
          onChange={(e) => onChange('projectId', e.target.value)}
          placeholder="Firebase project ID"
          data-testid="input-projectId"
        />
        <p className="text-sm text-muted-foreground">The Firebase project ID</p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="serviceAccountCredentials">
          Service Account Credentials {!value.serviceAccountCredentials && <span className="text-destructive">*</span>}
        </Label>
        <Textarea
          id="serviceAccountCredentials"
          value={value.serviceAccountCredentials || ''}
          onChange={(e) => onChange('serviceAccountCredentials', e.target.value)}
          placeholder="Paste the contents of serviceAccountCredentials.json file"
          rows={8}
          className="font-mono text-sm"
          data-testid="textarea-serviceAccountCredentials"
        />
        <p className="text-sm text-muted-foreground">The contents of the serviceAccountCredentials.json file</p>
      </div>
    </>
  );
}

function GooglePlayConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
  return (
    <>
      <CommonConfigFields value={value} onChange={onChange} />
      
      <div className="space-y-2">
        <Label htmlFor="applicationId">Application ID</Label>
        <Input
          id="applicationId"
          value={value.applicationId || ''}
          onChange={(e) => onChange('applicationId', e.target.value)}
          placeholder="com.example.app"
          data-testid="input-applicationId"
        />
        <p className="text-sm text-muted-foreground">Application ID as defined in Google Play (e.g., com.mycompany.app)</p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="jsonKey">Google Play JSON Key</Label>
        <Textarea
          id="jsonKey"
          value={typeof value.jsonKey === 'string' ? value.jsonKey : JSON.stringify(value.jsonKey || {}, null, 2)}
          onChange={(e) => {
            try {
              const parsed = JSON.parse(e.target.value);
              onChange('jsonKey', parsed);
            } catch {
              onChange('jsonKey', e.target.value);
            }
          }}
          placeholder="Paste Google Play service account JSON key"
          rows={8}
          className="font-mono text-sm"
          data-testid="textarea-jsonKey"
        />
        <p className="text-sm text-muted-foreground">Google Play service account JSON key</p>
      </div>

      <ProductBundlesField value={value.productBundles || []} onChange={(bundles) => onChange('productBundles', bundles)} />
    </>
  );
}

function IOSConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
  return (
    <>
      <CommonConfigFields value={value} onChange={onChange} />
      
      <div className="space-y-2">
        <Label htmlFor="applicationId">
          Application ID {!value.applicationId && <span className="text-destructive">*</span>}
        </Label>
        <Input
          id="applicationId"
          value={value.applicationId || ''}
          onChange={(e) => onChange('applicationId', e.target.value)}
          placeholder="com.example.app"
          data-testid="input-applicationId"
        />
        <p className="text-sm text-muted-foreground">Application ID as defined in the AppStore (e.g., com.mycompany.app)</p>
      </div>

      <ProductBundlesField value={value.productBundles || []} onChange={(bundles) => onChange('productBundles', bundles)} />
    </>
  );
}

function MatchmakingConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
  const [useDefaultMatchmaker, setUseDefaultMatchmaker] = useState(!value.matchmaker);
  const [defineSuccessCallback, setDefineSuccessCallback] = useState(!!value.success);
  const [specSearchOpen, setSpecSearchOpen] = useState(false);
  const [selectedSpecName, setSelectedSpecName] = useState<string>('');
  const maxProfilesValue = value.maxProfiles;
  const showMaxProfilesWarning = maxProfilesValue !== undefined && maxProfilesValue !== null && maxProfilesValue !== '' && maxProfilesValue < 2;

  // Update checkbox state when value prop changes (e.g., when editing existing configuration)
  useEffect(() => {
    setUseDefaultMatchmaker(!value.matchmaker);
    setDefineSuccessCallback(!!value.success);
  }, [value.matchmaker, value.success]);

  return (
    <>
      <CommonConfigFields value={value} onChange={onChange} />
      
      <div className="space-y-2">
        <Label htmlFor="maxProfiles">
          Max Profiles {(!value.maxProfiles || value.maxProfiles < 2) && <span className="text-destructive">*</span>}
        </Label>
        <Input
          id="maxProfiles"
          type="number"
          min={2}
          value={value.maxProfiles || ''}
          onChange={(e) => {
            const val = e.target.value === '' ? undefined : parseInt(e.target.value);
            onChange('maxProfiles', val);
          }}
          placeholder="Minimum value: 2"
          data-testid="input-maxProfiles"
          className={showMaxProfilesWarning ? 'border-destructive' : ''}
        />
        {showMaxProfilesWarning && (
          <p className="text-sm text-destructive" data-testid="warning-maxProfiles">
            Max Profiles must be 2 or greater
          </p>
        )}
        {!showMaxProfilesWarning && (
          <p className="text-sm text-muted-foreground">The maximum number of profiles that can be matched in a single match (minimum: 2)</p>
        )}
      </div>

      <div className="space-y-2">
        <Label htmlFor="lingerSeconds">
          Linger Seconds
        </Label>
        <Input
          id="lingerSeconds"
          type="number"
          min={0}
          value={value.lingerSeconds ?? 300}
          onChange={(e) => {
            const val = e.target.value === '' ? 300 : parseInt(e.target.value);
            onChange('lingerSeconds', val);
          }}
          placeholder="300"
          data-testid="input-lingerSeconds"
        />
        <p className="text-sm text-muted-foreground">Time a match will linger after expiry (default: 300 = 5 minutes)</p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="timeoutSeconds">
          Timeout Seconds
        </Label>
        <Input
          id="timeoutSeconds"
          type="number"
          min={0}
          value={value.timeoutSeconds ?? 86400}
          onChange={(e) => {
            const val = e.target.value === '' ? 86400 : parseInt(e.target.value);
            onChange('timeoutSeconds', val);
          }}
          placeholder="86400"
          data-testid="input-timeoutSeconds"
        />
        <p className="text-sm text-muted-foreground">Absolute match timeout (default: 86400 = 24 hours)</p>
      </div>

      <div className="space-y-2">
        <Label>Metadata Spec (Optional)</Label>
        <Button
          type="button"
          variant="outline"
          className="w-full justify-between"
          onClick={() => setSpecSearchOpen(true)}
          data-testid="button-search-metadata-spec"
        >
          <span className="truncate">
            {selectedSpecName || value.metadataSpec?.id || value.metadataSpec || 'Select a metadata spec'}
          </span>
          <Search className="w-4 h-4 ml-2 flex-shrink-0" />
        </Button>
        {value.metadataSpec && (
          <p className="text-xs text-muted-foreground font-mono">
            {typeof value.metadataSpec === 'string' ? value.metadataSpec : value.metadataSpec.id}
          </p>
        )}
        <p className="text-sm text-muted-foreground">Defines the structure of metadata for this matchmaking configuration</p>
      </div>

      <ResourceSearchDialog
        open={specSearchOpen}
        onOpenChange={setSpecSearchOpen}
        onSelect={(specId, spec) => {
          onChange('metadataSpec', specId);
          setSelectedSpecName(spec.name || specId);
        }}
        resourceType="metadata_spec"
        endpoint="/api/rest/metadata_spec"
        title="Search Metadata Specs"
        description="Search for a metadata specification"
        displayFields={[
          { label: 'Name', key: 'name' },
          { label: 'ID', key: 'id' },
        ]}
        searchPlaceholder="Search by name or ID..."
        currentResourceId={typeof value.metadataSpec === 'string' ? value.metadataSpec : value.metadataSpec?.id}
      />

      <div className="flex items-center space-x-2">
        <Checkbox
          id="defineSuccessCallback"
          checked={defineSuccessCallback}
          onCheckedChange={(checked) => {
            setDefineSuccessCallback(checked as boolean);
            if (!checked) {
              onChange('success', undefined);
            }
          }}
          data-testid="checkbox-defineSuccessCallback"
        />
        <Label htmlFor="defineSuccessCallback" className="text-sm font-normal cursor-pointer">
          Define Success Callback
        </Label>
      </div>

      {defineSuccessCallback && (
        <CallbackDefinitionField
          label="Success Callback"
          value={value.success}
          onChange={(callback) => onChange('success', callback)}
        />
      )}

      <div className="flex items-center space-x-2">
        <Checkbox
          id="useDefaultMatchmaker"
          checked={useDefaultMatchmaker}
          onCheckedChange={(checked) => {
            setUseDefaultMatchmaker(checked as boolean);
            if (checked) {
              onChange('matchmaker', undefined);
            }
          }}
          data-testid="checkbox-useDefaultMatchmaker"
        />
        <Label htmlFor="useDefaultMatchmaker" className="text-sm font-normal cursor-pointer">
          Use Default Matchmaker
        </Label>
      </div>

      {!useDefaultMatchmaker && (
        <ElementServiceReferenceField
          label="Matchmaker Service"
          value={value.matchmaker}
          onChange={(ref) => onChange('matchmaker', ref)}
        />
      )}

      <div className="space-y-2">
        <Label>Metadata</Label>
        <MetadataEditor
          value={value.metadata || {}}
          onChange={(metadata) => onChange('metadata', metadata)}
          specId={undefined}
          mode="create"
        />
      </div>
    </>
  );
}

function ProductBundlesField({ value, onChange }: { value: any[]; onChange: (bundles: any[]) => void }) {
  const [bundles, setBundles] = useState<any[]>(value || []);

  const handleAdd = () => {
    const newBundles = [...bundles, { productId: '', displayName: '', description: '', productBundleRewards: [], metadata: {}, display: true }];
    setBundles(newBundles);
    onChange(newBundles);
  };

  const handleRemove = (index: number) => {
    const newBundles = bundles.filter((_, i) => i !== index);
    setBundles(newBundles);
    onChange(newBundles);
  };

  const handleChange = (index: number, field: string, fieldValue: any) => {
    const newBundles = [...bundles];
    newBundles[index] = { ...newBundles[index], [field]: fieldValue };
    setBundles(newBundles);
    onChange(newBundles);
  };

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between">
        <Label>Product Bundles</Label>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={handleAdd}
          data-testid="button-add-product-bundle"
        >
          <Plus className="h-4 w-4 mr-2" />
          Add Bundle
        </Button>
      </div>
      {bundles.length === 0 ? (
        <p className="text-sm text-muted-foreground">No product bundles configured</p>
      ) : (
        <div className="space-y-4">
          {bundles.map((bundle, index) => (
            <Card key={index}>
              <CardHeader className="flex flex-row items-center justify-between gap-2 space-y-0 pb-3">
                <CardTitle className="text-sm">Bundle {index + 1}</CardTitle>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => handleRemove(index)}
                  data-testid={`button-remove-bundle-${index}`}
                >
                  <Trash2 className="h-4 w-4" />
                </Button>
              </CardHeader>
              <CardContent className="space-y-3">
                <Input
                  placeholder="Product ID (SKU)"
                  value={bundle.productId || ''}
                  onChange={(e) => handleChange(index, 'productId', e.target.value)}
                  data-testid={`input-bundle-productId-${index}`}
                />
                <Input
                  placeholder="Display Name"
                  value={bundle.displayName || ''}
                  onChange={(e) => handleChange(index, 'displayName', e.target.value)}
                  data-testid={`input-bundle-displayName-${index}`}
                />
                <Input
                  placeholder="Description"
                  value={bundle.description || ''}
                  onChange={(e) => handleChange(index, 'description', e.target.value)}
                  data-testid={`input-bundle-description-${index}`}
                />
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}

function CallbackDefinitionField({ label, value, onChange }: { label: string; value: any; onChange: (val: any) => void }) {
  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle className="text-sm">{label}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        <Input
          placeholder="Method name"
          value={value?.method || ''}
          onChange={(e) => onChange({ ...value, method: e.target.value })}
          data-testid="input-callback-method"
        />
        <ElementServiceReferenceField
          label="Service"
          value={value?.service}
          onChange={(service) => onChange({ ...value, service })}
        />
      </CardContent>
    </Card>
  );
}

function ElementServiceReferenceField({ label, value, onChange }: { label: string; value: any; onChange: (val: any) => void }) {
  return (
    <div className="space-y-2">
      <Label className="text-sm font-medium">{label}</Label>
      <div className="space-y-2 rounded-md border p-3">
        <Input
          placeholder="Element Name"
          value={value?.elementName || ''}
          onChange={(e) => onChange({ ...value, elementName: e.target.value })}
          data-testid="input-service-elementName"
        />
        <Input
          placeholder="Service Type (optional)"
          value={value?.serviceType || ''}
          onChange={(e) => onChange({ ...value, serviceType: e.target.value })}
          data-testid="input-service-serviceType"
        />
        <Input
          placeholder="Service Name (optional)"
          value={value?.serviceName || ''}
          onChange={(e) => onChange({ ...value, serviceName: e.target.value })}
          data-testid="input-service-serviceName"
        />
      </div>
    </div>
  );
}
