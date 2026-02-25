import { useState, useRef, useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter } from '@/components/ui/dialog';
import { Checkbox } from '@/components/ui/checkbox';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useToast } from '@/hooks/use-toast';
import { apiClient, getApiPath } from '@/lib/api-client';
import { Loader2, Plus, Pencil, Trash2, Search, Rocket, ChevronLeft, ChevronRight, ChevronDown, RefreshCw, X, Upload, HardDrive, Package, Database, Check, Sparkles, FileText } from 'lucide-react';

interface ElementArtifactRepository {
  id: string;
  url: string;
}

interface ElementPathDefinition {
  path: string;
  apiArtifacts?: string[];
  spiBuiltins?: string[];
  spiArtifacts?: string[];
  elementArtifacts?: string[];
  attributes?: Record<string, any>;
}

interface ElementPackageDefinition {
  elmArtifact: string;
  pathSpiBuiltins?: Record<string, string[]>;
  pathSpiClassPaths?: Record<string, string[]>;
  pathAttributes?: Record<string, Record<string, any>>;
}

interface ElementDeployment {
  id: string;
  application?: { id: string; name: string; description?: string } | null;
  elm?: any;
  pathSpiBuiltins?: Record<string, string[]>;
  pathSpiClassPaths?: Record<string, string[]>;
  pathAttributes?: Record<string, Record<string, any>>;
  elements?: ElementPathDefinition[];
  packages?: ElementPackageDefinition[];
  useDefaultRepositories: boolean;
  repositories?: ElementArtifactRepository[];
  state: 'UNLOADED' | 'ENABLED' | 'DISABLED';
  version: number;
}

interface PaginatedResponse {
  objects: ElementDeployment[];
  total: number;
}

interface FormData {
  state: string;
  appNameOrId: string;
  useDefaultRepositories: boolean;
  elements: ElementPathDefinition[];
  packages: ElementPackageDefinition[];
  repositories: ElementArtifactRepository[];
  pathAttributes: Record<string, Record<string, any>>;
  pathSpiBuiltins: Record<string, string[]>;
}

const DEPLOYMENT_STATES_EDIT = ['ENABLED', 'DISABLED'] as const;

interface ElementSpi {
  id: string;
  description?: string;
}

interface ElmInspectorRecord {
  path: string;
  attributes: Record<string, unknown>;
  manifest?: {
    builtinSpis?: string[];
  };
}

function getStateBadgeVariant(state: string): 'default' | 'secondary' | 'outline' | 'destructive' {
  switch (state) {
    case 'ENABLED': return 'default';
    case 'DISABLED': return 'secondary';
    case 'UNLOADED': return 'outline';
    default: return 'outline';
  }
}

function getStateColor(state: string): string {
  switch (state) {
    case 'ENABLED': return 'bg-green-500';
    case 'DISABLED': return 'bg-orange-500';
    case 'UNLOADED': return 'bg-gray-500';
    default: return 'bg-gray-500';
  }
}

const emptyFormData: FormData = {
  state: 'ENABLED',
  appNameOrId: '',
  useDefaultRepositories: true,
  elements: [],
  packages: [],
  repositories: [],
  pathAttributes: {},
  pathSpiBuiltins: {},
};

function deploymentToFormData(d: ElementDeployment): FormData {
  return {
    state: d.state || 'ENABLED',
    appNameOrId: '',
    useDefaultRepositories: d.useDefaultRepositories ?? true,
    elements: (d.elements || []).map(e => ({
      path: e.path || '',
      apiArtifacts: e.apiArtifacts || [],
      spiBuiltins: Array.isArray(e.spiBuiltins) ? e.spiBuiltins : (e.spiBuiltins ? [e.spiBuiltins] : []),
      spiArtifacts: e.spiArtifacts || [],
      elementArtifacts: e.elementArtifacts || [],
      attributes: e.attributes || {},
    })),
    packages: (d.packages || []).map(p => ({
      elmArtifact: p.elmArtifact || '',
      pathSpiBuiltins: p.pathSpiBuiltins || {},
      pathSpiClassPaths: p.pathSpiClassPaths || {},
      pathAttributes: p.pathAttributes || {},
    })),
    repositories: (d.repositories || []).map(r => ({
      id: r.id || '',
      url: r.url || '',
    })),
    pathAttributes: d.pathAttributes || {},
    pathSpiBuiltins: d.pathSpiBuiltins || {},
  };
}

function ElmDropZone({
  onFile,
  uploading,
  buttonLabel,
  testIdPrefix,
  fileName: externalFileName,
}: {
  onFile: (file: File) => void;
  uploading: boolean;
  buttonLabel?: string;
  testIdPrefix: string;
  fileName?: string;
}) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [dragOver, setDragOver] = useState(false);
  const [internalFileName, setInternalFileName] = useState<string | null>(null);
  const displayFileName = internalFileName ?? externalFileName ?? null;

  const handleFile = useCallback((file: File) => {
    setInternalFileName(file.name);
    onFile(file);
  }, [onFile]);

  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragOver(false);
    const file = e.dataTransfer.files?.[0];
    if (file && !uploading) {
      handleFile(file);
    }
  }, [handleFile, uploading]);

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!uploading) setDragOver(true);
  }, [uploading]);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragOver(false);
  }, []);

  const hasFile = !!displayFileName && !uploading;

  return (
    <div
      onDrop={handleDrop}
      onDragOver={handleDragOver}
      onDragLeave={handleDragLeave}
      className={`flex flex-col items-center justify-center gap-3 rounded-md border-2 border-dashed p-6 transition-colors ${
        dragOver ? 'border-primary bg-primary/5' :
        hasFile ? 'border-primary/40 bg-primary/5' :
        'border-muted-foreground/25'
      } ${uploading ? 'opacity-60 pointer-events-none' : ''}`}
      data-testid={`${testIdPrefix}-dropzone`}
    >
      <input
        ref={fileInputRef}
        type="file"
        accept=".elm"
        onChange={(e) => {
          const file = e.target.files?.[0];
          if (file) handleFile(file);
          if (fileInputRef.current) fileInputRef.current.value = '';
        }}
        className="hidden"
        data-testid={`${testIdPrefix}-input`}
      />
      {uploading ? (
        <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
      ) : hasFile ? (
        <FileText className="w-8 h-8 text-primary" />
      ) : (
        <Upload className="w-8 h-8 text-muted-foreground" />
      )}
      <div className="text-center space-y-1">
        <p
          className={`text-sm font-medium truncate max-w-sm ${hasFile ? 'text-primary' : ''}`}
          title={displayFileName ?? undefined}
        >
          {uploading ? 'Uploading...' : displayFileName ?? 'Drag and drop your .elm file here'}
        </p>
        <p className="text-xs text-muted-foreground">
          {hasFile ? 'Drag and drop to replace, or' : 'or'}
        </p>
      </div>
      <Button
        variant="outline"
        size="sm"
        onClick={() => fileInputRef.current?.click()}
        disabled={uploading}
        data-testid={`${testIdPrefix}-button`}
      >
        {buttonLabel || (hasFile ? 'Replace File' : 'Browse Files')}
      </Button>
    </div>
  );
}

