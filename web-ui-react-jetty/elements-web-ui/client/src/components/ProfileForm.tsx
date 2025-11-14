import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Trash2, Plus, Package } from 'lucide-react';
import { InventoryViewer } from './InventoryViewer';
import { UserSearchDialog } from './UserSearchDialog';
import { ApplicationSearchDialog } from './ApplicationSearchDialog';
import { useFormDraft } from '@/hooks/use-form-draft';
import { useToast } from '@/hooks/use-toast';

const profileSchema = z.object({
  id: z.string().optional(),
  user: z.string().min(1, 'User is required'),
  application: z.string().min(1, 'Application is required'),
  displayName: z.string().optional(),
  imageUrl: z.string().url('Must be a valid URL').optional().or(z.literal('')),
  metadata: z.record(z.string(), z.any()).optional(),
});

type ProfileFormData = z.infer<typeof profileSchema>;

interface ProfileFormProps {
  mode: 'create' | 'update';
  initialData?: any;
  onSubmit: (data: any) => void;
  onCancel: () => void;
  isPending?: boolean;
  onFormChange?: (data: Record<string, any>) => void;
}

export function ProfileForm({ mode, initialData, onSubmit, onCancel, isPending, onFormChange }: ProfileFormProps) {
  const { toast } = useToast();
  const [userSearchOpen, setUserSearchOpen] = useState(false);
  const [appSearchOpen, setAppSearchOpen] = useState(false);
  const [inventoryOpen, setInventoryOpen] = useState(false);
  const [selectedUserName, setSelectedUserName] = useState<string>('');
  const [selectedAppName, setSelectedAppName] = useState<string>('');
  const [metadataEntries, setMetadataEntries] = useState<Array<{ key: string; value: string }>>(
    initialData?.metadata ? Object.entries(initialData.metadata).map(([key, value]) => ({ 
      key, 
      value: typeof value === 'string' ? value : JSON.stringify(value) 
    })) : []
  );

  // Draft management
  const { saveDraft } = useFormDraft({
    resourceName: 'Profiles',
    mode,
    itemId: initialData?.id,
  });

  const form = useForm<ProfileFormData>({
    resolver: zodResolver(profileSchema),
    defaultValues: {
      id: initialData?.id,
      user: initialData?.user?.id || initialData?.user || '',
      application: initialData?.application?.id || initialData?.application || '',
      displayName: initialData?.displayName || '',
      imageUrl: initialData?.imageUrl || '',
      metadata: initialData?.metadata || {},
    },
  });

  const handleSubmit = (data: ProfileFormData) => {
    // Convert metadata entries to object
    const metadata = metadataEntries.reduce((acc, entry) => {
      if (entry.key && entry.value) {
        try {
          acc[entry.key] = JSON.parse(entry.value);
        } catch {
          acc[entry.key] = entry.value;
        }
      }
      return acc;
    }, {} as Record<string, any>);

    const payload = {
      ...(mode === 'update' && data.id ? { id: data.id } : {}),
      user: { id: data.user },
      application: { id: data.application },
      displayName: data.displayName || undefined,
      imageUrl: data.imageUrl || undefined,
      metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
    };
    onSubmit(payload);
  };

  const addMetadataEntry = () => {
    setMetadataEntries([...metadataEntries, { key: '', value: '' }]);
  };

  const removeMetadataEntry = (index: number) => {
    setMetadataEntries(metadataEntries.filter((_, i) => i !== index));
  };

  const updateMetadataEntry = (index: number, field: 'key' | 'value', value: string) => {
    const updated = [...metadataEntries];
    updated[index][field] = value;
    setMetadataEntries(updated);
  };

  // Initialize selected names from initialData
  useEffect(() => {
    if (initialData?.user) {
      const userName = typeof initialData.user === 'object' 
        ? (initialData.user.username || initialData.user.name || initialData.user.id)
        : initialData.user;
      setSelectedUserName(userName);
    }
    if (initialData?.application) {
      const appName = typeof initialData.application === 'object'
        ? (initialData.application.displayName || initialData.application.name || initialData.application.id)
        : initialData.application;
      setSelectedAppName(appName);
    }
  }, [initialData]);

  // Sync form data with parent component for JSON editor
  useEffect(() => {
    if (!onFormChange) return;
    
    const subscription = form.watch((values) => {
      // Convert metadata entries to object
      const metadata = metadataEntries.reduce((acc, entry) => {
        if (entry.key && entry.value) {
          try {
            acc[entry.key] = JSON.parse(entry.value);
          } catch {
            acc[entry.key] = entry.value;
          }
        }
        return acc;
      }, {} as Record<string, any>);

      const formData = {
        ...(mode === 'update' && values.id ? { id: values.id } : {}),
        user: values.user ? { id: values.user } : undefined,
        application: values.application ? { id: values.application } : undefined,
        displayName: values.displayName || undefined,
        imageUrl: values.imageUrl || undefined,
        metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
      };

      onFormChange(formData);
    });

    return () => subscription.unsubscribe();
  }, [form, onFormChange, mode, metadataEntries]);

  return (
    <>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
          {/* User Picker */}
          <FormField
            control={form.control}
            name="user"
            render={({ field }) => (
              <FormItem className="flex flex-col">
                <FormLabel>User *</FormLabel>
                <FormControl>
                  <Button
                    variant="outline"
                    onClick={() => setUserSearchOpen(true)}
                    className="justify-start"
                    data-testid="button-user-picker"
                  >
                    {selectedUserName || 'Select user...'}
                  </Button>
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* Application Picker */}
          <FormField
            control={form.control}
            name="application"
            render={({ field }) => (
              <FormItem className="flex flex-col">
                <FormLabel>Application *</FormLabel>
                <FormControl>
                  <Button
                    variant="outline"
                    onClick={() => setAppSearchOpen(true)}
                    className="justify-start"
                    data-testid="button-application-picker"
                  >
                    {selectedAppName || 'Select application...'}
                  </Button>
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* Display Name */}
          <FormField
            control={form.control}
            name="displayName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Display Name</FormLabel>
                <FormControl>
                  <Input {...field} placeholder="Enter display name" data-testid="input-display-name" />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* Image URL */}
          <FormField
            control={form.control}
            name="imageUrl"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Image URL</FormLabel>
                <FormControl>
                  <Input {...field} placeholder="https://example.com/image.png" data-testid="input-image-url" />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          {/* Metadata Key-Value Editor */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <FormLabel>Metadata</FormLabel>
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
                    <Trash2 className="w-4 w-4" />
                  </Button>
                </div>
              ))}
              {metadataEntries.length === 0 && (
                <p className="text-sm text-muted-foreground">No metadata entries. Click "Add Entry" to add key-value pairs.</p>
              )}
            </div>
          </div>

          {/* Inventory Management - Fallback button for update mode */}
          {mode === 'update' && initialData?.id && initialData?.user && (
            <div className="pt-4 border-t">
              <p className="text-sm text-muted-foreground mb-2">
                Note: Inventory can also be accessed from the Actions column in the Profiles list
              </p>
              <Button
                type="button"
                variant="outline"
                onClick={() => setInventoryOpen(true)}
                data-testid="button-view-inventory"
              >
                <Package className="w-4 h-4 mr-2" />
                View Inventory (Fallback)
              </Button>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex gap-3 justify-end pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={onCancel}
              disabled={isPending}
              data-testid="button-cancel"
            >
              Cancel
            </Button>
            {mode === 'create' && (
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  const currentValues = form.getValues();
                  saveDraft(currentValues);
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
            <Button
              type="submit"
              disabled={isPending}
              data-testid="button-submit"
            >
              {isPending ? 'Saving...' : mode === 'create' ? 'Create Profile' : 'Update Profile'}
            </Button>
          </div>
        </form>
      </Form>

      {/* Inventory Viewer Dialog - Fallback access */}
      {mode === 'update' && initialData?.id && initialData?.user && (
        <Dialog open={inventoryOpen} onOpenChange={(open) => {
          setInventoryOpen(open);
        }}>
          <DialogContent className="max-w-6xl max-h-[90vh]">
            <DialogHeader>
              <DialogTitle>
                Inventory for {initialData.displayName || (typeof initialData.user === 'object' ? initialData.user.name : initialData.user) || 'Profile'}
              </DialogTitle>
            </DialogHeader>
            <InventoryViewer 
              userId={typeof initialData.user === 'object' ? initialData.user.id : initialData.user} 
              username={typeof initialData.user === 'object' ? initialData.user.name : initialData.user}
            />
          </DialogContent>
        </Dialog>
      )}

      {/* User Search Dialog */}
      <UserSearchDialog
        open={userSearchOpen}
        onOpenChange={setUserSearchOpen}
        onSelect={(userId, user) => {
          form.setValue('user', userId);
          setSelectedUserName(user.username || user.name || userId);
        }}
      />

      {/* Application Search Dialog */}
      <ApplicationSearchDialog
        open={appSearchOpen}
        onOpenChange={setAppSearchOpen}
        onSelect={(appId, app) => {
          form.setValue('application', appId);
          setSelectedAppName(app.displayName || app.name || appId);
        }}
      />
    </>
  );
}
