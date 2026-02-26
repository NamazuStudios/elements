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
import { Badge } from '@/components/ui/badge';
import { Search, Loader2, HardDrive } from 'lucide-react';
import { ScrollArea } from '@/components/ui/scroll-area';
import { apiClient } from '@/lib/api-client';

interface LargeObjectSearchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSelect: (obj: any) => void;
}

export function LargeObjectSearchDialog({
  open,
  onOpenChange,
  onSelect,
}: LargeObjectSearchDialogProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const limit = 20;
  const offset = page * limit;

  const { data, isLoading } = useQuery({
    queryKey: ['/api/rest/large_object', offset, limit, searchQuery],
    queryFn: async () => {
      const params = new URLSearchParams({
        offset: offset.toString(),
        count: limit.toString(),
      });
      if (searchQuery) {
        params.append('search', searchQuery);
      }
      return apiClient.request<any>(`/api/rest/large_object?${params}`);
    },
    enabled: open,
  });

  const handleSelect = (obj: any) => {
    onSelect(obj);
    onOpenChange(false);
    setSearchQuery('');
    setPage(0);
  };

  const itemList = data?.objects || [];
  const totalCount = data?.total || 0;
  const hasMore = totalCount ? (page + 1) * limit < totalCount : itemList.length >= limit;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[80vh]">
        <DialogHeader>
          <DialogTitle>Select Large Object</DialogTitle>
          <DialogDescription>Search and select a large object for the profile image</DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search by path or filename..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(0);
              }}
              className="pl-9"
              data-testid="input-search-large-object"
            />
          </div>

          <ScrollArea className="h-[400px] border rounded-md">
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
              </div>
            ) : itemList.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-muted-foreground">
                <HardDrive className="w-8 h-8 mb-2" />
                <p>No large objects found</p>
              </div>
            ) : (
              <div className="p-2 space-y-1">
                {itemList.map((obj: any) => (
                  <Button
                    key={obj.id}
                    variant="ghost"
                    className="w-full justify-start"
                    onClick={() => handleSelect(obj)}
                    data-testid={`button-select-large-object-${obj.id}`}
                  >
                    <div className="grid grid-cols-2 gap-x-4 gap-y-1 w-full text-left text-sm">
                      <div className="col-span-2 flex items-center gap-2">
                        <span className="text-muted-foreground">Path:</span>
                        <span className="font-medium truncate">{obj.path || 'N/A'}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground">MIME:</span>{' '}
                        <span>{obj.mimeType || 'N/A'}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-muted-foreground">State:</span>
                        <Badge variant="secondary" className="text-xs">
                          {obj.state || 'N/A'}
                        </Badge>
                      </div>
                      {obj.originalFilename && (
                        <div className="col-span-2">
                          <span className="text-muted-foreground">Filename:</span>{' '}
                          <span className="text-xs">{obj.originalFilename}</span>
                        </div>
                      )}
                      <div className="col-span-2 truncate">
                        <span className="text-muted-foreground">ID:</span>{' '}
                        <span className="text-xs">{obj.id}</span>
                      </div>
                    </div>
                  </Button>
                ))}
              </div>
            )}
          </ScrollArea>

          {!isLoading && itemList.length > 0 && (
            <div className="flex items-center justify-between">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                data-testid="button-previous-page-large-object"
              >
                Previous
              </Button>
              <span className="text-sm text-muted-foreground">
                Page {page + 1}
                {totalCount > 0 && ` of ${Math.ceil(totalCount / limit)}`}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(page + 1)}
                disabled={!hasMore}
                data-testid="button-next-page-large-object"
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
