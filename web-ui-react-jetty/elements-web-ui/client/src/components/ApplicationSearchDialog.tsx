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

interface ApplicationSearchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSelect: (applicationId: string, application: any) => void;
}

export function ApplicationSearchDialog({
  open,
  onOpenChange,
  onSelect,
}: ApplicationSearchDialogProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const limit = 20;
  const offset = page * limit;

  const { data, isLoading } = useQuery({
    queryKey: ['/api/rest/application', offset, limit, searchQuery],
    queryFn: async () => {
      const params = new URLSearchParams({
        offset: offset.toString(),
        total: limit.toString(),
      });
      if (searchQuery) {
        params.append('search', searchQuery);
      }
      const response = await apiRequest('GET', `/api/proxy/api/rest/application?${params}`);
      return response.json();
    },
    enabled: open,
  });

  const handleSelect = (appId: string, app: any) => {
    onSelect(appId, app);
    onOpenChange(false);
    setSearchQuery('');
    setPage(0);
  };

  // Handle paginated response from Elements API
  const applicationList = Array.isArray(data) 
    ? data 
    : ((data as any)?.objects || []);
  const totalCount = (data as any)?.total || 0;
  const hasMore = totalCount ? (page + 1) * limit < totalCount : applicationList.length >= limit;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[80vh]">
        <DialogHeader>
          <DialogTitle>Search Applications</DialogTitle>
          <DialogDescription>
            Search for an application to associate with this profile
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4">
          {/* Search input */}
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search by name or ID..."
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(0);
              }}
              className="pl-9"
              data-testid="input-search-application"
            />
          </div>

          {/* Results */}
          <ScrollArea className="h-[400px] border rounded-md">
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
              </div>
            ) : applicationList.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-muted-foreground">
                <p>No applications found</p>
              </div>
            ) : (
              <div className="p-2 space-y-1">
                {applicationList.map((app: any) => (
                  <Button
                    key={app.id}
                    variant="ghost"
                    className="w-full justify-start hover-elevate p-3"
                    onClick={() => handleSelect(app.id, app)}
                    data-testid={`button-select-application-${app.id}`}
                  >
                    <div className="grid grid-cols-2 gap-x-4 gap-y-1 w-full text-left text-sm">
                      <div>
                        <span className="text-muted-foreground">Name: </span>
                        <span className="font-medium">{app.name || 'N/A'}</span>
                      </div>
                      <div>
                        <span className="text-muted-foreground">Display Name: </span>
                        <span>{app.displayName || 'N/A'}</span>
                      </div>
                      {app.description && (
                        <div className="col-span-2">
                          <span className="text-muted-foreground">Description: </span>
                          <span className="text-xs">{app.description}</span>
                        </div>
                      )}
                      <div className="truncate col-span-2">
                        <span className="text-muted-foreground">ID: </span>
                        <span className="text-xs">{app.id}</span>
                      </div>
                    </div>
                  </Button>
                ))}
              </div>
            )}
          </ScrollArea>

          {/* Pagination */}
          {!isLoading && applicationList.length > 0 && (
            <div className="flex items-center justify-between">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                data-testid="button-previous-page-application"
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
                data-testid="button-next-page-application"
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
