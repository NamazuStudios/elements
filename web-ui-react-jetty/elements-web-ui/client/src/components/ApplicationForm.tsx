import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage, FormDescription } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Label } from '@/components/ui/label';
import { ExternalLink, Plus, Pencil, Trash2, Loader2 } from 'lucide-react';
import { ApplicationConfigurationDialog } from './ApplicationConfigurationDialog';
import { useState, useMemo } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { queryClient } from '@/lib/queryClient';
import { apiClient } from '@/lib/api-client';
import { useToast } from '@/hooks/use-toast';

// Common configuration fields (shared by all configuration types)
const commonConfigFields = {
  name: z.string().optional(),
  description: z.string().optional(),
  id: z.string().optional(),
};

// Configuration type schemas
const facebookConfigSchema = z.object({
  ...commonConfigFields,
  '@class': z.literal('dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration'),
  applicationId: z.string().min(1, 'Application ID is required'),
  applicationSecret: z.string().min(1, 'Application Secret is required'),
  builtinApplicationPermissions: z.array(z.string()).optional(),
  productBundles: z.array(z.any()).optional(),
});

const firebaseConfigSchema = z.object({
  ...commonConfigFields,
  '@class': z.literal('dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration'),
  projectId: z.string().min(1, 'Project ID is required'),
  serviceAccountCredentials: z.string().min(1, 'Service Account Credentials are required'),
});

const googlePlayConfigSchema = z.object({
  ...commonConfigFields,
  '@class': z.literal('dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration'),
  applicationId: z.string().optional(),
  jsonKey: z.any().optional(),
  productBundles: z.array(z.any()).optional(),
});

const iosConfigSchema = z.object({
  ...commonConfigFields,
  '@class': z.literal('dev.getelements.elements.sdk.model.application.IosApplicationConfiguration'),
  applicationId: z.string().min(1, 'Application ID is required'),
  productBundles: z.array(z.any()).optional(),
});

const matchmakingConfigSchema = z.object({
  ...commonConfigFields,
  '@class': z.literal('dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration'),
  success: z.any().optional(),
  matchmaker: z.any().optional(),
  maxProfiles: z.number({
    required_error: 'Max Profiles is required',
    invalid_type_error: 'Max Profiles must be a number',
  }).min(2, 'Max Profiles must be 2 or greater'),
  metadata: z.any().optional(),
});

const oculusConfigSchema = z.object({
  ...commonConfigFields,
  '@class': z.literal('dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration'),
  applicationId: z.string().min(1, 'Application ID is required'),
  applicationSecret: z.string().min(1, 'Application Secret is required'),
  builtinApplicationPermissions: z.array(z.string()).optional(),
  productBundles: z.array(z.any()).optional(),
});

const applicationConfigSchema = z.union([
  facebookConfigSchema,
  firebaseConfigSchema,
  googlePlayConfigSchema,
  iosConfigSchema,
  matchmakingConfigSchema,
  oculusConfigSchema,
]);

// Create schema based on mode - don't validate applicationConfiguration in update mode
const createApplicationSchema = (mode: 'create' | 'update') => z.object({
  id: z.string().optional(),
  name: z.string().min(1, 'Name is required'),
  description: z.string().optional(),
  gitBranch: z.string().optional(),
  scriptRepoUrl: z.string().optional(),
  attributes: z.record(z.any()).optional(),
  // Only validate applicationConfiguration in create mode
  applicationConfiguration: mode === 'create' 
    ? z.array(applicationConfigSchema).optional()
    : z.any().optional(),
  httpDocumentationUrl: z.string().optional(),
  httpDocumentationUiUrl: z.string().optional(),
  httpTunnelEndpointUrl: z.string().optional(),
});

type ApplicationFormData = z.infer<ReturnType<typeof createApplicationSchema>>;

interface ApplicationFormProps {
  initialData?: Partial<ApplicationFormData>;
  onSubmit: (data: ApplicationFormData) => void;
  mode: 'create' | 'update';
}

