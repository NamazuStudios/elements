import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Loader2, Plus, Pencil, Trash2, Calendar } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiClient } from '@/lib/api-client';
import { queryClient } from '@/lib/queryClient';
import { EventForm } from './EventForm';

interface ScheduleEventsEditorProps {
  schedule: any;
  onClose: () => void;
}

export function ScheduleEventsEditor({ schedule, onClose }: ScheduleEventsEditorProps) {
  const { toast } = useToast();
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [dialogMode, setDialogMode] = useState<'create' | 'update'>('create');
  const [selectedEvent, setSelectedEvent] = useState<any>(null);
  const [jsonText, setJsonText] = useState<string>('');
  const [jsonError, setJsonError] = useState<string>('');
  const [viewMode, setViewMode] = useState<'form' | 'json'>('form');

  const endpoint = `/api/rest/schedule/${schedule.id}/event`;

  const { data: events, isLoading } = useQuery({
    queryKey: [endpoint],
    queryFn: async () => {
      const response = await apiClient.request<any>(endpoint);
      if (response && typeof response === 'object' && 'objects' in response) {
        return response.objects || [];
      }
      return Array.isArray(response) ? response : [];
    },
  });

  const saveMutation = useMutation({
    mutationFn: async (data: any) => {
      if (dialogMode === 'create') {
        return await apiClient.request(endpoint, {
          method: 'POST',
          body: JSON.stringify(data),
        });
      } else {
        return await apiClient.request(`${endpoint}/${data.id}`, {
          method: 'PUT',
          body: JSON.stringify(data),
        });
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [endpoint] });
      toast({
        title: 'Success',
        description: `Event ${dialogMode === 'create' ? 'created' : 'updated'} successfully`,
      });
      setIsDialogOpen(false);
      setSelectedEvent(null);
    },
    onError: (error: Error & { status?: number }) => {
      toast({
        title: 'Error',
        description: error.message || `Failed to ${dialogMode} event`,
        variant: 'destructive',
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.request(`${endpoint}/${id}`, { method: 'DELETE' });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [endpoint] });
      toast({
        title: 'Success',
        description: 'Event deleted successfully',
      });
    },
    onError: (error: Error & { status?: number }) => {
      toast({
        title: 'Error',
        description: error.message || 'Failed to delete event',
        variant: 'destructive',
      });
    },
  });

  const handleSave = async (data: any) => {
    await saveMutation.mutateAsync(data);
  };

  const handleJsonSave = () => {
    try {
      const parsed = JSON.parse(jsonText);
      handleSave(parsed);
    } catch (error) {
      setJsonError('Invalid JSON format');
    }
  };

  const handleDelete = async (event: any) => {
    if (confirm(`Are you sure you want to delete this event?`)) {
      await deleteMutation.mutateAsync(event.id);
    }
  };

  const columns = events && events.length > 0 
    ? Object.keys(events[0]).filter(key => !['schedule'].includes(key))
    : [];
  
  const formatColumnHeader = (col: string) => {
    return col.charAt(0).toUpperCase() + col.slice(1);
  };
  
  const formatCellValue = (col: string, value: any) => {
    if ((col === 'begin' || col === 'end') && typeof value === 'number') {
      return new Date(value).toLocaleString();
    }
    if (col === 'missions' && Array.isArray(value)) {
      return value.map((m: any) => m.displayName || m.name || m.id || m).join(', ');
    }
    if (typeof value === 'object') {
      return JSON.stringify(value);
    }
    return String(value ?? '');
  };

  return (
    <>
      <div className="flex items-center justify-between p-6 pb-4 border-b">
        <div>
          <h2 className="text-lg font-semibold flex items-center gap-2">
            <Calendar className="w-5 h-5" />
            Events for {schedule.displayName || schedule.name}
          </h2>
          <p className="text-sm text-muted-foreground mt-1">
            Manage scheduled events for this schedule
          </p>
        </div>
        <Button
          onClick={() => {
            setSelectedEvent(null);
            setJsonText('{}');
            setJsonError('');
            setDialogMode('create');
            setIsDialogOpen(true);
          }}
          data-testid="button-create-event"
          className="mr-8"
        >
          <Plus className="w-4 h-4 mr-2" />
          Create Event
        </Button>
      </div>
      <div className="p-6">
        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
            <span className="ml-3 text-muted-foreground">Loading events...</span>
          </div>
        ) : events && events.length > 0 ? (
          <div className="border rounded-lg overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  {columns.map((col) => (
                    <TableHead key={col}>{formatColumnHeader(col)}</TableHead>
                  ))}
                  <TableHead className="sticky right-0 bg-background w-[100px] z-10">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {events.map((event: any, idx: number) => (
                  <TableRow key={event.id || idx} data-testid={`row-event-${idx}`}>
                    {columns.map((col) => (
                      <TableCell key={col} data-testid={`cell-${col}-${idx}`}>
                        {formatCellValue(col, event[col])}
                      </TableCell>
                    ))}
                    <TableCell className="sticky right-0 bg-background z-10">
                      <div className="flex gap-2">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => {
                            setSelectedEvent(event);
                            setJsonText(JSON.stringify(event, null, 2));
                            setJsonError('');
                            setDialogMode('update');
                            setIsDialogOpen(true);
                          }}
                          data-testid={`button-edit-event-${idx}`}
                        >
                          <Pencil className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleDelete(event)}
                          data-testid={`button-delete-event-${idx}`}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        ) : (
          <div className="text-center py-12 text-muted-foreground">
            No events found. Create your first event to get started.
          </div>
        )}
      </div>

      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>
              {dialogMode === 'create' ? 'Create Event' : 'Update Event'}
            </DialogTitle>
          </DialogHeader>

          <Tabs value={viewMode} onValueChange={(v) => setViewMode(v as 'form' | 'json')}>
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="form" data-testid="tab-guided-form">Guided Form</TabsTrigger>
              <TabsTrigger value="json" data-testid="tab-json-editor">JSON Editor</TabsTrigger>
            </TabsList>

            <TabsContent value="form" className="mt-4">
              <EventForm
                mode={dialogMode}
                scheduleId={schedule.id}
                initialData={selectedEvent}
                onSubmit={handleSave}
                onCancel={() => setIsDialogOpen(false)}
                isPending={saveMutation.isPending}
              />
            </TabsContent>

            <TabsContent value="json" className="mt-4">
              <div className="space-y-4">
                <div className="space-y-2">
                  <label className="text-sm font-medium">Event JSON</label>
                  <textarea
                    value={jsonText}
                    onChange={(e) => {
                      setJsonText(e.target.value);
                      setJsonError('');
                    }}
                    className="w-full h-96 p-4 font-mono text-sm border rounded-lg bg-background"
                    data-testid="textarea-json"
                  />
                  {jsonError && (
                    <p className="text-sm text-destructive" data-testid="text-json-error">
                      {jsonError}
                    </p>
                  )}
                </div>
                <div className="flex justify-end gap-3">
                  <Button
                    variant="outline"
                    onClick={() => setIsDialogOpen(false)}
                    data-testid="button-cancel-json"
                  >
                    Cancel
                  </Button>
                  <Button
                    onClick={handleJsonSave}
                    disabled={saveMutation.isPending}
                    data-testid="button-save-json"
                  >
                    {saveMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                    {dialogMode === 'create' ? 'Create' : 'Update'}
                  </Button>
                </div>
              </div>
            </TabsContent>
          </Tabs>
        </DialogContent>
      </Dialog>
    </>
  );
}
