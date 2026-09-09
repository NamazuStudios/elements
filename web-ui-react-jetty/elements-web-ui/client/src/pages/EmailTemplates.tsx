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
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { Mail, Plus, Pencil, Trash2, Eye, ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';

const RESERVED_KEY_PREFIX = 'dev.getelements.elements.';

interface TemplateVariable {
  token: string;
  description: string;
  sampleValue: string;
}

// Documented variables for the core, built-in templates. Custom Elements' own templates don't have
// a documented set here since the platform has no way to know what placeholders they use.
const CORE_TEMPLATE_VARIABLES: Record<string, TemplateVariable[]> = {
  'dev.getelements.elements.password_reset.email_template': [
    { token: '{link}', description: 'Full password-reset URL the recipient should click', sampleValue: 'https://example.com/reset-password?token=sample-token-1234' },
  ],
  'dev.getelements.elements.verification.email_template': [
    { token: '{link}', description: 'Full email-verification URL the recipient should click', sampleValue: 'https://example.com/verify-email?token=sample-token-1234' },
  ],
};

const renderPreviewHtml = (key: string, body: string) => {
  const variables = CORE_TEMPLATE_VARIABLES[key];
  if (variables) {
    return variables.reduce((html, v) => html.split(v.token).join(v.sampleValue), body);
  }
  // Fall back to a generic {link} substitution so previewing custom templates that happen to use
  // the same convention still renders something sensible.
  return body.split('{link}').join('https://example.com/sample-link');
};

interface EmailTemplate {
  id: string;
  key: string;
  name: string;
  subject: string;
  body: string;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

interface EmailTemplateResponse {
  objects: EmailTemplate[];
  offset: number;
  count: number;
  total: number;
}

interface EmailTemplateFormData {
  key: string;
  name: string;
  subject: string;
  description: string;
  body: string;
}

const EMPTY_FORM: EmailTemplateFormData = {
  key: '',
  name: '',
  subject: '',
  description: '',
  body: '',
};

const isReserved = (key: string) => key.startsWith(RESERVED_KEY_PREFIX);

export default function EmailTemplates() {
  const { toast } = useToast();
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false);
  const [createForm, setCreateForm] = useState<EmailTemplateFormData>(EMPTY_FORM);
  const [editDialog, setEditDialog] = useState<{ open: boolean; template: EmailTemplate | null }>({
    open: false,
    template: null,
  });
  const [editForm, setEditForm] = useState<EmailTemplateFormData>(EMPTY_FORM);
  const [currentPage, setCurrentPage] = useState(0);
  const [previewDialog, setPreviewDialog] = useState<{ open: boolean; key: string; subject: string; body: string }>({
    open: false,
    key: '',
    subject: '',
    body: '',
  });

  const openPreview = (key: string, subject: string, body: string) => {
    setPreviewDialog({ open: true, key, subject, body });
  };

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

  const { data: response, isLoading, isFetching, error } = useQuery<EmailTemplateResponse>({
    queryKey: ['/api/rest/email_template', currentPage, pageSize],
    queryFn: async () => {
      const path = `/api/rest/email_template?offset=${currentPage * pageSize}&count=${pageSize}`;
      const response = await apiRequest('GET', path);
      return response.json();
    },
    retry: false,
  });

  const templates = response?.objects || [];
  const totalPages = response ? Math.ceil(response.total / pageSize) : 0;

  const createMutation = useMutation({
    mutationFn: async (data: { key: string; name: string; subject: string; body: string; description?: string }) => {
      const response = await apiRequest('POST', '/api/rest/email_template', data);
      return response.json();
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/email_template'] });
      toast({
        title: 'Success',
        description: `Email template "${data.name || data.key}" created`,
      });
      setIsCreateDialogOpen(false);
      setCreateForm(EMPTY_FORM);
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to create email template',
        variant: 'destructive',
      });
    },
  });

  const updateMutation = useMutation({
    mutationFn: async (vars: { id: string; data: { name: string; subject: string; body: string; description?: string } }) => {
      const response = await apiRequest('PUT', `/api/rest/email_template/${vars.id}`, vars.data);
      return response.json();
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/email_template'] });
      toast({
        title: 'Success',
        description: `Email template "${data.name || data.key}" updated`,
      });
      setEditDialog({ open: false, template: null });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to update email template',
        variant: 'destructive',
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiRequest('DELETE', `/api/rest/email_template/${id}`);
      return id;
    },
    onSuccess: (deletedId) => {
      // If we're deleting the last item on this page and it's not the first page, go back one page
      const queryKey = ['/api/rest/email_template', currentPage, pageSize];
      const currentData = queryClient.getQueryData(queryKey) as EmailTemplateResponse | undefined;

      if (currentData && currentData.objects.length === 1 && currentPage > 0) {
        setCurrentPage(Math.max(0, currentPage - 1));
      }

      // Update all cached pages for this resource
      queryClient.setQueriesData(
        { queryKey: ['/api/rest/email_template'] },
        (oldData: any) => {
          if (!oldData || typeof oldData.total !== 'number') {
            return oldData; // Skip non-paginated responses
          }

          if (Array.isArray(oldData.objects)) {
            const filteredObjects = oldData.objects.filter((obj: any) => obj.id !== deletedId);

            if (filteredObjects.length < oldData.objects.length) {
              return {
                ...oldData,
                objects: filteredObjects,
                total: Math.max(0, oldData.total - 1),
              };
            }
          }

          return {
            ...oldData,
            total: Math.max(0, oldData.total - 1),
          };
        }
      );

      toast({ title: 'Success', description: 'Email template deleted successfully' });
    },
    onError: (error: any) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete email template',
        variant: 'destructive',
      });
    },
  });

  const handleCreate = () => {
    if (!createForm.key.trim() || !createForm.name.trim() || !createForm.subject.trim() || !createForm.body.trim()) {
      toast({
        title: 'Error',
        description: 'Key, Name, Subject, and Body are required',
        variant: 'destructive',
      });
      return;
    }

    createMutation.mutate({
      key: createForm.key.trim(),
      name: createForm.name.trim(),
      subject: createForm.subject.trim(),
      body: createForm.body,
      description: createForm.description.trim() || undefined,
    });
  };

  const openEditDialog = (template: EmailTemplate) => {
    setEditForm({
      key: template.key,
      name: template.name,
      subject: template.subject,
      description: template.description || '',
      body: template.body,
    });
    setEditDialog({ open: true, template });
  };

  const handleUpdate = () => {
    if (!editDialog.template) return;

    if (!editForm.name.trim() || !editForm.subject.trim() || !editForm.body.trim()) {
      toast({
        title: 'Error',
        description: 'Name, Subject, and Body are required',
        variant: 'destructive',
      });
      return;
    }

    updateMutation.mutate({
      id: editDialog.template.id,
      data: {
        name: editForm.name.trim(),
        subject: editForm.subject.trim(),
        body: editForm.body,
        description: editForm.description.trim() || undefined,
      },
    });
  };

  const handleDelete = (template: EmailTemplate) => {
    if (isReserved(template.key)) return;
    if (confirm(`Are you sure you want to delete email template "${template.name}"?`)) {
      deleteMutation.mutate(template.id);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Email Templates</h1>
          <p className="text-muted-foreground mt-1">Manage transactional and system email templates</p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            onClick={async () => {
              await queryClient.invalidateQueries({ queryKey: ['/api/rest/email_template'] });
            }}
            variant="outline"
            size="sm"
            disabled={isFetching}
            data-testid="button-refresh-email-templates"
          >
            <RefreshCw className={`w-4 h-4 mr-2 ${isFetching ? 'animate-spin' : ''}`} />
            Refresh
          </Button>
          <Dialog
            open={isCreateDialogOpen}
            onOpenChange={(open) => {
              setIsCreateDialogOpen(open);
              if (!open) setCreateForm(EMPTY_FORM);
            }}
          >
            <DialogTrigger asChild>
              <Button data-testid="button-create-email-template">
                <Plus className="w-4 h-4 mr-2" />
                Create Template
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>Create Email Template</DialogTitle>
                <DialogDescription>
                  Define a new email template. The key cannot be changed after creation.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="create-key">Key</Label>
                  <Input
                    id="create-key"
                    value={createForm.key}
                    onChange={(e) => setCreateForm((f) => ({ ...f, key: e.target.value }))}
                    placeholder="com.mystudio.mygame.welcome_email"
                    className="font-mono"
                    data-testid="input-create-key"
                  />
                  <p className="text-xs text-muted-foreground">
                    Reverse-DNS style, e.g. com.mystudio.mygame.welcome_email. Cannot be changed after creation.
                  </p>
                  {createForm.key.trim().startsWith(RESERVED_KEY_PREFIX) && (
                    <p className="text-xs text-destructive">
                      Keys starting with "{RESERVED_KEY_PREFIX}" are reserved for core templates and will be rejected by the server.
                    </p>
                  )}
                </div>
                <div className="space-y-2">
                  <Label htmlFor="create-name">Name</Label>
                  <Input
                    id="create-name"
                    value={createForm.name}
                    onChange={(e) => setCreateForm((f) => ({ ...f, name: e.target.value }))}
                    placeholder="Welcome Email"
                    data-testid="input-create-name"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="create-subject">Subject</Label>
                  <Input
                    id="create-subject"
                    value={createForm.subject}
                    onChange={(e) => setCreateForm((f) => ({ ...f, subject: e.target.value }))}
                    placeholder="Welcome to the game!"
                    data-testid="input-create-subject"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="create-description">Description (optional)</Label>
                  <Input
                    id="create-description"
                    value={createForm.description}
                    onChange={(e) => setCreateForm((f) => ({ ...f, description: e.target.value }))}
                    placeholder="Sent when a new user registers"
                    data-testid="input-create-description"
                  />
                </div>
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="create-body">Body (HTML)</Label>
                    <Button
                      type="button"
                      size="sm"
                      variant="ghost"
                      onClick={() => openPreview(createForm.key, createForm.subject, createForm.body)}
                      data-testid="button-preview-create-email-template"
                    >
                      <Eye className="w-4 h-4 mr-1" />
                      Preview
                    </Button>
                  </div>
                  <Textarea
                    id="create-body"
                    value={createForm.body}
                    onChange={(e) => setCreateForm((f) => ({ ...f, body: e.target.value }))}
                    placeholder="<html>...</html>"
                    className="font-mono text-xs"
                    rows={16}
                    data-testid="textarea-create-body"
                  />
                </div>
                <Button
                  onClick={handleCreate}
                  disabled={createMutation.isPending}
                  className="w-full"
                  data-testid="button-submit-create-email-template"
                >
                  {createMutation.isPending ? 'Creating...' : 'Create Template'}
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Mail className="w-5 h-5" />
            Templates
          </CardTitle>
          <CardDescription>
            {response ? `${response.total} template${response.total !== 1 ? 's' : ''}` : 'All email templates'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-8 text-muted-foreground">Loading templates...</div>
          ) : error ? (
            <div className="text-center py-8 text-muted-foreground">
              Failed to load email templates. Please try again.
            </div>
          ) : !templates || templates.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground">No email templates found</div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-auto">Key</TableHead>
                  <TableHead className="w-[160px]">Name</TableHead>
                  <TableHead className="w-[200px]">Subject</TableHead>
                  <TableHead className="w-[110px]">Updated At</TableHead>
                  <TableHead className="w-[150px] text-center sticky right-0 bg-background border-l z-10">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {templates.map((template) => {
                  const reserved = isReserved(template.key);
                  return (
                    <TableRow key={template.id}>
                      <TableCell data-testid={`cell-key-${template.id}`}>
                        <div className="flex items-center gap-2">
                          <span className="font-mono text-xs break-all">
                            {template.key}
                          </span>
                          {reserved && (
                            <Badge variant="outline" className="shrink-0" data-testid={`badge-core-${template.id}`}>
                              Core
                            </Badge>
                          )}
                        </div>
                      </TableCell>
                      <TableCell data-testid={`cell-name-${template.id}`}>{template.name}</TableCell>
                      <TableCell data-testid={`cell-subject-${template.id}`} className="max-w-xs truncate">
                        {template.subject}
                      </TableCell>
                      <TableCell data-testid={`cell-updated-${template.id}`}>
                        {template.updatedAt
                          ? new Date(template.updatedAt).toLocaleDateString()
                          : <span className="text-muted-foreground">—</span>}
                      </TableCell>
                      <TableCell className="w-[150px] sticky right-0 bg-background border-l z-10">
                        <div className="flex items-center justify-center gap-1">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => openPreview(template.key, template.subject, template.body)}
                            data-testid={`button-preview-${template.id}`}
                          >
                            <Eye className="w-4 h-4" />
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => openEditDialog(template)}
                            data-testid={`button-edit-${template.id}`}
                          >
                            <Pencil className="w-4 h-4" />
                          </Button>
                          {!reserved && (
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleDelete(template)}
                              data-testid={`button-delete-${template.id}`}
                            >
                              <Trash2 className="w-4 h-4" />
                            </Button>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
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
                  onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
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
                  onClick={() => setCurrentPage((p) => Math.min(totalPages - 1, p + 1))}
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

      {/* Edit Dialog */}
      <Dialog
        open={editDialog.open}
        onOpenChange={(open) => {
          if (!open) setEditDialog({ open: false, template: null });
        }}
      >
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit Email Template</DialogTitle>
            <DialogDescription>
              {editDialog.template && isReserved(editDialog.template.key)
                ? 'This is a core template. Its key cannot be changed and it cannot be deleted.'
                : 'Update the template fields below.'}
            </DialogDescription>
          </DialogHeader>
          {editDialog.template && (
            <div className="space-y-4">
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Label htmlFor="edit-key">Key</Label>
                  {isReserved(editDialog.template.key) && (
                    <Badge variant="outline">Core</Badge>
                  )}
                </div>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Input
                      id="edit-key"
                      value={editForm.key}
                      disabled
                      className="font-mono"
                      data-testid="input-edit-key"
                    />
                  </TooltipTrigger>
                  <TooltipContent>
                    Key cannot be changed after creation
                  </TooltipContent>
                </Tooltip>
                <p className="text-xs text-muted-foreground">Key cannot be changed after creation.</p>
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-name">Name</Label>
                <Input
                  id="edit-name"
                  value={editForm.name}
                  onChange={(e) => setEditForm((f) => ({ ...f, name: e.target.value }))}
                  data-testid="input-edit-name"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-subject">Subject</Label>
                <Input
                  id="edit-subject"
                  value={editForm.subject}
                  onChange={(e) => setEditForm((f) => ({ ...f, subject: e.target.value }))}
                  data-testid="input-edit-subject"
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-description">Description (optional)</Label>
                <Input
                  id="edit-description"
                  value={editForm.description}
                  onChange={(e) => setEditForm((f) => ({ ...f, description: e.target.value }))}
                  data-testid="input-edit-description"
                />
              </div>
              {CORE_TEMPLATE_VARIABLES[editForm.key] && (
                <div className="rounded-md border bg-muted/50 p-3 space-y-1.5" data-testid="box-edit-variables">
                  <p className="text-xs font-medium">Available variables</p>
                  {CORE_TEMPLATE_VARIABLES[editForm.key].map((v) => (
                    <p key={v.token} className="text-xs text-muted-foreground">
                      <code className="font-mono text-foreground">{v.token}</code> — {v.description}
                    </p>
                  ))}
                </div>
              )}
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <Label htmlFor="edit-body">Body (HTML)</Label>
                  <Button
                    type="button"
                    size="sm"
                    variant="ghost"
                    onClick={() => openPreview(editForm.key, editForm.subject, editForm.body)}
                    data-testid="button-preview-edit-email-template"
                  >
                    <Eye className="w-4 h-4 mr-1" />
                    Preview
                  </Button>
                </div>
                <Textarea
                  id="edit-body"
                  value={editForm.body}
                  onChange={(e) => setEditForm((f) => ({ ...f, body: e.target.value }))}
                  className="font-mono text-xs"
                  rows={16}
                  data-testid="textarea-edit-body"
                />
              </div>
              <Button
                onClick={handleUpdate}
                disabled={updateMutation.isPending}
                className="w-full"
                data-testid="button-submit-edit-email-template"
              >
                {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
              </Button>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* Preview Dialog */}
      <Dialog
        open={previewDialog.open}
        onOpenChange={(open) => setPreviewDialog((d) => ({ ...d, open }))}
      >
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Preview Email</DialogTitle>
            <DialogDescription>
              Rendered with sample values in place of any known variables. This is an approximation —
              actual rendering may vary by email client.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-3">
            <div className="rounded-md border px-3 py-2 bg-muted/50">
              <p className="text-xs text-muted-foreground">Subject</p>
              <p className="text-sm font-medium">{previewDialog.subject || <span className="text-muted-foreground">(no subject)</span>}</p>
            </div>
            <iframe
              title="email-preview"
              className="w-full h-[500px] rounded-md border bg-white"
              sandbox=""
              srcDoc={renderPreviewHtml(previewDialog.key, previewDialog.body)}
              data-testid="iframe-email-preview"
            />
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