export function ApplicationForm({ initialData = {}, onSubmit, mode }: ApplicationFormProps) {
  const [configDialogOpen, setConfigDialogOpen] = useState(false);
  const [editingConfig, setEditingConfig] = useState<{ index: number | null; type: any; value: any } | null>(null);
  const { toast } = useToast();

  // Memoize app ID for stable query keys
  const appId = useMemo(() => initialData.id ?? initialData.name, [initialData.id, initialData.name]);

  // Fetch configurations separately when in update mode  
  const { data: configurationsData, isLoading: configurationsLoading } = useQuery<any>({
    queryKey: ['/api/rest/application', appId, 'configuration'],
    queryFn: () => apiClient.request<any>(`/api/rest/application/${appId}/configuration`),
    enabled: mode === 'update' && !!appId,
  });

  // Handle both array and paginated response formats
  const configurations = Array.isArray(configurationsData) 
    ? configurationsData 
    : (configurationsData?.objects || []);

  // Mutation to create a new configuration
  const createConfigMutation = useMutation({
    mutationFn: async ({ type, config }: { type: string, config: any }) => {
      if (!appId) throw new Error('Application ID is required');
      const typeEndpoint = getConfigTypeEndpoint(type);
      
      // Add default values for type-specific fields
      const configWithDefaults = { ...config };
      if (type === 'GooglePlay') {
        // Ensure jsonKey is an object, not a string (if it's a string, parsing failed)
        if (typeof configWithDefaults.jsonKey === 'string') {
          configWithDefaults.jsonKey = {};
        } else {
          configWithDefaults.jsonKey = configWithDefaults.jsonKey || {};
        }
        configWithDefaults.productBundles = configWithDefaults.productBundles || [];
      } else if (type === 'iOS') {
        configWithDefaults.productBundles = configWithDefaults.productBundles || [];
      }
      
      // Add type and parent reference to the configuration (don't include id for create)
      const configWithMetadata = {
        ...configWithDefaults,
        type: getConfigurationClass(type),
        parent: { id: appId }
      };
      console.log('Creating configuration with payload:', JSON.stringify(configWithMetadata, null, 2));
      return apiClient.request(`/api/rest/application/${appId}/configuration/${typeEndpoint}`, {
        method: 'POST',
        body: JSON.stringify(configWithMetadata),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/application', appId, 'configuration'] });
    },
  });

  // Mutation to update an existing configuration
  const updateConfigMutation = useMutation({
    mutationFn: async ({ type, configId, config }: { type: string, configId: string, config: any }) => {
      if (!appId) throw new Error('Application ID is required');
      const typeEndpoint = getConfigTypeEndpoint(type);
      
      // Add default values for type-specific fields
      const configWithDefaults = { ...config };
      if (type === 'GooglePlay') {
        // Ensure jsonKey is an object, not a string (if it's a string, parsing failed)
        if (typeof configWithDefaults.jsonKey === 'string') {
          configWithDefaults.jsonKey = {};
        } else {
          configWithDefaults.jsonKey = configWithDefaults.jsonKey || {};
        }
        configWithDefaults.productBundles = configWithDefaults.productBundles || [];
      } else if (type === 'iOS') {
        configWithDefaults.productBundles = configWithDefaults.productBundles || [];
      }
      
      // Ensure type and parent reference are included
      const configWithMetadata = {
        ...configWithDefaults,
        type: getConfigurationClass(type),
        parent: config.parent || { id: appId }
      };
      console.log('Updating configuration with payload:', JSON.stringify(configWithMetadata, null, 2));
      return apiClient.request(`/api/rest/application/${appId}/configuration/${typeEndpoint}/${configId}`, {
        method: 'PUT',
        body: JSON.stringify(configWithMetadata),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/application', appId, 'configuration'] });
    },
  });

  // Mutation to delete a configuration
  const deleteConfigMutation = useMutation({
    mutationFn: async ({ type, configId }: { type: string, configId: string }) => {
      if (!appId) throw new Error('Application ID is required');
      const typeEndpoint = getConfigTypeEndpoint(type);
      return apiClient.request(`/api/rest/application/${appId}/configuration/${typeEndpoint}/${configId}`, {
        method: 'DELETE',
      });
    },
    onSuccess: () => {
      toast({
        title: 'Success',
        description: 'Configuration deleted successfully',
      });
      queryClient.invalidateQueries({ queryKey: ['/api/rest/application', appId, 'configuration'] });
    },
    onError: (error) => {
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to delete configuration',
        variant: 'destructive',
      });
    },
  });

  const form = useForm<ApplicationFormData>({
    resolver: zodResolver(createApplicationSchema(mode)),
    defaultValues: {
      // Always include id in update mode, even if empty
      ...(mode === 'update' ? { id: initialData.id || '' } : {}),
      name: initialData.name || '',
      description: initialData.description || '',
      attributes: initialData.attributes || {},
      // Only include system fields if they exist in initialData (update mode)
      ...(initialData.gitBranch && { gitBranch: initialData.gitBranch }),
      ...(initialData.scriptRepoUrl && { scriptRepoUrl: initialData.scriptRepoUrl }),
      ...(initialData.httpDocumentationUrl && { httpDocumentationUrl: initialData.httpDocumentationUrl }),
      ...(initialData.httpDocumentationUiUrl && { httpDocumentationUiUrl: initialData.httpDocumentationUiUrl }),
      ...(initialData.httpTunnelEndpointUrl && { httpTunnelEndpointUrl: initialData.httpTunnelEndpointUrl }),
    },
  });

  const handleFormSubmit = (data: ApplicationFormData) => {
    console.log('ApplicationForm handleFormSubmit - raw data:', data);
    console.log('ApplicationForm mode:', mode);
    
    // System-generated read-only fields that should never be sent
    const systemFields = ['gitBranch', 'scriptRepoUrl', 'httpDocumentationUrl', 'httpDocumentationUiUrl', 'httpTunnelEndpointUrl'];
    
    const cleanedData = Object.fromEntries(
      Object.entries(data).filter(([key, value]) => {
        // In update mode, exclude id (it's in the URL), system fields, and applicationConfiguration (managed separately)
        if (mode === 'update') {
          if (key === 'id' || key === 'applicationConfiguration' || systemFields.includes(key)) return false;
        }
        // In create mode, exclude system fields and applicationConfiguration (managed separately)
        if (mode === 'create' && (systemFields.includes(key) || key === 'applicationConfiguration')) return false;
        
        return value !== undefined && value !== null && value !== '';
      })
    ) as ApplicationFormData;
    
    // Preserve _configurations from initialData if it exists (for duplication)
    const dataWithConfigs = (initialData as any)?._configurations 
      ? { ...cleanedData, _configurations: (initialData as any)._configurations }
      : cleanedData;
    
    console.log('ApplicationForm handleFormSubmit - cleaned data:', dataWithConfigs);
    onSubmit(dataWithConfigs);
  };

  const watchedValues = form.watch();

  return (
    <div className="w-full">
      <Form {...form}>
        <form onSubmit={form.handleSubmit(handleFormSubmit)} className="flex flex-col h-full max-h-[600px]">
          <div className="flex-1 overflow-y-auto space-y-6 pr-2">
              <Card>
              <CardContent className="pt-6 space-y-4">
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>
                        Name {!field.value && <span className="text-destructive">*</span>}
                      </FormLabel>
                      <FormControl>
                        <Input {...field} placeholder="my-application" data-testid="input-name" />
                      </FormControl>
                      <FormDescription>Unique alpha-numeric name for the application</FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                <FormField
                  control={form.control}
                  name="description"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Description</FormLabel>
                      <FormControl>
                        <Textarea {...field} placeholder="Application description" data-testid="textarea-description" />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </CardContent>
            </Card>

            {mode === 'update' && (watchedValues.gitBranch || watchedValues.scriptRepoUrl || watchedValues.httpDocumentationUrl || watchedValues.httpDocumentationUiUrl || watchedValues.httpTunnelEndpointUrl) && (
              <Card>
                <CardContent className="pt-6 space-y-4">
                  <div>
                    <h3 className="text-sm font-semibold mb-3">System Information (Read-only)</h3>
                    <p className="text-sm text-muted-foreground mb-4">These values are automatically assigned by the system</p>
                  </div>

                  {watchedValues.gitBranch && (
                    <div className="space-y-2">
                      <Label>Git Branch</Label>
                      <Input
                        value={watchedValues.gitBranch}
                        readOnly
                        className="bg-muted"
                        data-testid="input-gitBranch-readonly"
                      />
                    </div>
                  )}

                  {watchedValues.scriptRepoUrl && (
                    <div className="space-y-2">
                      <Label>Script Repository URL</Label>
                      <Input
                        value={watchedValues.scriptRepoUrl}
                        readOnly
                        className="bg-muted"
                        data-testid="input-scriptRepoUrl-readonly"
                      />
                    </div>
                  )}

                  {watchedValues.httpDocumentationUrl && (
                    <div className="space-y-2">
                      <Label>HTTP Documentation URL</Label>
                      <div className="flex items-center gap-2">
                        <Input
                          value={watchedValues.httpDocumentationUrl}
                          readOnly
                          className="flex-1 bg-muted"
                          data-testid="input-httpDocumentationUrl"
                        />
                        <Button
                          type="button"
                          variant="outline"
                          size="icon"
                          onClick={() => window.open(watchedValues.httpDocumentationUrl, '_blank')}
                          data-testid="button-open-httpDocumentationUrl"
                        >
                          <ExternalLink className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  )}

                  {watchedValues.httpDocumentationUiUrl && (
                    <div className="space-y-2">
                      <Label>HTTP Documentation UI URL</Label>
                      <div className="flex items-center gap-2">
                        <Input
                          value={watchedValues.httpDocumentationUiUrl}
                          readOnly
                          className="flex-1 bg-muted"
                          data-testid="input-httpDocumentationUiUrl"
                        />
                        <Button
                          type="button"
                          variant="outline"
                          size="icon"
                          onClick={() => window.open(watchedValues.httpDocumentationUiUrl, '_blank')}
                          data-testid="button-open-httpDocumentationUiUrl"
                        >
                          <ExternalLink className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  )}

                  {watchedValues.httpTunnelEndpointUrl && (
                    <div className="space-y-2">
                      <Label>HTTP Tunnel Endpoint URL</Label>
                      <div className="flex items-center gap-2">
                        <Input
                          value={watchedValues.httpTunnelEndpointUrl}
                          readOnly
                          className="flex-1 bg-muted"
                          data-testid="input-httpTunnelEndpointUrl"
                        />
                        <Button
                          type="button"
                          variant="outline"
                          size="icon"
                          onClick={() => window.open(watchedValues.httpTunnelEndpointUrl, '_blank')}
                          data-testid="button-open-httpTunnelEndpointUrl"
                        >
                          <ExternalLink className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}

            {/* Application Configurations Section - Only available in update mode */}
            {mode === 'update' && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold">Application Configurations</h3>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setEditingConfig({ index: null, type: null, value: {} });
                    setConfigDialogOpen(true);
                  }}
                  data-testid="button-add-configuration"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  Add Configuration
                </Button>
              </div>
              
              {configurationsLoading ? (
                <div className="flex items-center justify-center py-6">
                  <Loader2 className="w-5 h-5 animate-spin text-muted-foreground" />
                  <span className="ml-2 text-sm text-muted-foreground">Loading configurations...</span>
                </div>
              ) : configurations && configurations.length > 0 ? (
                <div className="space-y-3">
                  {configurations.map((config: any, index: number) => {
                    const configType = detectConfigurationType(config);
                    const configName = getConfigurationName(config, configType);
                    
                    return (
                      <Card key={config.id || `config-${index}`}>
                        <CardContent className="pt-6">
                          <div className="flex items-center justify-between">
                            <div className="space-y-2 flex-1">
                              {config?.id && (
                                <div className="flex items-center gap-2 text-sm">
                                  <span className="text-muted-foreground">ID:</span>
                                  <span className="font-mono">{config.id}</span>
                                </div>
                              )}
                              <div className="flex items-center gap-4 text-sm">
                                {configName && (
                                  <>
                                    <div className="flex items-center gap-2">
                                      <span className="text-muted-foreground">Name:</span>
                                      <span>{configName}</span>
                                    </div>
                                    <span className="text-muted-foreground">•</span>
                                  </>
                                )}
                                <div className="flex items-center gap-2">
                                  <span className="text-muted-foreground">Type:</span>
                                  <span className="font-medium">{configType}</span>
                                </div>
                              </div>
                            </div>
                            <div className="flex gap-2">
                              <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={async () => {
                                  if (mode === 'update' && config.id && configType) {
                                    // Fetch full configuration details from type-specific endpoint
                                    const typeEndpoint = getConfigTypeEndpoint(configType);
                                    try {
                                      const fullConfig = await apiClient.request<any>(
                                        `/api/rest/application/${initialData?.id}/configuration/${typeEndpoint}/${config.id}`
                                      );
                                      setEditingConfig({ index, type: configType, value: fullConfig });
                                      setConfigDialogOpen(true);
                                    } catch (error) {
                                      console.error('Failed to fetch full config details:', error);
                                      toast({
                                        title: 'Error',
                                        description: 'Failed to load configuration details',
                                        variant: 'destructive',
                                      });
                                    }
                                  } else {
                                    // In create mode or no ID, use what we have
                                    setEditingConfig({ index, type: configType, value: config });
                                    setConfigDialogOpen(true);
                                  }
                                }}
                                data-testid={`button-edit-configuration-${index}`}
                              >
                                <Pencil className="h-4 w-4 mr-2" />
                                Edit
                              </Button>
                              <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={() => {
                                  if (mode === 'update' && configType && config.id) {
                                    deleteConfigMutation.mutate({ type: configType, configId: config.id });
                                  }
                                }}
                                disabled={!config.id || deleteConfigMutation.isPending}
                                data-testid={`button-delete-configuration-${index}`}
                              >
                                {deleteConfigMutation.isPending ? (
                                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                                ) : (
                                  <Trash2 className="h-4 w-4 mr-2" />
                                )}
                                Remove
                              </Button>
                            </div>
                          </div>
                        </CardContent>
                      </Card>
                    );
                  })}
                </div>
              ) : (
                <p className="text-sm text-muted-foreground">No configurations added yet</p>
              )}
            </div>
            )}
          </div>

          <div className="flex justify-end gap-2 pt-4 border-t">
              <Button 
                type="submit" 
                data-testid="button-submit"
                onClick={() => {
                  console.log('Submit button clicked!');
                  console.log('Form errors:', form.formState.errors);
                  console.log('Form values:', form.getValues());
                }}
              >
                {mode === 'create' ? 'Create Application' : 'Update Application'}
              </Button>
            </div>
          </form>
        </Form>

        <ApplicationConfigurationDialog
          open={configDialogOpen}
          onOpenChange={(open) => {
            setConfigDialogOpen(open);
            if (!open) {
              setEditingConfig(null);
            }
          }}
          value={editingConfig?.value || {}}
          configurationType={editingConfig?.type || null}
          isEditing={!!editingConfig?.value?.id}
          onSave={async ({ type, value }) => {
            if (mode !== 'update') {
              throw new Error('Configurations can only be managed in update mode');
            }
            if (!type) {
              throw new Error('Configuration type is required');
            }
            
            if (editingConfig?.value?.id) {
              // Update existing configuration
              await updateConfigMutation.mutateAsync({ type, configId: editingConfig.value.id, config: value });
            } else {
              // Create new configuration
              await createConfigMutation.mutateAsync({ type, config: value });
            }
          }}
        />
      </div>
  );
}

function getConfigTypeEndpoint(type: string): string {
  const typeMap: Record<string, string> = {
    'Facebook': 'facebook',
    'Firebase': 'firebase',
    'GooglePlay': 'google_play',
    'iOS': 'ios',
    'Matchmaking': 'matchmaking',
    'Oculus': 'oculus',
  };
  return typeMap[type] || type.toLowerCase();
}

function getConfigurationClass(type: string): string {
  const classMap: Record<string, string> = {
    'Facebook': 'dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration',
    'Firebase': 'dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration',
    'GooglePlay': 'dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration',
    'iOS': 'dev.getelements.elements.sdk.model.application.IosApplicationConfiguration',
    'Matchmaking': 'dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration',
    'Oculus': 'dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration',
  };
  return classMap[type] || '';
}

function detectConfigurationType(config: any): string | null {
  if (!config) return null;
  
  // Check @class discriminator first
  if (config['@class']) {
    const className = config['@class'];
    if (className.includes('FacebookApplicationConfiguration')) return 'Facebook';
    if (className.includes('FirebaseApplicationConfiguration')) return 'Firebase';
    if (className.includes('GooglePlayApplicationConfiguration')) return 'GooglePlay';
    if (className.includes('IosApplicationConfiguration')) return 'iOS';
    if (className.includes('MatchmakingApplicationConfiguration')) return 'Matchmaking';
    if (className.includes('OculusApplicationConfiguration')) return 'Oculus';
  }
  
  // Check type field from API response
  if (config.type) {
    const typeName = config.type;
    if (typeName.includes('FacebookApplicationConfiguration')) return 'Facebook';
    if (typeName.includes('FirebaseApplicationConfiguration')) return 'Firebase';
    if (typeName.includes('GooglePlayApplicationConfiguration')) return 'GooglePlay';
    if (typeName.includes('IosApplicationConfiguration')) return 'iOS';
    if (typeName.includes('MatchmakingApplicationConfiguration')) return 'Matchmaking';
    if (typeName.includes('OculusApplicationConfiguration')) return 'Oculus';
  }
  
  // Fallback to field-based detection
  if (config.applicationId && config.applicationSecret) return 'Facebook';
  if (config.projectId && config.serviceAccountCredentials) return 'Firebase';
  if (config.jsonKey || (config.productBundles && !config.applicationId?.includes('.'))) return 'GooglePlay';
  if (config.applicationId?.includes('.') && config.productBundles) return 'iOS';
  if (config.maxProfiles || config.matchmaker) return 'Matchmaking';
  
  return null;
}

function getConfigurationName(config: any, type: string | null): string | null {
  if (!config || !type) return null;
  
  // Use config.name if available
  if (config.name) return config.name;
  
  // Fall back to type-specific identifiers
  switch (type) {
    case 'Facebook':
    case 'Oculus':
      return config.applicationId || null;
    case 'Firebase':
      return config.projectId || null;
    case 'GooglePlay':
    case 'iOS':
      return config.applicationId || null;
    case 'Matchmaking':
      return config.matchmaker?.name || 'Default Matchmaker';
    default:
      return null;
  }
}
