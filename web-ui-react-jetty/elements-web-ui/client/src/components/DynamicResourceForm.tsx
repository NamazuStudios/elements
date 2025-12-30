import { useEffect, useState, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Form } from '@/components/ui/form';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Loader2, AlertCircle, RotateCcw } from 'lucide-react';
import { FormFieldGenerator } from './FormFieldGenerator';
import { MetadataEditor } from './MetadataEditor';
import { MetadataSpecPropertyEditor } from './MetadataSpecPropertyEditor';
import { MissionStepsEditor } from './MissionStepsEditor';
import { ProfileForm } from './ProfileForm';
import { ApplicationForm } from './ApplicationForm';
import { SmartContractForm } from './SmartContractForm';
import { DateTimePicker } from './DateTimePicker';
import { IntervalEditor } from './IntervalEditor';
import { FormField, FormItem, FormLabel, FormControl, FormMessage } from '@/components/ui/form';
import { getResourceSchema, type ModelSchema, type FieldSchema } from '@/lib/schema-parser';
import { useFormDraft } from '@/hooks/use-form-draft';
import { useToast } from '@/hooks/use-toast';

interface DynamicResourceFormProps {
  resourceName: string;
  mode: 'create' | 'update';
  initialData?: Record<string, any>;
  onSubmit: (data: Record<string, any>) => Promise<void>;
  onCancel: () => void;
  onFormChange?: (data: Record<string, any>) => void;
}

