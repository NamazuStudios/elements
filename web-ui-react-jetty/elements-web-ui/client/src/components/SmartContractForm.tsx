import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Plus, Trash2, RotateCcw, Search } from 'lucide-react';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { apiRequest } from '@/lib/queryClient';
import { useFormDraft } from '@/hooks/use-form-draft';
import { useToast } from '@/hooks/use-toast';
import { ResourceSearchDialog } from '@/components/ResourceSearchDialog';

const BLOCKCHAIN_NETWORKS = [
  'NEO',
  'NEO_TEST',
  'ETHEREUM',
  'ETHEREUM_TEST',
  'BSC',
  'BSC_TEST',
  'POLYGON',
  'POLYGON_TEST',
  'SOLANA',
  'SOLANA_TEST',
  'FLOW',
  'FLOW_TEST',
  'NEAR',
  'NEAR_TEST',
] as const;

interface SmartContractFormProps {
  initialData?: any;
  onSubmit: (data: any) => void;
  onCancel?: () => void;
  mode: 'create' | 'update';
  isPending?: boolean;
}

interface KeyValueEntry {
  key: string;
  value: string;
}

const smartContractSchema = z.object({
  id: z.string().optional(),
  name: z.string().min(1, 'Name is required').regex(/^\S+$/, 'Name must not contain whitespace'),
  displayName: z.string().min(1, 'Display name is required'),
  vaultId: z.string().min(1, 'Vault ID is required'),
});

