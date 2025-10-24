import { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useQuery } from '@tanstack/react-query';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Check, ChevronsUpDown, Trash2, Plus, Package } from 'lucide-react';
import { cn } from '@/lib/utils';
import { apiClient } from '@/lib/api-client';
import { InventoryViewer } from './InventoryViewer';
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

  // Fetch users
  const { data: usersList, isLoading: usersLoading } = useQuery({
    queryKey: ['/api/rest/user'],
    queryFn: async () => {
      const response = await apiClient.request<any>('/api/rest/user');
      if (response && typeof response === 'object' && 'objects' in response) {
        return response.objects || [];
      }
      return Array.isArray(response) ? response : [];
    },
  });

  // Fetch applications
  const { data: appsList, isLoading: appsLoading } = useQuery({
    queryKey: ['/api/rest/application'],
    queryFn: async () => {
      const response = await apiClient.request<any>('/api/rest/application');
      if (response && typeof response === 'object' && 'objects' in response) {
        return response.objects || [];
      }
      return Array.isArray(response) ? response : [];
    },
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

  const selectedUser = usersList?.find((u: any) => u.id === form.watch('user'));
  const selectedApp = appsList?.find((a: any) => a.id === form.watch('application'));

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
                <Popover open={userSearchOpen} onOpenChange={setUserSearchOpen}>
                  <PopoverTrigger asChild>
                    <FormControl>
                      <Button
                        variant="outline"
                        role="combobox"
                        className={cn(
                          'justify-between',
                          !field.value && 'text-muted-foreground'
                        )}
                        data-testid="button-user-picker"
                      >
                        {field.value && selectedUser
                          ? `${selectedUser.username || selectedUser.name || selectedUser.id} (${selectedUser.level || 'USER'})`
                          : 'Select user...'}
                        <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                      </Button>
                    </FormControl>
                  </PopoverTrigger>
                  <PopoverContent className="w-[400px] p-0">
                    <Command>
                      <CommandInput placeholder="Search by username or ID..." data-testid="input-user-search" />
                      <CommandList>
                        <CommandEmpty>
                          {usersLoading ? 'Loading users...' : 'No users found.'}
                        </CommandEmpty>
                        <CommandGroup>
                          {usersList?.map((user: any) => {
                            const displayName = user.username || user.name || user.id;
                            return (
                              <CommandItem
                                key={user.id}
                                value={`${displayName} ${user.id || ''}`}
                                keywords={[user.username, user.name, user.id].filter(Boolean)}
                                onSelect={() => {
                                  form.setValue('user', user.id);
                                  setUserSearchOpen(false);
                                }}
                                data-testid={`option-user-${user.id}`}
                              >
                                <Check
                                  className={cn(
                                    'mr-2 h-4 w-4',
                                    user.id === field.value ? 'opacity-100' : 'opacity-0'
                                  )}
                                />
                                <div className="flex flex-col">
                                  <span className="font-medium">{displayName}</span>
                                  <span className="text-xs text-muted-foreground">
                                    {user.level || 'USER'} • {user.id}
                                  </span>
                                </div>
                              </CommandItem>
                            );
                          })}
                        </CommandGroup>
                      </CommandList>
                    </Command>
                  </PopoverContent>
                </Popover>
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
                <Popover open={appSearchOpen} onOpenChange={setAppSearchOpen}>
                  <PopoverTrigger asChild>
                    <FormControl>
                      <Button
                        variant="outline"
                        role="combobox"
                        className={cn(
                          'justify-between',
                          !field.value && 'text-muted-foreground'
                        )}
                        data-testid="button-application-picker"
                      >
                        {field.value && selectedApp
                          ? `${selectedApp.displayName || selectedApp.name}`
                          : 'Select application...'}
                        <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                      </Button>
                    </FormControl>
                  </PopoverTrigger>
                  <PopoverContent className="w-[400px] p-0">
                    <Command>
                      <CommandInput placeholder="Search by name or ID..." data-testid="input-application-search" />
                      <CommandList>
                        <CommandEmpty>
                          {appsLoading ? 'Loading applications...' : 'No applications found.'}
                        </CommandEmpty>
                        <CommandGroup>
                          {appsList?.map((app: any) => (
                            <CommandItem
                              key={app.id}
                              value={`${app.name} ${app.displayName || ''} ${app.id}`}
                              keywords={[app.name, app.displayName, app.id].filter(Boolean)}
                              onSelect={() => {
                                form.setValue('application', app.id);
                                setAppSearchOpen(false);
                              }}
                              data-testid={`option-application-${app.id}`}
                            >
                              <Check
                                className={cn(
                                  'mr-2 h-4 w-4',
                                  app.id === field.value ? 'opacity-100' : 'opacity-0'
                                )}
                              />
                              <div className="flex flex-col">
                                <span className="font-medium">{app.displayName || app.name}</span>
                                <span className="text-xs text-muted-foreground">
                                  {app.name} • {app.id}
                                </span>
                              </div>
                            </CommandItem>
                          ))}
                        </CommandGroup>
                      </CommandList>
                    </Command>
                  </PopoverContent>
                </Popover>
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
    </>
  );
}
