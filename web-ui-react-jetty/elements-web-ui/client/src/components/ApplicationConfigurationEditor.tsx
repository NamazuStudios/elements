import { useState, useEffect } from 'react';
import type React from 'react';
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
import { useToast } from '@/hooks/use-toast';

type ConfigurationType = 
  | 'Facebook'
  | 'Firebase'
  | 'GooglePlay'
  | 'iOS'
  | 'Matchmaking'
  | 'Oculus'
  | null;

interface ApplicationConfigurationEditorProps {
  value: any;
  onChange: (value: any) => void;
  configurationType: ConfigurationType;
  onChangeType: (type: ConfigurationType) => void;
  disableTypeSelector?: boolean;
}

export function ApplicationConfigurationEditor({
  value = {},
  onChange,
  configurationType,
  onChangeType,
  disableTypeSelector = false,
}: ApplicationConfigurationEditorProps) {
  const handleFieldChange = (fieldName: string, fieldValue: any) => {
    if (fieldName === '__batch' && typeof fieldValue === 'object') {
      onChange({ ...value, ...fieldValue });
    } else {
      onChange({ ...value, [fieldName]: fieldValue });
    }
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
          <Select value={configurationType || ''} onValueChange={(v) => onChangeType(v as ConfigurationType)} disabled={disableTypeSelector}>
            <SelectTrigger data-testid="select-configuration-type">
              <SelectValue placeholder="Select configuration type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="Facebook">Facebook</SelectItem>
              <SelectItem value="Firebase">Firebase</SelectItem>
              <SelectItem value="GooglePlay">Google Play</SelectItem>
              <SelectItem value="iOS">iOS</SelectItem>
              <SelectItem value="Matchmaking">Matchmaking</SelectItem>
              <SelectItem value="Oculus">Oculus</SelectItem>
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

        {configurationType === 'Oculus' && (
          <OculusConfigFields value={value} onChange={handleFieldChange} />
        )}
      </CardContent>
    </Card>
  );
}

// Validation: name must match pattern [^_]\w+ (no leading underscore, word characters only)
function validateConfigName(name: string): boolean {
  if (!name) return true; // Empty is valid (optional field)
  const pattern = /^[^_]\w*$/; // First char not underscore, then word chars (letters, digits, underscores)
  return pattern.test(name);
}

function CommonConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
  const nameValue = value.name || '';
  const isNameValid = validateConfigName(nameValue);
  const showNameError = nameValue && !isNameValid;

  return (
    <>
      <div className="space-y-2">
        <Label htmlFor="config-name">Name</Label>
        <Input
          id="config-name"
          value={nameValue}
          onChange={(e) => onChange('name', e.target.value)}
          placeholder="myConfig123"
          data-testid="input-config-name"
          className={showNameError ? 'border-destructive' : ''}
        />
        {showNameError ? (
          <p className="text-sm text-destructive" data-testid="error-config-name">
            Name must start with a letter or digit, and contain only letters, digits, or underscores (no spaces or special characters)
          </p>
        ) : (
          <p className="text-sm text-muted-foreground">Optional. Must start with a letter/digit, use only letters, digits, underscores</p>
        )}
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

      <ProductBundlesField value={value.productBundles || []} onChange={(bundles) => onChange('productBundles', bundles)} />
    </>
  );
}

