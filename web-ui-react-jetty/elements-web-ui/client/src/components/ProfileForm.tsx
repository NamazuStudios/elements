import { useState, useEffect, useRef } from 'react';
import { useForm } from 'react-hook-form';
import { useMutation } from '@tanstack/react-query';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Trash2, Plus, Package, HardDrive, Upload, Loader2, ImageIcon } from 'lucide-react';
import { InventoryViewer } from './InventoryViewer';
import { UserSearchDialog } from './UserSearchDialog';
import { ApplicationSearchDialog } from './ApplicationSearchDialog';
import { apiClient, getApiPath } from '@/lib/api-client';
import { useFormDraft } from '@/hooks/use-form-draft';
import { useToast } from '@/hooks/use-toast';

const profileSchema = z.object({
  id: z.string().optional(),
  user: z.string().min(1, 'User is required'),
  application: z.string().min(1, 'Application is required'),
  displayName: z.string().optional(),
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
  const [selectedImageObject, setSelectedImageObject] = useState<any>(initialData?.imageObject || null);
  const [imagePreviewUrl, setImagePreviewUrl] = useState<string | null>(null);
  const [imagePreviewLoading, setImagePreviewLoading] = useState(false);
  const [imagePreviewError, setImagePreviewError] = useState(false);
  const [previewVersion, setPreviewVersion] = useState(0);
  const fileInputRef = useRef<HTMLInputElement>(null);
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
      metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
    };
    onSubmit(payload);
  };

  const uploadImageMutation = useMutation({
    mutationFn: async (file: File) => {
      if (!selectedImageObject?.id) {
        throw new Error('No image object to upload to');
      }
      const formData = new FormData();
      formData.append('file', file);
      const uploadPath = `/api/rest/large_object/${selectedImageObject.id}/content`;
      const fullPath = await getApiPath(uploadPath);
      const headers: HeadersInit = {};
      const sessionToken = apiClient.getSessionToken();
      if (sessionToken) {
        headers['Elements-SessionSecret'] = sessionToken;
      }
      const response = await fetch(fullPath, {
        method: 'PUT',
        body: formData,
        credentials: 'include',
        headers,
      });
      if (!response.ok) {
        throw new Error(`Upload failed: ${response.statusText}`);
      }
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        try {
          return await response.json();
        } catch {
          return null;
        }
      }
      return null;
    },
    onSuccess: (updatedObject) => {
      if (updatedObject) {
        setSelectedImageObject(updatedObject);
      }
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      setPreviewVersion((v) => v + 1);
      toast({
        title: 'Image Updated',
        description: 'The image content has been uploaded successfully.',
      });
    },
    onError: (error: any) => {
      toast({
        title: 'Upload Failed',
        description: error.message || 'Failed to upload image content',
        variant: 'destructive',
      });
    },
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      uploadImageMutation.mutate(file);
    }
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

  // Initialize selected names and image object from initialData
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
    setSelectedImageObject(initialData?.imageObject || null);
    setPreviewVersion(0);
  }, [initialData]);

  useEffect(() => {
    if (!selectedImageObject?.id) {
      setImagePreviewUrl(null);
      setImagePreviewError(false);
      return;
    }
    let cancelled = false;
    const loadPreview = async () => {
      setImagePreviewLoading(true);
      setImagePreviewError(false);
      try {
        const cdnPath = `/cdn/object/${selectedImageObject.id}`;
        const fullPath = await getApiPath(cdnPath);
        const headers: Record<string, string> = {};
        const sessionToken = apiClient.getSessionToken();
        if (sessionToken) {
          headers['Elements-SessionSecret'] = sessionToken;
        }
        const response = await fetch(fullPath, { headers, credentials: 'include' });
        if (!response.ok) throw new Error('Failed to load');
        const blob = await response.blob();
        if (!cancelled) {
          const url = URL.createObjectURL(blob);
          setImagePreviewUrl((prev) => {
            if (prev) URL.revokeObjectURL(prev);
            return url;
          });
        }
      } catch {
        if (!cancelled) {
          setImagePreviewError(true);
          setImagePreviewUrl(null);
        }
      } finally {
        if (!cancelled) setImagePreviewLoading(false);
      }
    };
    loadPreview();
    return () => {
      cancelled = true;
    };
  }, [selectedImageObject?.id, previewVersion]);

  useEffect(() => {
    return () => {
      if (imagePreviewUrl) URL.revokeObjectURL(imagePreviewUrl);
    };
  }, [imagePreviewUrl]);

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

          {/* Image Object (LargeObjectReference) - content upload */}
          <div className="space-y-2">
            <FormLabel>Image Object</FormLabel>
            {selectedImageObject ? (
              <div className="space-y-3">
                <div className="flex items-start gap-3 p-3 border rounded-md bg-muted/30">
                  <div className="w-16 h-16 rounded-md border bg-muted flex items-center justify-center shrink-0 overflow-hidden">
                    {imagePreviewLoading ? (
                      <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
                    ) : imagePreviewUrl ? (
                      <img
                        src={imagePreviewUrl}
                        alt="Profile image preview"
                        className="w-full h-full object-cover"
                        data-testid="img-image-preview"
                      />
                    ) : (
                      <ImageIcon className="w-6 h-6 text-muted-foreground" />
                    )}
                  </div>
                  <div className="flex-1 min-w-0 space-y-1 text-sm">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="font-medium truncate" data-testid="text-image-object-name">{selectedImageObject.path || selectedImageObject.originalFilename || selectedImageObject.id}</span>
                      {selectedImageObject.state && (
                        <Badge variant="secondary" className="text-xs" data-testid="badge-image-state">
                          {selectedImageObject.state}
                        </Badge>
                      )}
                    </div>
                    {selectedImageObject.mimeType && (
                      <div className="text-muted-foreground" data-testid="text-image-mime-type">
                        MIME: {selectedImageObject.mimeType}
                      </div>
                    )}
                    <div className="text-xs text-muted-foreground truncate" data-testid="text-image-object-id">
                      ID: {selectedImageObject.id}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleFileChange}
                    className="hidden"
                    data-testid="input-image-upload"
                  />
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploadImageMutation.isPending}
                    data-testid="button-upload-image"
                  >
                    {uploadImageMutation.isPending ? (
                      <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                    ) : (
                      <Upload className="w-4 h-4 mr-2" />
                    )}
                    {uploadImageMutation.isPending ? 'Uploading...' : 'Upload New Image'}
                  </Button>
                  <span className="text-xs text-muted-foreground">
                    Replaces the content of this large object
                  </span>
                </div>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground" data-testid="text-no-image-object">
                No image object assigned to this profile.
              </p>
            )}
          </div>

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
