import { useState, useEffect, useRef } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Upload, Download, Trash2, Link as LinkIcon, HardDrive, File, ExternalLink, Eye, Search, ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';
import { getApiPath } from '@/lib/config';
import { apiClient } from '@/lib/api-client';

interface AccessPermissions {
  read: {
    wildcard: boolean;
    users: any[];
    profiles: any[];
  };
  write: {
    wildcard: boolean;
    users: any[];
    profiles: any[];
  };
  delete: {
    wildcard: boolean;
    users: any[];
    profiles: any[];
  };
}

interface LargeObject {
  id: string;
  url: string | null;
  path: string;
  originalFilename?: string | null;  // Note: lowercase 'n' to match Java property name
  mimeType: string | null;
  accessPermissions: AccessPermissions;
  state: string;
  lastModified: number | null;
}

interface LargeObjectResponse {
  offset: number;
  total: number;
  approximation: boolean;
  objects: LargeObject[];
}

export default function LargeObjects() {
  const { toast } = useToast();
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [objectUrl, setObjectUrl] = useState('');
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  
  // Read pagination limit from settings
  const getPageSize = () => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  };
  
  // Check localStorage on every query to ensure we use the latest setting
  const pageSize = getPageSize();
  
  // Reset to page 0 when page size changes to avoid empty pages
  const prevPageSizeRef = useRef(pageSize);
  useEffect(() => {
    if (prevPageSizeRef.current !== pageSize) {
      setCurrentPage(0);
      prevPageSizeRef.current = pageSize;
    }
  }, [pageSize]);

  const { data: response, isLoading, isFetching, error } = useQuery<LargeObjectResponse>({
    queryKey: ['/api/rest/large_object', currentPage, searchQuery, pageSize],
    queryFn: async () => {
      let path = `/api/rest/large_object?offset=${currentPage * pageSize}&count=${pageSize}`;
      if (searchQuery.trim()) {
        path += `&search=${encodeURIComponent(searchQuery.trim())}`;
      }
      console.log('[LARGE_OBJECTS] Fetching with pageSize:', pageSize, 'Path:', path);
      
      const response = await apiRequest('GET', path);
      const data = await response.json();
      console.log('[LARGE_OBJECTS] Received', data.objects?.length, 'objects');
      return data;
    },
    retry: false,
  });

  const objects = response?.objects || [];
  const totalPages = response ? Math.ceil(response.total / pageSize) : 0;

  interface SubjectRequest {
    wildcard: boolean;
    userIds: string[];
    profileIds: string[];
  }

  // Helper to create a public access SubjectRequest
  const createPublicAccess = (): SubjectRequest => ({
    wildcard: true,
    userIds: [] as string[],
    profileIds: [] as string[]
  });

  const createMutation = useMutation({
    mutationFn: async (data: { mimeType: string; write: SubjectRequest; read: SubjectRequest; delete: SubjectRequest }) => {
      const response = await apiRequest('POST', '/api/rest/large_object', data);
      return response.json();
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/large_object'] });
      toast({ 
        title: 'Success', 
        description: `Large object created with ID: ${data.id || 'Unknown'}` 
      });
      setIsCreateDialogOpen(false);
      setSelectedFile(null);
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create large object',
        variant: 'destructive',
      });
    },
  });

  const createFromUrlMutation = useMutation({
    mutationFn: async (urlData: { fileUrl: string; mimeType: string; write: SubjectRequest; read: SubjectRequest; delete: SubjectRequest }) => {
      const response = await apiRequest('POST', '/api/rest/large_object/from_url', urlData);
      return response.json();
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/large_object'] });
      toast({ 
        title: 'Success', 
        description: `Large object created with ID: ${data.id || 'Unknown'}` 
      });
      setIsCreateDialogOpen(false);
      setObjectUrl('');
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create large object from URL',
        variant: 'destructive',
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      const response = await apiRequest('DELETE', `/api/rest/large_object/${id}`);
      return id;
    },
    onSuccess: (deletedId) => {
      // Check if we need to adjust pagination after deletion
      const queryKey = ['/api/rest/large_object', currentPage, searchQuery, pageSize];
      const currentData = queryClient.getQueryData(queryKey) as LargeObjectResponse | undefined;
      
      // If we're deleting the last item on this page and it's not the first page, go back one page
      if (currentData && currentData.objects.length === 1 && currentPage > 0) {
        setCurrentPage(Math.max(0, currentPage - 1));
      }
      
      // Update all cached pages for this resource
      queryClient.setQueriesData(
        { queryKey: ['/api/rest/large_object'] },
        (oldData: any) => {
          if (!oldData || typeof oldData.total !== 'number') {
            return oldData; // Skip non-paginated responses
          }
          
          // Remove the deleted item if it's in this page
          if (Array.isArray(oldData.objects)) {
            const filteredObjects = oldData.objects.filter((obj: any) => obj.id !== deletedId);
            
            // Only update if something was actually removed
            if (filteredObjects.length < oldData.objects.length) {
              return {
                ...oldData,
                objects: filteredObjects,
                total: Math.max(0, oldData.total - 1),
              };
            }
          }
          
          // If item not in this page, just decrement the total
          return {
            ...oldData,
            total: Math.max(0, oldData.total - 1),
          };
        }
      );
      
      toast({ title: 'Success', description: 'Large object deleted successfully' });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete large object',
        variant: 'destructive',
      });
    },
  });

  const handleFileUpload = async () => {
    if (!selectedFile) {
      toast({
        title: 'Error',
        description: 'Please select a file to upload',
        variant: 'destructive',
      });
      return;
    }

    try {
      // First, create the large object with required metadata
      const createRequest = {
        mimeType: selectedFile.type || 'application/octet-stream',
        write: createPublicAccess(),
        read: createPublicAccess(),
        delete: createPublicAccess()
      };

      const createResponse = await apiRequest('POST', '/api/rest/large_object', createRequest);
      const createdObject = await createResponse.json();
      
      if (!createdObject.id) {
        throw new Error('Failed to create large object - no ID returned');
      }

      // Then upload the actual file content
      const formData = new FormData();
      formData.append('file', selectedFile);

      // Use apiRequest infrastructure for proper path handling
      // Note: We can't use apiRequest directly because it sets Content-Type to JSON
      const uploadPath = `/api/rest/large_object/${createdObject.id}/content`;
      const fullPath = await getApiPath(uploadPath);
      
      const headers: HeadersInit = {};
      const sessionToken = apiClient.getSessionToken();
      if (sessionToken) {
        headers['Elements-SessionSecret'] = sessionToken;
      }

      const uploadResponse = await fetch(fullPath, {
        method: 'PUT',
        body: formData,
        credentials: 'include',
        headers,
      });

      if (!uploadResponse.ok) {
        throw new Error(`Content upload failed: ${uploadResponse.statusText}`);
      }

      queryClient.invalidateQueries({ queryKey: ['/api/rest/large_object'] });
      toast({ 
        title: 'Success', 
        description: `Large object created with ID: ${createdObject.id}` 
      });
      setIsCreateDialogOpen(false);
      setSelectedFile(null);
    } catch (error: any) {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create large object',
        variant: 'destructive',
      });
    }
  };

  const handleUrlUpload = () => {
    if (!objectUrl) {
      toast({
        title: 'Error',
        description: 'Please provide a URL',
        variant: 'destructive',
      });
      return;
    }

    // Extract filename from URL for mimeType detection
    try {
      const urlPath = new URL(objectUrl).pathname;
      const filename = urlPath.substring(urlPath.lastIndexOf('/') + 1);
      const extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
      
      // Simple MIME type detection based on extension
      const mimeTypeMap: Record<string, string> = {
        'jpg': 'image/jpeg',
        'jpeg': 'image/jpeg',
        'png': 'image/png',
        'gif': 'image/gif',
        'pdf': 'application/pdf',
        'zip': 'application/zip',
        'json': 'application/json',
        'txt': 'text/plain',
      };
      
      const mimeType = mimeTypeMap[extension] || 'application/octet-stream';

      createFromUrlMutation.mutate({ 
        fileUrl: objectUrl,
        mimeType,
        write: createPublicAccess(),
        read: createPublicAccess(),
        delete: createPublicAccess()
      });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Invalid URL provided',
        variant: 'destructive',
      });
    }
  };

  const getCdnUrl = async (objectId: string): Promise<string> => {
    const cdnPath = await getApiPath(`/cdn/object/${objectId}`);
    return cdnPath;
  };

  const fetchObjectAsBlob = async (objectId: string, download: boolean = false): Promise<Blob> => {
    const cdnPath = `/cdn/object/${objectId}${download ? '?download=true' : ''}`;
    const fullPath = await getApiPath(cdnPath);
    
    const sessionToken = apiClient.getSessionToken();
    const headers: Record<string, string> = {};
    if (sessionToken) {
      headers['Elements-SessionSecret'] = sessionToken;
    }
    
    const response = await fetch(fullPath, { headers });
    
    if (!response.ok) {
      throw new Error(`Failed to fetch object: ${response.statusText}`);
    }
    
    return await response.blob();
  };

  const handleDownload = async (objectId: string, fileName?: string) => {
    try {
      const blob = await fetchObjectAsBlob(objectId, true);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName || `object-${objectId}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to download file',
        variant: 'destructive',
      });
    }
  };

  const isImageType = (mimeType?: string | null): boolean => {
    if (!mimeType) return false;
    return mimeType.startsWith('image/');
  };

  const [previewDialog, setPreviewDialog] = useState<{ open: boolean; object: LargeObject | null; imageUrl?: string }>({
    open: false,
    object: null,
  });

  const formatFileSize = (bytes?: number) => {
    if (!bytes) return 'Unknown';
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${sizes[i]}`;
  };

  const handlePreview = async (obj: LargeObject) => {
    try {
      const blob = await fetchObjectAsBlob(obj.id);
      const imageUrl = window.URL.createObjectURL(blob);
      setPreviewDialog({ open: true, object: obj, imageUrl });
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Failed to load image preview',
        variant: 'destructive',
      });
    }
  };

  // Cleanup blob URLs when dialog closes
  useEffect(() => {
    return () => {
      if (previewDialog.imageUrl) {
        window.URL.revokeObjectURL(previewDialog.imageUrl);
      }
    };
  }, [previewDialog.imageUrl]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Large Objects</h1>
          <p className="text-muted-foreground mt-1">Manage file storage and large binary objects</p>
        </div>
        <div className="flex items-center gap-2">
          <Button 
            onClick={async () => {
              await queryClient.invalidateQueries({ queryKey: ['/api/rest/large_object'] });
            }} 
            variant="outline" 
            size="sm"
            disabled={isFetching}
            data-testid="button-refresh-objects"
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${isFetching ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
            <DialogTrigger asChild>
              <Button data-testid="button-create-large-object">
                <Upload className="w-4 h-4 mr-2" />
                Upload Object
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Upload Large Object</DialogTitle>
              <DialogDescription>
                Upload a file from your device or import from a URL
              </DialogDescription>
            </DialogHeader>
            <Tabs defaultValue="file" className="w-full">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="file" data-testid="tab-file-upload">
                  <File className="w-4 h-4 mr-2" />
                  File Upload
                </TabsTrigger>
                <TabsTrigger value="url" data-testid="tab-url-upload">
                  <LinkIcon className="w-4 h-4 mr-2" />
                  From URL
                </TabsTrigger>
              </TabsList>
              <TabsContent value="file" className="space-y-4 mt-4">
                <div className="space-y-2">
                  <Label htmlFor="file-upload">Select File</Label>
                  <Input
                    id="file-upload"
                    type="file"
                    onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
                    data-testid="input-file-upload"
                  />
                  {selectedFile && (
                    <p className="text-sm text-muted-foreground">
                      Selected: {selectedFile.name} ({formatFileSize(selectedFile.size)})
                    </p>
                  )}
                </div>
                <Button
                  onClick={handleFileUpload}
                  disabled={!selectedFile || createMutation.isPending}
                  className="w-full"
                  data-testid="button-upload-file"
                >
                  {createMutation.isPending ? 'Uploading...' : 'Upload File'}
                </Button>
              </TabsContent>
              <TabsContent value="url" className="space-y-4 mt-4">
                <div className="space-y-2">
                  <Label htmlFor="object-url">URL</Label>
                  <Input
                    id="object-url"
                    type="url"
                    placeholder="https://example.com/file.png"
                    value={objectUrl}
                    onChange={(e) => setObjectUrl(e.target.value)}
                    data-testid="input-object-url"
                  />
                </div>
                <Button
                  onClick={handleUrlUpload}
                  disabled={!objectUrl || createFromUrlMutation.isPending}
                  className="w-full"
                  data-testid="button-upload-from-url"
                >
                  {createFromUrlMutation.isPending ? 'Importing...' : 'Import from URL'}
                </Button>
              </TabsContent>
            </Tabs>
          </DialogContent>
          </Dialog>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between gap-4">
            <div>
              <CardTitle className="flex items-center gap-2">
                <HardDrive className="w-5 h-5" />
                Stored Objects
              </CardTitle>
              <CardDescription>
                {response ? `${response.total} object${response.total !== 1 ? 's' : ''} in storage` : 'All large objects in storage'}
              </CardDescription>
            </div>
            <div className="relative w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                type="text"
                placeholder="Search objects..."
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value);
                  setCurrentPage(0);
                }}
                className="pl-9"
                data-testid="input-search-objects"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-8 text-muted-foreground">Loading objects...</div>
          ) : error ? (
            <div className="text-center py-8 text-muted-foreground">
              Failed to load large objects. Please try again.
            </div>
          ) : !objects || objects.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">No large objects found</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>File Name</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>State</TableHead>
                  <TableHead>Last Modified</TableHead>
                  <TableHead className="w-[140px] text-center sticky right-0 bg-background border-l z-10">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {objects.map((obj) => (
                  <TableRow key={obj.id}>
                    <TableCell className="font-mono text-xs" data-testid={`cell-id-${obj.id}`}>
                      {obj.id}
                    </TableCell>
                    <TableCell data-testid={`cell-filename-${obj.id}`} className="max-w-xs truncate">
                      {obj.originalFilename || obj.path.split('/').pop() || obj.path}
                    </TableCell>
                    <TableCell>
                      {obj.mimeType ? (
                        <Badge variant="outline">{obj.mimeType}</Badge>
                      ) : (
                        <span className="text-muted-foreground">—</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge variant={obj.state === 'UPLOADED' ? 'default' : 'outline'}>
                        {obj.state}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {obj.lastModified
                        ? new Date(obj.lastModified).toLocaleDateString()
                        : <span className="text-muted-foreground">—</span>}
                    </TableCell>
                    <TableCell className="w-[140px] sticky right-0 bg-background border-l z-10">
                      <div className="flex items-center justify-center gap-1">
                        {isImageType(obj.mimeType) && (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handlePreview(obj)}
                            data-testid={`button-preview-${obj.id}`}
                          >
                            <Eye className="w-4 h-4" />
                          </Button>
                        )}
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => handleDownload(obj.id, obj.originalFilename || obj.path.split('/').pop())}
                          data-testid={`button-download-${obj.id}`}
                        >
                          <Download className="w-4 h-4" />
                        </Button>
                        {obj.url && (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={async () => {
                              const url = await getCdnUrl(obj.id);
                              window.open(url, '_blank');
                            }}
                            data-testid={`button-open-url-${obj.id}`}
                          >
                            <ExternalLink className="w-4 h-4" />
                          </Button>
                        )}
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => {
                            if (confirm(`Are you sure you want to delete object ${obj.id}?`)) {
                              deleteMutation.mutate(obj.id);
                            }
                          }}
                          data-testid={`button-delete-${obj.id}`}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          
          {response && response.total > pageSize && (
            <div className="flex items-center justify-between pt-4 border-t">
              <div className="text-sm text-muted-foreground">
                Showing {currentPage * pageSize + 1}-{Math.min((currentPage + 1) * pageSize, response.total)} of {response.total}
              </div>
              <div className="flex items-center gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                  disabled={currentPage === 0}
                  data-testid="button-prev-page"
                >
                  <ChevronLeft className="w-4 h-4 mr-1" />
                  Previous
                </Button>
                <div className="text-sm text-muted-foreground">
                  Page {currentPage + 1} of {totalPages}
                </div>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setCurrentPage(p => Math.min(totalPages - 1, p + 1))}
                  disabled={currentPage >= totalPages - 1}
                  data-testid="button-next-page"
                >
                  Next
                  <ChevronRight className="w-4 h-4 ml-1" />
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Image Preview Dialog */}
      <Dialog open={previewDialog.open} onOpenChange={(open) => {
        if (!open && previewDialog.imageUrl) {
          window.URL.revokeObjectURL(previewDialog.imageUrl);
        }
        setPreviewDialog({ open, object: null });
      }}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Image Preview</DialogTitle>
            <DialogDescription>
              {previewDialog.object?.originalFilename || previewDialog.object?.path.split('/').pop() || 'Object Preview'}
            </DialogDescription>
          </DialogHeader>
          {previewDialog.object && previewDialog.imageUrl && (
            <div className="space-y-4">
              <div className="flex items-center justify-center bg-muted rounded-lg p-4 max-h-[50vh] overflow-hidden">
                <img
                  src={previewDialog.imageUrl}
                  alt={previewDialog.object.originalFilename || previewDialog.object.path}
                  className="max-w-full max-h-[50vh] w-auto h-auto object-contain"
                  data-testid={`img-preview-${previewDialog.object.id}`}
                />
              </div>
              <div className="grid grid-cols-2 gap-4 text-sm">
                <div>
                  <p className="text-muted-foreground">ID</p>
                  <p className="font-mono text-xs break-all" data-testid="text-preview-id">
                    {previewDialog.object.id}
                  </p>
                </div>
                <div>
                  <p className="text-muted-foreground">MIME Type</p>
                  <p data-testid="text-preview-mime">{previewDialog.object.mimeType}</p>
                </div>
                <div>
                  <p className="text-muted-foreground">State</p>
                  <Badge variant={previewDialog.object.state === 'UPLOADED' ? 'default' : 'outline'}>
                    {previewDialog.object.state}
                  </Badge>
                </div>
                <div>
                  <p className="text-muted-foreground">Last Modified</p>
                  <p data-testid="text-preview-modified">
                    {previewDialog.object.lastModified
                      ? new Date(previewDialog.object.lastModified).toLocaleString()
                      : '—'}
                  </p>
                </div>
              </div>
              <div className="flex gap-2">
                <Button
                  onClick={() => previewDialog.object && handleDownload(previewDialog.object.id, previewDialog.object.originalFilename || previewDialog.object.path.split('/').pop())}
                  data-testid="button-preview-download"
                >
                  <Download className="w-4 h-4 mr-2" />
                  Download
                </Button>
                <Button
                  variant="outline"
                  onClick={async () => {
                    if (previewDialog.object) {
                      try {
                        const blob = await fetchObjectAsBlob(previewDialog.object.id);
                        const url = window.URL.createObjectURL(blob);
                        window.open(url, '_blank');
                        // Clean up after a delay to allow the new tab to load
                        setTimeout(() => window.URL.revokeObjectURL(url), 1000);
                      } catch (error) {
                        toast({
                          title: 'Error',
                          description: 'Failed to open object',
                          variant: 'destructive',
                        });
                      }
                    }
                  }}
                  data-testid="button-preview-open"
                >
                  <ExternalLink className="w-4 h-4 mr-2" />
                  Open in New Tab
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
