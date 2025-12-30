import { FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { TagsInput } from '@/components/TagsInput';
import { OAuth2HeaderParamEditor } from '@/components/OAuth2HeaderParamEditor';
import { type FieldSchema } from '@/lib/schema-parser';
import { type UseFormReturn } from 'react-hook-form';
import { Copy } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

function getFirstSentence(text: string | undefined): string | undefined {
  if (!text) return undefined;
  const match = text.match(/^[^.!?]+[.!?]/);
  return match ? match[0].trim() : text;
}

interface FormFieldGeneratorProps {
  field: FieldSchema;
  form: UseFormReturn<any>;
  mode: 'create' | 'update';
}

function OAuth2FieldWithUserIdExclusion({ 
  form, 
  fieldName, 
  value, 
  onChange 
}: { 
  form: UseFormReturn<any>; 
  fieldName: string; 
  value: any; 
  onChange: (value: any) => void;
}) {
  const otherFields = ['headers', 'params', 'body'].filter(f => f !== fieldName);
  
  const watchedValues = form.watch(otherFields);
  
  const hasUserIdInOtherFields = otherFields.some((fieldName, index) => {
    const fieldValues = watchedValues[index];
    return Array.isArray(fieldValues) && fieldValues.some((item: any) => item?.userId === true);
  });

  return (
    <OAuth2HeaderParamEditor
      value={Array.isArray(value) ? value : []}
      onChange={onChange}
      placeholder={`No ${fieldName.toLowerCase()} configured`}
      showUserIdCheckbox={true}
      hasUserIdElsewhere={hasUserIdInOtherFields}
    />
  );
}

export function FormFieldGenerator({ field, form, mode }: FormFieldGeneratorProps) {
  // Determine if field should be shown based on validation groups
  const shouldShow = () => {
    // Skip ID field in create mode (IDs are auto-generated)
    if (mode === 'create' && field.name.toLowerCase() === 'id') {
      return false;
    }
    
    if (!field.validationGroups) return true;
    
    const groups = field.validationGroups;
    if (mode === 'create') {
      // Don't show fields marked as @Null(groups = Insert.class)
      return groups.insert !== 'null' && groups.create !== 'null';
    } else {
      // Show all fields for update
      return true;
    }
  };
  
  // Determine if field is required based on validation groups and mode
  const isRequired = () => {
    // ID field is never required (it's immutable in update mode)
    if (field.name.toLowerCase() === 'id') {
      return false;
    }
    
    if (field.validationGroups) {
      const groups = field.validationGroups;
      if (mode === 'create') {
        return groups.insert === 'notNull' || groups.create === 'notNull' || field.required;
      } else {
        // In update mode, only show asterisk for fields explicitly required in update
        return groups.update === 'notNull';
      }
    }
    // In update mode, don't show asterisk for generally required fields
    return mode === 'create' && field.required;
  };
  
  if (!shouldShow()) {
    return null;
  }
  
  const required = isRequired();
  const label = formatLabel(field.name);
  const description = field.description;
  
  return (
    <FormField
      control={form.control}
      name={field.name}
      render={({ field: formField }) => (
        <FormItem data-testid={`form-field-${field.name}`}>
          <FormLabel>
            {label}
            {required && <span className="text-destructive ml-1">*</span>}
          </FormLabel>
          <FormControl>
            {renderInput(field, formField, form, mode)}
          </FormControl>
          {description && (
            <FormDescription className="text-xs">
              {description}
            </FormDescription>
          )}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

function renderInput(
  schema: FieldSchema,
  formField: any,
  form: UseFormReturn<any>,
  mode?: 'create' | 'update'
) {
  // Check if this is an ID field in update mode (should be read-only)
  const isIdFieldInUpdate = mode === 'update' && schema.name.toLowerCase() === 'id';
  // Handle enums with Select dropdown
  if (schema.enumValues) {
    return (
      <Select
        onValueChange={formField.onChange}
        value={formField.value || undefined}
        data-testid={`input-${schema.name}`}
      >
        <SelectTrigger>
          <SelectValue placeholder={`Select ${formatLabel(schema.name).toLowerCase()}`} />
        </SelectTrigger>
        <SelectContent>
          {schema.enumValues.map((enumValue) => (
            <SelectItem key={enumValue} value={enumValue}>
              {formatEnumValue(enumValue)}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    );
  }
  
  // Handle booleans with Checkbox
  if (schema.type === 'boolean') {
    return (
      <div className="flex items-center space-x-2">
        <Checkbox
          checked={formField.value}
          onCheckedChange={formField.onChange}
          data-testid={`input-${schema.name}`}
        />
      </div>
    );
  }
  
  // Handle arrays BEFORE objects (arrays can have object type)
  if (schema.isArray) {
    // Special handling for OAuth2 headers, params, and body
    if ((schema.name === 'headers' || schema.name === 'params' || schema.name === 'body') && schema.type === 'object') {
      return (
        <OAuth2FieldWithUserIdExclusion
          form={form}
          fieldName={schema.name}
          value={formField.value}
          onChange={formField.onChange}
        />
      );
    }

    // Special handling for validStatusCodes - array of integers
    if (schema.name === 'validStatusCodes' && schema.type === 'integer') {
      const currentValues: number[] = Array.isArray(formField.value) ? formField.value : [];
      return (
        <TagsInput
          value={currentValues.map(v => String(v))}
          onChange={(values) => {
            const nums = values.map(v => parseInt(v, 10)).filter(n => !isNaN(n));
            formField.onChange(nums);
          }}
          placeholder="Type status code and press Enter (e.g. 200, 201)"
          testId={`input-${schema.name}`}
        />
      );
    }
    
    // Use TagsInput for string arrays (especially 'tags' fields)
    if (schema.type === 'string' || schema.name.toLowerCase() === 'tags') {
      return (
        <TagsInput
          value={Array.isArray(formField.value) ? formField.value : []}
          onChange={formField.onChange}
          placeholder={`Type and press Space or Enter to add ${formatLabel(schema.name).toLowerCase()}`}
          testId={`input-${schema.name}`}
        />
      );
    }
    
    // Use JSON textarea for complex arrays
    // Make keys field taller for OIDC
    const textareaHeight = schema.name === 'keys' ? 'min-h-[160px]' : 'min-h-[80px]';
    
    return (
      <Textarea
        placeholder={`Enter JSON array for ${formatLabel(schema.name).toLowerCase()}`}
        className={`font-mono text-sm ${textareaHeight}`}
        name={formField.name}
        value={formField.value ?? ''}
        onChange={(e) => {
          formField.onChange(e.target.value);
        }}
        onBlur={formField.onBlur}
        ref={formField.ref}
        data-testid={`input-${schema.name}`}
      />
    );
  }
  
  // Handle objects/maps with JSON textarea (after array check)
  if (schema.type === 'object' || schema.isMap) {
    return (
      <Textarea
        placeholder={`Enter JSON for ${formatLabel(schema.name).toLowerCase()}`}
        className="font-mono text-sm min-h-[100px]"
        name={formField.name}
        value={formField.value ?? ''}
        onChange={(e) => {
          formField.onChange(e.target.value);
        }}
        onBlur={formField.onBlur}
        ref={formField.ref}
        data-testid={`input-${schema.name}`}
      />
    );
  }
  
  // Handle ABI field for Smart Contracts - use textarea with monospace font
  if (schema.name === 'abi') {
    return (
      <Textarea
        placeholder="Enter Smart Contract ABI (JSON format)"
        className="font-mono text-sm min-h-[200px]"
        name={formField.name}
        value={formField.value ?? ''}
        onChange={(e) => {
          formField.onChange(e.target.value);
        }}
        onBlur={formField.onBlur}
        ref={formField.ref}
        data-testid={`input-${schema.name}`}
      />
    );
  }
  
  // Handle password fields
  if (schema.name.toLowerCase().includes('password')) {
    return (
      <Input
        type="password"
        placeholder={getFirstSentence(schema.description) || `Enter ${formatLabel(schema.name).toLowerCase()}`}
        name={formField.name}
        value={formField.value ?? ''}
        onChange={formField.onChange}
        onBlur={formField.onBlur}
        ref={formField.ref}
        data-testid={`input-${schema.name}`}
      />
    );
  }
  
  // Handle email fields
  if (schema.name.toLowerCase().includes('email') || schema.pattern?.includes('EMAIL')) {
    return (
      <Input
        type="email"
        placeholder={getFirstSentence(schema.description) || `Enter ${formatLabel(schema.name).toLowerCase()}`}
        name={formField.name}
        value={formField.value ?? ''}
        onChange={formField.onChange}
        onBlur={formField.onBlur}
        ref={formField.ref}
        data-testid={`input-${schema.name}`}
      />
    );
  }
  
  // Handle number types
  if (schema.type === 'number') {
    return (
      <Input
        type="number"
        placeholder={getFirstSentence(schema.description) || `Enter ${formatLabel(schema.name).toLowerCase()}`}
        name={formField.name}
        value={formField.value ?? ''}
        onChange={(e) => {
          const value = e.target.value;
          formField.onChange(value === '' ? '' : Number(value));
        }}
        onBlur={formField.onBlur}
        ref={formField.ref}
        data-testid={`input-${schema.name}`}
      />
    );
  }
  
  // Special handling for publicKey field - add copy button
  if (schema.name === 'publicKey') {
    return <PublicKeyInput formField={formField} schema={schema} mode={mode} />;
  }
  
  // Default to text input for strings
  return (
    <Input
      type="text"
      placeholder={getFirstSentence(schema.description) || `Enter ${formatLabel(schema.name).toLowerCase()}`}
      name={formField.name}
      value={formField.value ?? ''}
      onChange={formField.onChange}
      onBlur={formField.onBlur}
      ref={formField.ref}
      data-testid={`input-${schema.name}`}
      readOnly={isIdFieldInUpdate}
      className={isIdFieldInUpdate ? 'bg-muted cursor-not-allowed' : ''}
    />
  );
}

function PublicKeyInput({ formField, schema, mode }: { formField: any; schema: FieldSchema; mode?: 'create' | 'update' }) {
  const { toast } = useToast();
  
  const handleCopy = async () => {
    if (formField.value) {
      try {
        await navigator.clipboard.writeText(formField.value);
        toast({
          title: 'Copied!',
          description: 'Public key copied to clipboard',
        });
      } catch (err) {
        toast({
          title: 'Failed to copy',
          description: 'Could not copy to clipboard',
          variant: 'destructive',
        });
      }
    }
  };
  
  return (
    <div className="flex gap-2">
      <Input
        type="text"
        placeholder={getFirstSentence(schema.description) || `Enter ${formatLabel(schema.name).toLowerCase()}`}
        name={formField.name}
        value={formField.value ?? ''}
        onChange={formField.onChange}
        onBlur={formField.onBlur}
        ref={formField.ref}
        data-testid={`input-${schema.name}`}
        className="flex-1"
      />
      {formField.value && (
        <Button
          type="button"
          variant="outline"
          size="icon"
          onClick={handleCopy}
          data-testid="button-copy-publickey"
        >
          <Copy className="w-4 h-4" />
        </Button>
      )}
    </div>
  );
}

function formatLabel(fieldName: string): string {
  const labelOverrides: Record<string, string> = {
    'responseValidMapping': 'Valid Response Mapping',
    'responseValidExpectedValue': 'Valid Response Expected Value',
  };
  
  if (labelOverrides[fieldName]) {
    return labelOverrides[fieldName];
  }
  
  // Convert camelCase to Title Case
  return fieldName
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (str) => str.toUpperCase())
    .trim();
}

function formatEnumValue(enumValue: string): string {
  // Convert UPPER_CASE to Title Case
  return enumValue
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
}