export function SmartContractForm({ initialData, onSubmit, onCancel, mode, isPending }: SmartContractFormProps) {
  const { toast } = useToast();
  const [addressEntries, setAddressEntries] = useState<KeyValueEntry[]>([]);
  const [metadataEntries, setMetadataEntries] = useState<KeyValueEntry[]>([]);
  const [showDraftRestore, setShowDraftRestore] = useState(false);
  const [vaultSearchOpen, setVaultSearchOpen] = useState(false);
  const [selectedVaultName, setSelectedVaultName] = useState<string>('');

  // Draft management
  const { saveDraft, loadDraft, hasDraft, clearDraft } = useFormDraft({
    resourceName: 'Smart Contracts',
    mode,
    itemId: initialData?.id,
  });

  // Fetch vault details if vaultId is provided
  const { data: vaultData } = useQuery({
    queryKey: ['/api/rest/blockchain/omni/vault', initialData?.vaultId],
    queryFn: async () => {
      if (!initialData?.vaultId) return null;
      const response = await apiRequest('GET', `/api/proxy/api/rest/blockchain/omni/vault/${initialData.vaultId}`);
      return response.json();
    },
    enabled: !!initialData?.vaultId,
  });

  // Set vault name when loaded
  useEffect(() => {
    if (vaultData) {
      setSelectedVaultName(vaultData.displayName || vaultData.id);
    }
  }, [vaultData]);

  const form = useForm({
    resolver: zodResolver(smartContractSchema),
    defaultValues: {
      id: initialData?.id || '',
      name: initialData?.name || '',
      displayName: initialData?.displayName || '',
      vaultId: initialData?.vaultId || '',
    },
  });

  // Check for draft on mount
  useEffect(() => {
    if (mode === 'create') {
      setShowDraftRestore(hasDraft());
    }
  }, [mode, hasDraft]);

  // Initialize entries from existing data or draft
  useEffect(() => {
    if (initialData?.addresses && typeof initialData.addresses === 'object') {
      const entries = Object.entries(initialData.addresses).map(([key, value]) => ({
        key,
        // SmartContractAddress is an object with "address" field
        value: typeof value === 'object' && value !== null && 'address' in value 
          ? String(value.address) 
          : String(value),
      }));
      setAddressEntries(entries);
    }

    if (initialData?.metadata && typeof initialData.metadata === 'object') {
      const entries = Object.entries(initialData.metadata).map(([key, value]) => ({
        key,
        value: typeof value === 'object' ? JSON.stringify(value) : String(value),
      }));
      setMetadataEntries(entries);
    }
  }, [initialData]);

  const restoreDraft = () => {
    const draft = loadDraft();
    if (draft) {
      // Restore form values
      Object.keys(draft).forEach(key => {
        if (key !== 'addresses' && key !== 'metadata') {
          form.setValue(key as any, draft[key]);
        }
      });

      // Restore address entries
      if (draft.addresses && typeof draft.addresses === 'object') {
        const entries = Object.entries(draft.addresses).map(([key, value]) => ({
          key,
          value: typeof value === 'object' && value !== null && 'address' in value 
            ? String(value.address) 
            : String(value),
        }));
        setAddressEntries(entries);
      }

      // Restore metadata entries
      if (draft.metadata && typeof draft.metadata === 'object') {
        const entries = Object.entries(draft.metadata).map(([key, value]) => ({
          key,
          value: typeof value === 'object' ? JSON.stringify(value) : String(value),
        }));
        setMetadataEntries(entries);
      }

      setShowDraftRestore(false);
      toast({
        title: 'Draft Restored',
        description: 'Your saved draft has been restored.',
      });
    }
  };

  const addAddressEntry = () => {
    setAddressEntries([...addressEntries, { key: '', value: '' }]);
  };

  const updateAddressEntry = (index: number, field: 'key' | 'value', value: string) => {
    const updated = [...addressEntries];
    updated[index][field] = value;
    setAddressEntries(updated);
  };

  const removeAddressEntry = (index: number) => {
    setAddressEntries(addressEntries.filter((_, i) => i !== index));
  };

  const addMetadataEntry = () => {
    setMetadataEntries([...metadataEntries, { key: '', value: '' }]);
  };

  const updateMetadataEntry = (index: number, field: 'key' | 'value', value: string) => {
    const updated = [...metadataEntries];
    updated[index][field] = value;
    setMetadataEntries(updated);
  };

  const removeMetadataEntry = (index: number) => {
    setMetadataEntries(metadataEntries.filter((_, i) => i !== index));
  };

  const handleFormSubmit = (values: any) => {
    // Convert entries to objects
    const addresses: Record<string, any> = {};
    addressEntries.forEach((entry) => {
      if (entry.key) {
        // SmartContractAddress expects an object with "address" field
        addresses[entry.key] = {
          address: entry.value
        };
      }
    });

    const metadata: Record<string, any> = {};
    metadataEntries.forEach((entry) => {
      if (entry.key) {
        try {
          // Try to parse as JSON first
          metadata[entry.key] = JSON.parse(entry.value);
        } catch {
          // If not JSON, use as string
          metadata[entry.key] = entry.value;
        }
      }
    });

    const submitData: any = {
      ...values,
      addresses,
    };

    // Only include metadata if there are entries
    if (Object.keys(metadata).length > 0) {
      submitData.metadata = metadata;
    }

    // Remove id field in create mode
    if (mode === 'create') {
      delete submitData.id;
    }

    onSubmit(submitData);
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleFormSubmit)} className="space-y-6">
        {showDraftRestore && (
          <div className="flex items-center justify-between p-4 bg-muted rounded-lg">
            <p className="text-sm text-muted-foreground">You have a saved draft for this Smart Contract.</p>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={restoreDraft}
              data-testid="button-restore-draft-banner"
            >
              <RotateCcw className="w-4 h-4 mr-2" />
              Restore Draft
            </Button>
          </div>
        )}

        {mode === 'update' && (
          <FormField
            control={form.control}
            name="id"
            render={({ field }) => (
              <FormItem>
                <FormLabel>ID</FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    readOnly
                    className="bg-muted cursor-not-allowed"
                    data-testid="input-id"
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
        )}

        <FormField
          control={form.control}
          name="name"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Name *</FormLabel>
              <FormControl>
                <Input
                  {...field}
                  placeholder="Enter unique symbolic name (no whitespace)"
                  data-testid="input-name"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="displayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Display Name *</FormLabel>
              <FormControl>
                <Input
                  {...field}
                  placeholder="Enter display name"
                  data-testid="input-displayName"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="vaultId"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Vault ID *</FormLabel>
              <FormControl>
                <div className="space-y-2">
                  <Button
                    type="button"
                    variant="outline"
                    className="w-full justify-between"
                    onClick={() => setVaultSearchOpen(true)}
                    data-testid="button-search-vault"
                  >
                    <span className="truncate">
                      {selectedVaultName || field.value || 'Select a vault'}
                    </span>
                    <Search className="w-4 h-4 ml-2 flex-shrink-0" />
                  </Button>
                  {field.value && (
                    <p className="text-xs text-muted-foreground font-mono">{field.value}</p>
                  )}
                </div>
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <ResourceSearchDialog
          open={vaultSearchOpen}
          onOpenChange={setVaultSearchOpen}
          onSelect={(vaultId, vault) => {
            form.setValue('vaultId', vaultId);
            setSelectedVaultName(vault.displayName || vault.id);
          }}
          resourceType="vault"
          endpoint="/api/rest/blockchain/omni/vault"
          title="Search Vaults"
          description="Search for a vault to associate with this smart contract"
          displayFields={[
            { label: 'Display Name', key: 'displayName' },
            { label: 'User ID', key: 'userId' },
            { label: 'ID', key: 'id' },
          ]}
          searchPlaceholder="Search by name or ID..."
          currentResourceId={form.getValues('vaultId')}
        />

        <Tabs defaultValue="addresses" className="w-full">
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="addresses" data-testid="tab-addresses">
              Addresses
            </TabsTrigger>
            <TabsTrigger value="metadata" data-testid="tab-metadata">
              Metadata
            </TabsTrigger>
          </TabsList>

          <TabsContent value="addresses" className="space-y-4 mt-4">
            <div>
              <div className="flex items-center justify-between mb-2">
                <Label>Blockchain Addresses *</Label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={addAddressEntry}
                  data-testid="button-add-address"
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Add Entry
                </Button>
              </div>
              <div className="space-y-2">
                {addressEntries.map((entry, index) => (
                  <div key={index} className="flex gap-2 items-start">
                    <Select
                      value={entry.key}
                      onValueChange={(value) => updateAddressEntry(index, 'key', value)}
                    >
                      <SelectTrigger className="flex-1" data-testid={`select-address-key-${index}`}>
                        <SelectValue placeholder="Select blockchain network" />
                      </SelectTrigger>
                      <SelectContent>
                        {BLOCKCHAIN_NETWORKS.map((network) => (
                          <SelectItem key={network} value={network}>
                            {network}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <Input
                      placeholder="Contract Address (JSON or text)"
                      value={entry.value}
                      onChange={(e) => updateAddressEntry(index, 'value', e.target.value)}
                      className="flex-1"
                      data-testid={`input-address-value-${index}`}
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => removeAddressEntry(index)}
                      data-testid={`button-remove-address-${index}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                ))}
                {addressEntries.length === 0 && (
                  <p className="text-sm text-muted-foreground">
                    No addresses. Click "Add Entry" to add blockchain network addresses.
                  </p>
                )}
              </div>
            </div>
          </TabsContent>

          <TabsContent value="metadata" className="space-y-4 mt-4">
            <div>
              <div className="flex items-center justify-between mb-2">
                <Label>Metadata (Optional)</Label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={addMetadataEntry}
                  data-testid="button-add-metadata"
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Add Entry
                </Button>
              </div>
              <div className="space-y-2">
                {metadataEntries.map((entry, index) => (
                  <div key={index} className="flex gap-2 items-start">
                    <Input
                      placeholder="Key"
                      value={entry.key}
                      onChange={(e) => updateMetadataEntry(index, 'key', e.target.value)}
                      className="flex-1"
                      data-testid={`input-metadata-key-${index}`}
                    />
                    <Input
                      placeholder="Value (JSON or text)"
                      value={entry.value}
                      onChange={(e) => updateMetadataEntry(index, 'value', e.target.value)}
                      className="flex-1"
                      data-testid={`input-metadata-value-${index}`}
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => removeMetadataEntry(index)}
                      data-testid={`button-remove-metadata-${index}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                ))}
                {metadataEntries.length === 0 && (
                  <p className="text-sm text-muted-foreground">
                    No metadata. Click "Add Entry" to add key-value pairs.
                  </p>
                )}
              </div>
            </div>
          </TabsContent>
        </Tabs>

        <div className="flex justify-end gap-2">
          {onCancel && (
            <Button
              type="button"
              variant="outline"
              onClick={onCancel}
              disabled={isPending}
              data-testid="button-cancel"
            >
              Cancel
            </Button>
          )}
          {mode === 'create' && (
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                const currentValues = form.getValues();
                
                // Build addresses and metadata from entry arrays
                const addresses: Record<string, any> = {};
                addressEntries.forEach((entry) => {
                  if (entry.key) {
                    addresses[entry.key] = { address: entry.value };
                  }
                });

                const metadata: Record<string, any> = {};
                metadataEntries.forEach((entry) => {
                  if (entry.key) {
                    try {
                      metadata[entry.key] = JSON.parse(entry.value);
                    } catch {
                      metadata[entry.key] = entry.value;
                    }
                  }
                });

                const draftData = {
                  ...currentValues,
                  addresses: Object.keys(addresses).length > 0 ? addresses : undefined,
                  metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
                };

                saveDraft(draftData);
                toast({
                  title: 'Draft Saved',
                  description: 'Your changes have been saved as a draft.',
                });
              }}
              disabled={isPending}
              data-testid="button-save-draft"
            >
              Save Draft
            </Button>
          )}
          <Button type="submit" disabled={isPending} data-testid="button-submit">
            {mode === 'create' ? 'Create Smart Contract' : 'Update Smart Contract'}
          </Button>
        </div>
      </form>
    </Form>
  );
}
