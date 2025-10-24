import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Loader2, Plus, Trash2, Eye, EyeOff } from 'lucide-react';
import { useState } from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

interface WalletFormProps {
  mode: 'create' | 'update';
  vaultId: string;
  initialData?: any;
  onSubmit: (data: any) => Promise<void>;
  onCancel: () => void;
}

// Blockchain API options (based on BlockchainApi enum)
// BSC, BSC_TEST, POLYGON, POLYGON_TEST all use the ETHEREUM API
const blockchainApiOptions = [
  'NEO',
  'ETHEREUM',
  'SOLANA',
  'FLOW',
  'NEAR',
];

// All available blockchain networks
const allNetworks = [
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
];

// Network options mapped by API based on BlockchainNetwork enum
// NEO API → NEO, NEO_TEST
// ETHEREUM API → ETHEREUM, ETHEREUM_TEST, BSC, BSC_TEST, POLYGON, POLYGON_TEST
// SOLANA API → SOLANA, SOLANA_TEST
// FLOW API → FLOW, FLOW_TEST
// NEAR API → NEAR, NEAR_TEST
const networkOptionsByApi: Record<string, string[]> = {
  NEO: ['NEO', 'NEO_TEST'],
  ETHEREUM: ['ETHEREUM', 'ETHEREUM_TEST', 'BSC', 'BSC_TEST', 'POLYGON', 'POLYGON_TEST'],
  SOLANA: ['SOLANA', 'SOLANA_TEST'],
  FLOW: ['FLOW', 'FLOW_TEST'],
  NEAR: ['NEAR', 'NEAR_TEST'],
};

const createAccountSchema = z.object({
  generate: z.boolean().default(false),
  address: z.string().optional(),
  privateKey: z.string().optional(),
});

const createSchema = z.object({
  displayName: z.string().min(1, 'Display name is required'),
  preferredAccount: z.number().min(1).default(1),
});

const updateSchema = z.object({
  displayName: z.string().min(1, 'Display name is required'),
  preferredAccount: z.number().min(1, 'Preferred account must be at least 1'),
  networks: z.array(z.string()).min(1, 'At least one network is required'),
});