function OculusConfigFields({ value, onChange }: { value: any; onChange: (field: string, val: any) => void }) {
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
          placeholder="Oculus App ID"
          data-testid="input-applicationId"
        />
        <p className="text-sm text-muted-foreground">The AppID as it appears in the Oculus Developer Dashboard</p>
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
          placeholder="Oculus App Secret"
          data-testid="input-applicationSecret"
        />
        <p className="text-sm text-muted-foreground">The App Secret as it appears in the Oculus Developer Dashboard</p>
      </div>

      <div className="space-y-2">
        <Label htmlFor="builtinApplicationPermissions">Permissions</Label>
        <TagsInput
          value={value.builtinApplicationPermissions || []}
          onChange={(tags) => onChange('builtinApplicationPermissions', tags)}
          placeholder="Add permission"
          data-testid="tags-builtinApplicationPermissions"
        />
        <p className="text-sm text-muted-foreground">Built-in permissions connected clients will need to request</p>
      </div>

      <ProductBundlesField value={value.productBundles || []} onChange={(bundles) => onChange('productBundles', bundles)} />
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
  const [jsonKeyError, setJsonKeyError] = useState<string | null>(null);
  
  const jsonKeyValue = typeof value.jsonKey === 'string' 
    ? value.jsonKey 
    : JSON.stringify(value.jsonKey || {}, null, 2);

  const handleJsonKeyChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const textValue = e.target.value;
    
    // If empty, set to empty object
    if (!textValue.trim()) {
      onChange('jsonKey', {});
      setJsonKeyError(null);
      return;
    }
    
    try {
      const parsed = JSON.parse(textValue);
      if (typeof parsed !== 'object' || parsed === null) {
        setJsonKeyError('JSON key must be an object, not a primitive value');
        onChange('jsonKey', textValue); // Store as string temporarily to show error
      } else {
        onChange('jsonKey', parsed);
        setJsonKeyError(null);
      }
    } catch (error) {
      setJsonKeyError('Invalid JSON format');
      onChange('jsonKey', textValue); // Store as string temporarily to show error
    }
  };

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
        <Label htmlFor="jsonKey">Google Play JSON Key (Optional)</Label>
        <Textarea
          id="jsonKey"
          value={jsonKeyValue}
          onChange={handleJsonKeyChange}
          placeholder='{"type": "service_account", "project_id": "..."}'
          rows={8}
          className={`font-mono text-sm ${jsonKeyError ? 'border-destructive' : ''}`}
          data-testid="textarea-jsonKey"
        />
        {jsonKeyError ? (
          <p className="text-sm text-destructive" data-testid="error-jsonKey">
            {jsonKeyError}
          </p>
        ) : (
          <p className="text-sm text-muted-foreground">Google Play service account JSON key (leave empty if not using)</p>
        )}
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
  const maxProfilesValue = value.maxProfiles;
  const showMaxProfilesWarning = maxProfilesValue !== undefined && maxProfilesValue !== null && maxProfilesValue !== '' && maxProfilesValue < 2;

  useEffect(() => {
    setUseDefaultMatchmaker(!value.matchmaker);
  }, [value.matchmaker]);

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
          onChange={(metadata, specId, spec) => {
            const updates: Record<string, any> = { metadata };
            if (specId !== undefined) {
              updates.metadataSpec = spec || (specId ? { id: specId } : undefined);
            }
            onChange('__batch', updates);
          }}
          specId={typeof value.metadataSpec === 'string' ? value.metadataSpec : value.metadataSpec?.id || undefined}
          initialSpec={typeof value.metadataSpec === 'object' ? value.metadataSpec : undefined}
          mode="create"
        />
      </div>
    </>
  );
}

