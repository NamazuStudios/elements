import { useState, useEffect } from 'react';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface IntervalEditorProps {
  value?: number;
  onChange: (milliseconds: number | null) => void;
}

const MS_PER_SECOND = 1000;
const MS_PER_MINUTE = MS_PER_SECOND * 60;
const MS_PER_HOUR = MS_PER_MINUTE * 60;
const MS_PER_DAY = MS_PER_HOUR * 24;
const MS_PER_MONTH = MS_PER_DAY * 30; // Approximation

export function IntervalEditor({ value, onChange }: IntervalEditorProps) {
  const [months, setMonths] = useState(0);
  const [days, setDays] = useState(0);
  const [hours, setHours] = useState(0);
  const [minutes, setMinutes] = useState(0);
  const [seconds, setSeconds] = useState(0);

  // Parse value (milliseconds) into components
  useEffect(() => {
    if (value) {
      let remaining = value;
      
      const monthsVal = Math.floor(remaining / MS_PER_MONTH);
      remaining -= monthsVal * MS_PER_MONTH;
      
      const daysVal = Math.floor(remaining / MS_PER_DAY);
      remaining -= daysVal * MS_PER_DAY;
      
      const hoursVal = Math.floor(remaining / MS_PER_HOUR);
      remaining -= hoursVal * MS_PER_HOUR;
      
      const minutesVal = Math.floor(remaining / MS_PER_MINUTE);
      remaining -= minutesVal * MS_PER_MINUTE;
      
      const secondsVal = Math.floor(remaining / MS_PER_SECOND);
      
      setMonths(monthsVal);
      setDays(daysVal);
      setHours(hoursVal);
      setMinutes(minutesVal);
      setSeconds(secondsVal);
    }
  }, [value]);

  const handleUpdate = (newMonths: number, newDays: number, newHours: number, newMinutes: number, newSeconds: number) => {
    const totalMs = 
      (newMonths * MS_PER_MONTH) +
      (newDays * MS_PER_DAY) +
      (newHours * MS_PER_HOUR) +
      (newMinutes * MS_PER_MINUTE) +
      (newSeconds * MS_PER_SECOND);
    
    onChange(totalMs || null);
  };

  const handleMonthsChange = (val: string) => {
    const num = parseInt(val) || 0;
    setMonths(num);
    handleUpdate(num, days, hours, minutes, seconds);
  };

  const handleDaysChange = (val: string) => {
    const num = parseInt(val) || 0;
    setDays(num);
    handleUpdate(months, num, hours, minutes, seconds);
  };

  const handleHoursChange = (val: string) => {
    const num = parseInt(val) || 0;
    setHours(num);
    handleUpdate(months, days, num, minutes, seconds);
  };

  const handleMinutesChange = (val: string) => {
    const num = parseInt(val) || 0;
    setMinutes(num);
    handleUpdate(months, days, hours, num, seconds);
  };

  const handleSecondsChange = (val: string) => {
    const num = parseInt(val) || 0;
    setSeconds(num);
    handleUpdate(months, days, hours, minutes, num);
  };

  return (
    <div className="space-y-3">
      <div className="grid grid-cols-5 gap-3">
        <div>
          <Label className="text-xs text-muted-foreground mb-1">Months</Label>
          <Input
            type="number"
            min="0"
            value={months}
            onChange={(e) => handleMonthsChange(e.target.value)}
            placeholder="0"
            data-testid="input-interval-months"
          />
        </div>
        <div>
          <Label className="text-xs text-muted-foreground mb-1">Days</Label>
          <Input
            type="number"
            min="0"
            value={days}
            onChange={(e) => handleDaysChange(e.target.value)}
            placeholder="0"
            data-testid="input-interval-days"
          />
        </div>
        <div>
          <Label className="text-xs text-muted-foreground mb-1">Hours</Label>
          <Input
            type="number"
            min="0"
            max="23"
            value={hours}
            onChange={(e) => handleHoursChange(e.target.value)}
            placeholder="0"
            data-testid="input-interval-hours"
          />
        </div>
        <div>
          <Label className="text-xs text-muted-foreground mb-1">Minutes</Label>
          <Input
            type="number"
            min="0"
            max="59"
            value={minutes}
            onChange={(e) => handleMinutesChange(e.target.value)}
            placeholder="0"
            data-testid="input-interval-minutes"
          />
        </div>
        <div>
          <Label className="text-xs text-muted-foreground mb-1">Seconds</Label>
          <Input
            type="number"
            min="0"
            max="59"
            value={seconds}
            onChange={(e) => handleSecondsChange(e.target.value)}
            placeholder="0"
            data-testid="input-interval-seconds"
          />
        </div>
      </div>
      {value !== undefined && value > 0 && (
        <p className="text-xs text-muted-foreground">
          Total: {value.toLocaleString()} milliseconds
        </p>
      )}
    </div>
  );
}
