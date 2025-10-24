import { useState, useEffect } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { queryClient, apiRequest } from '@/lib/queryClient';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from '@/components/ui/dialog';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from '@/components/ui/alert-dialog';
import { useToast } from '@/hooks/use-toast';
import { Badge } from '@/components/ui/badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { ResourceSearchDialog } from '@/components/ResourceSearchDialog';
import { Loader2, Plus, Trash2, Edit, Search, ChevronLeft, ChevronRight } from 'lucide-react';

interface MultiMatch {
  id?: string;
  status: 'OPEN' | 'FULL' | 'CLOSED' | 'ENDED';
  configuration: any;
  metadata?: Record<string, any>;
  count?: number;
  expiry?: number;
  created?: number;
}

interface Pagination<T> {
  content: T[];
  total: number;
  offset: number;
  count: number;
}

const STATUS_COLORS = {
  OPEN: 'bg-green-500/10 text-green-500 border-green-500/20',
  FULL: 'bg-yellow-500/10 text-yellow-500 border-yellow-500/20',
  CLOSED: 'bg-red-500/10 text-red-500 border-red-500/20',
  ENDED: 'bg-gray-500/10 text-gray-500 border-gray-500/20',
};

export default function MultiMatchPage() {
  const { toast } = useToast();
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [editingMatch, setEditingMatch] = useState<MultiMatch | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [offset, setOffset] = useState(0);
  const [count] = useState(() => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  });

  // Form state
  const [status, setStatus] = useState<MultiMatch['status']>('OPEN');
  const [selectedApplicationId, setSelectedApplicationId] = useState('');
  const [selectedConfigurationId, setSelectedConfigurationId] = useState('');
  const [metadataEntries, setMetadataEntries] = useState<Array<{ key: string; value: string }>>([]);
  const [appSearchOpen, setAppSearchOpen] = useState(false);
  const [selectedAppName, setSelectedAppName] = useState<string>('');

  // Fetch all applications
  const { data: applicationsData } = useQuery({
    queryKey: ['/api/rest/application'],
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/proxy/api/rest/application');
      const data = await response.json();
      return Array.isArray(data) ? data : (data.objects || data.content || []);
    },
  });

  // Fetch configurations for selected application
  const { data: configurationsData } = useQuery({
    queryKey: ['/api/rest/application', selectedApplicationId, 'configuration'],
    queryFn: async () => {
      if (!selectedApplicationId) return [];
      const response = await apiRequest('GET', `/api/proxy/api/rest/application/${selectedApplicationId}/configuration`);
      const data = await response.json();
      // Normalize response format: Elements API can return array, {objects: [...]}, or {content: [...]}
      let normalized: any[] = [];
      if (Array.isArray(data)) {
        normalized = data;
      } else if (data && typeof data === 'object') {
        if ('objects' in data) normalized = data.objects || [];
        else if ('content' in data) normalized = data.content || [];
      }
      return normalized;
    },
    enabled: !!selectedApplicationId,
  });

  // Filter for matchmaking configurations only
  const matchmakingConfigs = (configurationsData || []).filter((config: any) => 
    config.type === 'dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration'
  );

  const { data: matchesData, isLoading } = useQuery<Pagination<MultiMatch>>({
    queryKey: ['/api/rest/multi_match', offset, count, searchTerm],
    queryFn: async () => {
      const params = new URLSearchParams({
        offset: offset.toString(),
        count: count.toString(),
      });
      if (searchTerm) {
        params.append('search', searchTerm);
      }
      const response = await apiRequest('GET', `/api/proxy/api/rest/multi_match?${params}`);
      const data = await response.json();
      
      // Normalize response format: Elements API can return either {objects: [...]} or {content: [...]}
      if (data && typeof data === 'object') {
        if ('objects' in data && !('content' in data)) {
          return { ...data, content: data.objects };
        }
      }
      return data;
    },
  });

  const createMutation = useMutation({
    mutationFn: async (data: Partial<MultiMatch>) => {
      const response = await apiRequest('POST', '/api/proxy/api/rest/multi_match', data);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/multi_match'] });
      toast({ title: 'Success', description: 'MultiMatch created successfully' });
      setIsCreateDialogOpen(false);
      resetForm();
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create MultiMatch',
        variant: 'destructive',
      });
    },
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, data }: { id: string; data: Partial<MultiMatch> }) => {
      const response = await apiRequest('PUT', `/api/proxy/api/rest/multi_match/${id}`, data);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/multi_match'] });
      toast({ title: 'Success', description: 'MultiMatch updated successfully' });
      setEditingMatch(null);
      resetForm();
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to update MultiMatch',
        variant: 'destructive',
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiRequest('DELETE', `/api/proxy/api/rest/multi_match/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/multi_match'] });
      toast({ title: 'Success', description: 'MultiMatch deleted successfully' });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete MultiMatch',
        variant: 'destructive',
      });
    },
  });

  const deleteAllMutation = useMutation({
    mutationFn: async () => {
      await apiRequest('DELETE', '/api/proxy/api/rest/multi_match');
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/multi_match'] });
      toast({ title: 'Success', description: 'All MultiMatches deleted successfully' });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete all MultiMatches',
        variant: 'destructive',
      });
    },
  });

  const resetForm = () => {
    setStatus('OPEN');
    setSelectedApplicationId('');
    setSelectedConfigurationId('');
    setMetadataEntries([]);
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

  // Reset configuration when application changes
  useEffect(() => {
    setSelectedConfigurationId('');
  }, [selectedApplicationId]);

  const handleCreate = () => {
    if (!selectedConfigurationId) {
      toast({
        title: 'Validation Error',
        description: 'Please select an application and configuration',
        variant: 'destructive',
      });
      return;
    }

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

    const selectedConfig = matchmakingConfigs.find((config: any) => config.id === selectedConfigurationId);
    if (!selectedConfig) {
      toast({
        title: 'Error',
        description: 'Selected configuration not found',
        variant: 'destructive',
      });
      return;
    }

    const data: Partial<MultiMatch> = {
      status,
      configuration: selectedConfig,
      metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
    };

    createMutation.mutate(data);
  };

  const handleUpdate = () => {
    if (!editingMatch?.id) return;

    if (!selectedConfigurationId) {
      toast({
        title: 'Validation Error',
        description: 'Please select an application and configuration',
        variant: 'destructive',
      });
      return;
    }

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

    const selectedConfig = matchmakingConfigs.find((config: any) => config.id === selectedConfigurationId);
    if (!selectedConfig) {
      toast({
        title: 'Error',
        description: 'Selected configuration not found',
        variant: 'destructive',
      });
      return;
    }

    const data: Partial<MultiMatch> = {
      status,
      configuration: selectedConfig,
      metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
    };

    updateMutation.mutate({ id: editingMatch.id, data });
  };

  const startEdit = async (match: MultiMatch) => {
    setEditingMatch(match);
    setStatus(match.status);
    
    // Convert metadata object to entries
    setMetadataEntries(
      match.metadata ? Object.entries(match.metadata).map(([key, value]) => ({ 
        key, 
        value: typeof value === 'string' ? value : JSON.stringify(value) 
      })) : []
    );
    
    // If we have a configuration, we need to find which application it belongs to
    if (match.configuration) {
      // Search through all applications to find the one with this configuration
      const apps = applicationsData || [];
      for (const app of apps) {
        try {
          const response = await apiRequest('GET', `/api/proxy/api/rest/application/${app.id}/configuration`);
          const configs = await response.json();
          // Normalize response format
          let configsArray: any[] = [];
          if (Array.isArray(configs)) {
            configsArray = configs;
          } else if (configs && typeof configs === 'object') {
            if ('objects' in configs) configsArray = configs.objects || [];
            else if ('content' in configs) configsArray = configs.content || [];
          }
          
          if (configsArray.some((config: any) => config.id === match.configuration.id)) {
            setSelectedApplicationId(app.id);
            // Set configuration after a brief delay to ensure query has run
            setTimeout(() => {
              setSelectedConfigurationId(match.configuration.id);
            }, 100);
            break;
          }
        } catch (e) {
          console.error('Failed to fetch configurations for app', app.id, e);
        }
      }
    }
  };

  const formatTimestamp = (timestamp?: number) => {
    if (!timestamp) return 'N/A';
    return new Date(timestamp).toLocaleString();
  };

  const totalPages = Math.ceil((matchesData?.total || 0) / count);
  const currentPage = Math.floor(offset / count) + 1;

  return (
    <div className="h-full flex flex-col p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold" data-testid="text-page-title">Matchmaking</h1>
          <p className="text-muted-foreground mt-1">Manage multiplayer matches and matchmaking</p>
        </div>
        <div className="flex items-center gap-2">
          <AlertDialog>
            <AlertDialogTrigger asChild>
              <Button variant="destructive" data-testid="button-delete-all">
                <Trash2 className="w-4 h-4 mr-2" />
                Delete All
              </Button>
            </AlertDialogTrigger>
            <AlertDialogContent>
              <AlertDialogHeader>
                <AlertDialogTitle>Delete All Matches</AlertDialogTitle>
                <AlertDialogDescription>
                  This will delete all matches. This action cannot be undone. Proceed?
                </AlertDialogDescription>
              </AlertDialogHeader>
              <AlertDialogFooter>
                <AlertDialogCancel data-testid="button-cancel-delete-all">Cancel</AlertDialogCancel>
                <AlertDialogAction
                  onClick={() => deleteAllMutation.mutate()}
                  data-testid="button-confirm-delete-all"
                  className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                >
                  Delete All
                </AlertDialogAction>
              </AlertDialogFooter>
            </AlertDialogContent>
          </AlertDialog>
          <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
            <DialogTrigger asChild>
              <Button data-testid="button-create-match" onClick={resetForm}>
                <Plus className="w-4 h-4 mr-2" />
                Create Match
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>Create New MultiMatch</DialogTitle>
              </DialogHeader>
              <div className="space-y-4">
                <div>
                  <Label htmlFor="status">Status</Label>
                  <Select value={status} onValueChange={(val) => setStatus(val as MultiMatch['status'])}>
                    <SelectTrigger data-testid="select-status">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="OPEN">OPEN</SelectItem>
                      <SelectItem value="FULL">FULL</SelectItem>
                      <SelectItem value="CLOSED">CLOSED</SelectItem>
                      <SelectItem value="ENDED">ENDED</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <Label htmlFor="application">Application *</Label>
                  <div className="space-y-2">
                      <Button
                      type="button"
                      variant="outline"
                      className="w-full justify-between"
                      onClick={() => setAppSearchOpen(true)}
                      data-testid="button-search-application"
                    >
                      <span className="truncate">
                        {selectedAppName || selectedApplicationId || 'Select an application'}
                      </span>
                      <Search className="w-4 h-4 ml-2 flex-shrink-0" />
                    </Button>
                    {selectedApplicationId && (
                      <p className="text-xs text-muted-foreground font-mono">{selectedApplicationId}</p>
                    )}
                  </div>
                </div>

                <ResourceSearchDialog
                  open={appSearchOpen}
                  onOpenChange={setAppSearchOpen}
                  onSelect={(appId, app) => {
                    setSelectedApplicationId(appId);
                    setSelectedAppName(app.name || app.id);
                    setSelectedConfigurationId(''); // Reset configuration when app changes
                  }}
                  resourceType="application"
                  endpoint="/api/rest/application"
                  title="Search Applications"
                  description="Search for an application to use for matchmaking"
                  displayFields={[
                    { label: 'Name', key: 'name' },
                    { label: 'ID', key: 'id' },
                  ]}
                  searchPlaceholder="Search by name or ID..."
                  currentResourceId={selectedApplicationId}
                />

                <div>
                  <Label htmlFor="configuration">Matchmaking Configuration *</Label>
                    <Select 
                    value={selectedConfigurationId} 
                    onValueChange={setSelectedConfigurationId}
                    disabled={!selectedApplicationId || matchmakingConfigs.length === 0}
                  >
                    <SelectTrigger data-testid="select-configuration">
                      <SelectValue placeholder={
                        !selectedApplicationId 
                          ? "Select an application first" 
                          : matchmakingConfigs.length === 0 
                            ? "No matchmaking configs found" 
                            : "Select a configuration"
                      } />
                    </SelectTrigger>
                    <SelectContent>
                      {matchmakingConfigs.map((config: any) => (
                        <SelectItem key={config.id} value={config.id}>
                          {config.name || config.id} {config.name && `(ID: ${config.id})`}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

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
                      <p className="text-sm text-muted-foreground">No metadata entries. Click "Add Entry" to add key-value pairs.</p>
                    )}
                  </div>
                </div>

              </div>
              <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setIsCreateDialogOpen(false)}
                data-testid="button-cancel"
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreate}
                disabled={createMutation.isPending}
                data-testid="button-submit-create"
              >
                {createMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                Create
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>
    </div>

    <div className="flex items-center gap-4">
      <div className="flex-1 relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <Input
          placeholder="Search matches..."
          value={searchTerm}
          onChange={(e) => {
            setSearchTerm(e.target.value);
            setOffset(0);
          }}
          className="pl-10"
          data-testid="input-search"
        />
      </div>
    </div>

    {isLoading ? (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      </div>
    ) : (
      <>
        <div className="grid gap-4">
          {matchesData?.content?.map((match) => (
            <Card key={match.id} className="hover-elevate">
              <CardHeader>
                <div className="flex items-start justify-between">
                  <div className="space-y-1 flex-1">
                    <div className="flex items-center gap-2">
                      <CardTitle className="text-base font-mono" data-testid={`text-match-id-${match.id}`}>
                        {match.id}
                      </CardTitle>
                      <Badge className={STATUS_COLORS[match.status]} data-testid={`badge-status-${match.id}`}>
                        {match.status}
                      </Badge>
                    </div>
                    <div className="text-sm text-muted-foreground space-y-1">
                      <div className="grid grid-cols-2 gap-x-8">
                        <div>
                          {match.configuration && (
                            <div>Config: <span className="font-semibold">{match.configuration.name || match.configuration.id}</span></div>
                          )}
                        </div>
                        <div>Created: {formatTimestamp(match.created)}</div>
                      </div>
                      <div className="grid grid-cols-2 gap-x-8">
                        <div>Players: <span className="font-semibold">{match.count || 0}</span></div>
                        <div>{match.expiry && <>Expiry: {formatTimestamp(match.expiry)}</>}</div>
                      </div>
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <Button
                      size="icon"
                      variant="ghost"
                      onClick={() => startEdit(match)}
                      data-testid={`button-edit-${match.id}`}
                    >
                      <Edit className="w-4 h-4" />
                    </Button>
                    <Dialog open={editingMatch?.id === match.id} onOpenChange={(open) => { if (!open) setEditingMatch(null); }}>
                      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                        <DialogHeader>
                          <DialogTitle>Edit MultiMatch</DialogTitle>
                        </DialogHeader>
                        <div className="space-y-4">
                          <div>
                            <Label htmlFor="edit-status">Status</Label>
                            <Select value={status} onValueChange={(val) => setStatus(val as MultiMatch['status'])}>
                              <SelectTrigger data-testid="select-edit-status">
                                <SelectValue />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="OPEN">OPEN</SelectItem>
                                <SelectItem value="FULL">FULL</SelectItem>
                                <SelectItem value="CLOSED">CLOSED</SelectItem>
                                <SelectItem value="ENDED">ENDED</SelectItem>
                              </SelectContent>
                            </Select>
                          </div>

                          <div>
                            <Label htmlFor="edit-application">Application *</Label>
                            <div className="space-y-2">
                              <Button
                                type="button"
                                variant="outline"
                                className="w-full justify-between"
                                onClick={() => setAppSearchOpen(true)}
                                data-testid="button-search-edit-application"
                              >
                                <span className="truncate">
                                  {selectedAppName || selectedApplicationId || 'Select an application'}
                                </span>
                                <Search className="w-4 h-4 ml-2 flex-shrink-0" />
                              </Button>
                              {selectedApplicationId && (
                                <p className="text-xs text-muted-foreground font-mono">{selectedApplicationId}</p>
                              )}
                            </div>
                          </div>

                          <div>
                            <Label htmlFor="edit-configuration">Matchmaking Configuration *</Label>
                            <Select 
                              value={selectedConfigurationId} 
                              onValueChange={setSelectedConfigurationId}
                              disabled={!selectedApplicationId || matchmakingConfigs.length === 0}
                            >
                              <SelectTrigger data-testid="select-edit-configuration">
                                <SelectValue placeholder={
                                  !selectedApplicationId 
                                    ? "Select an application first" 
                                    : matchmakingConfigs.length === 0 
                                      ? "No matchmaking configs found" 
                                      : "Select a configuration"
                                } />
                              </SelectTrigger>
                              <SelectContent>
                                {matchmakingConfigs.map((config: any) => (
                                  <SelectItem key={config.id} value={config.id}>
                                    {config.name || config.id} {config.name && `(ID: ${config.id})`}
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                          </div>

                          <div>
                            <div className="flex items-center justify-between mb-2">
                              <Label>Metadata (Optional)</Label>
                              <Button
                                type="button"
                                variant="outline"
                                size="sm"
                                onClick={addMetadataEntry}
                                data-testid="button-edit-add-metadata"
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
                                    data-testid={`input-edit-metadata-key-${index}`}
                                  />
                                  <Input
                                    placeholder="Value (JSON or text)"
                                    value={entry.value}
                                    onChange={(e) => updateMetadataEntry(index, 'value', e.target.value)}
                                    className="flex-1"
                                    data-testid={`input-edit-metadata-value-${index}`}
                                  />
                                  <Button
                                    type="button"
                                    variant="ghost"
                                    size="icon"
                                    onClick={() => removeMetadataEntry(index)}
                                    data-testid={`button-edit-remove-metadata-${index}`}
                                  >
                                    <Trash2 className="w-4 h-4" />
                                  </Button>
                                </div>
                              ))}
                              {metadataEntries.length === 0 && (
                                <p className="text-sm text-muted-foreground">No metadata entries. Click "Add Entry" to add key-value pairs.</p>
                              )}
                            </div>
                          </div>

                        </div>
                        <DialogFooter>
                          <Button
                            variant="outline"
                            onClick={() => setEditingMatch(null)}
                            data-testid="button-cancel-edit"
                          >
                            Cancel
                          </Button>
                          <Button
                            onClick={handleUpdate}
                            disabled={updateMutation.isPending}
                            data-testid="button-submit-update"
                          >
                            {updateMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                            Update
                          </Button>
                        </DialogFooter>
                      </DialogContent>
                    </Dialog>
                    <Button
                      size="icon"
                      variant="ghost"
                      onClick={() => match.id && deleteMutation.mutate(match.id)}
                      disabled={deleteMutation.isPending}
                      data-testid={`button-delete-${match.id}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </CardHeader>
            </Card>
          ))}
        </div>

        {matchesData && matchesData.content?.length === 0 && (
          <div className="text-center py-12 text-muted-foreground">
            No matches found
          </div>
        )}

        {totalPages > 1 && (
          <div className="flex items-center justify-between">
            <div className="text-sm text-muted-foreground">
              Page {currentPage} of {totalPages} ({matchesData?.total || 0} total)
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
                disabled={offset + count >= (matchesData?.total || 0)}
                data-testid="button-next-page"
              >
                Next
                <ChevronRight className="w-4 h-4" />
              </Button>
            </div>
          </div>
        )}
      </>
    )}
  </div>
  );
}
