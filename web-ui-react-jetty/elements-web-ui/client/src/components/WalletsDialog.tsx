import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { apiRequest } from '@/lib/queryClient';
import { queryClient } from '@/lib/queryClient';
import { Button } from '@/components/ui/button';
import { Plus, Edit, Trash2, Loader2, Eye, EyeOff } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { WalletForm } from '@/components/WalletForm';
import { Card, CardHeader, CardTitle } from '@/components/ui/card';

interface Wallet {
  id: string;
  displayName: string;
  api: string;
  networks: string[];
  preferredAccount: number;
  accounts: WalletAccount[];
}

interface WalletAccount {
  address: string;
  privateKey?: string;
  encrypted: boolean;
}

interface WalletsDialogProps {
  vault: { id: string; displayName?: string };
  open: boolean;
  onClose: () => void;
}

interface Pagination<T> {
  offset: number;
  total: number;
  content?: T[];
  objects?: T[];
}

export function WalletsDialog({ vault, open, onClose }: WalletsDialogProps) {
  const { toast } = useToast();
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [editingWallet, setEditingWallet] = useState<Wallet | null>(null);
  const [deletingWallet, setDeletingWallet] = useState<Wallet | null>(null);
  const [showPrivateKeys, setShowPrivateKeys] = useState<Record<string, boolean>>({});

  const { data: walletsData, isLoading } = useQuery<Pagination<Wallet>>({
    queryKey: ['/api/rest/blockchain/omni/vault', vault.id, 'wallet'],
    queryFn: async () => {
      const response = await apiRequest('GET', `/api/proxy/api/rest/blockchain/omni/vault/${vault.id}/wallet`);
      return response.json();
    },
    enabled: open,
  });

  const deleteMutation = useMutation({
    mutationFn: async (walletId: string) => {
      await apiRequest('DELETE', `/api/proxy/api/rest/blockchain/omni/vault/${vault.id}/wallet/${walletId}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/blockchain/omni/vault', vault.id, 'wallet'] });
      setDeletingWallet(null);
      toast({ title: 'Success', description: 'Wallet deleted successfully' });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete wallet',
        variant: 'destructive',
      });
    },
  });

  const wallets = walletsData?.content || walletsData?.objects || [];

  return (
    <>
      <Dialog open={open} onOpenChange={onClose}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <div className="flex items-center justify-between">
              <div>
                <DialogTitle>Manage Wallets</DialogTitle>
                <p className="text-sm text-muted-foreground mt-1">
                  Vault: {vault.displayName || vault.id}
                </p>
              </div>
              <Button onClick={() => setIsCreateDialogOpen(true)} data-testid="button-create-wallet" className="pl-[16px] pr-[16px] ml-[20px] mr-[20px]">
                <Plus className="w-4 h-4 mr-2" />
                Add Wallet
              </Button>
            </div>
          </DialogHeader>

          <div className="space-y-4 mt-4">
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="w-8 h-8 animate-spin text-muted-foreground" />
              </div>
            ) : wallets.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-12 text-center">
                <p className="text-muted-foreground">No wallets found</p>
                <p className="text-sm text-muted-foreground mt-2">Create your first wallet to get started</p>
              </div>
            ) : (
              <div className="grid gap-4">
                {wallets.map((wallet) => (
                  <Card key={wallet.id} className="hover-elevate">
                    <CardHeader>
                      <div className="flex items-start justify-between">
                        <div className="space-y-1 flex-1">
                          <div className="flex items-center gap-2">
                            <CardTitle className="text-base">{wallet.displayName}</CardTitle>
                            <Badge variant="outline" data-testid={`badge-api-${wallet.id}`}>
                              {wallet.api}
                            </Badge>
                          </div>
                          <div className="text-sm text-muted-foreground space-y-2">
                            <div>
                              <span className="font-semibold">Networks:</span> {wallet.networks.join(', ')}
                            </div>
                            <div>
                              <span className="font-semibold">Preferred Account:</span> {wallet.preferredAccount}
                            </div>
                            <div className="space-y-1">
                              <div className="font-semibold">Accounts ({wallet.accounts.length}):</div>
                              {wallet.accounts.map((account, idx) => (
                                <div key={idx} className="ml-3 space-y-0.5 border-l-2 border-muted pl-2">
                                  <div className="text-xs">
                                    <span className="font-medium">Address:</span> <span className="font-mono">{account.address}</span>
                                  </div>
                                  <div className="text-xs flex items-center gap-2">
                                    <span className="font-medium">Private Key:</span>
                                    {account.privateKey ? (
                                      <div className="flex items-center gap-1">
                                        <span className="font-mono max-w-[200px] truncate">
                                          {showPrivateKeys[`${wallet.id}-${idx}`] ? account.privateKey : '••••••••••••••••'}
                                        </span>
                                        <Button
                                          size="icon"
                                          variant="ghost"
                                          className="h-4 w-4 flex-shrink-0"
                                          onClick={() => setShowPrivateKeys({ ...showPrivateKeys, [`${wallet.id}-${idx}`]: !showPrivateKeys[`${wallet.id}-${idx}`] })}
                                          data-testid={`button-toggle-private-key-${wallet.id}-${idx}`}
                                        >
                                          {showPrivateKeys[`${wallet.id}-${idx}`] ? <EyeOff className="w-3 h-3" /> : <Eye className="w-3 h-3" />}
                                        </Button>
                                      </div>
                                    ) : (
                                      <span className="text-muted-foreground">None</span>
                                    )}
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        </div>
                        <div className="flex gap-2">
                          <Button
                            size="icon"
                            variant="ghost"
                            onClick={() => setEditingWallet(wallet)}
                            data-testid={`button-edit-wallet-${wallet.id}`}
                          >
                            <Edit className="w-4 h-4" />
                          </Button>
                          <Button
                            size="icon"
                            variant="ghost"
                            onClick={() => setDeletingWallet(wallet)}
                            data-testid={`button-delete-wallet-${wallet.id}`}
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                    </CardHeader>
                  </Card>
                ))}
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>

      {/* Create Wallet Dialog */}
      <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
        <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Create Wallet</DialogTitle>
          </DialogHeader>
          <WalletForm
            mode="create"
            vaultId={vault.id}
            onSubmit={async (data: any) => {
              console.log('Creating wallet with data:', JSON.stringify(data, null, 2));
              await apiRequest('POST', `/api/proxy/api/rest/blockchain/omni/vault/${vault.id}/wallet`, data);
              queryClient.invalidateQueries({ queryKey: ['/api/rest/blockchain/omni/vault', vault.id, 'wallet'] });
              setIsCreateDialogOpen(false);
              toast({ title: 'Success', description: 'Wallet created successfully' });
            }}
            onCancel={() => setIsCreateDialogOpen(false)}
          />
        </DialogContent>
      </Dialog>

      {/* Edit Wallet Dialog */}
      {editingWallet && (
        <Dialog open={!!editingWallet} onOpenChange={() => setEditingWallet(null)}>
          <DialogContent className="max-w-3xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Edit Wallet</DialogTitle>
            </DialogHeader>
            <WalletForm
              mode="update"
              vaultId={vault.id}
              initialData={editingWallet}
              onSubmit={async (data: any) => {
                await apiRequest('PUT', `/api/proxy/api/rest/blockchain/omni/vault/${vault.id}/wallet/${editingWallet.id}`, data);
                queryClient.invalidateQueries({ queryKey: ['/api/rest/blockchain/omni/vault', vault.id, 'wallet'] });
                setEditingWallet(null);
                toast({ title: 'Success', description: 'Wallet updated successfully' });
              }}
              onCancel={() => setEditingWallet(null)}
            />
          </DialogContent>
        </Dialog>
      )}

      {/* Delete Wallet Dialog */}
      {deletingWallet && (
        <Dialog open={!!deletingWallet} onOpenChange={() => setDeletingWallet(null)}>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Delete Wallet</DialogTitle>
            </DialogHeader>
            <div className="py-4">
              <p>Are you sure you want to delete this wallet?</p>
              <p className="text-sm text-muted-foreground mt-2 font-mono">{deletingWallet.displayName}</p>
            </div>
            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => setDeletingWallet(null)}
                data-testid="button-cancel-delete-wallet"
              >
                Cancel
              </Button>
              <Button
                variant="destructive"
                onClick={() => deleteMutation.mutate(deletingWallet.id)}
                disabled={deleteMutation.isPending}
                data-testid="button-confirm-delete-wallet"
              >
                {deleteMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                Delete
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </>
  );
}
