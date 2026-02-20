import { useState, useRef } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Loader2, Package, Trash2, AlertCircle, Plus, Pencil } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import { queryClient } from '@/lib/queryClient';
import { ItemSearchDialog } from './ItemSearchDialog';

interface InventoryViewerProps {
  userId: string;
  username?: string;
}

export function InventoryViewer({ userId, username }: InventoryViewerProps) {
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState<'fungible' | 'distinct'>('fungible');
  const [fungibleSearch, setFungibleSearch] = useState<string>('');
  const [distinctSearch, setDistinctSearch] = useState<string>('');

  // Pagination state - page number only (items per page from global settings)
  const [fungiblePage, setFungiblePage] = useState(0);
  const [distinctPage, setDistinctPage] = useState(0);

  // Read pagination limit from global settings
  const getPageSize = () => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  };
  
  const itemsPerPage = getPageSize();

  // State for creating inventory
  const [createFungibleOpen, setCreateFungibleOpen] = useState(false);
  const [createDistinctOpen, setCreateDistinctOpen] = useState(false);
  const [fungibleItemSearchOpen, setFungibleItemSearchOpen] = useState(false);
  const [distinctItemSearchOpen, setDistinctItemSearchOpen] = useState(false);
  const [selectedFungibleItemId, setSelectedFungibleItemId] = useState<string>('');
  const [selectedFungibleItemName, setSelectedFungibleItemName] = useState<string>('');
  const [selectedDistinctItemId, setSelectedDistinctItemId] = useState<string>('');
  const [selectedDistinctItemName, setSelectedDistinctItemName] = useState<string>('');
  const [fungibleQuantity, setFungibleQuantity] = useState<string>('1');
  const [distinctMetadata, setDistinctMetadata] = useState<Array<{ key: string; value: string }>>([]);

  const fungibleSelectionMade = useRef(false);
  const distinctSelectionMade = useRef(false);

  // State for editing inventory
  const [editFungibleOpen, setEditFungibleOpen] = useState(false);
  const [editDistinctOpen, setEditDistinctOpen] = useState(false);
  const [editingFungibleItem, setEditingFungibleItem] = useState<any>(null);
  const [editingDistinctItem, setEditingDistinctItem] = useState<any>(null);
  const [editFungibleQuantity, setEditFungibleQuantity] = useState<string>('1');
  const [editDistinctMetadata, setEditDistinctMetadata] = useState<Array<{ key: string; value: string }>>([]);

  // Fetch Advanced (Fungible) Inventory
  const { data: fungibleResponse, isLoading: fungibleLoading, error: fungibleError } = useQuery({
    queryKey: ['/api/rest/inventory/advanced', userId, fungiblePage, itemsPerPage, fungibleSearch],
    enabled: !!userId,
    queryFn: async () => {
      const params = new URLSearchParams();
      if (userId) params.set('userId', userId);
      params.set('offset', (fungiblePage * itemsPerPage).toString());
      params.set('count', itemsPerPage.toString());
      if (fungibleSearch) params.set('search', `item.name:${fungibleSearch}`);
      
      const url = `/api/rest/inventory/advanced?${params.toString()}`;
      const response = await apiClient.request<any>(url);
      return response;
    },
  });

  // Fetch Distinct Inventory
  const { data: distinctResponse, isLoading: distinctLoading, error: distinctError } = useQuery({
    queryKey: ['/api/rest/inventory/distinct', userId, distinctPage, itemsPerPage, distinctSearch],
    enabled: !!userId,
    queryFn: async () => {
      const params = new URLSearchParams();
      if (userId) params.set('userId', userId);
      params.set('offset', (distinctPage * itemsPerPage).toString());
      params.set('count', itemsPerPage.toString());
      if (distinctSearch) params.set('search', `item.name:${distinctSearch}`);
      
      const url = `/api/rest/inventory/distinct?${params.toString()}`;
      const response = await apiClient.request<any>(url);
      return response;
    },
  });

  // Extract inventory data and pagination info
  const fungibleInventory = fungibleResponse?.objects || [];
  const fungibleTotal = fungibleResponse?.total || 0;
  const fungibleTotalPages = Math.ceil(fungibleTotal / itemsPerPage);

  const distinctInventory = distinctResponse?.objects || [];
  const distinctTotal = distinctResponse?.total || 0;
  const distinctTotalPages = Math.ceil(distinctTotal / itemsPerPage);

  // Create fungible inventory mutation
  const createFungibleMutation = useMutation({
    mutationFn: async (data: any) => {
      return await apiClient.request('/api/rest/inventory/advanced', {
        method: 'POST',
        body: JSON.stringify(data),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/inventory/advanced', userId] });
      toast({
        title: 'Success',
        description: 'Fungible inventory item created successfully',
      });
      setCreateFungibleOpen(false);
      setSelectedFungibleItemId('');
      setSelectedFungibleItemName('');
      setFungibleQuantity('1');
    },
    onError: (error: Error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create fungible inventory',
        variant: 'destructive',
      });
    },
  });

  // Create distinct inventory mutation
  const createDistinctMutation = useMutation({
    mutationFn: async (data: any) => {
      return await apiClient.request('/api/rest/inventory/distinct', {
        method: 'POST',
        body: JSON.stringify(data),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/inventory/distinct', userId] });
      toast({
        title: 'Success',
        description: 'Distinct inventory item created successfully',
      });
      setCreateDistinctOpen(false);
      setSelectedDistinctItemId('');
      setSelectedDistinctItemName('');
      setDistinctMetadata([]);
    },
    onError: (error: Error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create distinct inventory',
        variant: 'destructive',
      });
    },
  });

  // Handlers for creating inventory
  const handleCreateFungible = () => {
    if (!userId) {
      toast({
        title: 'Error',
        description: 'Profile data not loaded. Please try again.',
        variant: 'destructive',
      });
      return;
    }

    if (!selectedFungibleItemId) {
      toast({
        title: 'Error',
        description: 'Please select an item',
        variant: 'destructive',
      });
      return;
    }

    const quantity = parseInt(fungibleQuantity);
    if (isNaN(quantity) || quantity <= 0) {
      toast({
        title: 'Error',
        description: 'Quantity must be a positive number',
        variant: 'destructive',
      });
      return;
    }

    createFungibleMutation.mutate({
      userId,
      itemId: selectedFungibleItemId,
      quantity,
    });
  };

  const handleCreateDistinct = () => {
    if (!userId) {
      toast({
        title: 'Error',
        description: 'Profile data not loaded. Please try again.',
        variant: 'destructive',
      });
      return;
    }

    if (!selectedDistinctItemId) {
      toast({
        title: 'Error',
        description: 'Please select an item',
        variant: 'destructive',
      });
      return;
    }

    const metadata = distinctMetadata.reduce((acc, entry) => {
      if (entry.key && entry.value) {
        try {
          acc[entry.key] = JSON.parse(entry.value);
        } catch {
          acc[entry.key] = entry.value;
        }
      }
      return acc;
    }, {} as Record<string, any>);

    createDistinctMutation.mutate({
      userId,
      itemId: selectedDistinctItemId,
      metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
    });
  };

  // Delete mutation for fungible inventory
  const deleteFungibleMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.request(`/api/rest/inventory/advanced/${id}`, { method: 'DELETE' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/inventory/advanced', userId] });
      toast({
        title: 'Success',
        description: 'Fungible inventory item deleted successfully',
      });
    },
    onError: (error: Error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete inventory item',
        variant: 'destructive',
      });
    },
  });

  // Delete mutation for distinct inventory
  const deleteDistinctMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.request(`/api/rest/inventory/distinct/${id}`, { method: 'DELETE' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/inventory/distinct', userId] });
      toast({
        title: 'Success',
        description: 'Distinct inventory item deleted successfully',
      });
    },
    onError: (error: Error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete inventory item',
        variant: 'destructive',
      });
    },
  });

  // Update mutation for fungible inventory
  const updateFungibleMutation = useMutation({
    mutationFn: async ({ id, data }: { id: string; data: any }) => {
      return await apiClient.request(`/api/rest/inventory/advanced/${id}`, {
        method: 'PATCH',
        body: JSON.stringify(data),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/inventory/advanced', userId] });
      toast({
        title: 'Success',
        description: 'Fungible inventory item updated successfully',
      });
      setEditFungibleOpen(false);
      setEditingFungibleItem(null);
    },
    onError: (error: Error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to update fungible inventory',
        variant: 'destructive',
      });
    },
  });

  // Update mutation for distinct inventory (using PUT)
  const updateDistinctMutation = useMutation({
    mutationFn: async ({ id, data }: { id: string; data: any }) => {
      return await apiClient.request(`/api/rest/inventory/distinct/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/inventory/distinct', userId] });
      toast({
        title: 'Success',
        description: 'Distinct inventory item updated successfully',
      });
      setEditDistinctOpen(false);
      setEditingDistinctItem(null);
    },
    onError: (error: Error) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to update distinct inventory',
        variant: 'destructive',
      });
    },
  });

  // Handler for opening edit fungible dialog
  const handleEditFungible = (item: any) => {
    setEditingFungibleItem(item);
    setEditFungibleQuantity(item.quantity?.toString() || '1');
    setEditFungibleOpen(true);
  };

  // Handler for opening edit distinct dialog
  const handleEditDistinct = (item: any) => {
    setEditingDistinctItem(item);
    // Convert metadata object to array of key-value pairs
    if (item.metadata && typeof item.metadata === 'object') {
      const entries = Object.entries(item.metadata).map(([key, value]) => ({
        key,
        value: typeof value === 'string' ? value : JSON.stringify(value),
      }));
      setEditDistinctMetadata(entries);
    } else {
      setEditDistinctMetadata([]);
    }
    setEditDistinctOpen(true);
  };

  // Handler for updating fungible inventory
  const handleUpdateFungible = () => {
    if (!editingFungibleItem) return;

    const newQuantity = parseInt(editFungibleQuantity);
    if (isNaN(newQuantity) || newQuantity <= 0) {
      toast({
        title: 'Error',
        description: 'Quantity must be a positive number',
        variant: 'destructive',
      });
      return;
    }

    const currentQuantity = editingFungibleItem.quantity || 0;
    const quantityDelta = newQuantity - currentQuantity;

    updateFungibleMutation.mutate({
      id: editingFungibleItem.id,
      data: { 
        userId,
        quantityDelta 
      },
    });
  };

  // Handler for updating distinct inventory
  const handleUpdateDistinct = () => {
    if (!editingDistinctItem) return;

    const metadata = editDistinctMetadata.reduce((acc, entry) => {
      if (entry.key && entry.value) {
        try {
          acc[entry.key] = JSON.parse(entry.value);
        } catch {
          acc[entry.key] = entry.value;
        }
      }
      return acc;
    }, {} as Record<string, any>);

    updateDistinctMutation.mutate({
      id: editingDistinctItem.id,
      data: {
        userId: userId,
        metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
      },
    });
  };

  return (
    <Tabs value={activeTab} onValueChange={(v) => setActiveTab(v as 'fungible' | 'distinct')} className="w-full">
      <TabsList className="grid w-full grid-cols-2">
        <TabsTrigger value="fungible" data-testid="tab-fungible-inventory">
          Fungible Inventory
        </TabsTrigger>
        <TabsTrigger value="distinct" data-testid="tab-distinct-inventory">
          Distinct Inventory
        </TabsTrigger>
      </TabsList>

      {/* Fungible Inventory Tab */}
      <TabsContent value="fungible" className="mt-4">
        <div className="mb-4 flex flex-wrap items-center gap-3">
          <Button
            onClick={() => setCreateFungibleOpen(true)}
            data-testid="button-create-fungible"
          >
            <Plus className="w-4 h-4 mr-2" />
            Add Fungible Item
          </Button>
          <Input
            placeholder="Search fungible inventory..."
            value={fungibleSearch}
            onChange={(e) => {
              setFungibleSearch(e.target.value);
              setFungiblePage(0);
            }}
            className="max-w-sm"
            data-testid="input-search-fungible"
          />
        </div>
        <ScrollArea className="h-[500px]">
          {fungibleLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
            </div>
          ) : fungibleError ? (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                Failed to load fungible inventory: {(fungibleError as Error).message}
              </AlertDescription>
            </Alert>
          ) : fungibleInventory && fungibleInventory.length > 0 ? (
            <div className="space-y-3 pr-4">
              {fungibleInventory.map((item: any) => (
                <Card key={item.id} data-testid={`card-fungible-${item.id}`}>
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="text-base">
                          {item.item?.displayName || item.item?.name || 'Unknown Item'}
                        </CardTitle>
                        {item.item?.description && (
                          <CardDescription className="mt-1">{item.item.description}</CardDescription>
                        )}
                      </div>
                      <div className="flex gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEditFungible(item)}
                          data-testid={`button-edit-fungible-${item.id}`}
                        >
                          <Pencil className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => deleteFungibleMutation.mutate(item.id)}
                          disabled={deleteFungibleMutation.isPending}
                          data-testid={`button-delete-fungible-${item.id}`}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="space-y-2">
                      <div className="flex items-center gap-4 text-sm">
                        <div>
                          <span className="text-muted-foreground">Quantity:</span>{' '}
                          <Badge variant="secondary" data-testid={`text-quantity-${item.id}`}>
                            {item.quantity}
                          </Badge>
                        </div>
                      </div>
                      <div className="grid grid-cols-1 gap-1.5 text-xs">
                        {item.id && (
                          <div>
                            <span className="text-muted-foreground">Inventory Item ID:</span>{' '}
                            <code className="bg-muted px-1 rounded">{item.id}</code>
                          </div>
                        )}
                        {item.item?.id && (
                          <div>
                            <span className="text-muted-foreground">Item ID:</span>{' '}
                            <code className="bg-muted px-1 rounded">{item.item.id}</code>
                          </div>
                        )}
                        {item.item?.name && (
                          <div>
                            <span className="text-muted-foreground">Item Name:</span>{' '}
                            <code className="bg-muted px-1 rounded">{item.item.name}</code>
                          </div>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Package className="w-12 h-12 text-muted-foreground mb-3" />
              <p className="text-sm text-muted-foreground">No fungible inventory items found</p>
            </div>
          )}
        </ScrollArea>
        {fungibleTotal > 0 && (
          <div className="flex items-center justify-between mt-4">
            <div className="text-sm text-muted-foreground">
              Showing {fungiblePage * itemsPerPage + 1} to{' '}
              {Math.min((fungiblePage + 1) * itemsPerPage, fungibleTotal)} of {fungibleTotal} items
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setFungiblePage(Math.max(0, fungiblePage - 1))}
                disabled={fungiblePage === 0}
                data-testid="button-fungible-prev"
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {fungiblePage + 1} of {fungibleTotalPages || 1}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setFungiblePage(fungiblePage + 1)}
                disabled={fungiblePage >= fungibleTotalPages - 1}
                data-testid="button-fungible-next"
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </TabsContent>

      {/* Distinct Inventory Tab */}
      <TabsContent value="distinct" className="mt-4">
        <div className="mb-4 flex flex-wrap items-center gap-3">
          <Button
            onClick={() => setCreateDistinctOpen(true)}
            data-testid="button-create-distinct"
          >
            <Plus className="w-4 h-4 mr-2" />
            Add Distinct Item
          </Button>
          <Input
            placeholder="Search distinct inventory..."
            value={distinctSearch}
            onChange={(e) => {
              setDistinctSearch(e.target.value);
              setDistinctPage(0);
            }}
            className="max-w-sm"
            data-testid="input-search-distinct"
          />
        </div>
        <ScrollArea className="h-[500px]">
          {distinctLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
            </div>
          ) : distinctError ? (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                Failed to load distinct inventory: {(distinctError as Error).message}
              </AlertDescription>
            </Alert>
          ) : distinctInventory && distinctInventory.length > 0 ? (
            <div className="space-y-3 pr-4">
              {distinctInventory.map((item: any) => (
                <Card key={item.id} data-testid={`card-distinct-${item.id}`}>
                  <CardHeader className="pb-3">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="text-base">
                          {item.item?.displayName || item.item?.name || 'Unknown Item'}
                        </CardTitle>
                        {item.item?.description && (
                          <CardDescription className="mt-1">{item.item.description}</CardDescription>
                        )}
                      </div>
                      <div className="flex gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleEditDistinct(item)}
                          data-testid={`button-edit-distinct-${item.id}`}
                        >
                          <Pencil className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => deleteDistinctMutation.mutate(item.id)}
                          disabled={deleteDistinctMutation.isPending}
                          data-testid={`button-delete-distinct-${item.id}`}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="space-y-2">
                      <div className="grid grid-cols-1 gap-1.5 text-xs">
                        {item.id && (
                          <div>
                            <span className="text-muted-foreground">Inventory Item ID:</span>{' '}
                            <code className="bg-muted px-1 rounded">{item.id}</code>
                          </div>
                        )}
                        {item.item?.id && (
                          <div>
                            <span className="text-muted-foreground">Item ID:</span>{' '}
                            <code className="bg-muted px-1 rounded">{item.item.id}</code>
                          </div>
                        )}
                        {item.item?.name && (
                          <div>
                            <span className="text-muted-foreground">Item Name:</span>{' '}
                            <code className="bg-muted px-1 rounded">{item.item.name}</code>
                          </div>
                        )}
                      </div>
                      {item.metadata && Object.keys(item.metadata).length > 0 && (
                        <div className="text-sm">
                          <span className="text-muted-foreground">Metadata:</span>
                          <div className="mt-1 bg-muted rounded-md p-2">
                            <pre className="text-xs overflow-auto max-h-32">
                              {JSON.stringify(item.metadata, null, 2)}
                            </pre>
                          </div>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Package className="w-12 h-12 text-muted-foreground mb-3" />
              <p className="text-sm text-muted-foreground">No distinct inventory items found</p>
            </div>
          )}
        </ScrollArea>
        {distinctTotal > 0 && (
          <div className="flex items-center justify-between mt-4">
            <div className="text-sm text-muted-foreground">
              Showing {distinctPage * itemsPerPage + 1} to{' '}
              {Math.min((distinctPage + 1) * itemsPerPage, distinctTotal)} of {distinctTotal} items
            </div>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setDistinctPage(Math.max(0, distinctPage - 1))}
                disabled={distinctPage === 0}
                data-testid="button-distinct-prev"
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {distinctPage + 1} of {distinctTotalPages || 1}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setDistinctPage(distinctPage + 1)}
                disabled={distinctPage >= distinctTotalPages - 1}
                data-testid="button-distinct-next"
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </TabsContent>

      {/* Create Fungible Inventory Dialog */}
      <Dialog open={createFungibleOpen} onOpenChange={(open) => {
        setCreateFungibleOpen(open);
        if (!open) {
          setSelectedFungibleItemId('');
          setSelectedFungibleItemName('');
          setFungibleQuantity('1');
        }
      }}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Add Fungible Inventory Item</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            {/* Item Selector */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Item *</label>
              <Button
                variant="outline"
                onClick={() => setFungibleItemSearchOpen(true)}
                className="w-full justify-start"
                data-testid="button-item-picker"
              >
                {selectedFungibleItemName || 'Select item...'}
              </Button>
            </div>

            {/* Quantity Input */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Quantity *</label>
              <Input
                type="number"
                min="1"
                value={fungibleQuantity}
                onChange={(e) => setFungibleQuantity(e.target.value)}
                placeholder="Enter quantity"
                data-testid="input-fungible-quantity"
              />
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2 justify-end pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setCreateFungibleOpen(false);
                  setSelectedFungibleItemId('');
                  setSelectedFungibleItemName('');
                  setFungibleQuantity('1');
                }}
                data-testid="button-cancel-fungible"
              >
                Cancel
              </Button>
              <Button
                type="button"
                onClick={handleCreateFungible}
                disabled={!userId || createFungibleMutation.isPending}
                data-testid="button-submit-fungible"
              >
                {createFungibleMutation.isPending ? 'Creating...' : 'Create'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Create Distinct Inventory Dialog */}
      <Dialog open={createDistinctOpen} onOpenChange={(open) => {
        setCreateDistinctOpen(open);
        if (!open) {
          setSelectedDistinctItemId('');
          setSelectedDistinctItemName('');
          setDistinctMetadata([]);
        }
      }}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Add Distinct Inventory Item</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            {/* Item Selector */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Item *</label>
              <Button
                variant="outline"
                onClick={() => setDistinctItemSearchOpen(true)}
                className="w-full justify-start"
                data-testid="button-item-picker-distinct"
              >
                {selectedDistinctItemName || 'Select item...'}
              </Button>
            </div>

            {/* Metadata Editor */}
            <div className="space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-sm font-medium">Metadata (Optional)</label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setDistinctMetadata([...distinctMetadata, { key: '', value: '' }])}
                  data-testid="button-add-distinct-metadata"
                >
                  <Plus className="w-3 h-3 mr-1" />
                  Add Entry
                </Button>
              </div>
              <div className="space-y-2">
                {distinctMetadata.map((entry, index) => (
                  <div key={index} className="flex gap-2 items-start">
                    <Input
                      placeholder="Key"
                      value={entry.key}
                      onChange={(e) => {
                        const updated = [...distinctMetadata];
                        updated[index].key = e.target.value;
                        setDistinctMetadata(updated);
                      }}
                      className="flex-1"
                      data-testid={`input-distinct-metadata-key-${index}`}
                    />
                    <Input
                      placeholder="Value (JSON or text)"
                      value={entry.value}
                      onChange={(e) => {
                        const updated = [...distinctMetadata];
                        updated[index].value = e.target.value;
                        setDistinctMetadata(updated);
                      }}
                      className="flex-1"
                      data-testid={`input-distinct-metadata-value-${index}`}
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => {
                        setDistinctMetadata(distinctMetadata.filter((_, i) => i !== index));
                      }}
                      data-testid={`button-remove-distinct-metadata-${index}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                ))}
                {distinctMetadata.length === 0 && (
                  <p className="text-sm text-muted-foreground">No metadata entries. Click "Add Entry" to add key-value pairs.</p>
                )}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2 justify-end pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => {
                  setCreateDistinctOpen(false);
                  setSelectedDistinctItemId('');
                  setSelectedDistinctItemName('');
                  setDistinctMetadata([]);
                }}
                data-testid="button-cancel-distinct"
              >
                Cancel
              </Button>
              <Button
                type="button"
                onClick={handleCreateDistinct}
                disabled={!userId || createDistinctMutation.isPending}
                data-testid="button-submit-distinct"
              >
                {createDistinctMutation.isPending ? 'Creating...' : 'Create'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Fungible Inventory Dialog */}
      <Dialog open={editFungibleOpen} onOpenChange={(open) => {
        setEditFungibleOpen(open);
        if (!open) {
          setEditingFungibleItem(null);
          setEditFungibleQuantity('1');
        }
      }}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Edit Fungible Inventory</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            {/* Item Display (non-editable) */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Item</label>
              <div className="p-3 bg-muted rounded-md">
                <p className="font-medium">
                  {editingFungibleItem?.item?.displayName || editingFungibleItem?.item?.name || 'Unknown Item'}
                </p>
                {editingFungibleItem?.item?.description && (
                  <p className="text-sm text-muted-foreground mt-1">{editingFungibleItem.item.description}</p>
                )}
              </div>
            </div>

            {/* Quantity Input */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Quantity *</label>
              <Input
                type="number"
                min="1"
                value={editFungibleQuantity}
                onChange={(e) => setEditFungibleQuantity(e.target.value)}
                placeholder="Enter quantity"
                data-testid="input-edit-fungible-quantity"
              />
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2 justify-end pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setEditFungibleOpen(false)}
                data-testid="button-cancel-edit-fungible"
              >
                Cancel
              </Button>
              <Button
                type="button"
                onClick={handleUpdateFungible}
                disabled={updateFungibleMutation.isPending}
                data-testid="button-submit-edit-fungible"
              >
                {updateFungibleMutation.isPending ? 'Updating...' : 'Update'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Edit Distinct Inventory Dialog */}
      <Dialog open={editDistinctOpen} onOpenChange={(open) => {
        setEditDistinctOpen(open);
        if (!open) {
          setEditingDistinctItem(null);
          setEditDistinctMetadata([]);
        }
      }}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Edit Distinct Inventory</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            {/* Item Display (non-editable) */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Item</label>
              <div className="p-3 bg-muted rounded-md">
                <p className="font-medium">
                  {editingDistinctItem?.item?.displayName || editingDistinctItem?.item?.name || 'Unknown Item'}
                </p>
                {editingDistinctItem?.item?.description && (
                  <p className="text-sm text-muted-foreground mt-1">{editingDistinctItem.item.description}</p>
                )}
              </div>
            </div>

            {/* Metadata Editor */}
            <div className="space-y-2">
              <div className="flex justify-between items-center">
                <label className="text-sm font-medium">Metadata (Optional)</label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setEditDistinctMetadata([...editDistinctMetadata, { key: '', value: '' }])}
                  data-testid="button-add-edit-distinct-metadata"
                >
                  <Plus className="w-3 h-3 mr-1" />
                  Add Entry
                </Button>
              </div>
              <div className="space-y-2">
                {editDistinctMetadata.map((entry, index) => (
                  <div key={index} className="flex gap-2 items-start">
                    <Input
                      placeholder="Key"
                      value={entry.key}
                      onChange={(e) => {
                        const updated = [...editDistinctMetadata];
                        updated[index].key = e.target.value;
                        setEditDistinctMetadata(updated);
                      }}
                      className="flex-1"
                      data-testid={`input-edit-distinct-metadata-key-${index}`}
                    />
                    <Input
                      placeholder="Value (JSON or text)"
                      value={entry.value}
                      onChange={(e) => {
                        const updated = [...editDistinctMetadata];
                        updated[index].value = e.target.value;
                        setEditDistinctMetadata(updated);
                      }}
                      className="flex-1"
                      data-testid={`input-edit-distinct-metadata-value-${index}`}
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      onClick={() => {
                        setEditDistinctMetadata(editDistinctMetadata.filter((_, i) => i !== index));
                      }}
                      data-testid={`button-remove-edit-distinct-metadata-${index}`}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                ))}
                {editDistinctMetadata.length === 0 && (
                  <p className="text-sm text-muted-foreground">No metadata entries. Click "Add Entry" to add key-value pairs.</p>
                )}
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-2 justify-end pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setEditDistinctOpen(false)}
                data-testid="button-cancel-edit-distinct"
              >
                Cancel
              </Button>
              <Button
                type="button"
                onClick={handleUpdateDistinct}
                disabled={updateDistinctMutation.isPending}
                data-testid="button-submit-edit-distinct"
              >
                {updateDistinctMutation.isPending ? 'Updating...' : 'Update'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      {/* Item Search Dialogs */}
      <ItemSearchDialog
        open={fungibleItemSearchOpen}
        onOpenChange={(open) => {
          if (!open && !fungibleSelectionMade.current) {
            setSelectedFungibleItemId('');
            setSelectedFungibleItemName('');
          }
          fungibleSelectionMade.current = false;
          setFungibleItemSearchOpen(open);
        }}
        onSelect={(itemId, item) => {
          fungibleSelectionMade.current = true;
          setSelectedFungibleItemId(itemId);
          setSelectedFungibleItemName(item.name || item.displayName || itemId);
        }}
        category="FUNGIBLE"
        title="Search Fungible Items"
        description="Select a fungible item to add to inventory"
      />

      <ItemSearchDialog
        open={distinctItemSearchOpen}
        onOpenChange={(open) => {
          if (!open && !distinctSelectionMade.current) {
            setSelectedDistinctItemId('');
            setSelectedDistinctItemName('');
          }
          distinctSelectionMade.current = false;
          setDistinctItemSearchOpen(open);
        }}
        onSelect={(itemId, item) => {
          distinctSelectionMade.current = true;
          setSelectedDistinctItemId(itemId);
          setSelectedDistinctItemName(item.name || item.displayName || itemId);
        }}
        category="DISTINCT"
        title="Search Distinct Items"
        description="Select a distinct item to add to inventory"
      />
    </Tabs>
  );
}
