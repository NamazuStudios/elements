import { useState, useCallback } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiRequest, queryClient } from '@/lib/queryClient';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Plus, Edit, Trash2, Loader2, ChevronLeft, ChevronRight, RefreshCw, FileEdit, ChevronsUpDown, X, Check } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from '@/components/ui/dialog';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { TagsInput } from '@/components/TagsInput';
import { cn } from '@/lib/utils';
import { ProductBundleForm } from '@/components/ProductBundleForm';

interface ProductBundleReward {
  itemId: string;
  quantity: number | null;
}

interface ApplicationRef {
  id: string;
  name?: string;
}

interface ProductBundle {
  id: string;
  schema: string;
  application?: ApplicationRef;
  productId: string;
  displayName?: string;
  description?: string;
  productBundleRewards: ProductBundleReward[];
  display?: boolean;
  tags?: string[];
}

interface BundlePagination {
  offset: number;
  total: number;
  objects?: ProductBundle[];
  content?: ProductBundle[];
}

const DRAFT_KEY = 'draft_product_bundle_create';
const DRAFT_TTL_HOURS = 24;

function loadBundleDraft(): any | null {
  try {
    const stored = localStorage.getItem(DRAFT_KEY);
    if (!stored) return null;
    const parsed = JSON.parse(stored);
    if ((Date.now() - parsed.timestamp) / 3600000 > DRAFT_TTL_HOURS) {
      localStorage.removeItem(DRAFT_KEY);
      return null;
    }
    return parsed.data;
  } catch {
    return null;
  }
}

function saveBundleDraft(data: any) {
  try {
    localStorage.setItem(DRAFT_KEY, JSON.stringify({ data, timestamp: Date.now() }));
  } catch { /* ignore */ }
}

function deleteBundleDraft() {
  localStorage.removeItem(DRAFT_KEY);
}

