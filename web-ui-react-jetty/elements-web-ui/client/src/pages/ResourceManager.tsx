import { useState, useMemo, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { queryClient } from '@/lib/queryClient';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Input } from '@/components/ui/input';
import { Plus, Pencil, Trash2, RefreshCw, FileJson, FormInput, Calendar, AlertCircle, Copy, Search, ChevronLeft, ChevronRight, Package } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { getResourceSchema, type ModelSchema } from '@/lib/schema-parser';
import { 
  Dialog, 
  DialogContent, 
  DialogDescription, 
  DialogHeader, 
  DialogTitle 
} from '@/components/ui/dialog';
import { DynamicResourceForm } from '@/components/DynamicResourceForm';
import { ScheduleEventsEditor } from '@/components/ScheduleEventsEditor';
import { InventoryViewer } from '@/components/InventoryViewer';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

interface ResourceManagerProps {
  resourceName: string;
  endpoint: string;
}

function formatColumnName(fieldName: string): string {
  // Convert camelCase to Title Case with spaces
  // For example: responseIdMapping -> Response Id Mapping
  return fieldName
    .replace(/([A-Z])/g, ' $1') // Add space before capital letters
    .replace(/^./, (str) => str.toUpperCase()) // Capitalize first letter
    .trim();
}

