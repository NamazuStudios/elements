import { useState } from 'react';
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
import { Upload, Download, Trash2, Link as LinkIcon, HardDrive, File, ExternalLink, Eye, Search, ChevronLeft, ChevronRight } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';
import { getApiPath } from '@/lib/config';

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
  const pageSize = 20;

  const { data: response, isLoading, error } = useQuery<LargeObjectResponse>({
    queryKey: ['/api/proxy/api/rest/large_object', currentPage, searchQuery],
    queryFn: async () => {
      let url = `/api/proxy/api/rest/large_object?offset=${currentPage * pageSize}&limit=${pageSize}`;
      if (searchQuery.trim()) {
        url += `&search=${encodeURIComponent(searchQuery.trim())}`;
      }
      const response = await fetch(url);
      if (!response.ok) throw new Error('Failed to fetch');
      return response.json();
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
      const response = await apiRequest('POST', '/api/proxy/api/rest/large_object', data);
      return response.json();
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['/api/proxy/api/rest/large_object'] });
      setCurrentPage(0);
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
      const response = await apiRequest('POST', '/api/proxy/api/rest/large_object/from_url', urlData);
      return response.json();
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['/api/proxy/api/rest/large_object'] });
      setCurrentPage(0);
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
      const response = await apiRequest('DELETE', `/api/proxy/api/rest/large_object/${id}`);
      return response;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/proxy/api/rest/large_object'] });
      setCurrentPage(0);
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

      const createResponse = await apiRequest('POST', '/api/proxy/api/rest/large_object', createRequest);
      const createdObject = await createResponse.json();
      
      if (!createdObject.id) {
        throw new Error('Failed to create large object - no ID returned');
      }

      // Then upload the actual file content
      const formData = new FormData();
      formData.append('file', selectedFile);

      const uploadResponse = await fetch(`/api/proxy/api/rest/large_object/${createdObject.id}/content`, {
        method: 'PUT',
        body: formData,
      });

      if (!uploadResponse.ok) {
        throw new Error(`Content upload failed: ${uploadResponse.statusText}`);
      }

      queryClient.invalidateQueries({ queryKey: ['/api/proxy/api/rest/large_object'] });
      setCurrentPage(0);
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

  const handleDownload = async (objectId: string, fileName?: string) => {
    try {
      const cdnUrl = await getCdnUrl(objectId);
      // Add ?download=true to trigger attachment disposition
      const downloadUrl = `${cdnUrl}?download=true`;
      const response = await fetch(downloadUrl);

      if (!response.ok) throw new Error('Download failed');

      const blob = await response.blob();
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

  const [previewDialog, setPreviewDialog] = useState<{ open: boolean; object: LargeObject | null }>({
    open: false,
    object: null,
  });

  const formatFileSize = (bytes?: number) => {
    if (!bytes) return 'Unknown';
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${sizes[i]}`;
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Large Objects</h1>
          <p className="text-muted-foreground mt-1">Manage file storage and large binary objects</p>
        </div>
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
                  <TableHead className="text-right">Actions</TableHead>
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
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-2">
                        {isImageType(obj.mimeType) && (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => setPreviewDialog({ open: true, object: obj })}
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
      <Dialog open={previewDialog.open} onOpenChange={(open) => setPreviewDialog({ open, object: null })}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Image Preview</DialogTitle>
            <DialogDescription>
              {previewDialog.object?.originalFilename || previewDialog.object?.path.split('/').pop() || 'Object Preview'}
            </DialogDescription>
          </DialogHeader>
          {previewDialog.object && (
            <div className="space-y-4">
              <div className="flex items-center justify-center bg-muted rounded-lg p-4 max-h-[50vh] overflow-hidden">
                <img
                  src={`/api/proxy/cdn/object/${previewDialog.object.id}`}
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
                      const cdnUrl = await getCdnUrl(previewDialog.object.id);
                      window.open(cdnUrl, '_blank');
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
