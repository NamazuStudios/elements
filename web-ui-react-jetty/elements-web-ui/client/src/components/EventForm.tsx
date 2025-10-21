import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { format } from 'date-fns';
import { Calendar as CalendarIcon, Loader2, Search, X, Plus, AlertCircle } from 'lucide-react';
import { apiClient } from '@/lib/api-client';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { useToast } from '@/hooks/use-toast';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/ui/popover';
import { Badge } from '@/components/ui/badge';
import { ResourceSearchDialog } from '@/components/ResourceSearchDialog';
import { cn } from '@/lib/utils';

const eventSchema = z.object({
  begin: z.date({ required_error: 'Start date is required' }),
  end: z.date({ required_error: 'End date is required' }),
  missionNamesOrIds: z.array(z.string()).min(1, 'At least one mission is required'),
}).refine(
  (data) => data.end >= data.begin,
  {
    message: 'End date must be after or equal to start date',
    path: ['end'],
  }
);

type EventFormData = z.infer<typeof eventSchema>;

interface EventFormProps {
  mode: 'create' | 'update';
  scheduleId: string;
  initialData?: any;
  onSubmit: (data: any) => void;
  onCancel: () => void;
  isPending?: boolean;
}

export function EventForm({ mode, scheduleId, initialData, onSubmit, onCancel, isPending }: EventFormProps) {
  const { toast } = useToast();
  const [startOpen, setStartOpen] = useState(false);
  const [endOpen, setEndOpen] = useState(false);
  const [missionSearchOpen, setMissionSearchOpen] = useState(false);

  const { data: missionsList, isLoading: missionsLoading } = useQuery({
    queryKey: ['/api/rest/mission'],
    queryFn: async () => {
      const response = await apiClient.request<any>('/api/rest/mission');
      if (response && typeof response === 'object' && 'objects' in response) {
        return response.objects || [];
      }
      return Array.isArray(response) ? response : [];
    },
  });

  const form = useForm<EventFormData>({
    resolver: zodResolver(eventSchema),
    defaultValues: {
      begin: initialData?.begin ? new Date(initialData.begin) : undefined,
      end: initialData?.end ? new Date(initialData.end) : undefined,
      missionNamesOrIds: initialData?.missions?.map((m: any) => m.id || m) || [],
    },
  });

  // Watch the form value to derive selected missions
  const missionIds = form.watch('missionNamesOrIds') || [];
  
  // Derive selected missions from form value and missionsList
  const selectedMissions = missionIds.map((id: string) => {
    const mission = missionsList?.find((m: any) => m.id === id);
    return {
      id,
      name: mission?.displayName || mission?.name || id,
    };
  });

  const handleSubmit = (data: EventFormData) => {
    const payload = {
      ...(mode === 'update' && initialData?.id ? { id: initialData.id } : {}),
      missionNamesOrIds: data.missionNamesOrIds,
      begin: data.begin.getTime(),
      end: data.end.getTime(),
    };
    onSubmit(payload);
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        <div className="grid grid-cols-2 gap-4">
          <FormField
            control={form.control}
            name="begin"
            render={({ field }) => (
              <FormItem className="flex flex-col">
                <FormLabel>Start Date</FormLabel>
                <Popover open={startOpen} onOpenChange={setStartOpen}>
                  <PopoverTrigger asChild>
                    <FormControl>
                      <Button
                        variant="outline"
                        className={cn(
                          'w-full pl-3 text-left font-normal',
                          !field.value && 'text-muted-foreground'
                        )}
                        data-testid="button-start-date"
                      >
                        {field.value ? (
                          format(field.value, 'PPP')
                        ) : (
                          <span>Pick a date</span>
                        )}
                        <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                      </Button>
                    </FormControl>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={field.value}
                      onSelect={(date) => {
                        field.onChange(date);
                        setStartOpen(false);
                      }}
                      initialFocus
                    />
                  </PopoverContent>
                </Popover>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="end"
            render={({ field }) => (
              <FormItem className="flex flex-col">
                <FormLabel>End Date</FormLabel>
                <Popover open={endOpen} onOpenChange={setEndOpen}>
                  <PopoverTrigger asChild>
                    <FormControl>
                      <Button
                        variant="outline"
                        className={cn(
                          'w-full pl-3 text-left font-normal',
                          !field.value && 'text-muted-foreground'
                        )}
                        data-testid="button-end-date"
                      >
                        {field.value ? (
                          format(field.value, 'PPP')
                        ) : (
                          <span>Pick a date</span>
                        )}
                        <CalendarIcon className="ml-auto h-4 w-4 opacity-50" />
                      </Button>
                    </FormControl>
                  </PopoverTrigger>
                  <PopoverContent className="w-auto p-0" align="start">
                    <Calendar
                      mode="single"
                      selected={field.value}
                      onSelect={(date) => {
                        field.onChange(date);
                        setEndOpen(false);
                      }}
                      initialFocus
                    />
                  </PopoverContent>
                </Popover>
                <FormMessage />
              </FormItem>
            )}
          />
        </div>

        <FormField
          control={form.control}
          name="missionNamesOrIds"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Missions</FormLabel>
              <div className="space-y-3">
                <div className="flex flex-wrap gap-2 min-h-[2.5rem] p-3 border rounded-lg bg-background">
                  {missionsLoading ? (
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Loader2 className="w-4 h-4 animate-spin" />
                      <span>Loading missions...</span>
                    </div>
                  ) : selectedMissions.length > 0 ? (
                    selectedMissions.map((mission) => (
                      <Badge
                        key={mission.id}
                        variant="secondary"
                        className="flex items-center gap-1"
                        data-testid={`badge-mission-${mission.id}`}
                      >
                        <span>{mission.name}</span>
                        <button
                          type="button"
                          onClick={() => {
                            const currentIds = form.getValues('missionNamesOrIds') || [];
                            const newIds = currentIds.filter((id: string) => id !== mission.id);
                            form.setValue('missionNamesOrIds', newIds, { shouldValidate: true });
                          }}
                          className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
                          data-testid={`button-remove-mission-${mission.id}`}
                        >
                          <X className="w-3 h-3" />
                        </button>
                      </Badge>
                    ))
                  ) : (
                    <span className="text-sm text-muted-foreground">No missions selected</span>
                  )}
                </div>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setMissionSearchOpen(true)}
                  className="w-full"
                  data-testid="button-add-mission"
                >
                  <Plus className="w-4 h-4 mr-2" />
                  Add Mission
                </Button>
              </div>
              <FormMessage />
            </FormItem>
          )}
        />

        <ResourceSearchDialog
          open={missionSearchOpen}
          onOpenChange={setMissionSearchOpen}
          onSelect={(missionId, mission) => {
            const currentIds = form.getValues('missionNamesOrIds') || [];
            // Check if already selected
            if (currentIds.includes(missionId)) {
              toast({
                title: 'Mission already selected',
                description: `${mission.displayName || mission.name || missionId} is already in this event`,
                variant: 'default',
              });
              return; // Keep dialog open
            }
            form.setValue('missionNamesOrIds', [...currentIds, missionId], { shouldValidate: true });
            setMissionSearchOpen(false);
          }}
          resourceType="mission"
          endpoint="/api/rest/mission"
          title="Search Missions"
          description="Search for missions to add to this event"
          displayFields={[
            { label: 'Name', key: 'displayName' },
            { label: 'ID', key: 'id' },
          ]}
          searchPlaceholder="Search by name or ID..."
          currentResourceId={undefined}
        />

        <div className="flex justify-end gap-3">
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            data-testid="button-cancel"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            disabled={isPending}
            data-testid="button-submit"
          >
            {isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            {mode === 'create' ? 'Create' : 'Update'}
          </Button>
        </div>
      </form>
    </Form>
  );
}
