import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from '@/components/ui/command';
import { Loader2, Package, Trash2, AlertCircle, Plus, Check, ChevronsUpDown, Pencil } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import { queryClient } from '@/lib/queryClient';
import { cn } from '@/lib/utils';

interface InventoryViewerProps {
  userId: string;
  username?: string;
}

export function InventoryViewer({ userId, username }: InventoryViewerProps) {
  const { toast } = useToast();
  const [activeTab, setActiveTab] = useState<'fungible' | 'distinct'>('fungible');
  const [fungibleSearch, setFungibleSearch] = useState<string>('');
  const [distinctSearch, setDistinctSearch] = useState<string>('');

  // State for creating inventory
  const [createFungibleOpen, setCreateFungibleOpen] = useState(false);
  const [createDistinctOpen, setCreateDistinctOpen] = useState(false);
  const [itemSearchOpen, setItemSearchOpen] = useState(false);
  const [selectedItemId, setSelectedItemId] = useState<string>('');
  const [fungibleQuantity, setFungibleQuantity] = useState<string>('1');
  const [distinctMetadata, setDistinctMetadata] = useState<Array<{ key: string; value: string }>>([]);

  // State for editing inventory
  const [editFungibleOpen, setEditFungibleOpen] = useState(false);
  const [editDistinctOpen, setEditDistinctOpen] = useState(false);
  const [editingFungibleItem, setEditingFungibleItem] = useState<any>(null);
  const [editingDistinctItem, setEditingDistinctItem] = useState<any>(null);
  const [editFungibleQuantity, setEditFungibleQuantity] = useState<string>('1');
  const [editDistinctMetadata, setEditDistinctMetadata] = useState<Array<{ key: string; value: string }>>([]);

  // Fetch items for the dropdown
  const { data: itemsResponse, isLoading: itemsLoading } = useQuery({
    queryKey: ['/api/rest/item'],
    queryFn: async () => {
      return await apiClient.request<any>('/api/rest/item');
    },
  });

  const itemsList = itemsResponse?.objects || [];
  
  // Filter items by category
  const fungibleItems = itemsList.filter((item: any) => item.category === 'FUNGIBLE');
  const distinctItems = itemsList.filter((item: any) => item.category === 'DISTINCT');

  // Fetch Advanced (Fungible) Inventory
  const { data: fungibleInventoryData, isLoading: fungibleLoading, error: fungibleError } = useQuery({
    queryKey: ['/api/rest/inventory/advanced', userId],
    enabled: !!userId,
    queryFn: async () => {
      console.log('[INVENTORY-DEBUG] Starting fungible fetch for userId:', userId);
      const params = new URLSearchParams();
      if (userId) {
        params.set('userId', userId);
        console.log('[INVENTORY-DEBUG] Set userId param:', userId);
      }
      const paramString = params.toString();
      console.log('[INVENTORY-DEBUG] Params string:', paramString);
      const url = `/api/rest/inventory/advanced${paramString ? `?${paramString}` : ''}`;
      console.log('[INVENTORY-DEBUG] Final URL:', url);
      const response = await apiClient.request<any>(url);
      console.log('[INVENTORY] Fungible response:', response);
      if (response && typeof response === 'object' && 'objects' in response) {
        console.log('[INVENTORY] Fungible objects count:', response.objects?.length || 0);
        return response.objects || [];
      }
      console.log('[INVENTORY] Fungible no objects found in response');
      return [];
    },
  });

  // Fetch Distinct Inventory
  const { data: distinctInventoryData, isLoading: distinctLoading, error: distinctError } = useQuery({
    queryKey: ['/api/rest/inventory/distinct', userId],
    enabled: !!userId,
    queryFn: async () => {
      console.log('[INVENTORY-DEBUG] Starting distinct fetch for userId:', userId);
      const params = new URLSearchParams();
      if (userId) {
        params.set('userId', userId);
        console.log('[INVENTORY-DEBUG] Set userId param:', userId);
      }
      const paramString = params.toString();
      console.log('[INVENTORY-DEBUG] Params string:', paramString);
      const url = `/api/rest/inventory/distinct${paramString ? `?${paramString}` : ''}`;
      console.log('[INVENTORY-DEBUG] Final URL:', url);
      const response = await apiClient.request<any>(url);
      console.log('[INVENTORY] Distinct response:', response);
      if (response && typeof response === 'object' && 'objects' in response) {
        console.log('[INVENTORY] Distinct objects count:', response.objects?.length || 0);
        return response.objects || [];
      }
      console.log('[INVENTORY] Distinct no objects found in response');
      return [];
    },
  });

  // Filter inventory based on search
  const fungibleInventory = (fungibleInventoryData || []).filter((item: any) => {
    if (!fungibleSearch) return true;
    const searchLower = fungibleSearch.toLowerCase();
    const itemName = (item.item?.displayName || item.item?.name || '').toLowerCase();
    const itemId = (item.item?.id || '').toLowerCase();
    const itemDesc = (item.item?.description || '').toLowerCase();
    return itemName.includes(searchLower) || itemId.includes(searchLower) || itemDesc.includes(searchLower);
  });
  console.log('[INVENTORY] Fungible data:', fungibleInventoryData, 'Filtered:', fungibleInventory);

  const distinctInventory = (distinctInventoryData || []).filter((item: any) => {
    if (!distinctSearch) return true;
    const searchLower = distinctSearch.toLowerCase();
    const itemName = (item.item?.displayName || item.item?.name || '').toLowerCase();
    const itemId = (item.item?.id || item.id || '').toLowerCase();
    const itemDesc = (item.item?.description || '').toLowerCase();
    return itemName.includes(searchLower) || itemId.includes(searchLower) || itemDesc.includes(searchLower);
  });
  console.log('[INVENTORY] Distinct data:', distinctInventoryData, 'Filtered:', distinctInventory);

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
      setSelectedItemId('');
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
      setSelectedItemId('');
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

    if (!selectedItemId) {
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
      itemId: selectedItemId,
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

    if (!selectedItemId) {
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
      itemId: selectedItemId,
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
        <div className="mb-4 flex gap-3">
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
            onChange={(e) => setFungibleSearch(e.target.value)}
            className="max-w-sm"
            data-testid="input-search-fungible"
          />
        </div>
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
          <div className="space-y-3">
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
                  <div className="flex items-center gap-4 text-sm">
                    <div>
                      <span className="text-muted-foreground">Quantity:</span>{' '}
                      <Badge variant="secondary" data-testid={`text-quantity-${item.id}`}>
                        {item.quantity}
                      </Badge>
                    </div>
                    {item.item?.id && (
                      <div>
                        <span className="text-muted-foreground">Item ID:</span>{' '}
                        <code className="text-xs bg-muted px-1 rounded">{item.item.id}</code>
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
            <p className="text-sm text-muted-foreground">No fungible inventory items found</p>
          </div>
        )}
      </TabsContent>

      {/* Distinct Inventory Tab */}
      <TabsContent value="distinct" className="mt-4">
        <div className="mb-4 flex gap-3">
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
            onChange={(e) => setDistinctSearch(e.target.value)}
            className="max-w-sm"
            data-testid="input-search-distinct"
          />
        </div>
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
          <div className="space-y-3">
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
                    {item.item?.id && (
                      <div className="text-sm">
                        <span className="text-muted-foreground">Item ID:</span>{' '}
                        <code className="text-xs bg-muted px-1 rounded">{item.item.id}</code>
                      </div>
                    )}
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
      </TabsContent>

      {/* Create Fungible Inventory Dialog */}
      <Dialog open={createFungibleOpen} onOpenChange={(open) => {
        setCreateFungibleOpen(open);
        if (!open) {
          setItemSearchOpen(false);
          setSelectedItemId('');
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
              <Popover open={itemSearchOpen} onOpenChange={setItemSearchOpen}>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    role="combobox"
                    className={cn(
                      'w-full justify-between',
                      !selectedItemId && 'text-muted-foreground'
                    )}
                    data-testid="button-item-picker"
                  >
                    {selectedItemId
                      ? fungibleItems?.find((item: any) => item.id === selectedItemId)?.name || selectedItemId
                      : 'Select item...'}
                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-[400px] p-0">
                  <Command>
                    <CommandInput placeholder="Search fungible items..." data-testid="input-item-search" />
                    <CommandList>
                      <CommandEmpty>
                        {itemsLoading ? 'Loading items...' : 'No fungible items found.'}
                      </CommandEmpty>
                      <CommandGroup>
                        {fungibleItems?.map((item: any) => (
                          <CommandItem
                            key={item.id}
                            value={item.name || item.id}
                            onSelect={() => {
                              setSelectedItemId(item.id);
                              setItemSearchOpen(false);
                            }}
                            data-testid={`option-item-${item.id}`}
                          >
                            <Check
                              className={cn(
                                'mr-2 h-4 w-4',
                                item.id === selectedItemId ? 'opacity-100' : 'opacity-0'
                              )}
                            />
                            <div className="flex flex-col">
                              <span className="font-medium">{item.name}</span>
                              <span className="text-xs text-muted-foreground">
                                {item.category} • {item.id}
                              </span>
                            </div>
                          </CommandItem>
                        ))}
                      </CommandGroup>
                    </CommandList>
                  </Command>
                </PopoverContent>
              </Popover>
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
                onClick={() => setCreateFungibleOpen(false)}
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
          setItemSearchOpen(false);
          setSelectedItemId('');
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
              <Popover open={itemSearchOpen} onOpenChange={setItemSearchOpen}>
                <PopoverTrigger asChild>
                  <Button
                    variant="outline"
                    role="combobox"
                    className={cn(
                      'w-full justify-between',
                      !selectedItemId && 'text-muted-foreground'
                    )}
                    data-testid="button-item-picker-distinct"
                  >
                    {selectedItemId
                      ? distinctItems?.find((item: any) => item.id === selectedItemId)?.name || selectedItemId
                      : 'Select item...'}
                    <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-[400px] p-0">
                  <Command>
                    <CommandInput placeholder="Search distinct items..." data-testid="input-item-search-distinct" />
                    <CommandList>
                      <CommandEmpty>
                        {itemsLoading ? 'Loading items...' : 'No distinct items found.'}
                      </CommandEmpty>
                      <CommandGroup>
                        {distinctItems?.map((item: any) => (
                          <CommandItem
                            key={item.id}
                            value={item.name || item.id}
                            onSelect={() => {
                              setSelectedItemId(item.id);
                              setItemSearchOpen(false);
                            }}
                            data-testid={`option-item-distinct-${item.id}`}
                          >
                            <Check
                              className={cn(
                                'mr-2 h-4 w-4',
                                item.id === selectedItemId ? 'opacity-100' : 'opacity-0'
                              )}
                            />
                            <div className="flex flex-col">
                              <span className="font-medium">{item.name}</span>
                              <span className="text-xs text-muted-foreground">
                                {item.category} • {item.id}
                              </span>
                            </div>
                          </CommandItem>
                        ))}
                      </CommandGroup>
                    </CommandList>
                  </Command>
                </PopoverContent>
              </Popover>
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
                onClick={() => setCreateDistinctOpen(false)}
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
    </Tabs>
  );
}
