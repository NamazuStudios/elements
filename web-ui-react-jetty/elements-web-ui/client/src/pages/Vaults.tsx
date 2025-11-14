import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiRequest } from '@/lib/queryClient';
import { queryClient } from '@/lib/queryClient';
import { Button } from '@/components/ui/button';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Plus, Edit, Trash2, Loader2, Search, ChevronLeft, ChevronRight, Wallet } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { VaultForm } from '@/components/VaultForm';
import { WalletsDialog } from '@/components/WalletsDialog';

interface Vault {
  id: string;
  displayName?: string;
  user?: { id: string; name?: string };
  key?: { algorithm?: string; encrypted?: boolean };
}

interface Pagination<T> {
  offset: number;
  total: number;
  content?: T[];
  objects?: T[];
}

export default function VaultsPage() {
  const { toast } = useToast();
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [editingVault, setEditingVault] = useState<Vault | null>(null);
  const [walletsVault, setWalletsVault] = useState<Vault | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [offset, setOffset] = useState(0);
  const [count] = useState(() => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  });

  const { data: vaultsData, isLoading } = useQuery<Pagination<Vault>>({
    queryKey: ['/api/rest/blockchain/omni/vault', offset, count, searchTerm],
    queryFn: async () => {
      const params = new URLSearchParams({
        offset: offset.toString(),
        total: count.toString(),
      });
      if (searchTerm) {
        params.append('search', searchTerm);
      }
      const response = await apiRequest('GET', `/api/proxy/api/rest/blockchain/omni/vault?${params}`);
      return response.json();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiRequest('DELETE', `/api/proxy/api/rest/blockchain/omni/vault/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/blockchain/omni/vault'] });
      toast({ title: 'Success', description: 'Vault deleted successfully' });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete vault',
        variant: 'destructive',
      });
    },
  });

  const vaults = vaultsData?.content || vaultsData?.objects || [];
  const totalPages = Math.ceil((vaultsData?.total || 0) / count);
  const currentPage = Math.floor(offset / count) + 1;

  return (
    <div className="h-full flex flex-col p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold" data-testid="text-page-title">Vaults</h1>
          <p className="text-muted-foreground mt-1">Manage blockchain vaults and encryption keys</p>
        </div>
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button data-testid="button-create-vault">
              <Plus className="w-4 h-4 mr-2" />
              Create Vault
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Create New Vault</DialogTitle>
            </DialogHeader>
            <VaultForm
              mode="create"
              onSubmit={async (data) => {
                await apiRequest('POST', '/api/proxy/api/rest/blockchain/omni/vault', data);
                queryClient.invalidateQueries({ queryKey: ['/api/rest/blockchain/omni/vault'] });
                setIsCreateDialogOpen(false);
                toast({ title: 'Success', description: 'Vault created successfully' });
              }}
              onCancel={() => setIsCreateDialogOpen(false)}
            />
          </DialogContent>
        </Dialog>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex-1 relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search vaults..."
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

      <Card>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
            </div>
          ) : vaults.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <p className="text-muted-foreground">No vaults found</p>
              <p className="text-sm text-muted-foreground mt-2">Create your first vault to get started</p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>Display Name</TableHead>
                  <TableHead>User ID</TableHead>
                  <TableHead>Algorithm</TableHead>
                  <TableHead className="w-[140px] text-center sticky right-0 bg-background border-l z-10">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {vaults.map((vault) => (
                  <TableRow key={vault.id}>
                    <TableCell className="font-mono" data-testid={`text-vault-id-${vault.id}`}>
                      {vault.id}
                    </TableCell>
                    <TableCell>
                      {vault.displayName || <span className="text-muted-foreground">—</span>}
                    </TableCell>
                    <TableCell className="font-mono">
                      {vault.user?.id || <span className="text-muted-foreground">—</span>}
                    </TableCell>
                    <TableCell>
                      {vault.key?.algorithm ? (
                        <Badge variant="outline" data-testid={`badge-algorithm-${vault.id}`}>
                          {vault.key.algorithm}
                        </Badge>
                      ) : (
                        <span className="text-muted-foreground">—</span>
                      )}
                    </TableCell>
                    <TableCell className="w-[140px] sticky right-0 bg-background border-l z-10">
                      <div className="flex items-center justify-center gap-1">
                        <Button
                          size="icon"
                          variant="ghost"
                          onClick={() => setWalletsVault(vault)}
                          data-testid={`button-wallets-${vault.id}`}
                        >
                          <Wallet className="w-4 h-4" />
                        </Button>
                        <Dialog
                          open={editingVault?.id === vault.id}
                          onOpenChange={(open) => {
                            if (!open) setEditingVault(null);
                          }}
                        >
                          <DialogTrigger asChild>
                            <Button
                              size="icon"
                              variant="ghost"
                              onClick={async () => {
                                try {
                                  const response = await apiRequest('GET', `/api/proxy/api/rest/blockchain/omni/vault/${vault.id}`);
                                  const fullVault = await response.json();
                                  setEditingVault(fullVault);
                                } catch (error: any) {
                                  toast({
                                    title: 'Error',
                                    description: error.message || 'Failed to load vault details',
                                    variant: 'destructive',
                                  });
                                }
                              }}
                              data-testid={`button-edit-${vault.id}`}
                            >
                              <Edit className="w-4 h-4" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
                            <DialogHeader>
                              <DialogTitle>Edit Vault</DialogTitle>
                            </DialogHeader>
                            {editingVault && (
                              <VaultForm
                                mode="update"
                                initialData={editingVault}
                                onSubmit={async (data) => {
                                  await apiRequest('PUT', `/api/proxy/api/rest/blockchain/omni/vault/${editingVault.id}`, data);
                                  queryClient.invalidateQueries({ queryKey: ['/api/rest/blockchain/omni/vault'] });
                                  setEditingVault(null);
                                  toast({ title: 'Success', description: 'Vault updated successfully' });
                                }}
                                onCancel={() => setEditingVault(null)}
                              />
                            )}
                          </DialogContent>
                        </Dialog>
                        <Dialog>
                          <DialogTrigger asChild>
                            <Button
                              size="icon"
                              variant="ghost"
                              data-testid={`button-delete-${vault.id}`}
                            >
                              <Trash2 className="w-4 h-4" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent>
                            <DialogHeader>
                              <DialogTitle>Delete Vault</DialogTitle>
                            </DialogHeader>
                            <div className="py-4">
                              <p>Are you sure you want to delete this vault?</p>
                              <p className="text-sm text-muted-foreground mt-2 font-mono">{vault.id}</p>
                            </div>
                            <DialogFooter>
                              <Button
                                variant="outline"
                                onClick={() => {}}
                                data-testid="button-cancel-delete"
                              >
                                Cancel
                              </Button>
                              <Button
                                variant="destructive"
                                onClick={() => deleteMutation.mutate(vault.id)}
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

      {vaultsData && totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Showing {offset + 1}-{Math.min(offset + count, vaultsData.total)} of {vaultsData.total}
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
              disabled={offset + count >= vaultsData.total}
              data-testid="button-next-page"
            >
              Next
              <ChevronRight className="w-4 h-4 ml-1" />
            </Button>
          </div>
        </div>
      )}

      {/* Wallets Dialog */}
      {walletsVault && (
        <WalletsDialog
          vault={walletsVault}
          open={!!walletsVault}
          onClose={() => setWalletsVault(null)}
        />
      )}
    </div>
  );
}