function ProductBundlesField({ value, onChange }: { value: any[]; onChange: (bundles: any[]) => void }) {
  // Initialize bundles with metadataEntries converted from metadata
  const [bundles, setBundles] = useState<any[]>(
    (value || []).map(bundle => ({
      ...bundle,
      metadataEntries: bundle.metadata 
        ? Object.entries(bundle.metadata).map(([key, val]) => ({ 
            key, 
            value: typeof val === 'string' ? val : JSON.stringify(val) 
          }))
        : []
    }))
  );
  const [itemSearchOpen, setItemSearchOpen] = useState(false);
  const [editingReward, setEditingReward] = useState<{ bundleIndex: number; rewardIndex: number } | null>(null);
  const { toast } = useToast();

  // Convert metadataEntries to metadata and call onChange
  const handleBundlesChange = (updatedBundles: any[]) => {
    const bundlesWithMetadata = updatedBundles.map(bundle => {
      const metadata = (bundle.metadataEntries || []).reduce((acc: any, entry: any) => {
        if (entry.key && entry.value) {
          try {
            acc[entry.key] = JSON.parse(entry.value);
          } catch {
            acc[entry.key] = entry.value;
          }
        }
        return acc;
      }, {});
      
      const { metadataEntries, ...rest } = bundle;
      return { ...rest, metadata };
    });
    onChange(bundlesWithMetadata);
  };

  const handleAdd = () => {
    const newBundles = [...bundles, { productId: '', displayName: '', description: '', productBundleRewards: [], metadataEntries: [], display: true }];
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const handleRemove = (index: number) => {
    const newBundles = bundles.filter((_, i) => i !== index);
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const handleChange = (index: number, field: string, fieldValue: any) => {
    const newBundles = [...bundles];
    newBundles[index] = { ...newBundles[index], [field]: fieldValue };
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const handleAddReward = (bundleIndex: number) => {
    const newBundles = [...bundles];
    const currentRewards = newBundles[bundleIndex].productBundleRewards || [];
    newBundles[bundleIndex].productBundleRewards = [...currentRewards, { itemId: '', quantity: 1 }];
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const handleRemoveReward = (bundleIndex: number, rewardIndex: number) => {
    const newBundles = [...bundles];
    newBundles[bundleIndex].productBundleRewards = newBundles[bundleIndex].productBundleRewards.filter((_: any, i: number) => i !== rewardIndex);
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const handleRewardChange = (bundleIndex: number, rewardIndex: number, field: string, fieldValue: any) => {
    const newBundles = [...bundles];
    newBundles[bundleIndex].productBundleRewards[rewardIndex] = { 
      ...newBundles[bundleIndex].productBundleRewards[rewardIndex], 
      [field]: fieldValue 
    };
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const handleItemSelect = (item: any) => {
    if (editingReward) {
      handleRewardChange(editingReward.bundleIndex, editingReward.rewardIndex, 'itemId', item.id);
    }
    setItemSearchOpen(false);
    setEditingReward(null);
  };

  const addMetadataEntry = (bundleIndex: number) => {
    const bundle = bundles[bundleIndex];
    const entries = bundle.metadataEntries || [];
    const newBundles = [...bundles];
    newBundles[bundleIndex].metadataEntries = [...entries, { key: '', value: '' }];
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const removeMetadataEntry = (bundleIndex: number, entryIndex: number) => {
    const newBundles = [...bundles];
    const entries = newBundles[bundleIndex].metadataEntries || [];
    newBundles[bundleIndex].metadataEntries = entries.filter((_: any, i: number) => i !== entryIndex);
    setBundles(newBundles);
    handleBundlesChange(newBundles);
  };

  const updateMetadataEntry = (bundleIndex: number, entryIndex: number, field: 'key' | 'value', value: string) => {
    const newBundles = [...bundles];
    const entries = [...(newBundles[bundleIndex].metadataEntries || [])];
    entries[entryIndex] = { ...entries[entryIndex], [field]: value };
    newBundles[bundleIndex].metadataEntries = entries;
    setBundles(newBundles);
    handleBundlesChange(newBundles);
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
                <CardTitle className="text-sm">Bundle {index + 1}: {bundle.displayName || bundle.productId || 'New Bundle'}</CardTitle>
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
                <div className="space-y-2">
                  <Label>Product ID (SKU) *</Label>
                  <Input
                    placeholder="e.g., com.myapp.coins.100"
                    value={bundle.productId || ''}
                    onChange={(e) => handleChange(index, 'productId', e.target.value)}
                    data-testid={`input-bundle-productId-${index}`}
                  />
                  <p className="text-xs text-muted-foreground">Platform-specific product identifier</p>
                </div>

                <div className="space-y-2">
                  <Label>Display Name</Label>
                  <Input
                    placeholder="e.g., 100 Gold Coins"
                    value={bundle.displayName || ''}
                    onChange={(e) => handleChange(index, 'displayName', e.target.value)}
                    data-testid={`input-bundle-displayName-${index}`}
                  />
                </div>

                <div className="space-y-2">
                  <Label>Description</Label>
                  <Textarea
                    placeholder="Description of this product bundle"
                    value={bundle.description || ''}
                    onChange={(e) => handleChange(index, 'description', e.target.value)}
                    data-testid={`input-bundle-description-${index}`}
                    rows={2}
                  />
                </div>

                <div className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    id={`bundle-display-${index}`}
                    checked={bundle.display !== false}
                    onChange={(e) => handleChange(index, 'display', e.target.checked)}
                    data-testid={`checkbox-bundle-display-${index}`}
                    className="h-4 w-4"
                  />
                  <Label htmlFor={`bundle-display-${index}`} className="text-sm font-normal">
                    Display to end users
                  </Label>
                </div>

                <div className="space-y-2 pt-2">
                  <div className="flex items-center justify-between">
                    <Label>Product Bundle Rewards *</Label>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => handleAddReward(index)}
                      data-testid={`button-add-reward-${index}`}
                    >
                      <Plus className="h-3 w-3 mr-1" />
                      Add Reward
                    </Button>
                  </div>
                  <p className="text-xs text-muted-foreground">Items issued upon purchase</p>
                  {(!bundle.productBundleRewards || bundle.productBundleRewards.length === 0) ? (
                    <p className="text-sm text-muted-foreground italic">No rewards configured</p>
                  ) : (
                    <div className="space-y-2">
                      {bundle.productBundleRewards.map((reward: any, rewardIndex: number) => (
                        <div key={rewardIndex} className="flex gap-2 items-start p-2 rounded border">
                          <div className="flex-1 space-y-2">
                            <div className="flex gap-2">
                              <Input
                                placeholder="Item ID"
                                value={reward.itemId || ''}
                                onChange={(e) => handleRewardChange(index, rewardIndex, 'itemId', e.target.value)}
                                data-testid={`input-reward-itemId-${index}-${rewardIndex}`}
                                className="flex-1"
                              />
                              <Button
                                type="button"
                                variant="outline"
                                size="icon"
                                onClick={() => {
                                  setEditingReward({ bundleIndex: index, rewardIndex });
                                  setItemSearchOpen(true);
                                }}
                                data-testid={`button-search-item-${index}-${rewardIndex}`}
                              >
                                <Search className="h-4 w-4" />
                              </Button>
                            </div>
                            <Input
                              type="number"
                              placeholder="Quantity"
                              value={reward.quantity || 1}
                              onChange={(e) => handleRewardChange(index, rewardIndex, 'quantity', parseInt(e.target.value) || 1)}
                              data-testid={`input-reward-quantity-${index}-${rewardIndex}`}
                            />
                          </div>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => handleRemoveReward(index, rewardIndex)}
                            data-testid={`button-remove-reward-${index}-${rewardIndex}`}
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                <div className="space-y-2 pt-2">
                  <div className="flex items-center justify-between">
                    <Label>Metadata</Label>
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={() => addMetadataEntry(index)}
                      data-testid={`button-add-metadata-${index}`}
                    >
                      <Plus className="h-3 w-3 mr-2" />
                      Add Entry
                    </Button>
                  </div>
                  <p className="text-xs text-muted-foreground">Application-specific metadata key-value pairs (optional)</p>
                  <div className="space-y-2">
                    {(bundle.metadataEntries || []).map((entry: any, entryIndex: number) => (
                      <div key={entryIndex} className="flex gap-2 items-start">
                        <Input
                          placeholder="Key"
                          value={entry.key}
                          onChange={(e) => updateMetadataEntry(index, entryIndex, 'key', e.target.value)}
                          className="flex-1"
                          data-testid={`input-metadata-key-${index}-${entryIndex}`}
                        />
                        <Input
                          placeholder="Value (JSON or text)"
                          value={entry.value}
                          onChange={(e) => updateMetadataEntry(index, entryIndex, 'value', e.target.value)}
                          className="flex-1"
                          data-testid={`input-metadata-value-${index}-${entryIndex}`}
                        />
                        <Button
                          type="button"
                          variant="ghost"
                          size="icon"
                          onClick={() => removeMetadataEntry(index, entryIndex)}
                          data-testid={`button-remove-metadata-${index}-${entryIndex}`}
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    ))}
                    {(!bundle.metadataEntries || bundle.metadataEntries.length === 0) && (
                      <p className="text-sm text-muted-foreground italic">No metadata entries. Click "Add Entry" to add key-value pairs.</p>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
      
      <ResourceSearchDialog
        open={itemSearchOpen}
        onOpenChange={setItemSearchOpen}
        resourceType="item"
        endpoint="/api/rest/item"
        title="Select Item"
        description="Search and select an item to reward in this product bundle"
        displayFields={[
          { key: 'id', label: 'Item ID' },
          { key: 'name', label: 'Name' },
          { key: 'description', label: 'Description' },
        ]}
        onSelect={(resourceId, resource) => handleItemSelect(resource)}
      />
    </div>
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