export default function ProductBundles() {
  const { toast } = useToast();
  const [offset, setOffset] = useState(0);
  const [count] = useState(() => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  });

  // Filter state
  const [applicationFilter, setApplicationFilter] = useState('');
  const [applicationDisplayName, setApplicationDisplayName] = useState('');
  const [applicationFilterOpen, setApplicationFilterOpen] = useState(false);
  const [applicationSearch, setApplicationSearch] = useState('');
  const [schemaFilter, setSchemaFilter] = useState('');
  const [schemaFilterOpen, setSchemaFilterOpen] = useState(false);
  const [productIdFilter, setProductIdFilter] = useState('');
  const [tagFilters, setTagFilters] = useState<string[]>([]);

  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [hasDraft, setHasDraft] = useState(() => loadBundleDraft() !== null);
  const [createDraftData, setCreateDraftData] = useState<any>(null);
  const [currentFormData, setCurrentFormData] = useState<any>(null);
  const [editingBundle, setEditingBundle] = useState<ProductBundle | null>(null);
  const [deletingBundle, setDeletingBundle] = useState<ProductBundle | null>(null);

  const openCreateDialog = useCallback(() => {
    setCreateDraftData(loadBundleDraft());
    setCurrentFormData(null);
    setIsCreateDialogOpen(true);
  }, []);

  const closeCreateDialog = useCallback((skipDraftSave = false) => {
    if (!skipDraftSave && currentFormData) {
      saveBundleDraft(currentFormData);
      setHasDraft(true);
    }
    setIsCreateDialogOpen(false);
  }, [currentFormData]);

  // Application search for filter dropdown
  const { data: appSearchData } = useQuery({
    queryKey: ['/api/rest/application/filter-search', applicationSearch],
    queryFn: async () => {
      const params = new URLSearchParams({ offset: '0', total: '20' });
      if (applicationSearch) params.set('search', applicationSearch);
      const r = await apiRequest('GET', `/api/proxy/api/rest/application?${params}`);
      return r.json();
    },
    enabled: applicationFilterOpen,
  });
  const appOptions: { id: string; name?: string }[] = appSearchData?.objects ?? appSearchData?.content ?? [];

  // Schema list for filter dropdown
  const { data: schemasData } = useQuery({
    queryKey: ['/api/rest/product/sku/schema/filter'],
    queryFn: async () => {
      const r = await apiRequest('GET', '/api/proxy/api/rest/product/sku/schema?offset=0&count=100');
      return r.json();
    },
    enabled: schemaFilterOpen,
  });
  const schemaOptions: string[] = schemasData?.objects?.map((s: any) => s.schema) ?? [];

  const { data: bundleData, isLoading, isFetching } = useQuery<BundlePagination>({
    queryKey: ['/api/rest/product/bundle', offset, count, applicationFilter, schemaFilter, productIdFilter, tagFilters],
    queryFn: () => {
      const params = new URLSearchParams({ offset: String(offset), count: String(count) });
      if (applicationFilter) params.set('applicationNameOrId', applicationFilter);
      if (schemaFilter) params.set('schema', schemaFilter);
      if (productIdFilter) params.set('productId', productIdFilter);
      tagFilters.forEach(tag => params.append('tag', tag));
      return apiRequest('GET', `/api/rest/product/bundle?${params}`).then(r => r.json());
    },
  });

  const createMutation = useMutation({
    mutationFn: (data: any) => apiRequest('POST', '/api/rest/product/bundle', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/product/bundle'] });
      deleteBundleDraft();
      setHasDraft(false);
      setIsCreateDialogOpen(false);
      toast({ title: 'Success', description: 'Product Bundle created successfully' });
    },
    onError: (error: any) => {
      toast({ title: 'Error', description: error.message || 'Failed to create bundle', variant: 'destructive' });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: any }) =>
      apiRequest('PUT', `/api/rest/product/bundle/${id}`, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/product/bundle'] });
      setEditingBundle(null);
      toast({ title: 'Success', description: 'Product Bundle updated successfully' });
    },
    onError: (error: any) => {
      toast({ title: 'Error', description: error.message || 'Failed to update bundle', variant: 'destructive' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => apiRequest('DELETE', `/api/rest/product/bundle/${id}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/product/bundle'] });
      setDeletingBundle(null);
      toast({ title: 'Success', description: 'Product Bundle deleted successfully' });
    },
    onError: (error: any) => {
      toast({ title: 'Error', description: error.message || 'Failed to delete bundle', variant: 'destructive' });
    },
  });

  const bundles = bundleData?.content || bundleData?.objects || [];
  const totalPages = Math.ceil((bundleData?.total || 0) / count);
  const currentPage = Math.floor(offset / count) + 1;

  return (
    <div className="h-full flex flex-col p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold" data-testid="text-page-title">Product Bundles</h1>
          <p className="text-muted-foreground mt-1">Manage purchase provider bundles and item rewards</p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => queryClient.invalidateQueries({ queryKey: ['/api/rest/product/bundle'] })}
            disabled={isFetching}
            data-testid="button-refresh"
          >
            <RefreshCw className={cn('w-4 h-4 mr-2', isFetching && 'animate-spin')} />
            Refresh
          </Button>
          <Button onClick={openCreateDialog} data-testid="button-create-bundle">
            {hasDraft ? <FileEdit className="w-4 h-4 mr-2" /> : <Plus className="w-4 h-4 mr-2" />}
            {hasDraft ? 'Resume Draft' : 'Create Bundle'}
          </Button>
        </div>
        <Dialog open={isCreateDialogOpen} onOpenChange={(open) => { if (!open) closeCreateDialog(); }}>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>
                Create Product Bundle
                {createDraftData && (
                  <span className="ml-2 text-xs font-normal text-muted-foreground">(draft restored)</span>
                )}
              </DialogTitle>
            </DialogHeader>
            <ProductBundleForm
              mode="create"
              initialData={createDraftData}
              onSubmit={async (data) => { await createMutation.mutateAsync(data); }}
              onCancel={() => closeCreateDialog()}
              onFormChange={setCurrentFormData}
            />
          </DialogContent>
        </Dialog>
      </div>

      <div className="flex items-center gap-3 flex-wrap">
        {/* Application filter — dropdown */}
        <div className="flex items-center gap-1">
          <Popover open={applicationFilterOpen} onOpenChange={setApplicationFilterOpen}>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                role="combobox"
                className={cn('w-44 justify-between font-normal', !applicationDisplayName && 'text-muted-foreground')}
                data-testid="button-application-filter"
              >
                <span className="truncate">{applicationDisplayName || 'Filter by application…'}</span>
                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-64 p-0" align="start">
              <Command>
                <CommandInput
                  placeholder="Search applications…"
                  value={applicationSearch}
                  onValueChange={setApplicationSearch}
                />
                <CommandList>
                  <CommandEmpty>No applications found.</CommandEmpty>
                  <CommandGroup>
                    {appOptions.map(app => (
                      <CommandItem
                        key={app.id}
                        value={app.id}
                        onSelect={() => {
                          setApplicationFilter(app.id);
                          setApplicationDisplayName(app.name || app.id);
                          setApplicationFilterOpen(false);
                          setOffset(0);
                        }}
                      >
                        <Check className={cn('mr-2 h-4 w-4', applicationFilter === app.id ? 'opacity-100' : 'opacity-0')} />
                        {app.name || app.id}
                      </CommandItem>
                    ))}
                  </CommandGroup>
                </CommandList>
              </Command>
            </PopoverContent>
          </Popover>
          {applicationFilter && (
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 shrink-0"
              onClick={() => { setApplicationFilter(''); setApplicationDisplayName(''); setApplicationSearch(''); setOffset(0); }}
              data-testid="button-clear-application-filter"
            >
              <X className="h-3 w-3" />
            </Button>
          )}
        </div>

        {/* Schema filter — dropdown */}
        <div className="flex items-center gap-1">
          <Popover open={schemaFilterOpen} onOpenChange={setSchemaFilterOpen}>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                role="combobox"
                className={cn('w-44 justify-between font-normal', !schemaFilter && 'text-muted-foreground')}
                data-testid="button-schema-filter"
              >
                <span className="truncate">{schemaFilter || 'Filter by schema…'}</span>
                <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-64 p-0" align="start">
              <Command>
                <CommandInput placeholder="Search schemas…" />
                <CommandList>
                  <CommandEmpty>No schemas found.</CommandEmpty>
                  <CommandGroup>
                    {schemaOptions.map(s => (
                      <CommandItem
                        key={s}
                        value={s}
                        onSelect={() => {
                          setSchemaFilter(s);
                          setSchemaFilterOpen(false);
                          setOffset(0);
                        }}
                      >
                        <Check className={cn('mr-2 h-4 w-4', schemaFilter === s ? 'opacity-100' : 'opacity-0')} />
                        {s}
                      </CommandItem>
                    ))}
                  </CommandGroup>
                </CommandList>
              </Command>
            </PopoverContent>
          </Popover>
          {schemaFilter && (
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 shrink-0"
              onClick={() => { setSchemaFilter(''); setOffset(0); }}
              data-testid="button-clear-schema-filter"
            >
              <X className="h-3 w-3" />
            </Button>
          )}
        </div>

        {/* Product ID filter */}
        <Input
          placeholder="Filter by product ID…"
          value={productIdFilter}
          onChange={(e) => { setProductIdFilter(e.target.value); setOffset(0); }}
          className="w-44"
          data-testid="input-productid-filter"
        />

        {/* Tags filter — multi-tag */}
        <div className="w-72" data-testid="input-tag-filter">
          <TagsInput
            value={tagFilters}
            onChange={(tags) => { setTagFilters(tags); setOffset(0); }}
            placeholder="Filter by tag…"
          />
        </div>
      </div>

      <Card>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
            </div>
          ) : bundles.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <p className="text-muted-foreground">No product bundles found</p>
              <p className="text-sm text-muted-foreground mt-2">Create your first bundle to get started</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>Schema</TableHead>
                  <TableHead>Application</TableHead>
                  <TableHead>Product ID</TableHead>
                  <TableHead>Display Name</TableHead>
                  <TableHead>Rewards</TableHead>
                  <TableHead>Tags</TableHead>
                  <TableHead className="w-[100px] text-center sticky right-0 bg-background border-l z-10">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {bundles.map((bundle) => (
                  <TableRow key={bundle.id}>
                    <TableCell className="font-mono text-xs max-w-[120px] truncate" data-testid={`text-bundle-id-${bundle.id}`}>
                      {bundle.id}
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline" data-testid={`badge-schema-${bundle.id}`}>
                        {bundle.schema}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm max-w-[140px] truncate">
                      {bundle.application?.name || bundle.application?.id}
                    </TableCell>
                    <TableCell className="font-mono text-sm">
                      {bundle.productId}
                    </TableCell>
                    <TableCell className="text-sm max-w-[160px] truncate">
                      {bundle.displayName || <span className="text-muted-foreground italic">—</span>}
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary" data-testid={`badge-rewards-${bundle.id}`}>
                        {bundle.productBundleRewards?.length ?? 0} reward{(bundle.productBundleRewards?.length ?? 0) !== 1 ? 's' : ''}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {bundle.tags?.map(tag => (
                          <Badge key={tag} variant="outline" className="text-xs">
                            {tag}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell className="w-[100px] sticky right-0 bg-background border-l z-10">
                      <div className="flex items-center justify-center gap-1">
                        {/* Edit */}
                        <Dialog
                          open={editingBundle?.id === bundle.id}
                          onOpenChange={(open) => { if (!open) setEditingBundle(null); }}
                        >
                          <DialogTrigger asChild>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={() => setEditingBundle(bundle)}
                              data-testid={`button-edit-${bundle.id}`}
                            >
                              <Edit className="w-4 h-4" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                            <DialogHeader>
                              <DialogTitle>Edit Product Bundle</DialogTitle>
                            </DialogHeader>
                            {editingBundle?.id === bundle.id && (
                              <ProductBundleForm
                                mode="update"
                                initialData={editingBundle}
                                onSubmit={async (data) => {
                                  await updateMutation.mutateAsync({ id: editingBundle.id, data });
                                }}
                                onCancel={() => setEditingBundle(null)}
                              />
                            )}
                          </DialogContent>
                        </Dialog>

                        {/* Delete */}
                        <Dialog
                          open={deletingBundle?.id === bundle.id}
                          onOpenChange={(open) => { if (!open) setDeletingBundle(null); }}
                        >
                          <DialogTrigger asChild>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={() => setDeletingBundle(bundle)}
                              data-testid={`button-delete-${bundle.id}`}
                            >
                              <Trash2 className="w-4 h-4" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent>
                            <DialogHeader>
                              <DialogTitle>Delete Product Bundle</DialogTitle>
                            </DialogHeader>
                            <div className="py-4">
                              <p>Are you sure you want to delete this bundle?</p>
                              <p className="text-sm text-muted-foreground mt-2 font-mono">
                                {bundle.schema} / {bundle.productId}
                              </p>
                            </div>
                            <DialogFooter>
                              <Button
                                variant="outline"
                                onClick={() => setDeletingBundle(null)}
                                data-testid="button-cancel-delete"
                              >
                                Cancel
                              </Button>
                              <Button
                                variant="destructive"
                                onClick={() => deleteMutation.mutate(bundle.id)}
                                disabled={deleteMutation.isPending}
                                data-testid="button-confirm-delete"
                              >
                                {deleteMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                                Delete
                              </Button>
                            </DialogFooter>
                          </DialogContent>
                        </Dialog>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {bundleData && totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Showing {offset + 1}–{Math.min(offset + count, bundleData.total)} of {bundleData.total}
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setOffset(Math.max(0, offset - count))}
              disabled={offset === 0}
              data-testid="button-prev-page"
            >
              <ChevronLeft className="w-4 h-4 mr-1" />
              Previous
            </Button>
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted-foreground">
                Page {currentPage} of {totalPages}
              </span>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setOffset(offset + count)}
              disabled={offset + count >= bundleData.total}
              data-testid="button-next-page"
            >
              Next
              <ChevronRight className="w-4 h-4 ml-1" />
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