export default function ElementDeployments() {
  const { toast } = useToast();
  const queryClient = useQueryClient();
  const [offset, setOffset] = useState(0);
  const [count] = useState(20);
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedDeployment, setSelectedDeployment] = useState<ElementDeployment | null>(null);
  const [formData, setFormData] = useState<FormData>({ ...emptyFormData });
  const [wizardStep, setWizardStep] = useState(0);
  const [elmUploadDialogOpen, setElmUploadDialogOpen] = useState(false);
  const [elmUploadTarget, setElmUploadTarget] = useState<{ deploymentId: string; elm: any } | null>(null);
  const [wizardCreatedDeployment, setWizardCreatedDeployment] = useState<{ deploymentId: string; elmId?: string } | null>(null);
  const [wizardElmFile, setWizardElmFile] = useState<File | null>(null);
  const [featuresDialogOpen, setFeaturesDialogOpen] = useState(false);

  const { data, isLoading, error } = useQuery<PaginatedResponse>({
    queryKey: ['/api/rest/elements/deployment', { offset, count, search }],
    queryFn: async () => {
      const params = new URLSearchParams({ offset: String(offset), count: String(count) });
      if (search) params.set('search', search);
      return apiClient.request<PaginatedResponse>(`/api/rest/elements/deployment?${params.toString()}`);
    },
  });

  const createMutation = useMutation({
    mutationFn: async (body: any) => {
      return apiClient.request<ElementDeployment>('/api/rest/elements/deployment', {
        method: 'POST',
        body: JSON.stringify(body),
      });
    },
    onSuccess: (created) => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/deployment'] });
      setWizardCreatedDeployment({
        deploymentId: created.id,
        elmId: created?.elm?.id,
      });
      setWizardStep(3);
      if (wizardElmFile && created?.elm?.id) {
        elmUploadMutation.mutate({ largeObjectId: created.elm.id, file: wizardElmFile });
      }
      toast({
        title: 'Deployment Created',
        description: wizardElmFile && created?.elm?.id
          ? 'Uploading the ELM file automatically...'
          : created?.elm?.id
            ? 'You can now upload the .elm file, or skip this step.'
            : 'Deployment created successfully. You can close this dialog.',
      });
    },
    onError: (error: any) => {
      toast({ title: 'Error', description: error.message || 'Failed to create deployment', variant: 'destructive' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, body }: { id: string; body: any }) => {
      return apiClient.request<ElementDeployment>(`/api/rest/elements/deployment/${id}`, {
        method: 'PUT',
        body: JSON.stringify(body),
      });
    },
    onSuccess: () => {
      toast({ title: 'Success', description: 'Deployment updated successfully' });
      setEditDialogOpen(false);
      setSelectedDeployment(null);
      queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/deployment'] });
    },
    onError: (error: any) => {
      toast({ title: 'Error', description: error.message || 'Failed to update deployment', variant: 'destructive' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      return apiClient.request(`/api/rest/elements/deployment/${id}`, { method: 'DELETE' });
    },
    onSuccess: () => {
      toast({ title: 'Success', description: 'Deployment deleted successfully' });
      setDeleteDialogOpen(false);
      setSelectedDeployment(null);
      queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/deployment'] });
    },
    onError: (error: any) => {
      toast({ title: 'Error', description: error.message || 'Failed to delete deployment', variant: 'destructive' });
    },
  });

  const elmUploadMutation = useMutation({
    mutationFn: async ({ largeObjectId, file }: { largeObjectId: string; file: File }) => {
      const formData = new FormData();
      formData.append('file', file);
      const uploadPath = `/api/rest/large_object/${largeObjectId}/content`;
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
        try { return await response.json(); } catch { return null; }
      }
      return null;
    },
    onSuccess: () => {
      toast({ title: 'ELM Uploaded', description: 'The .elm file has been uploaded successfully.' });
      setElmUploadDialogOpen(false);
      setElmUploadTarget(null);
      queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/deployment'] });
    },
    onError: (error: any) => {
      toast({ title: 'Upload Failed', description: error.message || 'Failed to upload .elm file', variant: 'destructive' });
    },
  });

  const handleElmFileUpload = (file: File, largeObjectId: string) => {
    elmUploadMutation.mutate({ largeObjectId, file });
  };

  const openCreateDialog = () => {
    setFormData({ ...emptyFormData, elements: [], packages: [], repositories: [] });
    setWizardStep(0);
    setWizardCreatedDeployment(null);
    setWizardElmFile(null);
    setCreateDialogOpen(true);
  };

  const openEditDialog = (deployment: ElementDeployment) => {
    setSelectedDeployment(deployment);
    setFormData(deploymentToFormData(deployment));
    setEditDialogOpen(true);
  };

  const openDeleteDialog = (deployment: ElementDeployment) => {
    setSelectedDeployment(deployment);
    setDeleteDialogOpen(true);
  };

  const mapElements = (els: ElementPathDefinition[]) => els.map(e => {
    const el: any = {};
    if (e.path && e.path.trim()) el.path = e.path;
    if (e.apiArtifacts && e.apiArtifacts.length > 0) el.apiArtifacts = e.apiArtifacts.filter(a => a.trim());
    if (e.spiBuiltins && e.spiBuiltins.length > 0) el.spiBuiltins = e.spiBuiltins;
    if (e.spiArtifacts && e.spiArtifacts.length > 0) el.spiArtifacts = e.spiArtifacts.filter(a => a.trim());
    if (e.elementArtifacts && e.elementArtifacts.length > 0) el.elementArtifacts = e.elementArtifacts.filter(a => a.trim());
    if (e.attributes && Object.keys(e.attributes).length > 0) el.attributes = e.attributes;
    return el;
  });

  const mapPackages = (pkgs: ElementPackageDefinition[]) => pkgs.map(p => {
    const pk: any = { elmArtifact: p.elmArtifact };
    if (p.pathSpiBuiltins && Object.keys(p.pathSpiBuiltins).length > 0) pk.pathSpiBuiltins = p.pathSpiBuiltins;
    if (p.pathSpiClassPaths && Object.keys(p.pathSpiClassPaths).length > 0) pk.pathSpiClassPaths = p.pathSpiClassPaths;
    if (p.pathAttributes && Object.keys(p.pathAttributes).length > 0) pk.pathAttributes = p.pathAttributes;
    return pk;
  });

  const buildBody = () => {
    const body: any = {
      state: formData.state,
      useDefaultRepositories: formData.useDefaultRepositories,
    };
    if (formData.elements.length > 0) body.elements = mapElements(formData.elements);
    if (formData.packages.length > 0) body.packages = mapPackages(formData.packages);
    body.repositories = formData.repositories.filter(r => r.id.trim() || r.url.trim());
    if (formData.pathAttributes && Object.keys(formData.pathAttributes).length > 0) {
      body.pathAttributes = formData.pathAttributes;
    }
    if (formData.pathSpiBuiltins && Object.keys(formData.pathSpiBuiltins).length > 0) {
      body.pathSpiBuiltins = formData.pathSpiBuiltins;
    }
    return body;
  };

  const handleCreate = () => {
    const body = buildBody();
    body.state = 'ENABLED';
    if (formData.appNameOrId.trim()) {
      body.applicationNameOrId = formData.appNameOrId.trim();
    }
    createMutation.mutate(body);
  };

  const handleUpdate = () => {
    if (!selectedDeployment) return;
    const body = buildBody();
    updateMutation.mutate({ id: selectedDeployment.id, body });
  };

  const handleDelete = () => {
    if (!selectedDeployment) return;
    deleteMutation.mutate(selectedDeployment.id);
  };

  const handleSearch = () => {
    setSearch(searchInput);
    setOffset(0);
  };

  const deployments = data?.objects || [];
  const total = data?.total || 0;
  const hasNext = offset + count < total;
  const hasPrev = offset > 0;
  const formHasErrors = false;

  return (
    <div className="h-full p-6 overflow-y-auto">
      <div className="space-y-4">
        <div className="flex items-center justify-between gap-4 flex-wrap">
          <div>
            <h1 className="text-2xl font-bold" data-testid="text-deployments-title">Element Deployments</h1>
            <p className="text-sm text-muted-foreground mt-1">
              Manage Element deployment configurations
            </p>
          </div>
          <div className="flex items-center gap-2">
            <Button variant="outline" onClick={() => setFeaturesDialogOpen(true)} data-testid="button-view-features">
              <Sparkles className="w-4 h-4 mr-2" />
              Features
            </Button>
            <Button onClick={openCreateDialog} data-testid="button-create-deployment">
              <Plus className="w-4 h-4 mr-2" />
              New Deployment
            </Button>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <div className="relative flex-1 max-w-sm">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search deployments..."
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              className="pl-9"
              data-testid="input-search-deployments"
            />
          </div>
          <Button variant="outline" onClick={handleSearch} data-testid="button-search">
            Search
          </Button>
          <Button
            variant="outline"
            size="icon"
            onClick={() => queryClient.invalidateQueries({ queryKey: ['/api/rest/elements/deployment'] })}
            data-testid="button-refresh-deployments"
          >
            <RefreshCw className="w-4 h-4" />
          </Button>
        </div>

        {isLoading && (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
          </div>
        )}

        {error && (
          <Card className="border-destructive">
            <CardContent className="p-4">
              <p className="text-sm text-destructive" data-testid="text-error">
                {(error as Error).message || 'Failed to load deployments'}
              </p>
            </CardContent>
          </Card>
        )}

        {!isLoading && !error && deployments.length === 0 && (
          <Card>
            <CardContent className="p-8 text-center">
              <Rocket className="w-12 h-12 mx-auto mb-4 text-muted-foreground opacity-50" />
              <p className="text-lg font-medium" data-testid="text-no-deployments">No Deployments Found</p>
              <p className="text-sm text-muted-foreground mt-2">
                {search ? 'Try adjusting your search terms' : 'Create your first deployment to get started'}
              </p>
            </CardContent>
          </Card>
        )}

        {!isLoading && deployments.length > 0 && (
          <div className="space-y-3">
            {deployments.map((deployment) => (
              <Card key={deployment.id} data-testid={`card-deployment-${deployment.id}`}>
                <CardContent className="p-4">
                  <div className="flex items-start justify-between gap-4 flex-wrap">
                    <div className="flex-1 min-w-0 space-y-2">
                      <div className="flex items-center gap-2 flex-wrap">
                        <div className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${getStateColor(deployment.state)}`} />
                        <span className="font-mono text-sm font-medium truncate" data-testid={`text-deployment-id-${deployment.id}`}>
                          {deployment.id}
                        </span>
                        <Badge variant={getStateBadgeVariant(deployment.state)} data-testid={`badge-state-${deployment.id}`}>
                          {deployment.state}
                        </Badge>
                        <Badge variant="outline" className="text-xs">
                          v{deployment.version}
                        </Badge>
                      </div>

                      <div className="flex items-center gap-4 text-xs text-muted-foreground flex-wrap">
                        {deployment.application && (
                          <span data-testid={`text-app-${deployment.id}`}>
                            App: <span className="font-medium text-foreground">{deployment.application.name || deployment.application.id}</span>
                          </span>
                        )}
                        {!deployment.application && (
                          <span className="italic">Global (no application)</span>
                        )}
                        {deployment.elements && deployment.elements.length > 0 && (
                          <span>{deployment.elements.length} element{deployment.elements.length !== 1 ? 's' : ''}</span>
                        )}
                        {deployment.packages && deployment.packages.length > 0 && (
                          <span>{deployment.packages.length} package{deployment.packages.length !== 1 ? 's' : ''}</span>
                        )}
                        {deployment.repositories && deployment.repositories.length > 0 && (
                          <span>{deployment.repositories.length} repo{deployment.repositories.length !== 1 ? 's' : ''}</span>
                        )}
                        {deployment.useDefaultRepositories && (
                          <Badge variant="outline" className="text-[10px]">default repos</Badge>
                        )}
                        {deployment.elm && (
                          <Badge variant="outline" className="text-[10px]">ELM</Badge>
                        )}
                      </div>
                    </div>

                    <div className="flex items-center gap-1">
                      <Button
                        size="icon"
                        variant="ghost"
                        onClick={() => openEditDialog(deployment)}
                        data-testid={`button-edit-${deployment.id}`}
                      >
                        <Pencil className="w-4 h-4" />
                      </Button>
                      <Button
                        size="icon"
                        variant="ghost"
                        onClick={() => openDeleteDialog(deployment)}
                        data-testid={`button-delete-${deployment.id}`}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}

            <div className="flex items-center justify-between pt-2">
              <p className="text-sm text-muted-foreground" data-testid="text-pagination-info">
                Showing {offset + 1}–{Math.min(offset + count, total)} of {total}
              </p>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={!hasPrev}
                  onClick={() => setOffset(Math.max(0, offset - count))}
                  data-testid="button-prev-page"
                >
                  <ChevronLeft className="w-4 h-4 mr-1" />
                  Previous
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={!hasNext}
                  onClick={() => setOffset(offset + count)}
                  data-testid="button-next-page"
                >
                  Next
                  <ChevronRight className="w-4 h-4 ml-1" />
                </Button>
              </div>
            </div>
          </div>
        )}
      </div>

      <Dialog open={createDialogOpen} onOpenChange={(open) => {
        setCreateDialogOpen(open);
        if (!open) {
          setWizardCreatedDeployment(null);
          setWizardElmFile(null);
          setWizardStep(0);
        }
      }}>
        <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create Element Deployment</DialogTitle>
            <DialogDescription>
              {wizardStep === 0 && 'Add element definitions and/or package configurations for your deployment.'}
              {wizardStep === 1 && 'Set common deployment options.'}
              {wizardStep === 2 && 'Review your deployment configuration before creating.'}
              {wizardStep === 3 && 'Your deployment has been created.'}
            </DialogDescription>
          </DialogHeader>

          <div className="flex items-center gap-1 mb-2" data-testid="wizard-stepper">
            {['Configure', 'Settings', 'Review', 'Upload'].map((label, i, arr) => (
              <div key={label} className="flex items-center gap-1 flex-1">
                <div className={`flex items-center justify-center w-6 h-6 rounded-full text-xs font-medium shrink-0 ${
                  i < wizardStep ? 'bg-primary text-primary-foreground' :
                  i === wizardStep ? 'bg-primary text-primary-foreground' :
                  'bg-muted text-muted-foreground'
                }`}>
                  {i < wizardStep ? <Check className="w-3 h-3" /> : i + 1}
                </div>
                <span className={`text-xs truncate ${i === wizardStep ? 'font-medium' : 'text-muted-foreground'}`}>{label}</span>
                {i < arr.length - 1 && <div className={`flex-1 h-px ${i < wizardStep ? 'bg-primary' : 'bg-border'}`} />}
              </div>
            ))}
          </div>

          {wizardStep === 0 && (
            <div className="space-y-4" data-testid="wizard-step-configure">
              <WizardConfigStep
                formData={formData}
                setFormData={setFormData}
                onFileSelected={setWizardElmFile}
              />
            </div>
          )}

          {wizardStep === 1 && (
            <div className="space-y-4" data-testid="wizard-step-settings">
              <WizardSettingsStep
                formData={formData}
                setFormData={setFormData}
              />
            </div>
          )}

          {wizardStep === 2 && (
            <div className="space-y-4" data-testid="wizard-step-review">
              <div className="flex items-center gap-2 mb-2 flex-wrap">
                {formData.elements.length > 0 && (
                  <Badge variant="outline">{formData.elements.length} Element Definition{formData.elements.length !== 1 ? 's' : ''}</Badge>
                )}
                {formData.packages.length > 0 && (
                  <Badge variant="outline">{formData.packages.length} Package{formData.packages.length !== 1 ? 's' : ''}</Badge>
                )}
                {formData.appNameOrId && (
                  <Badge variant="secondary">App: {formData.appNameOrId}</Badge>
                )}
              </div>
              <ScrollArea className="h-[50vh] w-full rounded-md border">
                <pre className="p-4 text-xs font-mono whitespace-pre-wrap" data-testid="text-create-json-preview">
                  {JSON.stringify((() => { const b = buildBody(); b.state = 'ENABLED'; if (formData.appNameOrId.trim()) b.applicationNameOrId = formData.appNameOrId; return b; })(), null, 2)}
                </pre>
              </ScrollArea>
            </div>
          )}

          {wizardStep === 3 && (
            <div className="space-y-4" data-testid="wizard-step-upload">
              {wizardCreatedDeployment && (
                <div className="flex items-start gap-3 p-3 border rounded-md bg-muted/30">
                  <HardDrive className="w-5 h-5 mt-0.5 text-muted-foreground shrink-0" />
                  <div className="flex-1 min-w-0 space-y-1 text-sm">
                    <div className="text-muted-foreground" data-testid="text-wizard-deployment-id">
                      Deployment: <span className="font-mono font-medium text-foreground">{wizardCreatedDeployment.deploymentId}</span>
                    </div>
                    {wizardCreatedDeployment.elmId && (
                      <div className="text-xs text-muted-foreground truncate" data-testid="text-wizard-elm-object-id">
                        Large Object ID: {wizardCreatedDeployment.elmId}
                      </div>
                    )}
                  </div>
                </div>
              )}
              {wizardCreatedDeployment?.elmId ? (
                <>
                  <ElmDropZone
                    onFile={(file) => handleElmFileUpload(file, wizardCreatedDeployment.elmId!)}
                    uploading={elmUploadMutation.isPending}
                    fileName={wizardElmFile?.name}
                    testIdPrefix="wizard-elm"
                  />
                  <p className="text-xs text-muted-foreground">
                    Upload your .elm file now, or skip and do it later from the edit dialog.
                  </p>
                </>
              ) : (
                <p className="text-sm text-muted-foreground">
                  No ELM file is associated with this deployment. You can upload one later from the edit dialog if needed.
                </p>
              )}
            </div>
          )}

          <DialogFooter className="gap-2">
            {wizardStep > 0 && wizardStep < 3 && (
              <Button variant="outline" onClick={() => setWizardStep(s => s - 1)} data-testid="button-wizard-back">
                <ChevronLeft className="w-4 h-4 mr-1" />
                Back
              </Button>
            )}
            <div className="flex-1" />
            {wizardStep < 3 && (
              <Button variant="outline" onClick={() => setCreateDialogOpen(false)} data-testid="button-cancel-create">
                Cancel
              </Button>
            )}
            {wizardStep < 2 && (
              <Button
                onClick={() => setWizardStep(s => s + 1)}
                disabled={
                  wizardStep === 0 && (
                    (formData.elements.length === 0 && formData.packages.length === 0) ||
                    formData.packages.some(p => !p.elmArtifact.trim())
                  )
                }
                data-testid="button-wizard-next"
              >
                Next
                <ChevronRight className="w-4 h-4 ml-1" />
              </Button>
            )}
            {wizardStep === 2 && (
              <Button
                onClick={handleCreate}
                disabled={createMutation.isPending || formHasErrors}
                data-testid="button-submit-create"
              >
                {createMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                Create Deployment
              </Button>
            )}
            {wizardStep === 3 && (
              <Button
                variant="outline"
                onClick={() => {
                  setCreateDialogOpen(false);
                  setWizardCreatedDeployment(null);
                }}
                data-testid="button-wizard-done"
              >
                {elmUploadMutation.isSuccess ? 'Done' : wizardCreatedDeployment?.elmId ? 'Skip for Now' : 'Done'}
              </Button>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit Element Deployment</DialogTitle>
            <DialogDescription>
              Update the deployment configuration. Changes may trigger a re-deployment.
            </DialogDescription>
          </DialogHeader>
          <Tabs defaultValue="form">
            <TabsList>
              <TabsTrigger value="form" data-testid="tab-edit-form">Form</TabsTrigger>
              <TabsTrigger value="json" data-testid="tab-edit-json">JSON</TabsTrigger>
            </TabsList>
            <TabsContent value="form">
              <DeploymentForm
                mode="edit"
                formData={formData}
                setFormData={setFormData}
                deployment={selectedDeployment}
                onElmUpload={handleElmFileUpload}
                elmUploading={elmUploadMutation.isPending}
              />
            </TabsContent>
            <TabsContent value="json">
              <ScrollArea className="h-[60vh] w-full rounded-md border">
                <pre className="p-4 text-xs font-mono whitespace-pre-wrap" data-testid="text-edit-json-preview">
                  {JSON.stringify(buildBody(), null, 2)}
                </pre>
              </ScrollArea>
            </TabsContent>
          </Tabs>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditDialogOpen(false)} data-testid="button-cancel-edit">
              Cancel
            </Button>
            <Button
              onClick={handleUpdate}
              disabled={updateMutation.isPending || formHasErrors}
              data-testid="button-submit-edit"
            >
              {updateMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Update Deployment
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Deployment</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this deployment? This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          {selectedDeployment && (
            <div className="space-y-2 py-2">
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">ID:</span>
                <code className="text-sm font-mono" data-testid="text-delete-id">{selectedDeployment.id}</code>
              </div>
              {selectedDeployment.application && (
                <div className="flex items-center gap-2">
                  <span className="text-sm text-muted-foreground">Application:</span>
                  <span className="text-sm" data-testid="text-delete-app">{selectedDeployment.application.name || selectedDeployment.application.id}</span>
                </div>
              )}
              <div className="flex items-center gap-2">
                <span className="text-sm text-muted-foreground">State:</span>
                <Badge variant={getStateBadgeVariant(selectedDeployment.state)} data-testid="badge-delete-state">{selectedDeployment.state}</Badge>
              </div>
            </div>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteDialogOpen(false)} data-testid="button-cancel-delete">
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={handleDelete}
              disabled={deleteMutation.isPending}
              data-testid="button-confirm-delete"
            >
              {deleteMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={elmUploadDialogOpen} onOpenChange={(open) => {
        if (!open) {
          setElmUploadDialogOpen(false);
          setElmUploadTarget(null);
        }
      }}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Upload ELM File</DialogTitle>
            <DialogDescription>
              Your deployment has been created and a Large Object has been registered for the ELM file. You can upload your .elm file now, or skip and do it later from the edit dialog.
            </DialogDescription>
          </DialogHeader>
          {elmUploadTarget && (
            <div className="space-y-4">
              <div className="flex items-start gap-3 p-3 border rounded-md bg-muted/30">
                <HardDrive className="w-5 h-5 mt-0.5 text-muted-foreground shrink-0" />
                <div className="flex-1 min-w-0 space-y-1 text-sm">
                  <div className="text-muted-foreground" data-testid="text-elm-deployment-id">
                    Deployment: <span className="font-mono font-medium text-foreground">{elmUploadTarget.deploymentId}</span>
                  </div>
                  <div className="text-xs text-muted-foreground truncate" data-testid="text-elm-object-id">
                    Large Object ID: {elmUploadTarget.elm.id}
                  </div>
                </div>
              </div>
              <ElmDropZone
                onFile={(file) => handleElmFileUpload(file, elmUploadTarget.elm.id)}
                uploading={elmUploadMutation.isPending}
                testIdPrefix="post-create-elm"
              />
            </div>
          )}
          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setElmUploadDialogOpen(false);
                setElmUploadTarget(null);
              }}
              data-testid="button-skip-elm-upload"
            >
              Skip for Now
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <FeaturesDialog open={featuresDialogOpen} onOpenChange={setFeaturesDialogOpen} />
    </div>
  );
}

interface ElementFeature {
  name: string;
  description: string;
}

function FeaturesDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (open: boolean) => void }) {
  const { data: features, isLoading } = useQuery<ElementFeature[]>({
    queryKey: ['/api/rest/elements/features'],
    queryFn: async () => {
      return apiClient.request<ElementFeature[]>('/api/rest/elements/features');
    },
    enabled: open,
  });

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle>Element Features</DialogTitle>
          <DialogDescription>
            Permitted features of the system that can be exposed to Elements.
          </DialogDescription>
        </DialogHeader>
        {isLoading ? (
          <div className="flex items-center justify-center py-8">
            <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
          </div>
        ) : !features || features.length === 0 ? (
          <div className="py-6 text-center text-sm text-muted-foreground">
            No features available.
          </div>
        ) : (
          <ScrollArea className="max-h-[400px]">
            <div className="space-y-2 pr-2 overflow-x-auto">
              {features.map((feature, idx) => (
                <Card key={feature.name || idx} data-testid={`card-feature-${idx}`}>
                  <CardContent className="p-3">
                    <p className="font-mono text-sm font-medium whitespace-nowrap" data-testid={`text-feature-name-${idx}`}>{feature.name}</p>
                    {feature.description && (
                      <p className="text-xs text-muted-foreground mt-1" data-testid={`text-feature-desc-${idx}`}>{feature.description}</p>
                    )}
                  </CardContent>
                </Card>
              ))}
            </div>
          </ScrollArea>
        )}
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)} data-testid="button-close-features">
            Close
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function ArtifactListEditor({
  label,
  values,
  onChange,
  placeholder,
  description,
  testIdPrefix,
}: {
  label: string;
  values: string[];
  onChange: (vals: string[]) => void;
  placeholder: string;
  description?: string;
  testIdPrefix: string;
}) {
  const addItem = () => onChange([...values, '']);
  const removeItem = (index: number) => onChange(values.filter((_, i) => i !== index));
  const updateItem = (index: number, val: string) => {
    const updated = [...values];
    updated[index] = val;
    onChange(updated);
  };

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between gap-2">
        <Label className="text-xs text-muted-foreground">{label}</Label>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={addItem}
          data-testid={`button-add-${testIdPrefix}`}
        >
          <Plus className="w-3 h-3 mr-1" />
          Add
        </Button>
      </div>
      {description && (
        <p className="text-xs text-muted-foreground">{description}</p>
      )}
      {values.map((val, i) => (
        <div key={i} className="flex items-center gap-2">
          <Input
            value={val}
            onChange={(e) => updateItem(i, e.target.value)}
            placeholder={placeholder}
            className="font-mono text-xs"
            data-testid={`input-${testIdPrefix}-${i}`}
          />
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => removeItem(i)}
            data-testid={`button-remove-${testIdPrefix}-${i}`}
          >
            <X className="w-3 h-3" />
          </Button>
        </div>
      ))}
    </div>
  );
}

function KeyValueEditor({
  value,
  onChange,
  label,
  testIdPrefix,
  keyPlaceholder = 'key',
  valuePlaceholder = 'value',
}: {
  value: Record<string, any>;
  onChange: (val: Record<string, any>) => void;
  label: string;
  testIdPrefix: string;
  keyPlaceholder?: string;
  valuePlaceholder?: string;
}) {
  const entries = Object.entries(value || {});

  const addEntry = () => {
    const newKey = `key${entries.length + 1}`;
    onChange({ ...value, [newKey]: '' });
  };

  const removeEntry = (key: string) => {
    const copy = { ...value };
    delete copy[key];
    onChange(copy);
  };

  const updateKey = (oldKey: string, newKey: string) => {
    if (newKey === oldKey) return;
    const result: Record<string, any> = {};
    for (const [k, v] of Object.entries(value)) {
      result[k === oldKey ? newKey : k] = v;
    }
    onChange(result);
  };

  const parseValue = (raw: string): any => {
    const trimmed = raw.trim();
    if (trimmed === 'true') return true;
    if (trimmed === 'false') return false;
    if (trimmed === 'null') return null;
    if (trimmed !== '' && !isNaN(Number(trimmed))) return Number(trimmed);
    if ((trimmed.startsWith('{') && trimmed.endsWith('}')) || (trimmed.startsWith('[') && trimmed.endsWith(']'))) {
      try { return JSON.parse(trimmed); } catch { /* keep as string */ }
    }
    return raw;
  };

  const updateValue = (key: string, newVal: string) => {
    onChange({ ...value, [key]: newVal });
  };

  const commitValue = (key: string, rawVal: string) => {
    onChange({ ...value, [key]: parseValue(rawVal) });
  };

  return (
    <div className="space-y-1">
      <div className="flex items-center justify-between gap-2">
        <Label className="text-xs text-muted-foreground">{label}</Label>
        <Button type="button" variant="outline" size="sm" onClick={addEntry} data-testid={`${testIdPrefix}-add`}>
          <Plus className="w-3 h-3 mr-1" />
          Add
        </Button>
      </div>
      {entries.length === 0 && (
        <p className="text-xs text-muted-foreground italic">No entries</p>
      )}
      {entries.map(([key, val], i) => (
        <div key={i} className="flex items-center gap-1" data-testid={`${testIdPrefix}-row-${i}`}>
          <Input
            value={key}
            onChange={(e) => updateKey(key, e.target.value)}
            placeholder={keyPlaceholder}
            className="flex-shrink-0 w-2/5 font-mono text-xs"
            onBlur={(e) => {
              const trimmed = e.target.value.trim();
              if (trimmed && trimmed !== key) updateKey(key, trimmed);
            }}
            data-testid={`${testIdPrefix}-key-${i}`}
          />
          <Input
            value={typeof val === 'object' ? JSON.stringify(val) : String(val ?? '')}
            onChange={(e) => updateValue(key, e.target.value)}
            onBlur={(e) => commitValue(key, e.target.value)}
            placeholder={valuePlaceholder}
            className="flex-1 font-mono text-xs"
            data-testid={`${testIdPrefix}-val-${i}`}
          />
          <Button type="button" variant="ghost" size="icon" onClick={() => removeEntry(key)} data-testid={`${testIdPrefix}-remove-${i}`}>
            <X className="w-3 h-3" />
          </Button>
        </div>
      ))}
    </div>
  );
}

function PathClassPathsEditor({
  value,
  onChange,
  label,
  description,
  testIdPrefix,
}: {
  value: Record<string, string[]>;
  onChange: (val: Record<string, string[]>) => void;
  label: string;
  description?: string;
  testIdPrefix: string;
}) {
  const entries = Object.entries(value || {});

  const addPath = () => {
    const newKey = `path${entries.length + 1}`;
    onChange({ ...value, [newKey]: [''] });
  };

  const removePath = (key: string) => {
    const copy = { ...value };
    delete copy[key];
    onChange(copy);
  };

  const updatePathKey = (oldKey: string, newKey: string) => {
    if (newKey === oldKey) return;
    const result: Record<string, string[]> = {};
    for (const [k, v] of Object.entries(value)) {
      result[k === oldKey ? newKey : k] = v;
    }
    onChange(result);
  };

  const addClassPath = (pathKey: string) => {
    onChange({ ...value, [pathKey]: [...(value[pathKey] || []), ''] });
  };

  const removeClassPath = (pathKey: string, index: number) => {
    onChange({ ...value, [pathKey]: value[pathKey].filter((_, i) => i !== index) });
  };

  const updateClassPath = (pathKey: string, index: number, newVal: string) => {
    const arr = [...value[pathKey]];
    arr[index] = newVal;
    onChange({ ...value, [pathKey]: arr });
  };

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between gap-2">
        <Label className="text-xs text-muted-foreground">{label}</Label>
        <Button type="button" variant="outline" size="sm" onClick={addPath} data-testid={`${testIdPrefix}-add-path`}>
          <Plus className="w-3 h-3 mr-1" />
          Add Path
        </Button>
      </div>
      {description && (
        <p className="text-xs text-muted-foreground">{description}</p>
      )}
      {entries.length === 0 && (
        <p className="text-xs text-muted-foreground italic">No paths configured</p>
      )}
      {entries.map(([pathKey, classPaths], pi) => (
        <Card key={pi} data-testid={`${testIdPrefix}-path-${pi}`}>
          <CardContent className="p-2 space-y-2">
            <Label className="text-xs text-muted-foreground">Element Path</Label>
            <div className="flex items-center gap-1">
              <Input
                value={pathKey}
                onChange={(e) => updatePathKey(pathKey, e.target.value)}
                placeholder="element-path"
                className="flex-1 font-mono text-xs"
                data-testid={`${testIdPrefix}-pathkey-${pi}`}
              />
              <Button type="button" variant="ghost" size="icon" onClick={() => removePath(pathKey)} data-testid={`${testIdPrefix}-remove-path-${pi}`}>
                <X className="w-3 h-3" />
              </Button>
            </div>
            <div className="pl-3 space-y-1">
              <Label className="text-xs text-muted-foreground">Values</Label>
              {classPaths.map((cp, ci) => (
                <div key={ci} className="flex items-center gap-1">
                  <Input
                    value={cp}
                    onChange={(e) => updateClassPath(pathKey, ci, e.target.value)}
                    placeholder="com.example.SpiClass"
                    className="flex-1 font-mono text-xs"
                    data-testid={`${testIdPrefix}-cp-${pi}-${ci}`}
                  />
                  <Button type="button" variant="ghost" size="icon" onClick={() => removeClassPath(pathKey, ci)} data-testid={`${testIdPrefix}-rm-cp-${pi}-${ci}`}>
                    <X className="w-3 h-3" />
                  </Button>
                </div>
              ))}
              <Button type="button" variant="outline" size="sm" onClick={() => addClassPath(pathKey)} data-testid={`${testIdPrefix}-add-cp-${pi}`}>
                <Plus className="w-3 h-3 mr-1" />
                Add Value
              </Button>
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function PathKeyValueMapEditor({
  value,
  onChange,
  label,
  description,
  testIdPrefix,
}: {
  value: Record<string, Record<string, any>>;
  onChange: (val: Record<string, Record<string, any>>) => void;
  label: string;
  description?: string;
  testIdPrefix: string;
}) {
  const entries = Object.entries(value || {});

  const addPath = () => {
    const newKey = `path${entries.length + 1}`;
    onChange({ ...value, [newKey]: {} });
  };

  const removePath = (key: string) => {
    const copy = { ...value };
    delete copy[key];
    onChange(copy);
  };

  const updatePathKey = (oldKey: string, newKey: string) => {
    if (newKey === oldKey) return;
    const result: Record<string, Record<string, any>> = {};
    for (const [k, v] of Object.entries(value)) {
      result[k === oldKey ? newKey : k] = v;
    }
    onChange(result);
  };

  const updatePathAttrs = (pathKey: string, attrs: Record<string, any>) => {
    onChange({ ...value, [pathKey]: attrs });
  };

  return (
    <div className="space-y-2">
      <div className="flex items-center justify-between gap-2">
        <Label className="text-xs text-muted-foreground">{label}</Label>
        <Button type="button" variant="outline" size="sm" onClick={addPath} data-testid={`${testIdPrefix}-add-path`}>
          <Plus className="w-3 h-3 mr-1" />
          Add Path
        </Button>
      </div>
      {description && (
        <p className="text-xs text-muted-foreground">{description}</p>
      )}
      {entries.length === 0 && (
        <p className="text-xs text-muted-foreground italic">No paths configured</p>
      )}
      {entries.map(([pathKey, attrs], pi) => (
        <Card key={pi} data-testid={`${testIdPrefix}-path-${pi}`}>
          <CardContent className="p-2 space-y-2">
            <Label className="text-xs text-muted-foreground">Element Path</Label>
            <div className="flex items-center gap-1">
              <Input
                value={pathKey}
                onChange={(e) => updatePathKey(pathKey, e.target.value)}
                placeholder="element-path"
                className="flex-1 font-mono text-xs"
                data-testid={`${testIdPrefix}-pathkey-${pi}`}
              />
              <Button type="button" variant="ghost" size="icon" onClick={() => removePath(pathKey)} data-testid={`${testIdPrefix}-remove-path-${pi}`}>
                <X className="w-3 h-3" />
              </Button>
            </div>
            <div className="pl-3">
              <KeyValueEditor
                value={attrs}
                onChange={(a) => updatePathAttrs(pathKey, a)}
                label="Attributes"
                testIdPrefix={`${testIdPrefix}-attrs-${pi}`}
                keyPlaceholder="attribute key"
                valuePlaceholder="attribute value"
              />
            </div>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function SpiBuiltinEditor({
  value,
  onChange,
  available,
  loading,
  testIdPrefix,
}: {
  value: string[];
  onChange: (val: string[]) => void;
  available: ElementSpi[];
  loading: boolean;
  testIdPrefix: string;
}) {
  const predefinedIds = available.map(s => s.id);
  const selectedPredefined = value.find(v => predefinedIds.includes(v)) || '';
  const customEntries = value.filter(v => !predefinedIds.includes(v));

  const onPredefinedChange = (id: string) => {
    const withoutPredefined = value.filter(v => !predefinedIds.includes(v));
    if (id) {
      onChange([id, ...withoutPredefined]);
    } else {
      onChange(withoutPredefined);
    }
  };

  const onCustomChange = (vals: string[]) => {
    const predefined = value.filter(v => predefinedIds.includes(v));
    onChange([...predefined, ...vals]);
  };

  return (
    <div className="space-y-3">
      <div className="space-y-1">
        <Label className="text-xs">SPI Builtin</Label>
        {loading ? (
          <div className="flex items-center gap-2 text-xs text-muted-foreground">
            <Loader2 className="w-3 h-3 animate-spin" />
            Loading available SPIs...
          </div>
        ) : available.length === 0 ? (
          <p className="text-xs text-muted-foreground italic">No builtin SPIs available from the server.</p>
        ) : (
          <Select
            value={selectedPredefined || '__none__'}
            onValueChange={(v) => onPredefinedChange(v === '__none__' ? '' : v)}
          >
            <SelectTrigger data-testid={`${testIdPrefix}-trigger`}>
              <SelectValue placeholder="Select a builtin SPI" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__">None</SelectItem>
              {available.map((spi) => (
                <SelectItem key={spi.id} value={spi.id} data-testid={`${testIdPrefix}-option-${spi.id}`}>
                  {spi.id}{spi.description ? ` — ${spi.description}` : ''}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        )}
        <p className="text-xs text-muted-foreground">
          Framework-provided SPI implementation to use for this element path.
        </p>
      </div>
      <ArtifactListEditor
        label="Additional SPI Builtins (optional)"
        values={customEntries}
        onChange={onCustomChange}
        placeholder="custom-spi-builtin"
        description="Additional builtin SPI names beyond the predefined selection above."
        testIdPrefix={`${testIdPrefix}-custom`}
      />
    </div>
  );
}

function ElementDefinitionEditor({
  elements,
  onChange,
  availableSpis,
  spisLoading,
}: {
  elements: ElementPathDefinition[];
  onChange: (els: ElementPathDefinition[]) => void;
  availableSpis: ElementSpi[];
  spisLoading: boolean;
}) {
  const addElement = () => {
    onChange([...elements, {
      path: '',
      apiArtifacts: [],
      spiBuiltins: availableSpis.some(s => s.id === 'DEFAULT') ? ['DEFAULT'] : [],
      spiArtifacts: [],
      elementArtifacts: [],
      attributes: {},
    }]);
  };

  const removeElement = (index: number) => {
    onChange(elements.filter((_, i) => i !== index));
  };

  const updateElement = (index: number, updated: ElementPathDefinition) => {
    const copy = [...elements];
    copy[index] = updated;
    onChange(copy);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between gap-2">
        <Label>Element Definitions</Label>
        <Button type="button" variant="outline" size="sm" onClick={addElement} data-testid="button-add-element">
          <Plus className="w-3 h-3 mr-1" />
          Add Definition
        </Button>
      </div>
      <p className="text-xs text-muted-foreground">
        Each definition specifies the classpath and artifacts for an Element to deploy. Specify Maven artifact coordinates for SPI, Element implementations, and optionally API artifacts.
      </p>
      {elements.length === 0 && (
        <p className="text-xs text-muted-foreground italic">No element definitions. Click "Add Definition" to add one.</p>
      )}
      {elements.map((el, i) => (
        <Card key={i} data-testid={`card-element-def-${i}`}>
          <CardContent className="p-3 space-y-3">
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-medium text-muted-foreground">Definition #{i + 1}</span>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                onClick={() => removeElement(i)}
                data-testid={`button-remove-element-${i}`}
              >
                <X className="w-3 h-3" />
              </Button>
            </div>
            <div className="space-y-1">
              <Label htmlFor={`el-path-${i}`} className="text-xs">Path (optional)</Label>
              <Input
                id={`el-path-${i}`}
                value={el.path}
                onChange={(e) => updateElement(i, { ...el, path: e.target.value })}
                placeholder="my-element"
                className="font-mono text-xs"
                data-testid={`input-element-path-${i}`}
              />
              <p className="text-xs text-muted-foreground">
                Single directory name where this element is deployed. Must not contain "/" or "\".
              </p>
            </div>
            <SpiBuiltinEditor
              value={el.spiBuiltins || []}
              onChange={(val) => updateElement(i, { ...el, spiBuiltins: val })}
              available={availableSpis}
              loading={spisLoading}
              testIdPrefix={`el-${i}-spi-builtin`}
            />
            <ArtifactListEditor
              label="SPI Artifacts (optional)"
              values={el.spiArtifacts || []}
              onChange={(vals) => updateElement(i, { ...el, spiArtifacts: vals })}
              placeholder="com.example:spi-artifact:1.0"
              testIdPrefix={`el-${i}-spi-art`}
            />
            <p className="text-xs text-muted-foreground">
              Custom SPI implementation artifact coordinates, if needed beyond the builtins above.
            </p>
            <ArtifactListEditor
              label="Element Artifacts *"
              values={el.elementArtifacts || []}
              onChange={(vals) => updateElement(i, { ...el, elementArtifacts: vals })}
              placeholder="com.example:element-artifact:1.0"
              description="List of Element implementation artifact coordinates. These contain the actual Element code."
              testIdPrefix={`el-${i}-elem`}
            />
            <ArtifactListEditor
              label="API Artifacts (optional)"
              values={el.apiArtifacts || []}
              onChange={(vals) => updateElement(i, { ...el, apiArtifacts: vals })}
              placeholder="com.example:api-artifact:1.0"
              testIdPrefix={`el-${i}-api`}
            />
            <p className="text-xs text-muted-foreground">
              API Artifacts are loaded into a shared classloader accessible to all Elements. Only needed to expose methods to other Elements.
            </p>
            <KeyValueEditor
              value={el.attributes || {}}
              onChange={(attrs) => updateElement(i, { ...el, attributes: attrs })}
              label="Attributes"
              testIdPrefix={`el-${i}-attrs`}
              keyPlaceholder="attribute key"
              valuePlaceholder="attribute value"
            />
            <p className="text-xs text-muted-foreground">
              Custom attributes passed to this Element at load time via the AttributesLoader mechanism.
            </p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function PackageDefinitionEditor({
  packages,
  onChange,
}: {
  packages: ElementPackageDefinition[];
  onChange: (pkgs: ElementPackageDefinition[]) => void;
}) {
  const addPackage = () => {
    onChange([...packages, { elmArtifact: '', pathSpiBuiltins: {}, pathSpiClassPaths: {}, pathAttributes: {} }]);
  };

  const removePackage = (index: number) => {
    onChange(packages.filter((_, i) => i !== index));
  };

  const updatePackage = (index: number, updated: ElementPackageDefinition) => {
    const copy = [...packages];
    copy[index] = updated;
    onChange(copy);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between gap-2">
        <Label>Element Packages</Label>
        <Button type="button" variant="outline" size="sm" onClick={addPackage} data-testid="button-add-package">
          <Plus className="w-3 h-3 mr-1" />
          Add Package
        </Button>
      </div>
      <p className="text-xs text-muted-foreground">
        Each package specifies an ELM artifact to deploy with per-path attribute and SPI configuration. Multiple element paths within an ELM can have different configurations.
      </p>
      {packages.length === 0 && (
        <p className="text-xs text-muted-foreground italic">No packages. Click "Add Package" to add one.</p>
      )}
      {packages.map((pkg, i) => (
        <Card key={i} data-testid={`card-package-def-${i}`}>
          <CardContent className="p-3 space-y-3">
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-medium text-muted-foreground">Package #{i + 1}</span>
              <Button
                type="button"
                variant="ghost"
                size="icon"
                onClick={() => removePackage(i)}
                data-testid={`button-remove-package-${i}`}
              >
                <X className="w-3 h-3" />
              </Button>
            </div>
            <div className="space-y-1">
              <Label htmlFor={`pkg-elm-${i}`} className="text-xs">ELM Artifact *</Label>
              <Input
                id={`pkg-elm-${i}`}
                value={pkg.elmArtifact}
                onChange={(e) => updatePackage(i, { ...pkg, elmArtifact: e.target.value })}
                placeholder="com.example:my-elm:1.0"
                className="font-mono text-xs"
                data-testid={`input-package-elm-${i}`}
              />
              <p className="text-xs text-muted-foreground">
                The ELM artifact coordinate to resolve and deploy. This ELM file will be downloaded, extracted, and its contents organized into the deployment directory structure.
              </p>
            </div>
            <PathClassPathsEditor
              value={pkg.pathSpiBuiltins || {}}
              onChange={(val) => updatePackage(i, { ...pkg, pathSpiBuiltins: val })}
              label="SPI Builtin Path Mapping"
              description="Map of element paths to builtin SPI configurations. This allows for an individual SPI specification for each Element contained within the ELM file."
              testIdPrefix={`pkg-${i}-spi-builtins`}
            />
            <PathClassPathsEditor
              value={pkg.pathSpiClassPaths || {}}
              onChange={(val) => updatePackage(i, { ...pkg, pathSpiClassPaths: val })}
              label="SPI Classpath Path Mapping"
              description="Map of element paths to custom SPI class paths. This allows for an individual SPI specification for each Element contained within the ELM file in the specified ELM artifact."
              testIdPrefix={`pkg-${i}-spi-cp`}
            />
            <PathKeyValueMapEditor
              value={pkg.pathAttributes || {}}
              onChange={(val) => updatePackage(i, { ...pkg, pathAttributes: val })}
              label="Path Attributes"
              description="Map of element paths to their custom attributes. The key is the path inside the ELM for each Element, and the value is a map of custom attributes to pass to that specific element at load time via the AttributesLoader mechanism."
              testIdPrefix={`pkg-${i}-attrs`}
            />
          </CardContent>
        </Card>
      ))}
    </div>
  );
}

function RepositoryEditor({
  repositories,
  onChange,
}: {
  repositories: ElementArtifactRepository[];
  onChange: (repos: ElementArtifactRepository[]) => void;
}) {
  const addRepo = () => {
    onChange([...repositories, { id: '', url: '' }]);
  };

  const removeRepo = (index: number) => {
    onChange(repositories.filter((_, i) => i !== index));
  };

  const updateRepo = (index: number, field: 'id' | 'url', value: string) => {
    const copy = [...repositories];
    copy[index] = { ...copy[index], [field]: value };
    onChange(copy);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between gap-2">
        <Label>Artifact Repositories</Label>
        <Button type="button" variant="outline" size="sm" onClick={addRepo} data-testid="button-add-repo">
          <Plus className="w-3 h-3 mr-1" />
          Add Repository
        </Button>
      </div>
      {repositories.length === 0 && (
        <p className="text-xs text-muted-foreground italic">No custom repositories. Click "Add Repository" to add one.</p>
      )}
      {repositories.map((repo, i) => (
        <div key={i} className="flex items-center gap-2" data-testid={`row-repo-${i}`}>
          <Input
            value={repo.id}
            onChange={(e) => updateRepo(i, 'id', e.target.value)}
            placeholder="Repository ID"
            className="flex-shrink-0 w-1/3 font-mono text-xs"
            data-testid={`input-repo-id-${i}`}
          />
          <Input
            value={repo.url}
            onChange={(e) => updateRepo(i, 'url', e.target.value)}
            placeholder="https://repo.example.com/maven2"
            className="flex-1 font-mono text-xs"
            data-testid={`input-repo-url-${i}`}
          />
          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={() => removeRepo(i)}
            data-testid={`button-remove-repo-${i}`}
          >
            <X className="w-3 h-3" />
          </Button>
        </div>
      ))}
    </div>
  );
}

function ElementDefinitionSubDialog({
  open,
  onOpenChange,
  element,
  onSave,
  availableSpis,
  spisLoading,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  element: ElementPathDefinition;
  onSave: (el: ElementPathDefinition) => void;
  availableSpis: ElementSpi[];
  spisLoading: boolean;
}) {
  const [draft, setDraft] = useState<ElementPathDefinition>(element);
  const prevOpenRef = useRef(false);
  if (open && !prevOpenRef.current) {
    setDraft({ ...element });
  }
  prevOpenRef.current = open;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-xl max-h-[80vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
        <DialogHeader>
          <DialogTitle>Element Definition</DialogTitle>
          <DialogDescription>
            Configure the classpath and artifacts for an Element to deploy.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1">
            <Label className="text-xs">Path (optional)</Label>
            <Input
              value={draft.path}
              onChange={(e) => setDraft({ ...draft, path: e.target.value })}
              placeholder="my-element"
              className="font-mono text-xs"
              data-testid="input-subdialog-element-path"
            />
            <p className="text-xs text-muted-foreground">
              Single directory name where this element is deployed. Must not contain "/" or "\".
            </p>
          </div>
          <SpiBuiltinEditor
            value={draft.spiBuiltins || []}
            onChange={(val) => setDraft({ ...draft, spiBuiltins: val })}
            available={availableSpis}
            loading={spisLoading}
            testIdPrefix="subdialog-el-spi-builtin"
          />
          <ArtifactListEditor
            label="SPI Artifacts (optional)"
            values={draft.spiArtifacts || []}
            onChange={(vals) => setDraft({ ...draft, spiArtifacts: vals })}
            placeholder="com.example:spi-artifact:1.0"
            testIdPrefix="subdialog-el-spi-art"
          />
          <ArtifactListEditor
            label="Element Artifacts *"
            values={draft.elementArtifacts || []}
            onChange={(vals) => setDraft({ ...draft, elementArtifacts: vals })}
            placeholder="com.example:element-artifact:1.0"
            description="Element implementation artifact coordinates."
            testIdPrefix="subdialog-el-elem"
          />
          <ArtifactListEditor
            label="API Artifacts (optional)"
            values={draft.apiArtifacts || []}
            onChange={(vals) => setDraft({ ...draft, apiArtifacts: vals })}
            placeholder="com.example:api-artifact:1.0"
            testIdPrefix="subdialog-el-api"
          />
          <KeyValueEditor
            value={draft.attributes || {}}
            onChange={(attrs) => setDraft({ ...draft, attributes: attrs })}
            label="Attributes"
            testIdPrefix="subdialog-el-attrs"
            keyPlaceholder="attribute key"
            valuePlaceholder="attribute value"
          />
        </div>
        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={() => onOpenChange(false)} data-testid="button-subdialog-cancel-element">
            Cancel
          </Button>
          <Button onClick={() => { onSave(draft); onOpenChange(false); }} data-testid="button-subdialog-save-element">
            Save
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function PackageDefinitionSubDialog({
  open,
  onOpenChange,
  pkg,
  onSave,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  pkg: ElementPackageDefinition;
  onSave: (p: ElementPackageDefinition) => void;
}) {
  const [draft, setDraft] = useState<ElementPackageDefinition>(pkg);
  const prevOpenRef = useRef(false);
  if (open && !prevOpenRef.current) {
    setDraft({ ...pkg });
  }
  prevOpenRef.current = open;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-xl max-h-[80vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
        <DialogHeader>
          <DialogTitle>Package Configuration</DialogTitle>
          <DialogDescription>
            Configure an ELM package artifact with per-path SPI and attribute settings.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-1">
            <Label className="text-xs">ELM Artifact *</Label>
            <Input
              value={draft.elmArtifact}
              onChange={(e) => setDraft({ ...draft, elmArtifact: e.target.value })}
              placeholder="com.example:my-elm:1.0"
              className="font-mono text-xs"
              data-testid="input-subdialog-package-elm"
            />
            <p className="text-xs text-muted-foreground">
              The ELM artifact coordinate to resolve and deploy.
            </p>
          </div>
          <PathClassPathsEditor
            value={draft.pathSpiBuiltins || {}}
            onChange={(val) => setDraft({ ...draft, pathSpiBuiltins: val })}
            label="SPI Builtin Path Mapping"
            description="Map of element paths to builtin SPI configurations."
            testIdPrefix="subdialog-pkg-spi-builtins"
          />
          <PathClassPathsEditor
            value={draft.pathSpiClassPaths || {}}
            onChange={(val) => setDraft({ ...draft, pathSpiClassPaths: val })}
            label="SPI Classpath Path Mapping"
            description="Map of element paths to custom SPI class paths."
            testIdPrefix="subdialog-pkg-spi-cp"
          />
          <PathKeyValueMapEditor
            value={draft.pathAttributes || {}}
            onChange={(val) => setDraft({ ...draft, pathAttributes: val })}
            label="Path Attributes"
            description="Map of element paths to their custom attributes."
            testIdPrefix="subdialog-pkg-attrs"
          />
        </div>
        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={() => onOpenChange(false)} data-testid="button-subdialog-cancel-package">
            Cancel
          </Button>
          <Button
            onClick={() => { onSave(draft); onOpenChange(false); }}
            disabled={!draft.elmArtifact.trim()}
            data-testid="button-subdialog-save-package"
          >
            Save
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

function WizardConfigStep({
  formData,
  setFormData,
  onFileSelected,
}: {
  formData: FormData;
  setFormData: (fd: FormData) => void;
  onFileSelected?: (file: File) => void;
}) {
  const update = (partial: Partial<FormData>) => setFormData({ ...formData, ...partial });

  const { toast } = useToast();

  const elmInspectMutation = useMutation({
    mutationFn: async (file: File) => {
      const fd = new FormData();
      fd.append('elm', file);
      const fullPath = await getApiPath('/api/rest/elm/inspector/upload');
      const headers: Record<string, string> = {};
      const token = apiClient.getSessionToken();
      if (token) headers['Elements-SessionSecret'] = token;
      const response = await fetch(fullPath, { method: 'POST', body: fd, headers, credentials: 'include' });
      if (!response.ok) {
        const text = await response.text().catch(() => '');
        let msg = `Inspection failed: ${response.status}`;
        try { msg = JSON.parse(text).message ?? msg; } catch { if (text) msg = text; }
        throw new Error(msg);
      }
      return response.json() as Promise<ElmInspectorRecord[]>;
    },
    onSuccess: (records) => {
      const newElements: ElementPathDefinition[] = records.map(r => ({
        path: r.path,
        spiBuiltins: r.manifest?.builtinSpis ?? [],
        attributes: r.attributes as Record<string, any>,
        apiArtifacts: [],
        spiArtifacts: [],
        elementArtifacts: [],
      }));
      update({ elements: [...formData.elements, ...newElements] });
      toast({
        title: 'ELM Imported',
        description: `Added ${newElements.length} element definition${newElements.length !== 1 ? 's' : ''} from the ELM file.`,
      });
    },
    onError: (err: Error) => {
      toast({ title: 'Import Failed', description: err.message, variant: 'destructive' });
    },
  });

  const { data: availableSpis = [], isLoading: spisLoading } = useQuery<ElementSpi[]>({
    queryKey: ['/api/rest/elements/builtin_spi'],
    queryFn: async () => {
      const data = await apiClient.request<any>('/api/rest/elements/builtin_spi');
      if (Array.isArray(data)) return data;
      if (data && typeof data === 'object') {
        const keys = Object.keys(data);
        if (keys.length > 0 && keys.every(k => !isNaN(Number(k)))) {
          return keys.sort((a, b) => Number(a) - Number(b)).map(k => data[k]);
        }
        if (data.objects) return data.objects;
        if (data.content) return data.content;
      }
      return [];
    },
    staleTime: 60000,
  });

  const [elementDialogOpen, setElementDialogOpen] = useState(false);
  const [packageDialogOpen, setPackageDialogOpen] = useState(false);
  const [editingElementIndex, setEditingElementIndex] = useState<number | null>(null);
  const [editingPackageIndex, setEditingPackageIndex] = useState<number | null>(null);

  const newElementDefault = (): ElementPathDefinition => ({
    path: '',
    apiArtifacts: [],
    spiBuiltins: availableSpis.some(s => s.id === 'DEFAULT') ? ['DEFAULT'] : [],
    spiArtifacts: [],
    elementArtifacts: [],
    attributes: {},
  });

  const newPackageDefault = (): ElementPackageDefinition => ({
    elmArtifact: '',
    pathSpiBuiltins: {},
    pathSpiClassPaths: {},
    pathAttributes: {},
  });

  const [editingElement, setEditingElement] = useState<ElementPathDefinition>(newElementDefault());
  const [editingPackage, setEditingPackage] = useState<ElementPackageDefinition>(newPackageDefault());

  const openAddElement = () => {
    setEditingElementIndex(null);
    setEditingElement(newElementDefault());
    setElementDialogOpen(true);
  };

  const openEditElement = (index: number) => {
    setEditingElementIndex(index);
    setEditingElement({ ...formData.elements[index] });
    setElementDialogOpen(true);
  };

  const saveElement = (el: ElementPathDefinition) => {
    if (editingElementIndex !== null) {
      const copy = [...formData.elements];
      copy[editingElementIndex] = el;
      update({ elements: copy });
    } else {
      update({ elements: [...formData.elements, el] });
    }
  };

  const removeElement = (index: number) => {
    update({ elements: formData.elements.filter((_, i) => i !== index) });
  };

  const openAddPackage = () => {
    setEditingPackageIndex(null);
    setEditingPackage(newPackageDefault());
    setPackageDialogOpen(true);
  };

  const openEditPackage = (index: number) => {
    setEditingPackageIndex(index);
    setEditingPackage({ ...formData.packages[index] });
    setPackageDialogOpen(true);
  };

  const savePackage = (pkg: ElementPackageDefinition) => {
    if (editingPackageIndex !== null) {
      const copy = [...formData.packages];
      copy[editingPackageIndex] = pkg;
      update({ packages: copy });
    } else {
      update({ packages: [...formData.packages, pkg] });
    }
  };

  const removePackage = (index: number) => {
    update({ packages: formData.packages.filter((_, i) => i !== index) });
  };

  const hasItems = formData.elements.length > 0 || formData.packages.length > 0;

  const getElementLabel = (el: ElementPathDefinition, index: number) => {
    if (el.path && el.path.trim()) return el.path;
    if (el.elementArtifacts && el.elementArtifacts.length > 0 && el.elementArtifacts[0].trim()) {
      return el.elementArtifacts[0];
    }
    return `Element Definition ${index + 1}`;
  };

  const getPackageLabel = (pkg: ElementPackageDefinition, index: number) => {
    if (pkg.elmArtifact && pkg.elmArtifact.trim()) return pkg.elmArtifact;
    return `Package ${index + 1}`;
  };

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <div>
          <p className="text-sm font-medium">Import from ELM file</p>
          <p className="text-xs text-muted-foreground mt-0.5">
            Upload an .elm file to auto-populate element paths and SPI settings from its manifest.
          </p>
        </div>
        <ElmDropZone
          onFile={(file) => {
            onFileSelected?.(file);
            elmInspectMutation.mutate(file);
          }}
          uploading={elmInspectMutation.isPending}
          buttonLabel="Browse ELM file"
          testIdPrefix="wizard-import-elm"
        />
      </div>

      <div className="flex items-center justify-between gap-2 flex-wrap">
        <p className="text-sm text-muted-foreground">
          Add element definitions and/or package configurations. A single deployment can contain both types.
        </p>
        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={openAddElement}
            data-testid="button-add-element-config"
          >
            <Database className="w-4 h-4 mr-2" />
            Add Element Definition
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={openAddPackage}
            data-testid="button-add-package-config"
          >
            <Package className="w-4 h-4 mr-2" />
            Add Package
          </Button>
        </div>
      </div>

      {!hasItems && (
        <div className="py-8 text-center border-2 border-dashed rounded-md">
          <p className="text-sm text-muted-foreground">No configurations added yet.</p>
          <p className="text-xs text-muted-foreground mt-1">Use the buttons above to add element definitions or packages.</p>
        </div>
      )}

      {hasItems && (
        <div className="space-y-2">
          {formData.elements.map((el, i) => (
            <div
              key={`el-${i}`}
              className="flex items-center justify-between gap-3 p-3 border rounded-md"
              data-testid={`config-item-element-${i}`}
            >
              <div className="flex items-center gap-2 min-w-0 flex-1">
                <Database className="w-4 h-4 text-muted-foreground shrink-0" />
                <span className="font-mono text-sm truncate" data-testid={`text-element-label-${i}`}>
                  {getElementLabel(el, i)}
                </span>
                <Badge variant="secondary" className="shrink-0">Element</Badge>
              </div>
              <div className="flex items-center gap-1 shrink-0">
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={() => openEditElement(i)}
                  data-testid={`button-edit-element-${i}`}
                >
                  <Pencil className="w-3.5 h-3.5" />
                </Button>
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={() => removeElement(i)}
                  data-testid={`button-remove-element-${i}`}
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </Button>
              </div>
            </div>
          ))}

          {formData.packages.map((pkg, i) => (
            <div
              key={`pkg-${i}`}
              className="flex items-center justify-between gap-3 p-3 border rounded-md"
              data-testid={`config-item-package-${i}`}
            >
              <div className="flex items-center gap-2 min-w-0 flex-1">
                <Package className="w-4 h-4 text-muted-foreground shrink-0" />
                <span className="font-mono text-sm truncate" data-testid={`text-package-label-${i}`}>
                  {getPackageLabel(pkg, i)}
                </span>
                <Badge variant="secondary" className="shrink-0">Package</Badge>
              </div>
              <div className="flex items-center gap-1 shrink-0">
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={() => openEditPackage(i)}
                  data-testid={`button-edit-package-${i}`}
                >
                  <Pencil className="w-3.5 h-3.5" />
                </Button>
                <Button
                  size="icon"
                  variant="ghost"
                  onClick={() => removePackage(i)}
                  data-testid={`button-remove-package-${i}`}
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}

      <ElementDefinitionSubDialog
        open={elementDialogOpen}
        onOpenChange={setElementDialogOpen}
        element={editingElement}
        onSave={saveElement}
        availableSpis={availableSpis}
        spisLoading={spisLoading}
      />

      <PackageDefinitionSubDialog
        open={packageDialogOpen}
        onOpenChange={setPackageDialogOpen}
        pkg={editingPackage}
        onSave={savePackage}
      />
    </div>
  );
}

function WizardSettingsStep({
  formData,
  setFormData,
}: {
  formData: FormData;
  setFormData: (fd: FormData) => void;
}) {
  const update = (partial: Partial<FormData>) => setFormData({ ...formData, ...partial });
  const [advancedOpen, setAdvancedOpen] = useState(false);

  const { data: applications = [], isLoading: appsLoading } = useQuery<Array<{ id: string; name: string }>>({
    queryKey: ['/api/rest/application', 'picker'],
    queryFn: async () => {
      const data = await apiClient.request<any>('/api/rest/application?count=100');
      const list = Array.isArray(data) ? data : (data.objects || data.content || []);
      return list.map((a: any) => ({ id: a.id, name: a.name }));
    },
    staleTime: 30000,
  });

  return (
    <div className="space-y-5">
      <div className="space-y-2">
        <Label>Application</Label>
        <Select
          value={formData.appNameOrId || '__none__'}
          onValueChange={(v) => update({ appNameOrId: v === '__none__' ? '' : v })}
        >
          <SelectTrigger data-testid="select-application">
            <SelectValue placeholder={appsLoading ? 'Loading applications...' : 'Select application'} />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="__none__" data-testid="option-app-none">
              None (Global Deployment)
            </SelectItem>
            {applications.map((app) => (
              <SelectItem key={app.id} value={app.id} data-testid={`option-app-${app.id}`}>
                {app.name || app.id}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        <p className="text-xs text-muted-foreground">
          Select an application to scope the deployment, or "None" for a global deployment.
        </p>
      </div>

      <div className="border-t pt-1">
        <button
          type="button"
          className="flex items-center gap-2 w-full py-2 text-sm font-medium hover:text-foreground text-muted-foreground transition-colors"
          onClick={() => setAdvancedOpen(v => !v)}
          data-testid="button-toggle-advanced-settings"
        >
          {advancedOpen ? <ChevronDown className="w-4 h-4 shrink-0" /> : <ChevronRight className="w-4 h-4 shrink-0" />}
          Advanced Settings
        </button>

        {advancedOpen && (
          <div className="space-y-5 pt-2">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <Checkbox
                  id="wizardUseDefaultRepos"
                  checked={formData.useDefaultRepositories}
                  onCheckedChange={(checked) => update({ useDefaultRepositories: !!checked })}
                  data-testid="checkbox-use-default-repos"
                />
                <Label htmlFor="wizardUseDefaultRepos" className="cursor-pointer">
                  Use default artifact repositories (includes Maven Central)
                </Label>
              </div>
              <p className="text-xs text-muted-foreground ml-6">
                Recommended: this should generally always be checked unless you have a specific reason to disable it.
              </p>
            </div>

            <div className="border-t pt-4">
              <RepositoryEditor
                repositories={formData.repositories}
                onChange={(repositories) => update({ repositories })}
              />
            </div>

            <div className="border-t pt-4 space-y-4">
              <PathClassPathsEditor
                value={formData.pathSpiBuiltins}
                onChange={(val) => update({ pathSpiBuiltins: val })}
                label="Deployment Path SPI Builtins"
                testIdPrefix="wizard-path-spi-builtins"
              />
              <p className="text-xs text-muted-foreground">
                Per-path builtin SPI configurations at the deployment level. The key is the element path, and the value is the list of builtin SPI names.
              </p>
              <PathKeyValueMapEditor
                value={formData.pathAttributes}
                onChange={(val) => update({ pathAttributes: val })}
                label="Deployment Path Attributes"
                testIdPrefix="wizard-path-attrs"
              />
              <p className="text-xs text-muted-foreground">
                Per-path custom attributes passed at load time via the AttributesLoader mechanism.
              </p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

interface DeploymentFormProps {
  mode: 'create' | 'edit';
  formData: FormData;
  setFormData: (fd: FormData) => void;
  deployment?: ElementDeployment | null;
  onElmUpload?: (file: File, largeObjectId: string) => void;
  elmUploading?: boolean;
}

function DeploymentForm({ mode, formData, setFormData, deployment, onElmUpload, elmUploading }: DeploymentFormProps) {
  const update = (partial: Partial<FormData>) => setFormData({ ...formData, ...partial });

  const { data: applications = [], isLoading: appsLoading } = useQuery<Array<{ id: string; name: string }>>({
    queryKey: ['/api/rest/application', 'picker'],
    queryFn: async () => {
      const data = await apiClient.request<any>('/api/rest/application?count=100');
      const list = Array.isArray(data) ? data : (data.objects || data.content || []);
      return list.map((a: any) => ({ id: a.id, name: a.name }));
    },
    staleTime: 30000,
  });

  const { data: availableSpis = [], isLoading: spisLoading } = useQuery<ElementSpi[]>({
    queryKey: ['/api/rest/elements/builtin_spi'],
    queryFn: async () => {
      const data = await apiClient.request<any>('/api/rest/elements/builtin_spi');
      if (Array.isArray(data)) return data;
      if (data && typeof data === 'object') {
        const keys = Object.keys(data);
        if (keys.length > 0 && keys.every(k => !isNaN(Number(k)))) {
          return keys.sort((a, b) => Number(a) - Number(b)).map(k => data[k]);
        }
        if (data.objects) return data.objects;
        if (data.content) return data.content;
      }
      return [];
    },
    staleTime: 60000,
  });

  return (
    <div className="space-y-5">
      {mode === 'create' && (
        <div className="space-y-2">
          <Label>Application</Label>
          <Select
            value={formData.appNameOrId || '__none__'}
            onValueChange={(v) => update({ appNameOrId: v === '__none__' ? '' : v })}
          >
            <SelectTrigger data-testid="select-application">
              <SelectValue placeholder={appsLoading ? 'Loading applications...' : 'Select application'} />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="__none__" data-testid="option-app-none">
                None (Global Deployment)
              </SelectItem>
              {applications.map((app) => (
                <SelectItem key={app.id} value={app.id} data-testid={`option-app-${app.id}`}>
                  {app.name || app.id}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <p className="text-xs text-muted-foreground">
            Select an application to scope the deployment, or "None" for a global deployment.
          </p>
        </div>
      )}

      {mode === 'edit' && (
        <div className="space-y-2">
          <Label htmlFor="state">Deployment State</Label>
          {formData.state === 'UNLOADED' ? (
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <Badge variant="outline" data-testid="badge-state-readonly">UNLOADED</Badge>
                <span className="text-xs text-muted-foreground">(system-managed)</span>
              </div>
              <p className="text-xs text-muted-foreground">
                UNLOADED is inferred by the system. Set to ENABLED to load, or DISABLED to prevent loading.
              </p>
              <Select value="" onValueChange={(v) => update({ state: v })}>
                <SelectTrigger data-testid="select-state-override">
                  <SelectValue placeholder="Change state..." />
                </SelectTrigger>
                <SelectContent>
                  {DEPLOYMENT_STATES_EDIT.map((s) => (
                    <SelectItem key={s} value={s} data-testid={`option-state-${s}`}>{s}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          ) : (
            <>
              <Select value={formData.state} onValueChange={(v) => update({ state: v })}>
                <SelectTrigger data-testid="select-state">
                  <SelectValue placeholder="Select state" />
                </SelectTrigger>
                <SelectContent>
                  {DEPLOYMENT_STATES_EDIT.map((s) => (
                    <SelectItem key={s} value={s} data-testid={`option-state-${s}`}>{s}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <p className="text-xs text-muted-foreground">
                ENABLED deployments will be loaded as soon as possible. DISABLED prevents loading. UNLOADED is inferred by the system.
              </p>
            </>
          )}
        </div>
      )}

      <div className="space-y-1">
        <div className="flex items-center gap-2">
          <Checkbox
            id="useDefaultRepos"
            checked={formData.useDefaultRepositories}
            onCheckedChange={(checked) => update({ useDefaultRepositories: !!checked })}
            data-testid="checkbox-use-default-repos"
          />
          <Label htmlFor="useDefaultRepos" className="cursor-pointer">
            Use default artifact repositories (includes Maven Central)
          </Label>
        </div>
        <p className="text-xs text-muted-foreground ml-6">
          Recommended: this should generally always be checked unless you have a specific reason to disable it.
        </p>
      </div>

      <div className="border-t pt-4">
        <ElementDefinitionEditor
          elements={formData.elements}
          onChange={(elements) => update({ elements })}
          availableSpis={availableSpis}
          spisLoading={spisLoading}
        />
      </div>

      <div className="border-t pt-4">
        <PackageDefinitionEditor
          packages={formData.packages}
          onChange={(packages) => update({ packages })}
        />
      </div>

      <div className="border-t pt-4">
        <RepositoryEditor
          repositories={formData.repositories}
          onChange={(repositories) => update({ repositories })}
        />
      </div>

      {mode === 'edit' && deployment?.elm && (
        <div className="border-t pt-4 space-y-3">
          <Label>ELM File</Label>
          <div className="flex items-start gap-3 p-3 border rounded-md bg-muted/30">
            <HardDrive className="w-5 h-5 mt-0.5 text-muted-foreground shrink-0" />
            <div className="flex-1 min-w-0 space-y-1 text-sm">
              {deployment.elm.path && (
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-medium truncate" data-testid="text-elm-path">{deployment.elm.path}</span>
                  {deployment.elm.state && (
                    <Badge variant="secondary" className="text-xs" data-testid="badge-elm-state">{deployment.elm.state}</Badge>
                  )}
                </div>
              )}
              {deployment.elm.mimeType && (
                <div className="text-muted-foreground" data-testid="text-elm-mime">MIME: {deployment.elm.mimeType}</div>
              )}
              <div className="text-xs text-muted-foreground truncate" data-testid="text-elm-id">
                ID: {deployment.elm.id}
              </div>
            </div>
          </div>
          <ElmDropZone
            onFile={(file) => {
              if (deployment.elm?.id && onElmUpload) {
                onElmUpload(file, deployment.elm.id);
              }
            }}
            uploading={!!elmUploading}
            buttonLabel="Upload New .elm File"
            testIdPrefix="edit-elm"
          />
        </div>
      )}

      <div className="border-t pt-4 space-y-4">
        <PathClassPathsEditor
          value={formData.pathSpiBuiltins}
          onChange={(val) => update({ pathSpiBuiltins: val })}
          label="Deployment Path SPI Builtins"
          testIdPrefix="deploy-path-spi-builtins"
        />
        <p className="text-xs text-muted-foreground">
          Per-path builtin SPI configurations for Elements in the uploaded ELM file. The key is the element path inside the ELM, and the value is the list of builtin SPI names.
        </p>

        <PathKeyValueMapEditor
          value={formData.pathAttributes}
          onChange={(val) => update({ pathAttributes: val })}
          label="Deployment Path Attributes"
          testIdPrefix="deploy-path-attrs"
        />
        <p className="text-xs text-muted-foreground">
          Per-path custom attributes for Elements in the ELM file. The key is the element path inside the ELM, and the value is a map of attributes passed at load time via the AttributesLoader mechanism.
        </p>
      </div>
    </div>
  );
}
