import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Switch } from '@/components/ui/switch';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Loader2, Shield } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';
import {
  OPERATIONS,
  toEditableSubjects,
  toUpdateRequest,
  describeVisibility,
  type AccessPermissions,
  type EditableSubjects,
  type Operation,
} from '@/lib/large-object-permissions';

interface LargeObjectPermissionsTarget {
  id: string;
  mimeType: string | null;
  accessPermissions: AccessPermissions;
}

/**
 * Permissions editor for a Large Object (#157): surfaces the platform's existing per-operation
 * (read/write/delete) subject model -- public (wildcard) vs. restricted to specific users/profiles --
 * and saves via `PUT /api/rest/large_object/{id}`. The minimum access level, when present, is
 * display-only: the REST update surface does not carry it.
 */
export function LargeObjectPermissionsDialog({
  largeObject,
  open,
  onOpenChange,
}: {
  largeObject: LargeObjectPermissionsTarget | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const { toast } = useToast();

  const [edits, setEdits] = useState<Record<Operation, EditableSubjects> | null>(null);

  // (Re)seed the editable state from the object whenever the dialog is opened for it.
  const [seededFor, setSeededFor] = useState<string | null>(null);
  if (open && largeObject && seededFor !== largeObject.id) {
    setSeededFor(largeObject.id);
    setEdits({
      read: toEditableSubjects(largeObject.accessPermissions, 'read'),
      write: toEditableSubjects(largeObject.accessPermissions, 'write'),
      delete: toEditableSubjects(largeObject.accessPermissions, 'delete'),
    });
  }
  if (!open && seededFor !== null) {
    setSeededFor(null);
    setEdits(null);
  }

  const updateMutation = useMutation({
    mutationFn: async () => {
      const body = toUpdateRequest(largeObject!, edits!);
      const response = await apiRequest(
        'PUT',
        `/api/rest/large_object/${encodeURIComponent(largeObject!.id)}`,
        body,
      );
      return await response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/large_object'] });
      toast({ title: 'Permissions updated' });
      onOpenChange(false);
    },
    onError: (error: Error) => {
      // The server validates subject existence (unknown user/profile ids) and request shape;
      // surface its message verbatim.
      toast({ title: 'Failed to update permissions', description: error.message, variant: 'destructive' });
    },
  });

  const setEdit = (operation: Operation, patch: Partial<EditableSubjects>) => {
    setEdits(prev => prev ? { ...prev, [operation]: { ...prev[operation], ...patch } } : prev);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto" data-testid="dialog-large-object-permissions">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Shield className="h-5 w-5" />
            Permissions
          </DialogTitle>
          <DialogDescription>
            Who may access this object. Public objects are accessible by anyone with the URL; restricted
            objects are accessible only to the listed users and profiles.
          </DialogDescription>
        </DialogHeader>

        {!largeObject || !edits ? null : (
          <div className="space-y-4">
            {OPERATIONS.map(operation => {
              const edit = edits[operation];
              return (
                <Card key={operation}>
                  <CardHeader className="pb-2">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-base capitalize">{operation}</CardTitle>
                      <Badge variant={edit.wildcard ? 'default' : 'outline'} data-testid={`badge-visibility-${operation}`}>
                        {describeVisibility(edit)}
                      </Badge>
                    </div>
                    <CardDescription>
                      {operation === 'read' && 'Who may read/download the object\'s content.'}
                      {operation === 'write' && 'Who may upload new content to the object.'}
                      {operation === 'delete' && 'Who may delete the object.'}
                    </CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="flex items-center justify-between">
                      <Label htmlFor={`wildcard-${operation}`} className="font-normal">
                        Public (anyone with the URL)
                      </Label>
                      <Switch
                        id={`wildcard-${operation}`}
                        checked={edit.wildcard}
                        onCheckedChange={checked => setEdit(operation, { wildcard: checked })}
                        data-testid={`switch-wildcard-${operation}`}
                      />
                    </div>
                    <div className="space-y-1">
                      <Label htmlFor={`users-${operation}`} className="text-sm text-muted-foreground">
                        User IDs (comma-separated)
                      </Label>
                      <Input
                        id={`users-${operation}`}
                        value={edit.userIdsRaw}
                        onChange={e => setEdit(operation, { userIdsRaw: e.target.value })}
                        placeholder="e.g. 603a... 603b..."
                        data-testid={`input-users-${operation}`}
                      />
                    </div>
                    <div className="space-y-1">
                      <Label htmlFor={`profiles-${operation}`} className="text-sm text-muted-foreground">
                        Profile IDs (comma-separated)
                      </Label>
                      <Input
                        id={`profiles-${operation}`}
                        value={edit.profileIdsRaw}
                        onChange={e => setEdit(operation, { profileIdsRaw: e.target.value })}
                        placeholder="e.g. 603c... 603d..."
                        data-testid={`input-profiles-${operation}`}
                      />
                    </div>
                    {edit.minimumLevel && (
                      <p className="text-xs text-muted-foreground" data-testid={`minimum-level-${operation}`}>
                        Minimum access level: {edit.minimumLevel} (set at object creation; not editable here)
                      </p>
                    )}
                  </CardContent>
                </Card>
              );
            })}

            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => onOpenChange(false)} data-testid="button-cancel-permissions">
                Cancel
              </Button>
              <Button
                onClick={() => updateMutation.mutate()}
                disabled={updateMutation.isPending}
                data-testid="button-save-permissions"
              >
                {updateMutation.isPending && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
                Save permissions
              </Button>
            </div>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
