import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Search, Loader2 } from 'lucide-react';
import { ScrollArea } from '@/components/ui/scroll-area';
import { apiRequest } from '@/lib/queryClient';

interface ItemSearchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSelect: (itemId: string, item: any) => void;
  category?: 'FUNGIBLE' | 'DISTINCT';
  title?: string;
  description?: string;
}

export function ItemSearchDialog({
  open,
  onOpenChange,
  onSelect,
  category,
  title = 'Search Items',
  description = 'Search for an item',
}: ItemSearchDialogProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const limit = 20;
  const offset = page * limit;

  const { data, isLoading } = useQuery({
    queryKey: ['/api/rest/item', offset, limit, searchQuery, category],
    queryFn: async () => {
      const params = new URLSearchParams({
        offset: offset.toString(),
        total: limit.toString(),
      });
      if (searchQuery) {
        params.append('search', searchQuery);
      }
      if (category) {
        params.append('category', category);
      }
      const response = await apiRequest('GET', `/api/proxy/api/rest/item?${params}`);
      return response.json();
    },
    enabled: open,
  });

  const handleSelect = (itemId: string, item: any) => {
    onSelect(itemId, item);
    onOpenChange(false);
    setSearchQuery('');
    setPage(0);
  };

  // Handle paginated response from Elements API
  const itemList = Array.isArray(data) 
    ? data 
    : ((data as any)?.objects || []);
  
  const totalCount = (data as any)?.total || 0;
  const hasMore = totalCount ? (page + 1) * limit < totalCount : itemList.length >= limit;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[80vh]">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Search input */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search by name, description, or ID..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(0);
              }}
              className="pl-9"
              data-testid="input-search-item"
            />
          </div>

          {/* Results */}
          <ScrollArea className="h-[400px] border rounded-md">
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
              </div>
            ) : itemList.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-muted-foreground">
                <p>No items found</p>
              </div>
            ) : (
              <div className="p-2 space-y-1">
                {itemList.map((item: any) => (
                  <Button
                    key={item.id}
                    variant="ghost"
                    className="w-full justify-start"
                    onClick={() => handleSelect(item.id, item)}
                    data-testid={`button-select-item-${item.id}`}
                  >
                    <div className="grid grid-cols-2 gap-x-4 gap-y-1 w-full text-left text-sm">
                      <div>
                        <span className="text-muted-foreground">Name: </span>
                        <span className="font-medium">{item.name || item.displayName || 'N/A'}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Category: </span>
                        <span>{item.category || 'N/A'}</span>
                      </div>
                      {item.description && (
                        <div className="col-span-2">
                          <span className="text-muted-foreground">Description: </span>
                          <span className="text-xs">{item.description}</span>
                        </div>
                      )}
                      <div className="truncate col-span-2">
                        <span className="text-muted-foreground">ID: </span>
                        <span className="text-xs">{item.id}</span>
                      </div>
                    </div>
                  </Button>
                ))}
              </div>
            )}
          </ScrollArea>

          {/* Pagination */}
          {!isLoading && itemList.length > 0 && (
            <div className="flex items-center justify-between">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                data-testid="button-previous-page-item"
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {page + 1}
                {totalCount && ` of ${Math.ceil(totalCount / limit)}`}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(page + 1)}
                disabled={!hasMore}
                data-testid="button-next-page-item"
              >
                Next
              </Button>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
