import { useState, useEffect, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card } from '@/components/ui/card';
import { Trash2, Plus } from 'lucide-react';

interface KeyValueMapEditorProps {
  value?: Record<string, string>;
  onChange: (value: Record<string, string>) => void;
  keyPlaceholder?: string;
  valuePlaceholder?: string;
}

type JsonPrimitive = string | number | boolean | null;
type JsonValue = JsonPrimitive | JsonValue[] | { [key: string]: JsonValue };
type JsonObject = Record<string, JsonValue>;

interface Entry {
  key: string;
  value: JsonValue;
}

const VALUE_TYPES = ['String', 'Number', 'Boolean', 'Null', 'Object', 'Array'] as const;
type ValueType = (typeof VALUE_TYPES)[number];

function typeOf(value: JsonValue): ValueType {
  if (value === null) return 'Null';
  if (Array.isArray(value)) return 'Array';
  if (typeof value === 'object') return 'Object';
  if (typeof value === 'number') return 'Number';
  if (typeof value === 'boolean') return 'Boolean';
  return 'String';
}

// Only treat a value as structured when it unambiguously looks like a JSON object or array
// (a leading brace/bracket and a successful parse). Plain strings pass through unchanged.
function tryParseStructured(value: string): JsonValue | null {
  const trimmed = value.trim();
  if (!trimmed.startsWith('{') && !trimmed.startsWith('[')) {
    return null;
  }
  try {
    const parsed: unknown = JSON.parse(value);
    if (parsed !== null && typeof parsed === 'object') {
      return parsed as JsonValue;
    }
  } catch {
    // Not valid JSON; keep the raw string
  }
  return null;
}

// Convert the backend Map<String, String> into the internal typed structure, parsing any
// string value that is itself a JSON object/array (e.g. Twitch's 'claims' parameter) so it
// can be edited as nested rows instead of hand-typed JSON.
function toInternal(value?: Record<string, string>): JsonObject {
  if (typeof value !== 'object' || value === null || Array.isArray(value)) {
    return {};
  }
  const out: JsonObject = {};
  for (const [key, raw] of Object.entries(value)) {
    const parsed = tryParseStructured(raw);
    out[key] = parsed !== null ? parsed : raw;
  }
  return out;
}

function emitValue(value: JsonValue): string {
  if (typeof value === 'string') {
    return value;
  }
  if (value === null) {
    return 'null';
  }
  return JSON.stringify(value);
}

function emitMap(internal: JsonObject): Record<string, string> {
  const out: Record<string, string> = {};
  for (const [key, value] of Object.entries(internal)) {
    if (key.trim() !== '') {
      out[key] = emitValue(value);
    }
  }
  return out;
}

function convertType(value: JsonValue, newType: ValueType): JsonValue {
  switch (newType) {
    case 'String':
      if (value === null) return '';
      return typeof value === 'string' ? value : JSON.stringify(value, null, 2);
    case 'Number':
      if (typeof value === 'number') return value;
      {
        const parsed = parseFloat(String(value));
        return isNaN(parsed) ? 0 : parsed;
      }
    case 'Boolean':
      return value === true || value === 'true' ? true : false;
    case 'Null':
      return null;
    case 'Object':
      return value !== null && typeof value === 'object' && !Array.isArray(value) ? value : {};
    case 'Array':
      return Array.isArray(value) ? value : [];
  }
}

interface ObjectRowsEditorProps {
  value: JsonObject;
  onChange: (value: JsonObject) => void;
  depth: number;
  addLabel: string;
  emptyHint: string;
  keyPlaceholder?: string;
  valuePlaceholder?: string;
}