export function WalletForm({ mode, vaultId, initialData, onSubmit, onCancel }: WalletFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [selectedApi, setSelectedApi] = useState(initialData?.api || '');
  const [selectedNetworks, setSelectedNetworks] = useState<string[]>(initialData?.networks || []);
  const [accounts, setAccounts] = useState<any[]>(
    initialData?.accounts || [{ generate: true, address: null, privateKey: null }]
  );
  const [showPrivateKeys, setShowPrivateKeys] = useState<Record<number, boolean>>({});

  const schema = mode === 'create' ? createSchema : updateSchema;
  
  const form = useForm({
    resolver: zodResolver(schema),
    defaultValues: mode === 'update' && initialData
      ? {
          displayName: initialData.displayName || '',
          preferredAccount: initialData.preferredAccount || 1,
          networks: initialData.networks || [],
        }
      : {
          displayName: '',
          api: '',
          networks: [],
          preferredAccount: 1,
          accounts: [{ generate: true, address: '', privateKey: '' }],
        },
  });

  const handleSubmit = async (data: any) => {
    setIsSubmitting(true);
    try {
      const submitData = { ...data };
      
      if (mode === 'create') {
        // Validate required fields manually since they're not in the form
        if (!selectedApi) {
          form.setError('root', { message: 'Please select a blockchain API' });
          setIsSubmitting(false);
          return;
        }
        if (selectedNetworks.length === 0) {
          form.setError('root', { message: 'Please select at least one network' });
          setIsSubmitting(false);
          return;
        }
        if (accounts.length === 0) {
          form.setError('root', { message: 'Please add at least one account' });
          setIsSubmitting(false);
          return;
        }
        
        // Validate and clean up accounts
        for (let i = 0; i < accounts.length; i++) {
          const acc = accounts[i];
          if (!acc.generate) {
            // Both address and private key are required when not generating
            const address = acc.address;
            if (!address || (typeof address === 'string' && address.trim() === '')) {
              form.setError('root', { message: `Account ${i + 1}: Address is required when not generating an account` });
              setIsSubmitting(false);
              return;
            }
            const privateKey = acc.privateKey;
            if (!privateKey || (typeof privateKey === 'string' && privateKey.trim() === '')) {
              form.setError('root', { message: `Account ${i + 1}: Private key is required when not generating an account` });
              setIsSubmitting(false);
              return;
            }
          }
        }
        
        // Clean up accounts: remove empty string values, keep only meaningful data
        submitData.accounts = accounts.map(acc => {
          if (acc.generate) {
            // When generate is true, ONLY send generate flag - completely omit address and privateKey
            return { generate: true };
          } else {
            // When generate is false, send both address and privateKey (both required)
            const address = acc.address && typeof acc.address === 'string' ? acc.address.trim() : '';
            const privateKey = acc.privateKey && typeof acc.privateKey === 'string' ? acc.privateKey.trim() : '';
            return { 
              generate: false, 
              address: address,
              privateKey: privateKey
            };
          }
        });
        submitData.networks = selectedNetworks;
        submitData.api = selectedApi;
      } else {
        // Update mode: accounts cannot be modified per UpdateWalletRequest SDK model
        // Only displayName, preferredAccount, and networks are updatable
        if (selectedNetworks.length === 0) {
          form.setError('root', { message: 'Please select at least one network' });
          setIsSubmitting(false);
          return;
        }
        submitData.networks = selectedNetworks;
      }
      
      await onSubmit(submitData);
    } finally {
      setIsSubmitting(false);
    }
  };

  const addAccount = () => {
    setAccounts([...accounts, { generate: true, address: null, privateKey: null }]);
  };

  const removeAccount = (index: number) => {
    if (accounts.length > 1) {
      setAccounts(accounts.filter((_, i) => i !== index));
    }
  };

  const updateAccount = (index: number, field: string, value: any) => {
    const updated = [...accounts];
    updated[index] = { ...updated[index], [field]: value };
    setAccounts(updated);
  };

  const toggleNetwork = (network: string) => {
    if (selectedNetworks.includes(network)) {
      setSelectedNetworks(selectedNetworks.filter(n => n !== network));
    } else {
      setSelectedNetworks([...selectedNetworks, network]);
    }
  };

  const selectableNetworks = selectedApi ? (networkOptionsByApi[selectedApi] || []) : [];
  const displayNetworks = mode === 'create' ? allNetworks : (initialData?.api ? (networkOptionsByApi[initialData.api] || []) : []);

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        {/* Show form-level errors */}
        {form.formState.errors.root && (
          <div className="text-sm text-destructive">
            {form.formState.errors.root.message}
          </div>
        )}

        {/* Display Name */}
        <FormField
          control={form.control}
          name="displayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Display Name *</FormLabel>
              <FormControl>
                <Input {...field} data-testid="input-displayName" />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* API (create only) */}
        {mode === 'create' && (
          <div className="space-y-2">
            <Label>Blockchain API *</Label>
            <Select
              value={selectedApi}
              onValueChange={(value) => {
                setSelectedApi(value);
                setSelectedNetworks([]);
              }}
            >
              <SelectTrigger data-testid="select-api">
                <SelectValue placeholder="Select blockchain API" />
              </SelectTrigger>
              <SelectContent>
                {blockchainApiOptions.map((api) => (
                  <SelectItem key={api} value={api}>
                    {api}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        )}

        {/* Networks */}
        <div className="space-y-2">
          <Label>Networks * {mode === 'update' && <span className="text-xs text-muted-foreground">(API: {initialData?.api})</span>}</Label>
          <div className="grid grid-cols-2 gap-2">
            {displayNetworks.length > 0 ? (
              displayNetworks.map((network) => {
                const isSelectable = mode === 'create' ? selectableNetworks.includes(network) : true;
                return (
                  <div key={network} className="flex items-center space-x-2">
                    <Checkbox
                      checked={selectedNetworks.includes(network)}
                      onCheckedChange={() => toggleNetwork(network)}
                      disabled={!isSelectable}
                      data-testid={`checkbox-network-${network}`}
                    />
                    <label className={`text-sm ${!isSelectable ? 'text-muted-foreground' : ''}`}>{network}</label>
                  </div>
                );
              })
            ) : (
              <p className="text-sm text-muted-foreground col-span-2">
                {mode === 'create' ? 'Select an API to see available networks' : 'No networks available'}
              </p>
            )}
          </div>
        </div>

        {/* Preferred Account */}
        <FormField
          control={form.control}
          name="preferredAccount"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Preferred Account Index</FormLabel>
              <FormControl>
                <Input
                  type="number"
                  min={1}
                  {...field}
                  onChange={(e) => field.onChange(parseInt(e.target.value, 10))}
                  data-testid="input-preferredAccount"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Accounts (create only) */}
        {mode === 'create' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <Label>Accounts *</Label>
              <Button
                type="button"
                size="sm"
                variant="outline"
                onClick={addAccount}
                disabled={accounts.length >= 25}
                data-testid="button-add-account"
              >
                <Plus className="w-4 h-4 mr-2" />
                Add Account
              </Button>
            </div>

            <div className="space-y-4">
              {accounts.map((account, index) => (
                <Card key={index}>
                  <CardHeader className="pb-3">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-sm">Account {index + 1}</CardTitle>
                      {accounts.length > 1 && (
                        <Button
                          type="button"
                          size="icon"
                          variant="ghost"
                          onClick={() => removeAccount(index)}
                          data-testid={`button-remove-account-${index}`}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      )}
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        checked={account.generate}
                        onCheckedChange={(checked) => {
                          const updated = [...accounts];
                          updated[index] = { 
                            ...updated[index], 
                            generate: checked,
                            // When generate is true, address and privateKey must be null
                            address: checked ? null : (updated[index].address || ''),
                            privateKey: checked ? null : (updated[index].privateKey || '')
                          };
                          setAccounts(updated);
                        }}
                        data-testid={`checkbox-generate-${index}`}
                      />
                      <label className="text-sm">Generate new account</label>
                    </div>

                    {!account.generate && (
                      <>
                        <div className="space-y-2">
                          <Label>Address *</Label>
                          <Input
                            value={account.address || ''}
                            onChange={(e) => updateAccount(index, 'address', e.target.value)}
                            placeholder="Enter wallet address"
                            data-testid={`input-address-${index}`}
                          />
                        </div>

                        <div className="space-y-2">
                          <Label>Private Key *</Label>
                          <div className="relative">
                            <Input
                              type={showPrivateKeys[index] ? 'text' : 'password'}
                              value={account.privateKey || ''}
                              onChange={(e) => updateAccount(index, 'privateKey', e.target.value)}
                              placeholder="Enter private key"
                              className="pr-12"
                              data-testid={`input-privateKey-${index}`}
                            />
                            <div className="absolute inset-y-0 right-0 flex items-center pr-1">
                              <Button
                                type="button"
                                size="icon"
                                variant="ghost"
                                className="h-8 w-8"
                                onClick={() => setShowPrivateKeys({ ...showPrivateKeys, [index]: !showPrivateKeys[index] })}
                                data-testid={`button-toggle-privateKey-${index}`}
                              >
                                {showPrivateKeys[index] ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                              </Button>
                            </div>
                          </div>
                        </div>
                      </>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}

        {/* Read-only accounts display for update mode */}
        {mode === 'update' && initialData?.accounts && (
          <div className="space-y-2">
            <Label>Accounts (read-only)</Label>
            <div className="space-y-2">
              {initialData.accounts.map((account: any, index: number) => (
                <Card key={index} className="bg-muted">
                  <CardContent className="pt-4">
                    <div className="text-sm space-y-1">
                      <div><span className="font-semibold">Address:</span> <span className="font-mono">{account.address}</span></div>
                      <div><span className="font-semibold">Encrypted:</span> {account.encrypted ? 'Yes' : 'No'}</div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex justify-end gap-2 pt-4">
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            disabled={isSubmitting}
            data-testid="button-cancel"
          >
            Cancel
          </Button>
          <Button type="submit" disabled={isSubmitting} data-testid="button-submit">
            {isSubmitting && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            {mode === 'create' ? 'Create Wallet' : 'Update Wallet'}
          </Button>
        </div>
      </form>
    </Form>
  );
}