export default function ResourceManager({ resourceName, endpoint }: ResourceManagerProps) {
  const { toast } = useToast();
  const [selectedItem, setSelectedItem] = useState<any>(null);
  const [currentFormData, setCurrentFormData] = useState<any>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState<'create' | 'edit' | 'delete'>('create');
  const [jsonText, setJsonText] = useState<string>('');
  const [jsonError, setJsonError] = useState<string>('');
  const [viewMode, setViewMode] = useState<'form' | 'json'>('form');
  const [showEventsEditor, setShowEventsEditor] = useState(false);
  const [selectedSchedule, setSelectedSchedule] = useState<any>(null);
  const [inventoryOpen, setInventoryOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<any>(null);
  const [formResetKey, setFormResetKey] = useState(0);
  const [formInitialData, setFormInitialData] = useState<any>(undefined);
  const [schema, setSchema] = useState<ModelSchema | null>(null);
  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [draftRefreshKey, setDraftRefreshKey] = useState(0);
  const [searchInput, setSearchInput] = useState(''); // Input value (not debounced)
  const [searchTerm, setSearchTerm] = useState(''); // Actual search query (debounced)
  const [offset, setOffset] = useState(0);
  const [count] = useState(() => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  });
  const [paginationInfo, setPaginationInfo] = useState<{ total: number; offset: number; count: number } | null>(null);

  // Fetch metadata specs for ID resolution (only for Metadata resource)
  const { data: metadataSpecsResponse } = useQuery<{ objects: any[] }>({
    queryKey: ['/api/rest/metadata_spec'],
    queryFn: async () => apiClient.request<{ objects: any[] }>('/api/rest/metadata_spec'),
    enabled: resourceName === 'Metadata',
  });

  // Create metadata spec lookup map
  const metadataSpecLookup = useMemo(() => {
    if (!metadataSpecsResponse?.objects) return {};
    const lookup: Record<string, string> = {};
    metadataSpecsResponse.objects.forEach((spec: any) => {
      lookup[spec.id] = spec.name;
    });
    return lookup;
  }, [metadataSpecsResponse]);

  // Debounce search input to avoid refetching on every keystroke
  useEffect(() => {
    const timer = setTimeout(() => {
      setSearchTerm(searchInput);
      setOffset(0); // Reset to first page when search changes
    }, 500); // 500ms delay

    return () => clearTimeout(timer);
  }, [searchInput]);

  // Load schema for JSON validation
  useEffect(() => {
    async function loadSchema() {
      try {
        const loadedSchema = await getResourceSchema(resourceName, dialogMode === 'create' ? 'create' : 'update');
        setSchema(loadedSchema);
      } catch (err) {
        console.error('Failed to load schema for validation:', err);
      }
    }
    if (isDialogOpen && viewMode === 'json') {
      loadSchema();
    }
  }, [resourceName, dialogMode, isDialogOpen, viewMode]);

  // Load draft from localStorage if it exists
  const loadDraft = (): any | null => {
    try {
      const draftKey = `draft_${resourceName}_create_new`;
      const stored = localStorage.getItem(draftKey);
      if (!stored) return null;
      
      const parsed = JSON.parse(stored);
      // Only load drafts from the last 24 hours
      const ageInHours = (Date.now() - parsed.timestamp) / (1000 * 60 * 60);
      if (ageInHours > 24) {
        localStorage.removeItem(draftKey);
        return null;
      }
      
      return parsed.data;
    } catch (error) {
      console.error('Failed to load draft:', error);
      return null;
    }
  };

  const saveDraft = (data: any) => {
    try {
      const draftKey = `draft_${resourceName}_create_new`;
      localStorage.setItem(draftKey, JSON.stringify({
        data,
        timestamp: Date.now(),
      }));
    } catch (error) {
      console.error('Failed to save draft:', error);
    }
  };

  const deleteDraft = () => {
    try {
      const draftKey = `draft_${resourceName}_create_new`;
      localStorage.removeItem(draftKey);
      // Force re-render
      setFormResetKey(prev => prev + 1);
    } catch (error) {
      console.error('Failed to delete draft:', error);
    }
  };

  // Helper to close dialog and refresh draft list
  const closeDialog = (skipAutosave = false) => {
    // Auto-save draft when closing in create mode (if there's form data)
    // Skip auto-save when explicitly requested (e.g., after successful submit)
    if (!skipAutosave && dialogMode === 'create' && currentFormData && Object.keys(currentFormData).length > 0) {
      saveDraft(currentFormData);
    }
    setIsDialogOpen(false);
    setDraftRefreshKey(prev => prev + 1);
  };

  // Validate JSON against schema
  const validateJsonAgainstSchema = (jsonData: any): string[] => {
    if (!schema) return [];
    
    const errors: string[] = [];
    
    for (const field of schema.fields) {
      const value = jsonData[field.name];
      
      // Check required fields
      if (field.required && (value === undefined || value === null || value === '')) {
        errors.push(`Field "${field.name}" is required`);
        continue;
      }
      
      // Skip validation for undefined optional fields
      if (value === undefined || value === null || value === '') continue;
      
      // Array validation - check BEFORE primitive types
      if (field.isArray) {
        if (!Array.isArray(value)) {
          errors.push(`Field "${field.name}" must be an array`);
        }
        // Skip primitive type validation for arrays
        continue;
      }
      
      // Type validation for non-array fields
      if (field.type === 'string' && typeof value !== 'string') {
        errors.push(`Field "${field.name}" must be a string`);
      } else if (field.type === 'number' && typeof value !== 'number') {
        errors.push(`Field "${field.name}" must be a number`);
      } else if (field.type === 'boolean' && typeof value !== 'boolean') {
        errors.push(`Field "${field.name}" must be a boolean`);
      } else if (field.type === 'object' && typeof value !== 'object') {
        errors.push(`Field "${field.name}" must be an object`);
      } else if (field.type === 'enum' && field.enumValues && !field.enumValues.includes(value)) {
        errors.push(`Field "${field.name}" must be one of: ${field.enumValues.join(', ')}`);
      }
    }
    
    return errors;
  };

  // Clean data for submission (same logic as DynamicResourceForm)
  const cleanDataForSubmission = (data: Record<string, any>): Record<string, any> => {
    const cleanedData: Record<string, any> = {};
    const isMissionWithSteps = resourceName === 'Missions';
    
    for (const [key, value] of Object.entries(data)) {
      // For Mission steps, clean up the steps array
      if (isMissionWithSteps && key === 'steps' && Array.isArray(value)) {
        const cleanedSteps = value.map((step: any) => {
          const cleanedStep: any = {};
          const validFields = ['displayName', 'description', 'count', 'metadata', 'rewards'];
          
          validFields.forEach(field => {
            if (step[field] !== undefined && step[field] !== null && step[field] !== '') {
              if (field === 'rewards' && Array.isArray(step[field])) {
                // Transform rewards from { itemId: "...", quantity: 1 } to { item: { id: "..." }, quantity: 1 }
                cleanedStep[field] = step[field].map((reward: any) => ({
                  item: { id: reward.itemId },
                  quantity: reward.quantity
                }));
              } else {
                cleanedStep[field] = step[field];
              }
            }
          });
          
          return cleanedStep;
        });
        cleanedData[key] = cleanedSteps;
      } else if (value !== '' && value !== null && value !== undefined) {
        cleanedData[key] = value;
      }
    }
    
    return cleanedData;
  };

  // Convert JSON back to form-friendly format (reverse transformation)
  const convertJsonToFormData = (data: Record<string, any>): Record<string, any> => {
    const formData: Record<string, any> = { ...data };
    const isMissionWithSteps = resourceName === 'Missions';
    
    // For Mission steps, reverse transform rewards
    if (isMissionWithSteps && formData.steps && Array.isArray(formData.steps)) {
      formData.steps = formData.steps.map((step: any) => {
        const formStep: any = { ...step };
        
        // Transform rewards from { item: { id: "..." }, quantity: 1 } to { itemId: "...", quantity: 1 }
        if (formStep.rewards && Array.isArray(formStep.rewards)) {
          formStep.rewards = formStep.rewards.map((reward: any) => ({
            itemId: reward.item?.id || '',
            quantity: reward.quantity || 1
          }));
        }
        
        return formStep;
      });
    }
    
    // For Metadata resource, convert metadataSpec from { id: "..." } to string ID
    if (resourceName === 'Metadata' && formData.metadataSpec && typeof formData.metadataSpec === 'object' && formData.metadataSpec.id) {
      formData.metadataSpec = formData.metadataSpec.id;
    }
    
    return formData;
  };

  const { data: items, isLoading, isFetching, error } = useQuery({
    queryKey: [endpoint, offset, count, searchTerm],
    placeholderData: (previousData) => previousData, // Keep previous data while fetching new data
    queryFn: async () => {
      // Build query parameters for pagination and search
      const params = new URLSearchParams({
        offset: offset.toString(),
        count: count.toString(),
      });
      if (searchTerm) {
        params.append('search', searchTerm);
      }
      
      const urlWithParams = `${endpoint}?${params}`;
      const response = await apiClient.request<any>(urlWithParams);
      
      // Elements API returns paginated data: {offset, total, objects: [...]} or {offset, total, content: [...]}
      if (response && typeof response === 'object') {
        // Store pagination info if response has total field
        if ('total' in response) {
          setPaginationInfo({
            total: response.total,
            offset: response.offset || 0,
            count: response.count || count,
          });
        } else {
          // Clear pagination info for non-paginated responses
          setPaginationInfo(null);
        }
        
        // Extract items from response
        if ('objects' in response) {
          return response.objects || [];
        } else if ('content' in response) {
          return response.content || [];
        }
      }
      
      // Fallback for non-paginated responses - clear pagination info
      setPaginationInfo(null);
      return Array.isArray(response) ? response : [];
    },
  });

  // Combine items with draft (draft appears at top)
  const itemsWithDraft = useMemo(() => {
    const draft = loadDraft();
    if (!draft) return items || [];
    
    // Mark draft and add to top of list
    const draftItem = { ...draft, _isDraft: true };
    return [draftItem, ...(items || [])];
  }, [items, formResetKey, draftRefreshKey]); // Re-compute when items change, draft is deleted (formResetKey), or dialog closes (draftRefreshKey)

  const saveMutation = useMutation({
    mutationFn: async (data: any) => {
      const { _configurations, ...dataWithoutConfigs } = data;
      
      if (dialogMode === 'create') {
        const createdItem = await apiClient.request(endpoint, { 
          method: 'POST',
          body: JSON.stringify(dataWithoutConfigs),
        }) as any;
        
        // For Applications, create configurations if they were copied
        if (resourceName === 'Applications' && _configurations && Array.isArray(_configurations) && _configurations.length > 0) {
          const appId = createdItem.id || createdItem.name;
          const configResults: { success: number; failed: number; errors: string[] } = { 
            success: 0, 
            failed: 0, 
            errors: [] 
          };
          
          for (const config of _configurations) {
            try {
              // Determine configuration type from the config data
              const configType = detectConfigType(config);
              if (!configType) {
                configResults.failed++;
                configResults.errors.push(`Unknown configuration type for: ${config.name || 'unnamed'}`);
                continue;
              }
              
              const typeEndpoint = getConfigTypeEndpoint(configType);
              
              // Clean up null values - replace arrays with empty arrays, remove null objects/values
              const cleanedConfig = { ...config };
              Object.keys(cleanedConfig).forEach(key => {
                if (cleanedConfig[key] === null) {
                  // For known array fields, use empty array
                  if (key === 'productBundles' || key === 'builtinApplicationPermissions') {
                    cleanedConfig[key] = [];
                  } 
                  // For jsonKey specifically, use empty object (it's a special case)
                  else if (key === 'jsonKey') {
                    cleanedConfig[key] = {};
                  }
                  // For everything else (metadata, metadataSpec, etc.), remove the field entirely
                  else {
                    delete cleanedConfig[key];
                  }
                }
              });
              
              const configWithMetadata = {
                id: null,
                ...cleanedConfig,
                type: getConfigurationClass(configType),
                parent: { id: appId }
              };
              
              await apiClient.request(`/api/rest/application/${appId}/configuration/${typeEndpoint}`, {
                method: 'POST',
                body: JSON.stringify(configWithMetadata),
              });
              configResults.success++;
            } catch (error) {
              console.error('Failed to create configuration:', error);
              configResults.failed++;
              const errorMsg = error instanceof Error ? error.message : 'Unknown error';
              configResults.errors.push(errorMsg);
            }
          }
          
          // Store results on the created item to use in onSuccess
          (createdItem as any)._configCopyResults = configResults;
        }
        
        return createdItem;
      } else {
        const itemId = selectedItem?.id || dataWithoutConfigs.id;
        if (!itemId) {
          throw new Error('Cannot update: No ID found for the selected item');
        }
        return await apiClient.request(`${endpoint}/${itemId}`, { 
          method: 'PUT',
          body: JSON.stringify(dataWithoutConfigs),
        });
      }
    },
    onSuccess: (data: any) => {
      queryClient.invalidateQueries({ queryKey: [endpoint] });
      
      // Helper function to singularize resource name
      const getSingularName = (name: string) => {
        // Handle special cases
        if (name === 'Metadata') return 'Metadata';
        // Default: remove trailing 's' if present
        return name.endsWith('s') ? name.slice(0, -1) : name;
      };
      
      const singularName = getSingularName(resourceName);
      
      // Check for configuration copy results
      const configResults = (data as any)?._configCopyResults;
      if (configResults && (configResults.success > 0 || configResults.failed > 0)) {
        if (configResults.failed === 0) {
          // All configurations copied successfully
          toast({
            title: 'Success',
            description: `${singularName} created successfully with ${configResults.success} configuration${configResults.success !== 1 ? 's' : ''}`,
          });
        } else if (configResults.success === 0) {
          // All configurations failed
          toast({
            title: 'Partial Success',
            description: `${singularName} created but failed to copy ${configResults.failed} configuration${configResults.failed !== 1 ? 's' : ''}`,
            variant: 'destructive',
          });
        } else {
          // Some succeeded, some failed
          toast({
            title: 'Partial Success',
            description: `${singularName} created with ${configResults.success} configuration${configResults.success !== 1 ? 's' : ''}, but ${configResults.failed} failed to copy`,
            variant: 'destructive',
          });
        }
      } else {
        // Normal success message
        toast({
          title: 'Success',
          description: `${singularName} ${dialogMode === 'create' ? 'created' : 'updated'} successfully`,
        });
      }
      
      // Clear draft on successful create
      if (dialogMode === 'create') {
        deleteDraft();
      }
      // Close dialog without auto-saving (skip auto-save on successful submit)
      closeDialog(true);
      setSelectedItem(null);
      setCurrentFormData(null);
    },
    onError: (error: Error & { status?: number }) => {
      toast({
        title: 'Error',
        description: error.message || `Failed to ${dialogMode} ${resourceName.toLowerCase()}`,
        variant: 'destructive',
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.request(`${endpoint}/${id}`, { method: 'DELETE' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [endpoint] });
      toast({
        title: 'Success',
        description: `${resourceName} deleted successfully`,
      });
      // Skip auto-save on delete (no need to save draft)
      closeDialog(true);
    },
    onError: (error: Error & { status?: number }) => {
      const message = error.status === 409 
        ? 'Cannot delete: resource is in use'
        : error.message || 'Failed to delete';
      toast({
        title: 'Error',
        description: message,
        variant: 'destructive',
      });
    },
  });

  const handleDelete = (item: any) => {
    setSelectedItem(item);
    setDialogMode('delete');
    setIsDialogOpen(true);
  };

  const confirmDelete = () => {
    if (selectedItem?.id) {
      deleteMutation.mutate(selectedItem.id);
    }
  };

  // Derive columns from actual API items, not drafts (which may have incomplete keys)
  // Memoize columns to prevent table remounting during refetch
  // MUST be before early returns to follow Rules of Hooks
  const columns = useMemo(() => {
    if (!items || items.length === 0) return [];
    
    const keys = Object.keys(items[0]);
        
        // Custom column filtering and ordering for Profiles resource
        if (resourceName === 'Profiles') {
          // Remove image-related and metadata columns (keep user to show username)
          const filteredKeys = keys.filter(k => 
            k !== 'image' && k !== 'imageObject' && k !== 'imageUrl' && k !== 'metadata'
          );
          
          // Reorder to show displayName right after id
          const idIndex = filteredKeys.indexOf('id');
          const displayNameIndex = filteredKeys.indexOf('displayName');
          
          if (idIndex !== -1 && displayNameIndex !== -1 && displayNameIndex !== idIndex + 1) {
            const reordered = [...filteredKeys];
            reordered.splice(displayNameIndex, 1);
            reordered.splice(idIndex + 1, 0, 'displayName');
            return reordered;
          }
          
          return filteredKeys;
        }
        
        // Custom column filtering for Users resource
        if (resourceName === 'Users') {
          // Remove firstName, lastName, primaryPhoneNb columns
          const filteredKeys = keys.filter(k => 
            k !== 'firstName' && k !== 'lastName' && k !== 'primaryPhoneNb'
          );
          return filteredKeys;
        }
        
        // Custom column filtering for Missions resource
        if (resourceName === 'Missions') {
          // Remove metadata column
          const filteredKeys = keys.filter(k => k !== 'metadata');
          return filteredKeys;
        }
        
        // Custom column filtering and ordering for Items resource
        if (resourceName === 'Items') {
          // Remove metadataSpec and metadata columns
          const filteredKeys = keys.filter(k => k !== 'metadataSpec' && k !== 'metadata');
          
          // Move displayName to the left of tags
          const displayNameIndex = filteredKeys.indexOf('displayName');
          const tagsIndex = filteredKeys.indexOf('tags');
          
          if (displayNameIndex !== -1 && tagsIndex !== -1 && displayNameIndex > tagsIndex) {
            // displayName is currently to the right of tags, move it to the left
            const reordered = [...filteredKeys];
            reordered.splice(displayNameIndex, 1);
            reordered.splice(tagsIndex, 0, 'displayName');
            return reordered;
          }
          
          return filteredKeys;
        }
        
        // Custom column filtering for Applications resource
        if (resourceName === 'Applications') {
          // Show only id, name, and description
          const filteredKeys = keys.filter(k => k === 'id' || k === 'name' || k === 'description');
          return filteredKeys;
        }
        
        // Custom column filtering for OIDC resource
        if (resourceName === 'OIDC') {
          // Show only id, name, and issuer
          const filteredKeys = keys.filter(k => k === 'id' || k === 'name' || k === 'issuer');
          return filteredKeys;
        }
        
        // Custom column filtering for OAuth2 resource
        if (resourceName === 'OAuth2') {
          // Show only id, name, and validationUrl
          const filteredKeys = keys.filter(k => k === 'id' || k === 'name' || k === 'validationUrl');
          return filteredKeys;
        }
        
        // Custom column filtering and ordering for Metadata resource
        if (resourceName === 'Metadata') {
          // Remove metadata column
          const filteredKeys = keys.filter(k => k !== 'metadata');
          
          // Reorder to move accessLevel before metadataSpec
          const accessLevelIndex = filteredKeys.indexOf('accessLevel');
          const metadataSpecIndex = filteredKeys.indexOf('metadataSpec');
          
          if (accessLevelIndex > -1 && metadataSpecIndex > -1 && accessLevelIndex > metadataSpecIndex) {
            // Remove accessLevel from its current position
            filteredKeys.splice(accessLevelIndex, 1);
            // Insert it before metadataSpec
            filteredKeys.splice(metadataSpecIndex, 0, 'accessLevel');
          }
          
          return filteredKeys;
        }
        
        // Custom column filtering for Metadata Spec resource
        if (resourceName === 'Metadata Spec') {
          // Remove type and properties columns
          const filteredKeys = keys.filter(k => k !== 'type' && k !== 'properties');
          return filteredKeys;
        }
        
    return keys;
  }, [items, resourceName]);

  // Only show full-page loading on initial load (no cached data)
  if (isLoading && !items) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <RefreshCw className="w-8 h-8 animate-spin mx-auto mb-4 text-primary" />
          <p className="text-muted-foreground">Loading {resourceName}...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-96">
        <div className="text-center">
          <p className="text-destructive mb-4">Failed to load {resourceName}</p>
          <p className="text-muted-foreground text-sm">{(error as Error).message}</p>
        </div>
      </div>
    );
  }

  const draft = loadDraft();

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">
            {resourceName === 'Custom' ? 'Custom Auth Scheme' : resourceName}
          </h1>
          <p className="text-muted-foreground mt-1">
            {resourceName === 'OAuth2' 
              ? 'Manage OAuth2 Auth Schemes in your Elements application'
              : resourceName === 'Custom'
              ? 'Manage custom auth schemes in your Elements application'
              : `Manage ${resourceName.toLowerCase()} in your Elements application`}
          </p>
        </div>
        <div className="flex gap-2 items-center">
          {draft && (
            <Badge variant="secondary" data-testid="badge-draft-exists">
              Draft Saved
            </Badge>
          )}
          <Button 
            onClick={async () => {
              await queryClient.invalidateQueries({ queryKey: [endpoint] });
            }} 
            variant="outline" 
            size="sm"
            disabled={isFetching}
            data-testid="button-refresh-resources"
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${isFetching ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Button 
            onClick={() => {
              const draftData = loadDraft();
              setSelectedItem(null);
              setCurrentFormData(draftData);
              setFormInitialData(draftData || undefined);
              setJsonText(draftData ? JSON.stringify(draftData, null, 2) : '{}');
              setJsonError('');
              setDialogMode('create');
              setIsDialogOpen(true);
            }}
            data-testid="button-create"
          >
            <Plus className="w-4 h-4 mr-2" />
            {draft 
              ? 'Resume Draft' 
              : resourceName === 'OAuth2' 
              ? 'Create OAuth2 Scheme' 
              : resourceName === 'OIDC'
              ? 'Create OIDC Scheme'
              : resourceName === 'Custom'
              ? 'Create Custom Auth Scheme'
              : resourceName === 'Metadata Spec' 
              ? 'Create Metadata Spec' 
              : resourceName === 'Metadata' 
              ? 'Create Metadata' 
              : `Create ${resourceName.slice(0, -1)}`}
          </Button>
        </div>
      </div>

      <div className="space-y-2">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder={`Search ${resourceName.toLowerCase()}...`}
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="pl-10 pr-10"
            data-testid="input-search"
          />
          {isFetching && (
            <RefreshCw className="absolute right-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground animate-spin" />
          )}
        </div>
        {searchTerm && (
          <p className="text-xs text-muted-foreground">
            Searching for "{searchTerm}" (Note: Search may not be supported by all resources)
          </p>
        )}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>
            All {resourceName === 'OAuth2' 
              ? 'OAuth2 Auth Schemes' 
              : resourceName === 'Custom'
              ? 'Custom Auth Schemes'
              : resourceName === 'Metadata Spec' 
              ? 'Metadata Specs' 
              : resourceName}
          </CardTitle>
          <CardDescription>
            {paginationInfo ? `${paginationInfo.total} total` : `${items?.length || 0}`} {resourceName.toLowerCase()} found
            {draft && ' + 1 draft'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {itemsWithDraft && itemsWithDraft.length > 0 ? (
            <div className="border rounded-md overflow-x-auto">
              <table className="w-full caption-bottom text-sm">
                <thead className="[&_tr]:border-b">
                  <TableRow>
                    {columns.map((col) => (
                      <TableHead key={col}>{formatColumnName(col)}</TableHead>
                    ))}
                    <TableHead className="w-[140px] text-center sticky right-0 bg-background border-l z-10">Actions</TableHead>
                  </TableRow>
                </thead>
                <tbody className="[&_tr:last-child]:border-0">
                  {itemsWithDraft.map((item: any, idx: number) => (
                    <TableRow key={item._isDraft ? 'draft' : (item.id || idx)} data-testid={`row-${resourceName.toLowerCase()}-${idx}`}>
                      {columns.map((col) => {
                        // Custom rendering for Profiles resource
                        let cellValue = item[col];
                        
                        if (resourceName === 'Profiles') {
                          if (col === 'application' && typeof cellValue === 'object') {
                            // Show only application name instead of full object
                            cellValue = cellValue?.name ?? '';
                          } else if (col === 'user' && typeof cellValue === 'object') {
                            // Show only user name instead of full object
                            cellValue = cellValue?.name ?? '';
                          }
                        }
                        
                        // Custom rendering for Users resource linkedAccounts
                        if (resourceName === 'Users' && col === 'linkedAccounts' && Array.isArray(cellValue)) {
                          // Transform each linked account by taking the last part after splitting by "."
                          // Example: "dev.getelements.auth.scheme.email" → "Email"
                          cellValue = cellValue.map((account: string) => {
                            const parts = account.split('.');
                            const lastPart = parts[parts.length - 1];
                            // Capitalize first letter
                            return lastPart.charAt(0).toUpperCase() + lastPart.slice(1);
                          }).join(', ');
                        }
                        
                        // Custom rendering for Missions resource
                        if (resourceName === 'Missions') {
                          if (col === 'steps' && Array.isArray(cellValue)) {
                            // Show only the display names of the steps
                            cellValue = cellValue.map((step: any) => {
                              return step?.displayName || step?.name || '';
                            }).filter(name => name).join(', ');
                          } else if (col === 'finalRepeatStep' && typeof cellValue === 'object' && cellValue !== null) {
                            // Show only the name of the final repeat step
                            cellValue = cellValue?.displayName || cellValue?.name || '';
                          }
                        }
                        
                        // Custom rendering for Leaderboards resource
                        if (resourceName === 'Leaderboards') {
                          if (col === 'firstEpochTimestamp' && typeof cellValue === 'number') {
                            // Format timestamp as date
                            cellValue = new Date(cellValue).toLocaleString();
                          } else if (col === 'epochInterval' && typeof cellValue === 'number') {
                            // Format interval as duration (convert milliseconds to readable format)
                            const seconds = Math.floor(cellValue / 1000);
                            const minutes = Math.floor(seconds / 60);
                            const hours = Math.floor(minutes / 60);
                            const days = Math.floor(hours / 24);
                            
                            if (days > 0) {
                              cellValue = `${days} day${days !== 1 ? 's' : ''}`;
                            } else if (hours > 0) {
                              cellValue = `${hours} hour${hours !== 1 ? 's' : ''}`;
                            } else if (minutes > 0) {
                              cellValue = `${minutes} minute${minutes !== 1 ? 's' : ''}`;
                            } else {
                              cellValue = `${seconds} second${seconds !== 1 ? 's' : ''}`;
                            }
                          }
                        }
                        
                        // Custom rendering for Metadata resource
                        if (resourceName === 'Metadata') {
                          if (col === 'metadataSpec') {
                            if (typeof cellValue === 'object' && cellValue !== null) {
                              // If object has name property, use it
                              if (cellValue.name) {
                                cellValue = cellValue.name;
                              } 
                              // If object only has id, lookup the name
                              else if (cellValue.id) {
                                cellValue = metadataSpecLookup[cellValue.id] || cellValue.id;
                              }
                            } else if (typeof cellValue === 'string') {
                              // Lookup spec name from ID (for drafts with string IDs)
                              cellValue = metadataSpecLookup[cellValue] || cellValue;
                            }
                          }
                        }
                        
                        return (
                          <TableCell key={col} data-testid={`cell-${col}-${idx}`}>
                            <div className="flex items-center gap-2">
                              {col === columns[0] && item._isDraft && (
                                <Badge variant="outline" className="text-xs" data-testid="badge-draft-row">
                                  Draft
                                </Badge>
                              )}
                              {typeof cellValue === 'object' 
                                ? JSON.stringify(cellValue) 
                                : String(cellValue ?? '')}
                            </div>
                          </TableCell>
                        );
                      })}
                      <TableCell className="w-[140px] sticky right-0 bg-background border-l z-10">
                        <div className="flex items-center justify-center gap-1">
                    {resourceName === 'Schedules' && !item._isDraft && (
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => {
                          setSelectedSchedule(item);
                          setShowEventsEditor(true);
                        }}
                        data-testid={`button-events-${idx}`}
                      >
                        <Calendar className="w-4 h-4" />
                      </Button>
                    )}
                    {(resourceName === 'Users' || resourceName === 'Profiles') && !item._isDraft && (
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={() => {
                          // For Profiles, extract userId from the user field
                          if (resourceName === 'Profiles') {
                            const userObj = typeof item.user === 'object' ? item.user : null;
                            const userId = userObj?.id || item.user;
                            if (userId) {
                              // Create a user object that matches what InventoryViewer expects
                              setSelectedUser({
                                id: userId,
                                name: userObj?.name || item.displayName || userId,
                              });
                              setInventoryOpen(true);
                            } else {
                              toast({
                                title: 'Error',
                                description: 'Cannot view inventory: User information missing from profile',
                                variant: 'destructive',
                              });
                            }
                          } else {
                            // For Users, use the item directly
                            setSelectedUser(item);
                            setInventoryOpen(true);
                          }
                        }}
                        data-testid={`button-inventory-${idx}`}
                      >
                        <Package className="w-4 h-4" />
                      </Button>
                    )}
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={async () => {
                        if (item._isDraft) {
                          const { _isDraft, ...draftData } = item;
                          setSelectedItem(null);
                          setCurrentFormData(draftData);
                          setFormInitialData(draftData);
                          setJsonText(JSON.stringify(draftData, null, 2));
                          setJsonError('');
                          setDialogMode('create');
                          setIsDialogOpen(true);
                        } else {
                          // For auth schemes (OAuth2, OIDC, Custom), fetch full details
                          const isAuthScheme = resourceName === 'OAuth2' || resourceName === 'OIDC' || resourceName === 'Custom';
                          
                          if (isAuthScheme && item.id) {
                            try {
                              // Fetch full details from individual endpoint
                              const fullItem = await apiClient.request<any>(`${endpoint}/${item.id}`);
                              setSelectedItem(fullItem);
                              setCurrentFormData(fullItem);
                              setFormInitialData(fullItem);
                              setJsonText(JSON.stringify(fullItem, null, 2));
                              setJsonError('');
                              setDialogMode('edit');
                              // Increment key to force form re-render with new data
                              setFormResetKey(prev => prev + 1);
                              setIsDialogOpen(true);
                            } catch (error) {
                              console.error('Failed to fetch full item details:', error);
                              toast({
                                title: 'Error',
                                description: 'Failed to load full item details for editing.',
                                variant: 'destructive',
                              });
                            }
                          } else {
                            // For other resources, use item from list directly
                            setSelectedItem(item);
                            setCurrentFormData(item);
                            setFormInitialData(item);
                            setJsonText(JSON.stringify(item, null, 2));
                            setJsonError('');
                            setDialogMode('edit');
                            setIsDialogOpen(true);
                          }
                        }
                      }}
                      data-testid={`button-edit-${idx}`}
                    >
                      <Pencil className="w-4 h-4" />
                    </Button>
                    {!item._isDraft && (
                      <Button
                        variant="ghost"
                        size="icon"
                        onClick={async () => {
                          const { id, ...itemDataWithoutId } = item;
                          
                          // For Applications, fetch and include configurations
                          if (resourceName === 'Applications' && id) {
                            try {
                              // First, get the list of configurations (base fields only)
                              const configsResponse = await apiClient.request<any>(`/api/rest/application/${id}/configuration`);
                              const configs = Array.isArray(configsResponse) 
                                ? configsResponse 
                                : (configsResponse?.objects || []);
                              
                              // Fetch full details for each configuration
                              const configsToCopy = [];
                              for (const config of configs) {
                                try {
                                  // Use type or @class field from list response to determine endpoint
                                  let configType = null;
                                  if (config['@class']) {
                                    configType = detectConfigType(config);
                                  } else if (config.type) {
                                    const className = config.type;
                                    if (className.includes('FacebookApplicationConfiguration')) configType = 'Facebook';
                                    else if (className.includes('FirebaseApplicationConfiguration')) configType = 'Firebase';
                                    else if (className.includes('GooglePlayApplicationConfiguration')) configType = 'GooglePlay';
                                    else if (className.includes('IosApplicationConfiguration')) configType = 'iOS';
                                    else if (className.includes('MatchmakingApplicationConfiguration')) configType = 'Matchmaking';
                                  }
                                  
                                  if (!configType) {
                                    console.warn('Could not detect type for config:', config);
                                    continue;
                                  }
                                  
                                  // Get the type-specific endpoint
                                  const typeEndpoint = getConfigTypeEndpoint(configType);
                                  
                                  // Fetch full configuration details
                                  const fullConfig = await apiClient.request<any>(
                                    `/api/rest/application/${id}/configuration/${typeEndpoint}/${config.id}`
                                  );
                                  
                                  // Remove fields that shouldn't be copied
                                  const { id: configId, parent, type, ...configData } = fullConfig;
                                  configsToCopy.push(configData);
                                } catch (error) {
                                  console.error('Failed to fetch full config details:', error);
                                }
                              }
                              
                              // Add configurations to the draft
                              if (configsToCopy.length > 0) {
                                itemDataWithoutId._configurations = configsToCopy;
                              }
                            } catch (error) {
                              console.error('Failed to fetch configurations:', error);
                            }
                          }
                          
                          saveDraft(itemDataWithoutId);
                          setSelectedItem(null);
                          setCurrentFormData(itemDataWithoutId);
                          setFormInitialData(itemDataWithoutId);
                          setJsonText(JSON.stringify(itemDataWithoutId, null, 2));
                          setJsonError('');
                          setDialogMode('create');
                          setIsDialogOpen(true);
                          toast({
                            title: 'Item Duplicated',
                            description: resourceName === 'Applications' 
                              ? 'A draft has been created with the copied data and configurations.' 
                              : 'A draft has been created with the copied data.',
                          });
                        }}
                        data-testid={`button-duplicate-${idx}`}
                      >
                        <Copy className="w-4 h-4" />
                      </Button>
                    )}
                    <Button
                      variant="ghost"
                      size="icon"
                      onClick={() => {
                        if (item._isDraft) {
                          deleteDraft();
                          toast({
                            title: 'Draft Deleted',
                            description: 'The draft has been removed.',
                          });
                        } else {
                          handleDelete(item);
                        }
                      }}
                      data-testid={`button-delete-${idx}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="text-center py-12">
              <p className="text-muted-foreground mb-4">
                No {resourceName.toLowerCase()} found
              </p>
              <Button 
                onClick={() => {
                  setSelectedItem(null);
                  setCurrentFormData(null);
                  setFormInitialData(undefined);
                  setJsonText('{}');
                  setJsonError('');
                  setDialogMode('create');
                  setIsDialogOpen(true);
                }}
                data-testid="button-create-first"
              >
                <Plus className="w-4 h-4 mr-2" />
                Create your first {resourceName === 'Custom' 
                  ? 'custom auth scheme' 
                  : resourceName.slice(0, -1).toLowerCase()}
              </Button>
            </div>
          )}
          
          {paginationInfo && paginationInfo.total > count && (
            <div className="flex items-center justify-between pt-4 mt-4 border-t">
              <div className="text-sm text-muted-foreground">
                Showing {offset + 1}-{Math.min(offset + count, paginationInfo.total)} of {paginationInfo.total}
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setOffset(Math.max(0, offset - count))}
                  disabled={offset === 0}
                  data-testid="button-prev-page"
                >
                  <ChevronLeft className="w-4 h-4" />
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setOffset(offset + count)}
                  disabled={offset + count >= paginationInfo.total}
                  data-testid="button-next-page"
                >
                  Next
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Dialog open={isDialogOpen} onOpenChange={(open) => open ? setIsDialogOpen(true) : closeDialog()}>
        <DialogContent className="max-w-5xl max-h-[90vh] flex flex-col" data-testid={`dialog-${dialogMode}`}>
          <DialogHeader>
            <DialogTitle>
              {dialogMode === 'delete' ? 'Confirm Delete' : 
               dialogMode === 'create' ? (
                 resourceName === 'OAuth2' ? 'Create OAuth2 Auth Scheme' :
                 resourceName === 'OIDC' ? 'Create OIDC Auth Scheme' :
                 resourceName === 'Custom' ? 'Create Custom Auth Scheme' :
                 resourceName === 'Metadata Spec' ? 'Create Metadata Spec' :
                 resourceName === 'Metadata' ? 'Create Metadata' :
                 `Create ${resourceName.slice(0, -1)}`
               ) : (
                 resourceName === 'OAuth2' ? 'Edit OAuth2 Auth Scheme' :
                 resourceName === 'OIDC' ? 'Edit OIDC Auth Scheme' :
                 resourceName === 'Custom' ? 'Edit Custom Auth Scheme' :
                 resourceName === 'Metadata Spec' ? 'Edit Metadata Spec' :
                 resourceName === 'Metadata' ? 'Edit Metadata' :
                 `Edit ${resourceName.slice(0, -1)}`
               )}
            </DialogTitle>
            <DialogDescription>
              {dialogMode === 'delete' 
                ? `Are you sure you want to delete this ${
                    resourceName === 'OAuth2' ? 'OAuth2 auth scheme' :
                    resourceName === 'OIDC' ? 'OIDC auth scheme' :
                    resourceName === 'Custom' ? 'custom auth scheme' :
                    resourceName === 'Metadata Spec' ? 'metadata spec' :
                    resourceName.slice(0, -1).toLowerCase()
                  }? This action cannot be undone.`
                : dialogMode === 'create'
                ? `Add a new ${
                    resourceName === 'OAuth2' ? 'OAuth2 auth scheme' :
                    resourceName === 'OIDC' ? 'OIDC auth scheme' :
                    resourceName === 'Custom' ? 'custom auth scheme' :
                    resourceName === 'Metadata Spec' ? 'metadata object' :
                    resourceName.slice(0, -1).toLowerCase()
                  } to your Elements application.`
                : resourceName === 'OAuth2'
                ? 'Update the OAuth2 auth scheme details.'
                : resourceName === 'OIDC'
                ? 'Update the OIDC auth scheme details.'
                : resourceName === 'Custom'
                ? 'Update the custom auth scheme details.'
                : resourceName === 'Metadata Spec'
                ? 'Update the metadata spec details.'
                : resourceName === 'Metadata'
                ? 'Update the metadata details.'
                : `Update the ${resourceName.slice(0, -1).toLowerCase()} details.`}
            </DialogDescription>
          </DialogHeader>
          
          {dialogMode === 'delete' ? (
            <div className="flex gap-3 justify-end mt-4">
              <Button
                variant="outline"
                onClick={() => closeDialog()}
                data-testid="button-cancel-delete"
              >
                Cancel
              </Button>
              <Button
                variant="destructive"
                onClick={confirmDelete}
                disabled={deleteMutation.isPending}
                data-testid="button-confirm-delete"
              >
                {deleteMutation.isPending ? 'Deleting...' : 'Delete'}
              </Button>
            </div>
          ) : (
            <Tabs value={viewMode} onValueChange={(v) => {
              const newMode = v as 'form' | 'json';
              
              // When switching to JSON tab, update jsonText with cleaned current form data
              if (newMode === 'json' && currentFormData) {
                const cleaned = cleanDataForSubmission(currentFormData);
                setJsonText(JSON.stringify(cleaned, null, 2));
                setJsonError('');
              }
              
              // When switching to Form tab, parse JSON and convert to form-friendly format
              if (newMode === 'form' && jsonText) {
                try {
                  const parsed = JSON.parse(jsonText);
                  const formFriendly = convertJsonToFormData(parsed);
                  setCurrentFormData(formFriendly);
                  setFormInitialData(formFriendly);
                  setJsonError('');
                  // Increment key to force form re-render with new data
                  setFormResetKey(prev => prev + 1);
                } catch (error) {
                  // If JSON is invalid, keep current data
                  console.error('Invalid JSON:', error);
                }
              }
              
              setViewMode(newMode);
            }} className="mt-4 flex flex-col flex-1 overflow-hidden">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="form" data-testid="tab-form">
                  <FormInput className="w-4 h-4 mr-2" />
                  Guided Form
                </TabsTrigger>
                <TabsTrigger value="json" data-testid="tab-json">
                  <FileJson className="w-4 h-4 mr-2" />
                  JSON Editor
                </TabsTrigger>
              </TabsList>
              
              <TabsContent value="form" className="mt-4 overflow-y-auto flex-1">
                <DynamicResourceForm
                  key={formResetKey}
                  resourceName={resourceName}
                  mode={dialogMode === 'create' ? 'create' : 'update'}
                  initialData={formInitialData}
                  onSubmit={async (data) => {
                    await saveMutation.mutateAsync(data);
                  }}
                  onCancel={closeDialog}
                  onFormChange={(data) => {
                    setCurrentFormData(data);
                  }}
                />
              </TabsContent>
              
              <TabsContent value="json" className="mt-4 overflow-y-auto flex-1">
                <div className="space-y-4">
                  <div className="space-y-3">
                    <p className="text-sm text-muted-foreground">
                      Edit the JSON data below:
                    </p>
                    <textarea
                      className="w-full min-h-[200px] p-3 rounded-md border bg-background font-mono text-sm"
                      value={jsonText}
                      onChange={(e) => {
                        setJsonText(e.target.value);
                        
                        // Validate on every keystroke
                        try {
                          const parsed = JSON.parse(e.target.value);
                          setJsonError('');
                          
                          // Validate against schema
                          const schemaErrors = validateJsonAgainstSchema(parsed);
                          setValidationErrors(schemaErrors);
                        } catch (error) {
                          setJsonError(error instanceof Error ? error.message : 'Invalid JSON');
                          setValidationErrors([]);
                        }
                      }}
                      data-testid="input-json-editor"
                    />
                    {jsonError && (
                      <p className="text-sm text-destructive" data-testid="text-json-error">
                        JSON Syntax Error: {jsonError}
                      </p>
                    )}
                    {validationErrors.length > 0 && (
                      <div className="text-sm text-destructive space-y-1" data-testid="text-validation-errors">
                        <p className="font-medium">Validation Errors:</p>
                        <ul className="list-disc list-inside pl-2">
                          {validationErrors.map((err, idx) => (
                            <li key={idx}>{err}</li>
                          ))}
                        </ul>
                      </div>
                    )}
                  </div>
                  <div className="flex gap-3 justify-end">
                    <Button
                      variant="outline"
                      onClick={() => closeDialog()}
                      data-testid="button-cancel-save"
                    >
                      Cancel
                    </Button>
                    <Button
                      onClick={() => {
                        // Validate JSON syntax and schema before saving
                        try {
                          const parsed = JSON.parse(jsonText);
                          setJsonError('');
                          
                          // Validate against schema
                          const schemaErrors = validateJsonAgainstSchema(parsed);
                          if (schemaErrors.length > 0) {
                            setValidationErrors(schemaErrors);
                            return;
                          }
                          
                          setValidationErrors([]);
                          setSelectedItem(parsed);
                          saveMutation.mutate(parsed);
                        } catch (error) {
                          setJsonError(error instanceof Error ? error.message : 'Invalid JSON');
                          setValidationErrors([]);
                        }
                      }}
                      disabled={saveMutation.isPending || !!jsonError || validationErrors.length > 0}
                      data-testid="button-confirm-save"
                    >
                      {saveMutation.isPending ? 'Saving...' : dialogMode === 'create' ? 'Create' : 'Update'}
                    </Button>
                  </div>
                </div>
              </TabsContent>
            </Tabs>
          )}
        </DialogContent>
      </Dialog>

      {showEventsEditor && selectedSchedule && (
        <Dialog open={showEventsEditor} onOpenChange={setShowEventsEditor}>
          <DialogContent className="max-w-6xl max-h-[90vh] p-0 overflow-hidden">
            <ScheduleEventsEditor
              schedule={selectedSchedule}
              onClose={() => {
                setShowEventsEditor(false);
                setSelectedSchedule(null);
              }}
            />
          </DialogContent>
        </Dialog>
      )}

      {inventoryOpen && selectedUser && (
        <Dialog open={inventoryOpen} onOpenChange={(open) => {
          setInventoryOpen(open);
          if (!open) setSelectedUser(null);
        }}>
          <DialogContent className="max-w-6xl max-h-[90vh]">
            <DialogHeader>
              <DialogTitle>Inventory for {selectedUser.name || selectedUser.id}</DialogTitle>
            </DialogHeader>
            <InventoryViewer userId={selectedUser.id} username={selectedUser.name || selectedUser.id} />
          </DialogContent>
        </Dialog>
      )}
    </div>
  );
}

// Helper functions for configuration copying
function detectConfigType(config: any): string | null {
  if (!config) return null;
  
  // First check for @class field
  if (config['@class']) {
    const className = config['@class'];
    if (className.includes('FacebookApplicationConfiguration')) return 'Facebook';
    if (className.includes('FirebaseApplicationConfiguration')) return 'Firebase';
    if (className.includes('GooglePlayApplicationConfiguration')) return 'GooglePlay';
    if (className.includes('IosApplicationConfiguration')) return 'iOS';
    if (className.includes('MatchmakingApplicationConfiguration')) return 'Matchmaking';
  }
  
  // Fallback: Check for type-specific fields
  if (config.applicationId && config.applicationSecret) return 'Facebook';
  if (config.projectId && config.serviceAccountCredentials) return 'Firebase';
  if (config.jsonKey !== undefined) return 'GooglePlay'; // jsonKey can be empty object
  if (config.applicationId?.includes('.') && config.productBundles !== undefined) return 'iOS';
  if (config.maxProfiles !== undefined || config.matchmaker !== undefined) return 'Matchmaking';
  
  return null;
}

function getConfigTypeEndpoint(type: string): string {
  const typeMap: Record<string, string> = {
    'Facebook': 'facebook',
    'Firebase': 'firebase',
    'GooglePlay': 'google_play',
    'iOS': 'ios',
    'Matchmaking': 'matchmaking',
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
  };
  return classMap[type] || '';
}
