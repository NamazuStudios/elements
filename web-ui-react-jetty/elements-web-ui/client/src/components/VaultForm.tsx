import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Form,
  FormControl,
  FormDescription,
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
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Loader2, Eye, EyeOff, Search, X, Copy, Check } from 'lucide-react';
import { useState } from 'react';
import { UserSearchDialog } from '@/components/UserSearchDialog';
import { useToast } from '@/hooks/use-toast';

interface VaultFormProps {
  mode: 'create' | 'update';
  initialData?: any;
  onSubmit: (data: any) => Promise<void>;
  onCancel: () => void;
}

const algorithmOptions = [
  'RSA_256',
  'RSA_384',
  'RSA_512',
];

export function VaultForm({ mode, initialData, onSubmit, onCancel }: VaultFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPrivateKey, setShowPrivateKey] = useState(false);
  const [showPassphrase, setShowPassphrase] = useState(false);
  const [showConfirmPassphrase, setShowConfirmPassphrase] = useState(false);
  const [userSearchOpen, setUserSearchOpen] = useState(false);
  const [copiedPublicKey, setCopiedPublicKey] = useState(false);
  const { toast } = useToast();

  // Map API response to expected format
  // API returns: { key: {...}, user: {...} } but we expect: { vaultKey: {...}, userId: "..." }
  const vaultKey = initialData?.key || initialData?.vaultKey;
  const userId = initialData?.user?.id || initialData?.userId;
  const algorithm = vaultKey?.algorithm || initialData?.algorithm;


  // Create validation schema
  const createSchema = z.object({
    displayName: z.string().optional(),
    userId: z.string().optional(),
    algorithm: z.enum(['RSA_256', 'RSA_384', 'RSA_512'] as const, {
      required_error: 'Algorithm is required',
    }),
    passphrase: z.string().optional(),
    confirmPassphrase: z.string().optional(),
  }).refine((data) => {
    // If passphrase is provided, confirmation must match
    if (data.passphrase && data.passphrase.length > 0) {
      return data.passphrase === data.confirmPassphrase;
    }
    return true;
  }, {
    message: 'Passphrases must match',
    path: ['confirmPassphrase'],
  });

  const updateSchema = z.object({
    displayName: z.string().optional(),
    passphrase: z.string().optional(),
    confirmPassphrase: z.string().optional(),
  }).refine((data) => {
    if (data.passphrase && data.passphrase.length > 0) {
      return data.passphrase === data.confirmPassphrase;
    }
    return true;
  }, {
    message: 'Passphrases must match',
    path: ['confirmPassphrase'],
  });

  const form = useForm({
    resolver: zodResolver(mode === 'create' ? createSchema : updateSchema),
    defaultValues: mode === 'create' 
      ? {
          displayName: '',
          userId: '',
          algorithm: 'RSA_256' as const,
          passphrase: '',
          confirmPassphrase: '',
        }
      : {
          displayName: initialData?.displayName || '',
          passphrase: '',
          confirmPassphrase: '',
        },
  });

  const handleSubmit = async (data: any) => {
    setIsSubmitting(true);
    try {
      // Remove confirmPassphrase and encrypted fields before submitting
      const { confirmPassphrase, encrypted, ...submitData } = data;
      
      // Remove passphrase if it's empty
      if (!submitData.passphrase || submitData.passphrase.length === 0) {
        delete submitData.passphrase;
      }
      
      // For update mode, only send editable fields (displayName and passphrase)
      // Remove read-only fields that shouldn't be in the update request
      if (mode === 'update') {
        const { userId, algorithm, id, vaultKey, ...updateData } = submitData;
        await onSubmit(updateData);
      } else {
        await onSubmit(submitData);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCopyPublicKey = async () => {
    const publicKey = vaultKey?.publicKey;
    if (publicKey) {
      try {
        await navigator.clipboard.writeText(publicKey);
        setCopiedPublicKey(true);
        toast({ title: 'Copied', description: 'Public key copied to clipboard' });
        setTimeout(() => setCopiedPublicKey(false), 2000);
      } catch (error) {
        toast({ title: 'Error', description: 'Failed to copy to clipboard', variant: 'destructive' });
      }
    }
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        {/* Display Name */}
        <FormField
          control={form.control}
          name="displayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Display Name</FormLabel>
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

        {/* User ID (create only) */}
        {mode === 'create' && (
          <FormField
            control={form.control}
            name="userId"
            render={({ field }) => (
              <FormItem>
                <FormLabel>User ID</FormLabel>
                <div className="flex gap-2">
                  <FormControl>
                    <Input 
                      {...field} 
                      placeholder="Select a user"
                      readOnly
                      data-testid="input-userId"
                    />
                  </FormControl>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setUserSearchOpen(true)}
                    data-testid="button-search-user"
                  >
                    <Search className="w-4 h-4" />
                  </Button>
                  {field.value && (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => field.onChange('')}
                      data-testid="button-clear-user"
                    >
                      <X className="w-4 h-4" />
                    </Button>
                  )}
                </div>
                <FormDescription>The User to associate with this vault</FormDescription>
                <FormMessage />
              </FormItem>
            )}
          />
        )}

        {/* Algorithm (create only) */}
        {mode === 'create' && (
          <FormField
            control={form.control}
            name="algorithm"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Algorithm *</FormLabel>
                <Select onValueChange={field.onChange} defaultValue={field.value}>
                  <FormControl>
                    <SelectTrigger data-testid="select-algorithm">
                      <SelectValue placeholder="Select algorithm" />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {algorithmOptions.map((algo) => (
                      <SelectItem key={algo} value={algo}>
                        {algo}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <FormMessage />
              </FormItem>
            )}
          />
        )}


        {/* Show read-only fields when editing */}
        {mode === 'update' && initialData && (
          <>
            {/* User ID (read-only) */}
            {userId && (
              <div className="space-y-2">
                <Label>User ID</Label>
                <Input 
                  value={userId} 
                  readOnly 
                  className="bg-muted"
                  data-testid="input-userId-readonly"
                />
              </div>
            )}

            {/* Algorithm (read-only) */}
            {algorithm && (
              <div className="space-y-2">
                <Label>Algorithm</Label>
                <Input 
                  value={algorithm} 
                  readOnly 
                  className="bg-muted"
                  data-testid="input-algorithm-readonly"
                />
              </div>
            )}

            {/* Vault Key Info */}
            {vaultKey && (
              <>
                {/* Encrypted Status */}
                <div className="space-y-2">
                  <Label>Encrypted</Label>
                  <div className="flex items-center space-x-2">
                    <Checkbox
                      checked={vaultKey.encrypted ?? false}
                      disabled
                      data-testid="checkbox-encrypted-readonly"
                    />
                    <span className="text-sm text-muted-foreground">
                      {vaultKey.encrypted ? 'Yes' : 'No'}
                    </span>
                  </div>
                </div>

                {/* Public Key (read-only with copy button) */}
                {vaultKey.publicKey && (
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <Label>Public Key</Label>
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        onClick={handleCopyPublicKey}
                        data-testid="button-copy-publicKey"
                      >
                        {copiedPublicKey ? (
                          <>
                            <Check className="w-4 h-4 mr-2" />
                            Copied
                          </>
                        ) : (
                          <>
                            <Copy className="w-4 h-4 mr-2" />
                            Copy
                          </>
                        )}
                      </Button>
                    </div>
                    <textarea 
                      value={vaultKey.publicKey}
                      readOnly
                      className="w-full min-h-[100px] p-3 rounded-md border bg-muted font-mono text-xs resize-y"
                      data-testid="textarea-publicKey"
                    />
                  </div>
                )}
              </>
            )}

            {/* Private Key (read-only, hidden by default) */}
            {vaultKey?.privateKey && (
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label>Private Key</Label>
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setShowPrivateKey(!showPrivateKey)}
                    data-testid="button-toggle-privateKey"
                  >
                    {showPrivateKey ? (
                      <>
                        <EyeOff className="w-4 h-4 mr-2" />
                        Hide
                      </>
                    ) : (
                      <>
                        <Eye className="w-4 h-4 mr-2" />
                        Show
                      </>
                    )}
                  </Button>
                </div>
                <textarea 
                  value={showPrivateKey ? vaultKey.privateKey : '••••••••••••••••••••'}
                  readOnly
                  className="w-full min-h-[100px] p-3 rounded-md border bg-muted font-mono text-xs resize-y"
                  data-testid="textarea-privateKey"
                />
              </div>
            )}
          </>
        )}

        {/* Passphrase section (at the bottom) */}
        {/* Passphrase is always optional in both create and update modes */}
        <>
            <FormField
              control={form.control}
              name="passphrase"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>
                    {mode === 'create' ? 'Passphrase (Optional)' : 'New Passphrase'}
                  </FormLabel>
                  <div className="relative">
                    <FormControl>
                      <Input 
                        {...field}
                        type={showPassphrase ? 'text' : 'password'}
                        placeholder="Enter passphrase"
                        className="pr-12"
                        data-testid="input-passphrase"
                      />
                    </FormControl>
                    <div className="absolute inset-y-0 right-0 flex items-center pr-1">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => setShowPassphrase(!showPassphrase)}
                        data-testid="button-toggle-passphrase"
                      >
                        {showPassphrase ? (
                          <EyeOff className="w-4 h-4" />
                        ) : (
                          <Eye className="w-4 h-4" />
                        )}
                      </Button>
                    </div>
                  </div>
                  <FormDescription>
                    {mode === 'create' 
                      ? 'Leave empty for an unencrypted vault, or provide a passphrase to encrypt'
                      : 'Leave empty to keep current passphrase (if any)'
                    }
                  </FormDescription>
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="confirmPassphrase"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>
                    {mode === 'create' ? 'Confirm Passphrase' : 'Confirm New Passphrase'}
                  </FormLabel>
                  <div className="relative">
                    <FormControl>
                      <Input 
                        {...field}
                        type={showConfirmPassphrase ? 'text' : 'password'}
                        placeholder="Confirm passphrase"
                        className="pr-12"
                        data-testid="input-confirmPassphrase"
                      />
                    </FormControl>
                    <div className="absolute inset-y-0 right-0 flex items-center pr-1">
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={() => setShowConfirmPassphrase(!showConfirmPassphrase)}
                        data-testid="button-toggle-confirmPassphrase"
                      >
                        {showConfirmPassphrase ? (
                          <EyeOff className="w-4 h-4" />
                        ) : (
                          <Eye className="w-4 h-4" />
                        )}
                      </Button>
                    </div>
                  </div>
                  <FormMessage />
                </FormItem>
              )}
            />
          </>

        {/* Form actions */}
        <div className="flex gap-2 justify-end pt-4">
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            disabled={isSubmitting}
            data-testid="button-cancel"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            disabled={isSubmitting}
            data-testid="button-submit"
          >
            {isSubmitting && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            {mode === 'create' ? 'Create Vault' : 'Update Vault'}
          </Button>
        </div>

        {/* User search dialog */}
        <UserSearchDialog
          open={userSearchOpen}
          onOpenChange={setUserSearchOpen}
          onSelect={(userId) => {
            form.setValue('userId', userId);
          }}
          currentUserId={form.watch('userId')}
        />
      </form>
    </Form>
  );
}