export function DynamicResourceForm({
  resourceName,
  mode,
  initialData,
  onSubmit,
  onCancel,
  onFormChange,
}: DynamicResourceFormProps) {
  const { toast } = useToast();
  const [schema, setSchema] = useState<ModelSchema | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [showDraftRestore, setShowDraftRestore] = useState(false);
  const [metadataValid, setMetadataValid] = useState(true);
  const [metadataValidationErrors, setMetadataValidationErrors] = useState<string[]>([]);

  // Draft management
  const { saveDraft, loadDraft, clearDraft, hasDraft } = useFormDraft({
    resourceName,
    mode,
    itemId: initialData?.id,
  });

  // Fetch schema on mount
  useEffect(() => {
    async function loadSchema() {
      setLoading(true);
      setError(null);
      
      try {
        let loadedSchema = await getResourceSchema(resourceName, mode);
        
        if (!loadedSchema) {
          setError(`Schema not found for ${resourceName}. Using JSON editor as fallback.`);
          setLoading(false);
          return;
        }
        
        // For Metadata resource, manually add accessLevel field to schema if not present
        if (resourceName === 'Metadata') {
          const hasAccessLevel = loadedSchema.fields.some(f => f.name === 'accessLevel');
          if (!hasAccessLevel) {
            const accessLevelField: FieldSchema = {
              name: 'accessLevel',
              type: 'enum',
              required: true,
              description: 'The minimum level of access required to view this metadata',
              enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'],
              isArray: false,
              isMap: false,
            };
            // Create a new schema object with the added field
            loadedSchema = {
              ...loadedSchema,
              fields: [...loadedSchema.fields, accessLevelField]
            };
          }
        }
        
        setSchema(loadedSchema);
        console.log(`Schema for ${resourceName}:`, loadedSchema);
      } catch (err) {
        console.error('Failed to load schema:', err);
        setError('Failed to load form schema. Using JSON editor as fallback.');
      } finally {
        setLoading(false);
      }
    }
    
    loadSchema();
  }, [resourceName, mode]);

  // Build Zod schema from field schemas
  const buildValidationSchema = (fields: FieldSchema[]) => {
    const shape: Record<string, z.ZodTypeAny> = {};
    
    for (const field of fields) {
      let fieldSchema: z.ZodTypeAny;
      
      // Skip ID field (auto-generated in create, immutable in update)
      if (field.name.toLowerCase() === 'id') {
        // In update mode, ID is read-only and doesn't need validation
        if (mode === 'update') {
          shape[field.name] = z.string().optional();
        }
        continue;
      }
      
      // Skip type field for Metadata Spec (defaults to OBJECT)
      if (isMetadataSpecResource && field.name === 'type') {
        continue;
      }
      
      // Determine if field should be shown based on validation groups
      if (field.validationGroups) {
        const groups = field.validationGroups;
        if (mode === 'create' && (groups.insert === 'null' || groups.create === 'null')) {
          continue; // Skip this field for create mode
        }
        if (mode === 'update' && groups.update === 'null') {
          continue; // Skip this field for update mode (must be null)
        }
      }
      
      // Special case: Metadata Spec properties field should accept array and be required
      if (isMetadataSpecResource && field.name === 'properties') {
        shape[field.name] = z.array(z.any()).min(1, 'At least one property is required');
        continue; // Skip the rest of the logic for this field
      }
      
      // Special case: Resources with metadata fields - metadataSpec should accept string ID
      if (hasMetadataFields && field.name === 'metadataSpec') {
        // Only required in create mode, optional in edit mode
        shape[field.name] = mode === 'create' 
          ? z.string().min(1, 'Metadata spec is required')
          : z.string().optional().or(z.literal(''));
        continue; // Skip the rest of the logic for this field
      }
      
      // Special case: Resources with metadata fields - metadata should accept object directly
      if (hasMetadataFields && field.name === 'metadata') {
        // Metadata is always optional (can be empty object)
        shape[field.name] = z.record(z.any()).optional();
        continue; // Skip the rest of the logic for this field
      }
      
      // Special case: Mission steps field should accept array of step objects
      if (isMissionWithSteps && field.name === 'steps') {
        shape[field.name] = z.array(z.any()).optional();
        continue; // Skip the rest of the logic for this field
      }
      
      // Special case: Metadata resource accessLevel field should accept enum
      if (isMetadataResource && field.name === 'accessLevel') {
        shape[field.name] = z.enum(['UNPRIVILEGED', 'USER', 'SUPERUSER']);
        continue; // Skip the rest of the logic for this field
      }
      
      // Special case: Metadata resource with spec/values - spec should accept string ID
      if (isMetadataWithSpecValues && field.name === 'spec') {
        shape[field.name] = mode === 'create' 
          ? z.string().min(1, 'Metadata spec is required')
          : z.string().optional().or(z.literal(''));
        continue; // Skip the rest of the logic for this field
      }
      
      // Special case: Metadata resource with spec/values - values should accept object
      if (isMetadataWithSpecValues && field.name === 'values') {
        shape[field.name] = z.record(z.any()).optional();
        continue; // Skip the rest of the logic for this field
      }
      
      // Special case: CustomAuthScheme publicKey field - optional but must be valid Base64 if provided
      if (resourceName === 'Custom' && field.name === 'publicKey') {
        shape[field.name] = z.string()
          .optional()
          .or(z.literal(''))
          .refine(
            (val) => {
              if (!val || val === '') return true; // Empty is allowed (optional field)
              // Base64 regex pattern
              const base64Regex = /^[A-Za-z0-9+/]*={0,2}$/;
              return base64Regex.test(val);
            },
            { message: 'Public key must be valid Base64 format (or leave empty to auto-generate)' }
          );
        continue; // Skip the rest of the logic for this field
      }
      
      // Build base schema based on type
      if (field.enumValues) {
        fieldSchema = z.enum(field.enumValues as [string, ...string[]]);
      } else if (field.type === 'string' && field.isArray) {
        // String arrays (like tags) - accept array directly from TagsInput
        fieldSchema = z.array(z.string());
      } else if (field.type === 'string') {
        let stringSchema = z.string();
        
        // Apply pattern validation if present
        if (field.pattern) {
          // Convert Java regex to JS regex (basic conversion)
          const jsPattern = convertJavaRegexToJS(field.pattern);
          if (jsPattern) {
            stringSchema = stringSchema.regex(
              new RegExp(jsPattern),
              `Invalid format for ${field.name}`
            );
          }
        }
        
        fieldSchema = stringSchema;
      } else if (field.type === 'number') {
        fieldSchema = z.number();
      } else if (field.type === 'boolean') {
        fieldSchema = z.boolean();
      } else if ((field.name === 'headers' || field.name === 'params' || field.name === 'body') && field.type === 'object' && field.isArray) {
        // OAuth2 headers, params, and body - accept array of objects directly
        fieldSchema = z.array(z.object({
          key: z.string(),
          value: z.string(),
          fromClient: z.boolean(),
          userId: z.boolean().optional(),
        })).optional();
      } else if (field.name === 'validStatusCodes' && field.type === 'integer' && field.isArray) {
        // Valid status codes - accept array of numbers directly
        fieldSchema = z.array(z.number()).optional();
      } else if (field.type === 'object' || field.isMap || field.isArray) {
        // For objects/arrays, accept string input (JSON) and parse it
        fieldSchema = z.string().transform((val, ctx) => {
          if (!val || val.trim() === '') {
            return field.isArray ? [] : {};
          }
          
          try {
            return JSON.parse(val);
          } catch {
            ctx.addIssue({
              code: z.ZodIssueCode.custom,
              message: 'Invalid JSON format',
            });
            return z.NEVER;
          }
        });
      } else {
        fieldSchema = z.any();
      }
      
      // Determine if field is required
      const isRequired = field.validationGroups
        ? (mode === 'create' && (field.validationGroups.insert === 'notNull' || field.validationGroups.create === 'notNull')) ||
          (mode === 'update' && field.validationGroups.update === 'notNull') ||
          field.required
        : field.required;
      
      // Make optional if not required
      if (!isRequired) {
        fieldSchema = fieldSchema.optional().or(z.literal(''));
      }
      
      shape[field.name] = fieldSchema;
    }
    
    return z.object(shape);
  };

  // Check if this is a Metadata Spec resource
  const isMetadataSpecResource = useMemo(() => resourceName === 'Metadata Spec', [resourceName]);
  
  // Check if this resource has metadata/metadataSpec fields (like Items, Metadata, etc.)
  const hasMetadataFields = useMemo(() => 
    schema?.fields.some(f => f.name === 'metadata') && 
    schema?.fields.some(f => f.name === 'metadataSpec'),
    [schema]
  );
  
  // Check if this is the dedicated Metadata resource (needs special handling)
  const isMetadataResource = useMemo(() => resourceName === 'Metadata', [resourceName]);
  
  // Check if this is the Metadata resource with spec/values fields
  const isMetadataWithSpecValues = useMemo(() => 
    resourceName === 'Metadata' && schema?.fields.some(f => f.name === 'spec') && schema?.fields.some(f => f.name === 'values'),
    [resourceName, schema]
  );
  
  // Check if this is a Mission resource with steps
  const isMissionWithSteps = useMemo(() => 
    resourceName === 'Missions' && schema?.fields.some(f => f.name === 'steps'),
    [resourceName, schema]
  );

  // Check if this is a Profile resource
  const isProfileResource = useMemo(() => resourceName === 'Profiles', [resourceName]);

  // Check if this is an Application resource
  const isApplicationResource = useMemo(() => resourceName === 'Applications', [resourceName]);

  // Check if this is a Smart Contract resource
  const isSmartContractResource = useMemo(() => resourceName === 'Smart Contracts', [resourceName]);

  // Memoize default values based on schema and initial data
  const defaultValues = useMemo(() => {
    if (!schema) return {};

    if (initialData) {
      // For update or when switching from JSON editor, use provided data
      const values: Record<string, any> = {};
      
      for (const field of schema.fields) {
        const value = initialData[field.name];
        
        // For resources with metadata fields, keep metadata as object
        if (hasMetadataFields && field.name === 'metadata') {
          values[field.name] = value || {};
        } else if (hasMetadataFields && field.name === 'metadataSpec') {
          // Keep metadataSpec as is (could be object with id or just id string)
          values[field.name] = value || null;
        } else if (isMetadataWithSpecValues && field.name === 'values') {
          // For Metadata resource, keep values as object
          values[field.name] = value || {};
        } else if (isMetadataWithSpecValues && field.name === 'spec') {
          // Keep spec as is (could be object with id or just id string)
          values[field.name] = value || null;
        } else if (isMetadataResource && field.name === 'accessLevel') {
          // Keep accessLevel as is (enum value)
          values[field.name] = value || 'USER';
        } else if (isMetadataSpecResource && field.name === 'properties') {
          // For Metadata Spec, keep properties as array
          values[field.name] = Array.isArray(value) ? value : [];
        } else if (isMissionWithSteps && field.name === 'steps') {
          // For Mission steps, keep as array
          values[field.name] = Array.isArray(value) ? value : [];
        } else if (field.type === 'string' && field.isArray) {
          // String arrays (like tags) - keep as array
          values[field.name] = Array.isArray(value) ? value : [];
        } else if ((field.name === 'headers' || field.name === 'params' || field.name === 'body') && field.type === 'object' && field.isArray) {
          // OAuth2 headers, params, and body - keep as array of objects
          values[field.name] = Array.isArray(value) ? value : [];
        } else if (field.name === 'validStatusCodes' && field.type === 'integer' && field.isArray) {
          // Valid status codes - keep as array of numbers
          values[field.name] = Array.isArray(value) ? value : [];
        } else if (field.type === 'object' || field.isMap || field.isArray) {
          // For other resources, convert objects/arrays to JSON strings
          values[field.name] = value ? JSON.stringify(value, null, 2) : '';
        } else {
          values[field.name] = value ?? '';
        }
      }
      
      return values;
    }
    
    // For create, use empty defaults
    const values: Record<string, any> = {};
    
    for (const field of schema.fields) {
      // For resources with metadata fields, initialize metadata as empty object
      if (hasMetadataFields && field.name === 'metadata') {
        values[field.name] = {};
      } else if (hasMetadataFields && field.name === 'metadataSpec') {
        values[field.name] = null;
      } else if (isMetadataWithSpecValues && field.name === 'values') {
        // For Metadata resource, initialize values as empty object
        values[field.name] = {};
      } else if (isMetadataWithSpecValues && field.name === 'spec') {
        values[field.name] = null;
      } else if (isMetadataResource && field.name === 'accessLevel') {
        values[field.name] = 'USER'; // Default to USER access level
      } else if (isMetadataSpecResource && field.name === 'properties') {
        // For Metadata Spec, initialize properties as empty array
        values[field.name] = [];
      } else if (isMissionWithSteps && field.name === 'steps') {
        // For Mission steps, initialize as empty array
        values[field.name] = [];
      } else if (field.type === 'string' && field.isArray) {
        // String arrays (like tags) - initialize as empty array
        values[field.name] = [];
      } else if ((field.name === 'headers' || field.name === 'params' || field.name === 'body') && field.type === 'object' && field.isArray) {
        // OAuth2 headers, params, and body - initialize as empty array
        values[field.name] = [];
      } else if (field.name === 'validStatusCodes' && field.type === 'integer' && field.isArray) {
        // Valid status codes - initialize as empty array
        values[field.name] = [];
      } else if (field.type === 'boolean') {
        values[field.name] = false;
      } else if (field.type === 'object' || field.isMap) {
        values[field.name] = '';
      } else if (field.isArray) {
        values[field.name] = '';
      } else {
        values[field.name] = '';
      }
    }
    
    return values;
  }, [schema, initialData, mode, isMetadataResource, isMetadataSpecResource, hasMetadataFields, isMissionWithSteps, isMetadataWithSpecValues]);

  const validationSchema = schema ? buildValidationSchema(schema.fields) : z.object({});
  
  const form = useForm({
    resolver: zodResolver(validationSchema),
    defaultValues,
  });

  const cleanStepsArray = (steps: any[]): any[] => {
    if (!Array.isArray(steps)) return steps;
    
    return steps.map(step => {
      const cleanedStep: any = {};
      
      // Only include valid Step fields
      const validFields = ['displayName', 'description', 'count', 'metadata', 'rewards'];
      
      validFields.forEach(field => {
        if (step[field] !== undefined && step[field] !== null && step[field] !== '') {
          if (field === 'rewards' && Array.isArray(step[field])) {
            // Transform rewards from { itemId: "...", quantity: 1 } to { item: { id: "..." }, quantity: 1 }
            cleanedStep[field] = step[field].map((reward: any) => ({
              item: { id: reward.itemId },
              quantity: reward.quantity
            }));
          } else {
            cleanedStep[field] = step[field];
          }
        }
      });
      
      return cleanedStep;
    });
  };

  const cleanFormData = (data: Record<string, any>): Record<string, any> => {
    const cleanedData: Record<string, any> = {};
    
    for (const [key, value] of Object.entries(data)) {
      // Skip UI-only fields (e.g., confirmPassword)
      const field = schema?.fields.find(f => f.name === key);
      if (field?.uiOnly) {
        continue;
      }
      
      // Skip fields that must be null in update mode
      if (field?.validationGroups) {
        if (mode === 'update' && field.validationGroups.update === 'null') {
          continue;
        }
        if (mode === 'create' && (field.validationGroups.insert === 'null' || field.validationGroups.create === 'null')) {
          continue;
        }
      }
      
      // Skip id field in update mode (it's already in the URL path)
      if (key === 'id' && mode === 'update') {
        continue;
      } else if (isMissionWithSteps && key === 'steps' && Array.isArray(value)) {
        // For Mission steps, clean up the steps array
        cleanedData[key] = cleanStepsArray(value);
      } else if (hasMetadataFields && key === 'metadata') {
        // For resources with metadata fields, preserve metadata as-is
        cleanedData[key] = value;
      } else if (isMetadataWithSpecValues && key === 'values') {
        // For Metadata resource, preserve values as-is
        cleanedData[key] = value;
      } else if (key === 'spec' && isMetadataWithSpecValues) {
        // For Metadata resource, spec should be a string ID
        if (value) {
          cleanedData[key] = value;
        }
      } else if (key === 'metadataSpec') {
        // Extract the ID whether it's a string or object
        let specId = value;
        if (typeof value === 'object' && value !== null) {
          specId = value.id || value;
        }
        
        // For Metadata resource, convert metadataSpec to object with id field
        // For other resources (Items, etc.), keep as string ID
        if (isMetadataResource) {
          cleanedData[key] = { id: specId };
        } else if (specId) {
          cleanedData[key] = specId;
        }
      } else if (value !== '' && value !== null && value !== undefined) {
        cleanedData[key] = value;
      }
    }
    
    // For Metadata Spec, default type to OBJECT
    if (isMetadataSpecResource) {
      cleanedData.type = 'OBJECT';
    }
    
    return cleanedData;
  };

  // Reset form when default values change (schema loaded or initialData updated)
  useEffect(() => {
    if (schema) {
      form.reset(defaultValues);
    }
  }, [defaultValues, schema]);

  // Check for draft on mount
  useEffect(() => {
    if (schema && mode === 'create') {
      setShowDraftRestore(hasDraft());
    }
  }, [schema, mode, hasDraft]);

  // Notify parent when form values change
  useEffect(() => {
    if (!schema) return;
    
    let isFirstEmission = true;
    
    const subscription = form.watch((value) => {
      // Skip the first emission (which is the initial/default values)
      if (isFirstEmission) {
        isFirstEmission = false;
        return;
      }
      
      // Notify parent of form changes immediately (with cleaned data)
      if (onFormChange && value) {
        const cleaned = cleanFormData(value as Record<string, any>);
        onFormChange(cleaned);
      }
    });
    
    return () => {
      subscription.unsubscribe();
    };
  }, [form, schema, onFormChange]);

  const restoreDraft = () => {
    const draft = loadDraft();
    if (draft) {
      Object.keys(draft).forEach(key => {
        form.setValue(key, draft[key]);
      });
      setShowDraftRestore(false);
    }
  };

  const handleSubmit = async (data: Record<string, any>) => {
    console.log('DynamicResourceForm handleSubmit called with data:', data);
    console.log('DynamicResourceForm mode:', mode);
    
    // Validate password confirmation if confirmPassword field exists
    if (data.password && data.confirmPassword) {
      if (data.password !== data.confirmPassword) {
        form.setError('confirmPassword', {
          type: 'manual',
          message: 'Passwords do not match'
        });
        return;
      }
    }
    
    setSubmitting(true);
    
    try {
      const cleanedData = cleanFormData(data);
      
      console.log('DynamicResourceForm - Submitting cleaned data:', cleanedData);
      await onSubmit(cleanedData);
      
      // Clear draft on successful submission
      clearDraft();
    } catch (error) {
      console.error('Form submission error:', error);
      // Error handling is done by parent component
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center py-12">
          <Loader2 className="w-6 h-6 animate-spin text-muted-foreground" />
          <span className="ml-3 text-muted-foreground">Loading form schema...</span>
        </CardContent>
      </Card>
    );
  }

  if (error || !schema) {
    return (
      <Alert variant="destructive">
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>
          {error || 'Failed to load form schema'}
        </AlertDescription>
      </Alert>
    );
  }

  // Use custom ApplicationForm for Application resource
  if (isApplicationResource) {
    return (
      <ApplicationForm
        mode={mode}
        initialData={initialData}
        onSubmit={handleSubmit}
      />
    );
  }

  // Use custom SmartContractForm for Smart Contract resource
  if (isSmartContractResource) {
    return (
      <SmartContractForm
        mode={mode}
        initialData={initialData}
        onSubmit={handleSubmit}
        onCancel={onCancel}
        isPending={submitting}
      />
    );
  }

  // Use custom ProfileForm for Profile resource
  if (isProfileResource) {
    return (
      <ProfileForm
        mode={mode}
        initialData={initialData}
        onSubmit={handleSubmit}
        onCancel={onCancel}
        isPending={submitting}
        onFormChange={onFormChange}
      />
    );
  }

  const renderField = (field: FieldSchema) => {
    // Check conditional visibility
    if (field.conditionalVisibility) {
      const { dependsOn, showWhen } = field.conditionalVisibility;
      const dependentValue = form.watch(dependsOn);
      
      // Check if the dependent value matches the condition
      const shouldShow = Array.isArray(showWhen) 
        ? showWhen.includes(dependentValue)
        : dependentValue === showWhen;
      
      if (!shouldShow) {
        return null;
      }
    }
    
    // Hide fields that must be null in update mode
    if (field.validationGroups) {
      if (mode === 'update' && field.validationGroups.update === 'null') {
        return null;
      }
      if (mode === 'create' && (field.validationGroups.insert === 'null' || field.validationGroups.create === 'null')) {
        return null;
      }
    }
    
    // Hide type field for Metadata Spec (defaults to OBJECT)
    if (isMetadataSpecResource && field.name === 'type') {
      return null;
    }
    
    // Hide metadataSpec field for resources with metadata fields (handled by MetadataEditor)
    if (hasMetadataFields && field.name === 'metadataSpec') {
      return null;
    }
    
    // Hide spec field for Metadata resource (handled by MetadataEditor)
    if (isMetadataWithSpecValues && field.name === 'spec') {
      return null;
    }
    
    // For resources with metadata fields, use MetadataEditor for the metadata field
    if (hasMetadataFields && field.name === 'metadata') {
      return (
        <FormField
          key={field.name}
          control={form.control}
          name={field.name}
          render={({ field: formField }) => (
            <FormItem data-testid="form-field-metadata">
              <FormControl>
                <MetadataEditor
                  value={formField.value}
                  specId={form.watch('metadataSpec')?.id || form.watch('metadataSpec')}
                  onChange={(metadata, specId) => {
                    formField.onChange(metadata);
                    // Update metadataSpec field with just the spec ID string
                    form.setValue('metadataSpec', specId);
                  }}
                  mode={mode}
                  onValidationChange={(isValid, errors) => {
                    setMetadataValid(isValid);
                    setMetadataValidationErrors(errors);
                  }}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }
    
    // For Metadata resource, use MetadataEditor for the values field
    if (isMetadataWithSpecValues && field.name === 'values') {
      return (
        <FormField
          key={field.name}
          control={form.control}
          name={field.name}
          render={({ field: formField }) => (
            <FormItem data-testid="form-field-values">
              <FormControl>
                <MetadataEditor
                  value={formField.value}
                  specId={form.watch('spec')?.id || form.watch('spec')}
                  onChange={(metadata, specId) => {
                    formField.onChange(metadata);
                    // Update spec field with just the spec ID string
                    form.setValue('spec', specId);
                  }}
                  mode={mode}
                  onValidationChange={(isValid, errors) => {
                    setMetadataValid(isValid);
                    setMetadataValidationErrors(errors);
                  }}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }
    
    // For Metadata Spec resource, use MetadataSpecPropertyEditor for the properties field
    if (isMetadataSpecResource && field.name === 'properties') {
      return (
        <FormField
          key={field.name}
          control={form.control}
          name={field.name}
          render={({ field: formField }) => (
            <FormItem data-testid="form-field-properties">
              <FormLabel>
                Properties
                <span className="text-destructive ml-1">*</span>
              </FormLabel>
              <FormControl>
                <MetadataSpecPropertyEditor
                  value={formField.value}
                  onChange={(properties) => {
                    formField.onChange(properties);
                  }}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }
    
    // For Mission resource, use MissionStepsEditor for the steps field
    if (isMissionWithSteps && field.name === 'steps') {
      return (
        <FormField
          key={field.name}
          control={form.control}
          name={field.name}
          render={({ field: formField }) => (
            <FormItem data-testid="form-field-steps">
              <FormControl>
                <MissionStepsEditor
                  value={formField.value}
                  onChange={(steps) => {
                    formField.onChange(steps);
                  }}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }
    
    // For Leaderboard resource, use DateTimePicker for firstEpochTimestamp
    if (resourceName === 'Leaderboards' && field.name === 'firstEpochTimestamp') {
      return (
        <FormField
          key={field.name}
          control={form.control}
          name={field.name}
          render={({ field: formField }) => (
            <FormItem data-testid="form-field-firstEpochTimestamp">
              <FormLabel>
                First Epoch Timestamp
                {field.required && <span className="text-destructive ml-1">*</span>}
              </FormLabel>
              <FormControl>
                <DateTimePicker
                  value={formField.value}
                  onChange={(ms) => {
                    formField.onChange(ms);
                  }}
                />
              </FormControl>
              {field.description && (
                <p className="text-sm text-muted-foreground">{field.description}</p>
              )}
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }
    
    // For Leaderboard resource, use IntervalEditor for epochInterval
    if (resourceName === 'Leaderboards' && field.name === 'epochInterval') {
      return (
        <FormField
          key={field.name}
          control={form.control}
          name={field.name}
          render={({ field: formField }) => (
            <FormItem data-testid="form-field-epochInterval">
              <FormLabel>
                Epoch Interval
                {field.required && <span className="text-destructive ml-1">*</span>}
              </FormLabel>
              <FormControl>
                <IntervalEditor
                  value={formField.value}
                  onChange={(ms) => {
                    formField.onChange(ms);
                  }}
                />
              </FormControl>
              {field.description && (
                <p className="text-sm text-muted-foreground">{field.description}</p>
              )}
              <FormMessage />
            </FormItem>
          )}
        />
      );
    }
    
    // Use default field generator for all other fields
    return (
      <FormFieldGenerator
        key={field.name}
        field={field}
        form={form}
        mode={mode}
      />
    );
  };

  return (
    <Card className="flex flex-col">
      <CardHeader className="flex-shrink-0">
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1">
            <CardTitle>{mode === 'create' ? 'Create' : 'Update'} {resourceName}</CardTitle>
            <CardDescription>
              {mode === 'create' 
                ? `Fill in the form below to create a new ${resourceName.toLowerCase().replace(/s$/, '')}`
                : `Update the fields below to modify this auth scheme`
              }
            </CardDescription>
          </div>
          {showDraftRestore && (
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={restoreDraft}
              className="flex-shrink-0"
              data-testid="button-restore-draft"
            >
              <RotateCcw className="w-4 h-4 mr-2" />
              Restore Draft
            </Button>
          )}
        </div>
      </CardHeader>
      <CardContent className="flex-1 overflow-y-auto">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(handleSubmit)} className="flex flex-col h-full">
            <div className="space-y-4 pb-4">
              {(() => {
                // For resources with metadata fields, ensure metadataSpec field appears before metadata field
                if (hasMetadataFields) {
                  const orderedFields: FieldSchema[] = [];
                  const metadataSpecField = schema.fields.find(f => f.name === 'metadataSpec');
                  const metadataField = schema.fields.find(f => f.name === 'metadata');
                  
                  // Add all other fields first
                  schema.fields.forEach(field => {
                    if (field.name !== 'metadata' && field.name !== 'metadataSpec') {
                      orderedFields.push(field);
                    }
                  });
                  
                  // Add metadataSpec then metadata at the end
                  if (metadataSpecField) orderedFields.push(metadataSpecField);
                  if (metadataField) orderedFields.push(metadataField);
                  
                  return orderedFields.map((field) => renderField(field));
                }
                
                // For Metadata resource with spec/values, ensure spec field appears before values field
                if (isMetadataWithSpecValues) {
                  const orderedFields: FieldSchema[] = [];
                  const specField = schema.fields.find(f => f.name === 'spec');
                  const valuesField = schema.fields.find(f => f.name === 'values');
                  
                  // Add all other fields first
                  schema.fields.forEach(field => {
                    if (field.name !== 'spec' && field.name !== 'values') {
                      orderedFields.push(field);
                    }
                  });
                  
                  // Add spec then values at the end
                  if (specField) orderedFields.push(specField);
                  if (valuesField) orderedFields.push(valuesField);
                  
                  return orderedFields.map((field) => renderField(field));
                }
                
                // Default: render fields in schema order
                return schema.fields.map((field) => renderField(field));
              })()}
            </div>
            {!metadataValid && metadataValidationErrors.length > 0 && (
              <Alert variant="destructive" className="mt-4">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  <div className="font-medium mb-1">Please fix the following validation errors:</div>
                  <ul className="list-disc list-inside space-y-1">
                    {metadataValidationErrors.map((error, index) => (
                      <li key={index} className="text-sm">{error}</li>
                    ))}
                  </ul>
                </AlertDescription>
              </Alert>
            )}
          </form>
        </Form>
      </CardContent>
      <div className="flex justify-end gap-3 p-6 pt-4 border-t flex-shrink-0">
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={submitting}
          data-testid="button-cancel"
        >
          Cancel
        </Button>
        {mode === 'create' && (
          <Button
            type="button"
            variant="secondary"
            onClick={() => {
              const currentValues = form.getValues();
              saveDraft(currentValues);
              toast({
                title: 'Draft Saved',
                description: 'Your changes have been saved as a draft.',
              });
              onCancel(); // Close the form after saving
            }}
            disabled={submitting}
            data-testid="button-save-draft"
          >
            Save Draft
          </Button>
        )}
        <Button
          type="button"
          disabled={submitting || !metadataValid}
          data-testid="button-submit"
          onClick={form.handleSubmit(handleSubmit)}
        >
          {submitting && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
          {mode === 'create' ? 'Create' : 'Update'}
        </Button>
      </div>
    </Card>
  );
}

/**
 * Convert Java regex pattern to JavaScript regex
 * This is a basic converter for common patterns
 */
function convertJavaRegexToJS(javaPattern: string): string | null {
  // Remove Constants.Regexp. prefix if present
  if (javaPattern.startsWith('Constants.Regexp.')) {
    // These are references to constants, we'd need to fetch the actual regex
    // For now, return null to skip validation
    return null;
  }
  
  // Basic conversion (most Java regex is compatible with JS)
  return javaPattern;
}
