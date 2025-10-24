import { useState, useEffect } from 'react';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { CalendarIcon, Clock } from 'lucide-react';
import { format } from 'date-fns';

interface DateTimePickerProps {
  value?: number;
  onChange: (milliseconds: number | null) => void;
}

const TIMEZONES = [
  { label: 'UTC', value: 'UTC', offset: 0 },
  { label: 'EST (UTC-5)', value: 'EST', offset: -5 },
  { label: 'CST (UTC-6)', value: 'CST', offset: -6 },
  { label: 'MST (UTC-7)', value: 'MST', offset: -7 },
  { label: 'PST (UTC-8)', value: 'PST', offset: -8 },
  { label: 'GMT (UTC+0)', value: 'GMT', offset: 0 },
  { label: 'CET (UTC+1)', value: 'CET', offset: 1 },
  { label: 'EET (UTC+2)', value: 'EET', offset: 2 },
  { label: 'IST (UTC+5:30)', value: 'IST', offset: 5.5 },
  { label: 'CST (UTC+8)', value: 'CST_CHINA', offset: 8 },
  { label: 'JST (UTC+9)', value: 'JST', offset: 9 },
  { label: 'AEST (UTC+10)', value: 'AEST', offset: 10 },
];

// Get system timezone offset and find matching timezone
function getSystemTimezone(): string {
  const offsetMinutes = -new Date().getTimezoneOffset();
  const offsetHours = offsetMinutes / 60;
  
  // Find matching timezone from our list
  const matchingTz = TIMEZONES.find(tz => tz.offset === offsetHours);
  return matchingTz?.value || 'UTC';
}

export function DateTimePicker({ value, onChange }: DateTimePickerProps) {
  const [date, setDate] = useState<Date | undefined>();
  const [time, setTime] = useState('00:00');
  const [timezone, setTimezone] = useState(getSystemTimezone());
  const [open, setOpen] = useState(false);

  // Initialize from value (milliseconds since epoch)
  useEffect(() => {
    if (value) {
      const dateObj = new Date(value);
      setDate(dateObj);
      const hours = dateObj.getUTCHours().toString().padStart(2, '0');
      const minutes = dateObj.getUTCMinutes().toString().padStart(2, '0');
      setTime(`${hours}:${minutes}`);
    }
  }, [value]);

  const handleApply = () => {
    if (!date) {
      onChange(null);
      setOpen(false);
      return;
    }

    const [hours, minutes] = time.split(':').map(Number);
    const selectedTz = TIMEZONES.find(tz => tz.value === timezone);
    const tzOffset = selectedTz?.offset || 0;

    // Create UTC date
    const utcDate = new Date(Date.UTC(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      hours,
      minutes,
      0,
      0
    ));

    // Adjust for timezone offset (convert selected time to UTC)
    const utcMs = utcDate.getTime() - (tzOffset * 60 * 60 * 1000);
    
    onChange(utcMs);
    setOpen(false);
  };

  const displayValue = value 
    ? (() => {
        const date = new Date(value);
        const year = date.getUTCFullYear();
        const month = date.toLocaleString('en-US', { month: 'long', timeZone: 'UTC' });
        const day = date.getUTCDate();
        const hours = date.getUTCHours().toString().padStart(2, '0');
        const minutes = date.getUTCMinutes().toString().padStart(2, '0');
        return `${month} ${day}, ${year} ${hours}:${minutes} UTC`;
      })()
    : 'Pick date and time';

  return (
    <div className="space-y-2">
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            className="w-full justify-start text-left font-normal"
            data-testid="button-datetime-picker"
          >
            <CalendarIcon className="mr-2 h-4 w-4" />
            {displayValue}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <div className="p-4 space-y-4">
            <Calendar
              mode="single"
              selected={date}
              onSelect={setDate}
              initialFocus
            />
            
            <div className="space-y-3 border-t pt-4">
              <div>
                <Label className="text-sm font-medium">Time</Label>
                <div className="flex items-center gap-2 mt-1">
                  <Clock className="h-4 w-4 text-muted-foreground" />
                  <Input
                    type="time"
                    value={time}
                    onChange={(e) => setTime(e.target.value)}
                    className="flex-1"
                    data-testid="input-time"
                  />
                </div>
              </div>

              <div>
                <Label className="text-sm font-medium">Timezone</Label>
                <Select value={timezone} onValueChange={setTimezone}>
                  <SelectTrigger className="mt-1" data-testid="select-timezone">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {TIMEZONES.map((tz) => (
                      <SelectItem key={tz.value} value={tz.value}>
                        {tz.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="flex gap-2 pt-2">
                <Button
                  variant="outline"
                  className="flex-1"
                  onClick={() => setOpen(false)}
                  data-testid="button-cancel-datetime"
                >
                  Cancel
                </Button>
                <Button
                  className="flex-1"
                  onClick={handleApply}
                  disabled={!date}
                  data-testid="button-apply-datetime"
                >
                  Apply
                </Button>
              </div>
            </div>
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
}
