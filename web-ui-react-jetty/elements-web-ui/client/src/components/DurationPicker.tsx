import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface DurationPickerProps {
  value: number; // Total seconds
  onChange: (seconds: number) => void;
  label?: string;
  id?: string;
  testId?: string;
}

export function DurationPicker({ value, onChange, label, id, testId }: DurationPickerProps) {
  // Convert total seconds to days, hours, minutes, seconds
  const days = Math.floor(value / 86400);
  const hours = Math.floor((value % 86400) / 3600);
  const minutes = Math.floor((value % 3600) / 60);
  const seconds = value % 60;

  const updateDuration = (field: 'days' | 'hours' | 'minutes' | 'seconds', newValue: string) => {
    const num = parseInt(newValue) || 0;
    let newDays = days;
    let newHours = hours;
    let newMinutes = minutes;
    let newSeconds = seconds;

    switch (field) {
      case 'days':
        newDays = num;
        break;
      case 'hours':
        newHours = num;
        break;
      case 'minutes':
        newMinutes = num;
        break;
      case 'seconds':
        newSeconds = num;
        break;
    }

    const totalSeconds = newDays * 86400 + newHours * 3600 + newMinutes * 60 + newSeconds;
    onChange(totalSeconds);
  };

  return (
    <div>
      {label && <Label htmlFor={id}>{label}</Label>}
      <div className="grid grid-cols-4 gap-2 mt-2">
        <div>
          <Label htmlFor={`${id}-days`} className="text-xs text-muted-foreground">Days</Label>
          <Input
            id={`${id}-days`}
            type="number"
            min="0"
            value={days}
            onChange={(e) => updateDuration('days', e.target.value)}
            data-testid={testId ? `${testId}-days` : undefined}
          />
        </div>
        <div>
          <Label htmlFor={`${id}-hours`} className="text-xs text-muted-foreground">Hours</Label>
          <Input
            id={`${id}-hours`}
            type="number"
            min="0"
            max="23"
            value={hours}
            onChange={(e) => updateDuration('hours', e.target.value)}
            data-testid={testId ? `${testId}-hours` : undefined}
          />
        </div>
        <div>
          <Label htmlFor={`${id}-minutes`} className="text-xs text-muted-foreground">Minutes</Label>
          <Input
            id={`${id}-minutes`}
            type="number"
            min="0"
            max="59"
            value={minutes}
            onChange={(e) => updateDuration('minutes', e.target.value)}
            data-testid={testId ? `${testId}-minutes` : undefined}
          />
        </div>
        <div>
          <Label htmlFor={`${id}-seconds`} className="text-xs text-muted-foreground">Seconds</Label>
          <Input
            id={`${id}-seconds`}
            type="number"
            min="0"
            max="59"
            value={seconds}
            onChange={(e) => updateDuration('seconds', e.target.value)}
            data-testid={testId ? `${testId}-seconds` : undefined}
          />
        </div>
      </div>
    </div>
  );
}
