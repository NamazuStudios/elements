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

interface DisplayField {
  label: string;
  key: string;
  format?: (value: any) => string;
}

interface ResourceSearchDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSelect: (resourceId: string, resource: any) => void;
  resourceType: string; // e.g., 'vault', 'metadataspec'
  endpoint: string; // e.g., '/api/rest/blockchain/omni/vault'
  title: string;
  description: string;
  displayFields: DisplayField[];
  searchPlaceholder?: string;
  currentResourceId?: string;
}

export function ResourceSearchDialog({
  open,
  onOpenChange,
  onSelect,
  resourceType,
  endpoint,
  title,
  description,
  displayFields,
  searchPlaceholder = 'Search...',
  currentResourceId,
}: ResourceSearchDialogProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage] = useState(0);
  const limit = 20;
  const offset = page * limit;

  const { data, isLoading } = useQuery({
    queryKey: [endpoint, offset, limit, searchQuery],
    queryFn: async () => {
      const params = new URLSearchParams({
        offset: offset.toString(),
        total: limit.toString(),
      });
      if (searchQuery) {
        params.append('search', searchQuery);
      }
      const response = await apiRequest('GET', `/api/proxy${endpoint}?${params}`);
      return response.json();
    },
    enabled: open,
  });

  const handleSelect = (resourceId: string, resource: any) => {
    onSelect(resourceId, resource);
    onOpenChange(false);
    setSearchQuery('');
    setPage(0);
  };

  // Handle paginated response from Elements API
  // Support both { objects: [...] } and { content: [...] } response formats
  const resourceList = Array.isArray(data) 
    ? data 
    : ((data as any)?.objects || (data as any)?.content || []);
  const totalCount = (data as any)?.total || 0;
  const hasMore = totalCount ? (page + 1) * limit < totalCount : resourceList.length >= limit;

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
              placeholder={searchPlaceholder}
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setPage(0);
              }}
              className="pl-9"
              data-testid={`input-search-${resourceType}`}
            />
          </div>

          {/* Results */}
          <ScrollArea className="h-[400px] border rounded-md">
            {isLoading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
              </div>
            ) : resourceList.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-muted-foreground">
                <p>No {resourceType}s found</p>
              </div>
            ) : (
              <div className="p-2 space-y-1">
                {resourceList.map((resource: any) => (
                  <Button
                    key={resource.id}
                    variant="ghost"
                    className="w-full justify-start hover-elevate p-3"
                    onClick={() => handleSelect(resource.id, resource)}
                    data-testid={`button-select-${resourceType}-${resource.id}`}
                  >
                    <div className={`grid grid-cols-${Math.min(displayFields.length, 2)} gap-x-4 gap-y-1 w-full text-left text-sm`}>
                      {displayFields.map((field, idx) => {
                        const value = resource[field.key];
                        const displayValue = field.format ? field.format(value) : value || 'N/A';
                        return (
                          <div key={idx} className={field.key === 'id' ? 'truncate' : ''}>
                            <span className="text-muted-foreground">{field.label}: </span>
                            <span className={field.key === 'id' ? 'text-xs' : 'font-medium'}>
                              {displayValue}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </Button>
                ))}
              </div>
            )}
          </ScrollArea>

          {/* Pagination */}
          {!isLoading && resourceList.length > 0 && (
            <div className="flex items-center justify-between">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                data-testid={`button-previous-page-${resourceType}`}
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
                data-testid={`button-next-page-${resourceType}`}
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
