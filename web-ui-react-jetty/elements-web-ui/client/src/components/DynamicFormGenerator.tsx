import { useForm } from 'react-hook-form';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Badge } from '@/components/ui/badge';
import { getSchemaProperties, getRequiredFields, OpenAPIParameter } from '@/lib/openapi-analyzer';
import { Loader2 } from 'lucide-react';

interface DynamicFormGeneratorProps {
  spec: any;
  schema: any;
  onSubmit: (data: any) => Promise<void>;
  onCancel: () => void;
  initialData?: any;
  isLoading?: boolean;
  submitLabel?: string;
  pathParams?: string[];
  queryParams?: OpenAPIParameter[];
}

export function DynamicFormGenerator({
  spec,
  schema,
  onSubmit,
  onCancel,
  initialData = {},
  isLoading = false,
  submitLabel = 'Submit',
  pathParams = [],
  queryParams = [],
}: DynamicFormGeneratorProps) {
  const properties = getSchemaProperties(spec, schema);
  const requiredFields = getRequiredFields(spec, schema);

  // Create default values for path parameters, query parameters, and body fields
  const defaultValues: Record<string, any> = {};
  
  // Add path parameters with prefix
  pathParams.forEach(param => {
    defaultValues[`__path_${param}`] = initialData[param] || '';
  });
  
  // Add query parameters with prefix
  queryParams.forEach(param => {
    const defaultValue = param.schema?.default ?? (param.schema?.type === 'boolean' ? false : '');
    defaultValues[`__query_${param.name}`] = initialData[param.name] ?? defaultValue;
  });
  
  // Add body fields (no exclusions - all body fields are independent)
  Object.keys(properties).forEach(key => {
    const prop = properties[key];
    const initialValue = initialData[key];
    
    if (initialValue !== undefined) {
      defaultValues[`__body_${key}`] = initialValue;
    } else if (prop.default !== undefined) {
      defaultValues[`__body_${key}`] = prop.default;
    } else if (prop.type === 'boolean') {
      defaultValues[`__body_${key}`] = false;
    } else if (prop.type === 'array') {
      defaultValues[`__body_${key}`] = [];
    } else {
      defaultValues[`__body_${key}`] = '';
    }
  });

  const form = useForm({
    defaultValues,
  });

  const handleSubmit = async (data: any) => {
    // Separate path params, query params, and body fields
    const result: any = {
      pathParams: {} as Record<string, string>,
      queryParams: {} as Record<string, string>,
      body: {} as Record<string, any>,
    };
    
    Object.keys(data).forEach(key => {
      const value = data[key];
      
      // Extract path parameters
      if (key.startsWith('__path_')) {
        const paramName = key.replace('__path_', '');
        result.pathParams[paramName] = value;
      }
      // Extract query parameters
      else if (key.startsWith('__query_')) {
        const paramName = key.replace('__query_', '');
        // Only include non-empty query params
        if (value !== '' && value !== null && value !== undefined) {
          result.queryParams[paramName] = value;
        }
      }
      // Extract body fields
      else if (key.startsWith('__body_')) {
        const fieldName = key.replace('__body_', '');
        const isRequired = requiredFields.includes(fieldName);
        
        // Include if required, or if not empty
        if (isRequired || (value !== '' && value !== null && value !== undefined)) {
          result.body[fieldName] = value;
        }
      }
    });
    
    await onSubmit(result);
  };

  const renderField = (fieldName: string, fieldSchema: any, isRequired?: boolean, label?: string) => {
    // Allow overriding isRequired and label for flexibility
    const required = isRequired !== undefined ? isRequired : requiredFields.includes(fieldName.replace('__body_', ''));
    const displayLabel = label || fieldName.replace('__body_', '').charAt(0).toUpperCase() + fieldName.replace('__body_', '').slice(1).replace(/([A-Z])/g, ' $1');

    // Handle enum (select dropdown)
    if (fieldSchema.enum) {
      return (
        <FormField
          key={fieldName}
          control={form.control}
          name={fieldName}
          rules={{ required: required ? `${displayLabel} is required` : false }}
          render={({ field }) => (
            <FormItem>
              <FormLabel>
                {displayLabel}
                {required && <span className="text-destructive ml-1">*</span>}
              </FormLabel>
              <Select onValueChange={field.onChange} value={field.value}>
                <FormControl>
                  <SelectTrigger data-testid={`select-${fieldName}`}>
                    <SelectValue placeholder={`Select ${displayLabel.toLowerCase()}`} />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  {fieldSchema.enum.map((option: string) => (
                    <SelectItem key={option} value={option}>
                      {option}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {fieldSchema.description && (
                <FormDescription>{fieldSchema.description}</FormDescription>
              )}
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }

    // Handle boolean (checkbox)
    if (fieldSchema.type === 'boolean') {
      return (
        <FormField
          key={fieldName}
          control={form.control}
          name={fieldName}
          render={({ field }) => (
            <FormItem className="flex flex-row items-start space-x-3 space-y-0 rounded-md border p-4">
              <FormControl>
                <Checkbox
                  checked={field.value}
                  onCheckedChange={field.onChange}
                  data-testid={`checkbox-${fieldName}`}
                />
              </FormControl>
              <div className="space-y-1 leading-none">
                <FormLabel>{displayLabel}</FormLabel>
                {fieldSchema.description && (
                  <FormDescription>{fieldSchema.description}</FormDescription>
                )}
              </div>
            </FormItem>
          )}
        />
      );
    }

    // Handle number/integer
    if (fieldSchema.type === 'number' || fieldSchema.type === 'integer') {
      return (
        <FormField
          key={fieldName}
          control={form.control}
          name={fieldName}
          rules={{
            required: required ? `${displayLabel} is required` : false,
            validate: (value) => {
              if (!value && !required) return true;
              const num = Number(value);
              if (isNaN(num)) return 'Must be a valid number';
              if (fieldSchema.minimum !== undefined && num < fieldSchema.minimum) {
                return `Must be at least ${fieldSchema.minimum}`;
              }
              if (fieldSchema.maximum !== undefined && num > fieldSchema.maximum) {
                return `Must be at most ${fieldSchema.maximum}`;
              }
              return true;
            },
          }}
          render={({ field }) => (
            <FormItem>
              <FormLabel>
                {displayLabel}
                {required && <span className="text-destructive ml-1">*</span>}
              </FormLabel>
              <FormControl>
                <Input
                  type="number"
                  placeholder={fieldSchema.example || `Enter ${displayLabel.toLowerCase()}`}
                  {...field}
                  data-testid={`input-${fieldName}`}
                />
              </FormControl>
              {fieldSchema.description && (
                <FormDescription>{fieldSchema.description}</FormDescription>
              )}
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }

    // Handle long text (textarea)
    if (fieldSchema.maxLength && fieldSchema.maxLength > 200) {
      return (
        <FormField
          key={fieldName}
          control={form.control}
          name={fieldName}
          rules={{ required: required ? `${displayLabel} is required` : false }}
          render={({ field }) => (
            <FormItem>
              <FormLabel>
                {displayLabel}
                {required && <span className="text-destructive ml-1">*</span>}
              </FormLabel>
              <FormControl>
                <Textarea
                  placeholder={fieldSchema.example || `Enter ${displayLabel.toLowerCase()}`}
                  {...field}
                  data-testid={`textarea-${fieldName}`}
                />
              </FormControl>
              {fieldSchema.description && (
                <FormDescription>{fieldSchema.description}</FormDescription>
              )}
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }

    // Default: string input
    return (
      <FormField
        key={fieldName}
        control={form.control}
        name={fieldName}
        rules={{ required: required ? `${displayLabel} is required` : false }}
        render={({ field }) => (
          <FormItem>
            <FormLabel>
              {displayLabel}
              {required && <span className="text-destructive ml-1">*</span>}
            </FormLabel>
            <FormControl>
              <Input
                placeholder={fieldSchema.example || `Enter ${displayLabel.toLowerCase()}`}
                {...field}
                data-testid={`input-${fieldName}`}
              />
            </FormControl>
            {fieldSchema.description && (
              <FormDescription>{fieldSchema.description}</FormDescription>
            )}
            <FormMessage />
          </FormItem>
        )}
      />
    );
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">
        {/* Path Parameters Section */}
        {pathParams.length > 0 && (
          <div className="space-y-4 pb-4 border-b">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-semibold">Path Parameters</h3>
              <Badge variant="secondary" className="text-xs">Required for URL</Badge>
            </div>
            {pathParams.map(param => (
              <FormField
                key={`path-${param}`}
                control={form.control}
                name={`__path_${param}`}
                rules={{ required: `${param} is required` }}
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      {param}
                      <span className="text-destructive ml-1">*</span>
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder={`Enter ${param}`}
                        {...field}
                        data-testid={`input-path-param-${param}`}
                        className="font-mono"
                      />
                    </FormControl>
                    <FormDescription className="text-xs">
                      This value will be used in the request URL path
                    </FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />
            ))}
          </div>
        )}

        {/* Query Parameters Section */}
        {queryParams.length > 0 && (
          <div className="space-y-4 pb-4 border-b">
            <div className="flex items-center gap-2">
              <h3 className="text-sm font-semibold">Query Parameters</h3>
              <Badge variant="outline" className="text-xs">Optional URL params</Badge>
            </div>
            {queryParams.map(param => {
              const paramRequired = param.required || false;
              const paramSchema = param.schema;
              
              return (
                <FormField
                  key={`query-${param.name}`}
                  control={form.control}
                  name={`__query_${param.name}`}
                  rules={{ required: paramRequired ? `${param.name} is required` : false }}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>
                        {param.name}
                        {paramRequired && <span className="text-destructive ml-1">*</span>}
                      </FormLabel>
                      <FormControl>
                        {paramSchema?.enum ? (
                          <Select onValueChange={field.onChange} value={field.value}>
                            <SelectTrigger data-testid={`select-query-${param.name}`}>
                              <SelectValue placeholder={`Select ${param.name}`} />
                            </SelectTrigger>
                            <SelectContent>
                              {paramSchema.enum.map((option: string) => (
                                <SelectItem key={option} value={option}>
                                  {option}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        ) : paramSchema?.type === 'boolean' ? (
                          <Checkbox
                            checked={field.value}
                            onCheckedChange={field.onChange}
                            data-testid={`checkbox-query-${param.name}`}
                          />
                        ) : (
                          <Input
                            type={paramSchema?.type === 'number' || paramSchema?.type === 'integer' ? 'number' : 'text'}
                            placeholder={param.description || `Enter ${param.name}`}
                            {...field}
                            data-testid={`input-query-param-${param.name}`}
                          />
                        )}
                      </FormControl>
                      {param.description && (
                        <FormDescription className="text-xs">
                          {param.description}
                        </FormDescription>
                      )}
                      <FormMessage />
                    </FormItem>
                  )}
                />
              );
            })}
          </div>
        )}
        
        {/* Request Body Fields */}
        {Object.keys(properties).length > 0 && (
          <>
            {(pathParams.length > 0 || queryParams.length > 0) && (
              <div className="flex items-center gap-2 pt-2">
                <h3 className="text-sm font-semibold">Request Body</h3>
              </div>
            )}
            {Object.entries(properties).map(([fieldName, fieldSchema]) => {
              // Render field with __body_ prefix
              const prefixedName = `__body_${fieldName}`;
              const isRequired = requiredFields.includes(fieldName);
              const label = fieldName.charAt(0).toUpperCase() + fieldName.slice(1).replace(/([A-Z])/g, ' $1');
              
              return renderField(prefixedName, fieldSchema, isRequired, label);
            })}
          </>
        )}
        
        <div className="flex items-center gap-2 pt-4">
          <Button
            type="submit"
            disabled={isLoading}
            data-testid="button-submit-form"
          >
            {isLoading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            {submitLabel}
          </Button>
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            disabled={isLoading}
            data-testid="button-cancel-form"
          >
            Cancel
          </Button>
        </div>
      </form>
    </Form>
  );
}