// Recursive key/value editor over a JSON object. Each level keeps its own row list so a
// freshly added (empty) row isn't dropped from the rendered map while the user types into
// it. Empty-key rows are excluded from the emitted object, so the upstream value never
// reflects in-progress rows.
function ObjectRowsEditor({
  value,
  onChange,
  depth,
  addLabel,
  emptyHint,
  keyPlaceholder = 'Key',
  valuePlaceholder = 'Value',
}: ObjectRowsEditorProps) {
  const [rows, setRows] = useState<Entry[]>(() => Object.entries(value).map(([key, value]) => ({ key, value })));
  const lastEmitted = useRef<string>(JSON.stringify(value));

  useEffect(() => {
    const incoming = JSON.stringify(value);
    if (incoming !== lastEmitted.current) {
      setRows(Object.entries(value).map(([key, value]) => ({ key, value })));
      lastEmitted.current = incoming;
    }
  }, [value]);

  const emit = (nextRows: Entry[]) => {
    setRows(nextRows);
    const object: JsonObject = {};
    for (const { key, value } of nextRows) {
      if (key.trim() !== '') {
        object[key] = value;
      }
    }
    lastEmitted.current = JSON.stringify(object);
    onChange(object);
  };

  const handleAdd = () => {
    emit([...rows, { key: '', value: '' }]);
  };

  const handleRemove = (index: number) => {
    emit(rows.filter((_, i) => i !== index));
  };

  const handleKeyChange = (index: number, key: string) => {
    emit(rows.map((row, i) => (i === index ? { ...row, key } : row)));
  };

  const handleValueChange = (index: number, updatedValue: JsonValue) => {
    emit(rows.map((row, i) => (i === index ? { ...row, value: updatedValue } : row)));
  };

  const handleTypeChange = (index: number, newType: ValueType) => {
    emit(rows.map((row, i) => (i === index ? { ...row, value: convertType(row.value, newType) } : row)));
  };

  const content = (
    <div className="space-y-3">
      <div className="flex items-center justify-end">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={handleAdd}
          data-testid={depth === 0 ? 'button-add-extra-authorize-param' : 'button-add-nested-key'}
        >
          <Plus className="w-4 h-4 mr-1" />
          {addLabel}
        </Button>
      </div>

      {rows.length === 0 ? (
        <Card className="p-4 text-center text-muted-foreground text-sm">
          {emptyHint}
        </Card>
      ) : (
        <div className="space-y-2">
          {rows.map((entry, index) => {
            const type = typeOf(entry.value);
            const isObject = type === 'Object';
            return (
              <div
                key={index}
                className={depth > 0 ? 'ml-3 border-l-2 border-muted/50 pl-2' : ''}
              >
                <div className="flex items-start gap-2">
                  <Input
                    value={entry.key}
                    onChange={(e) => handleKeyChange(index, e.target.value)}
                    placeholder={keyPlaceholder}
                    className={`mt-0 ${depth > 0 ? 'w-32' : 'w-44'}`}
                    data-testid={`input-extra-authorize-param-key-${index}`}
                  />
                  <Select
                    value={type}
                    onValueChange={(t) => handleTypeChange(index, t as ValueType)}
                    data-testid={`select-extra-authorize-param-type-${index}`}
                  >
                    <SelectTrigger className="w-28">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {VALUE_TYPES.map((valueType) => (
                        <SelectItem key={valueType} value={valueType}>
                          {valueType}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  {!isObject && (
                    <div className="flex-1">
                      <ValueEditor
                        value={entry.value}
                        type={type}
                        onChange={(v) => handleValueChange(index, v)}
                        depth={depth + 1}
                        placeholder={valuePlaceholder}
                        testId={`input-extra-authorize-param-value-${index}`}
                      />
                    </div>
                  )}
                  {isObject && <div className="flex-1" />}
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    className="mt-0"
                    onClick={() => handleRemove(index)}
                    data-testid={`button-remove-extra-authorize-param-${index}`}
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
                {isObject && (
                  <div className="mt-2">
                    <ValueEditor
                      value={entry.value}
                      type={type}
                      onChange={(v) => handleValueChange(index, v)}
                      depth={depth + 1}
                      placeholder={valuePlaceholder}
                      testId={`input-extra-authorize-param-value-${index}`}
                    />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );

  if (depth > 0) {
    return content;
  }

  return (
    <div className="space-y-3 p-3 border rounded-lg">
      {content}
    </div>
  );
}

function ValueEditor({
  value,
  type,
  onChange,
  depth,
  placeholder,
  testId,
}: {
  value: JsonValue;
  type: ValueType;
  onChange: (value: JsonValue) => void;
  depth: number;
  placeholder?: string;
  testId: string;
}) {
  switch (type) {
    case 'String':
      return (
        <Textarea
          value={typeof value === 'string' ? value : ''}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          className="font-mono text-sm min-h-[60px] resize-y"
          data-testid={testId}
        />
      );
    case 'Number':
      return (
        <Input
          type="number"
          value={typeof value === 'number' ? value : ''}
          onChange={(e) => onChange(e.target.value === '' ? 0 : parseFloat(e.target.value))}
          className="mt-0"
          data-testid={testId}
        />
      );
    case 'Boolean':
      return (
        <div className="flex items-center h-9">
          <Checkbox
            checked={value === true}
            onCheckedChange={(checked) => onChange(checked === true)}
            data-testid={testId}
          />
        </div>
      );
    case 'Null':
      return (
        <div className="flex items-center h-9 text-xs text-muted-foreground italic font-mono">
          null
        </div>
      );
    case 'Array':
      return (
        <Textarea
          value={Array.isArray(value) ? JSON.stringify(value, null, 2) : '[]'}
          onChange={(e) => {
            try {
              const parsed: unknown = JSON.parse(e.target.value);
              if (Array.isArray(parsed)) {
                onChange(parsed);
              }
            } catch {
              // Invalid JSON mid-edit; keep the previous value
            }
          }}
          placeholder="Enter JSON array, e.g. [1, 2, 3]"
          className="font-mono text-sm min-h-[80px] resize-y"
          data-testid={testId}
        />
      );
    case 'Object':
      return (
        <ObjectRowsEditor
          value={value !== null && typeof value === 'object' && !Array.isArray(value) ? value : {}}
          onChange={onChange}
          depth={depth}
          addLabel="Add Key"
          emptyHint="No keys"
        />
      );
  }
}

export function KeyValueMapEditor({
  value,
  onChange,
  keyPlaceholder = 'Parameter name',
  valuePlaceholder = 'Parameter value',
}: KeyValueMapEditorProps) {
  return (
    <ObjectRowsEditor
      value={toInternal(value)}
      onChange={(object) => onChange(emitMap(object))}
      depth={0}
      addLabel="Add Param"
      emptyHint="No extra authorize params configured"
      keyPlaceholder={keyPlaceholder}
      valuePlaceholder={valuePlaceholder}
    />
  );
}