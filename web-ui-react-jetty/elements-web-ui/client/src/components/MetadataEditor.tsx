import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiClient } from '@/lib/api-client';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Textarea } from '@/components/ui/textarea';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { TagsInput } from '@/components/TagsInput';
import { ResourceSearchDialog } from '@/components/ResourceSearchDialog';
import { AlertCircle, XCircle, Search, Plus, X } from 'lucide-react';

interface MetadataSpecProperty {
  name: string;
  displayName: string;
  type: 'STRING' | 'NUMBER' | 'BOOLEAN' | 'ARRAY' | 'ENUM' | 'OBJECT' | 'TAGS';
  required: boolean;
  placeholder?: string;
  defaultValue?: any;
  properties?: MetadataSpecProperty[];
}

interface MetadataSpec {
  id: string;
  name: string;
  type: string;
  properties: MetadataSpecProperty[];
}

interface MetadataEditorProps {
  value?: Record<string, any>;
  specId?: string;
  initialSpec?: any;
  onChange: (metadata: Record<string, any>, specId: string, spec?: any) => void;
  mode: 'create' | 'update';
  onValidationChange?: (isValid: boolean, errors: string[]) => void;
}

export function MetadataEditor({ value, specId, initialSpec, onChange, mode, onValidationChange }: MetadataEditorProps) {
  const [selectedSpecId, setSelectedSpecId] = useState<string>(() => {
    if (specId) {
      if (typeof specId === 'object' && specId !== null && 'id' in specId) {
        return (specId as { id: string }).id || '';
      }
      return specId;
    }
    return '';
  });
  const [metadataValues, setMetadataValues] = useState<Record<string, any>>(value || {});
  const [validationErrors, setValidationErrors] = useState<Record<string, string>>({});
  const [specSearchOpen, setSpecSearchOpen] = useState(false);
  const [selectedSpecName, setSelectedSpecName] = useState<string>(initialSpec?.name || '');

  // Fetch available metadata specs using proxy
  const { data: specsResponse, isLoading: specsLoading } = useQuery<{ objects: MetadataSpec[] }>({
    queryKey: ['/api/rest/metadata_spec'],
    queryFn: async () => apiClient.request<{ objects: MetadataSpec[] }>('/api/rest/metadata_spec'),
    enabled: true,
  });

  // Extract specs from paginated response
  const specs: MetadataSpec[] = specsResponse?.objects || [];

  // Fetch selected spec details using proxy
  const { data: selectedSpec, isLoading: specLoading } = useQuery<MetadataSpec>({
    queryKey: ['/api/rest/metadata_spec', selectedSpecId],
    queryFn: async () => apiClient.request<MetadataSpec>(`/api/rest/metadata_spec/${selectedSpecId}`),
    enabled: !!selectedSpecId && selectedSpecId !== 'none',
  });

  // Update spec name when spec is loaded
  useEffect(() => {
    if (selectedSpec) {
      setSelectedSpecName(selectedSpec.name);
    }
  }, [selectedSpec]);

  // Initialize from existing specId prop
  useEffect(() => {
    if (specId) {
      const extractedId = typeof specId === 'object' && specId !== null && 'id' in specId ? (specId as { id: string }).id : specId;
      if (extractedId && typeof extractedId === 'string' && extractedId !== selectedSpecId) {
        setSelectedSpecId(extractedId);
      }
    }
  }, [specId]);

  // When specId is missing but initialSpec has a name, resolve by name from available specs
  useEffect(() => {
    if (!selectedSpecId && initialSpec?.name && specs.length > 0) {
      const match = specs.find(s => s.name === initialSpec.name);
      if (match) {
        setSelectedSpecId(match.id);
        setSelectedSpecName(match.name);
      }
    }
  }, [selectedSpecId, initialSpec, specs]);

  // Initialize metadata values from prop (only on mount or when value substantially changes)
  useEffect(() => {
    if (value && Object.keys(value).length > 0) {
      // Only update if the value is actually different (deep comparison check)
      const currentValuesStr = JSON.stringify(metadataValues);
      const newValuesStr = JSON.stringify(value);
      if (currentValuesStr !== newValuesStr) {
        setMetadataValues(value);
      }
    }
  }, [value]);

  // Initialize metadata values with defaults when spec is selected or changes
  useEffect(() => {
    if (selectedSpec && selectedSpec.properties) {
      setMetadataValues((prevValues) => {
        const newValues: Record<string, any> = { ...prevValues };
        
        const applyDefaults = (properties: MetadataSpecProperty[], target: Record<string, any>) => {
          properties.forEach((prop) => {
            // Only set default value if the field is currently empty/undefined
            const isEmpty = target[prop.name] === undefined || target[prop.name] === null || target[prop.name] === '';
            
            if (isEmpty && prop.defaultValue !== null && prop.defaultValue !== undefined) {
              target[prop.name] = prop.defaultValue;
            }
            
            // Handle nested object properties
            if (prop.type === 'OBJECT' && prop.properties && prop.properties.length > 0) {
              if (!target[prop.name] || typeof target[prop.name] !== 'object') {
                target[prop.name] = {};
              }
              applyDefaults(prop.properties, target[prop.name]);
            }
          });
        };
        
        applyDefaults(selectedSpec.properties, newValues);
        return newValues;
      });
    }
  }, [selectedSpec]);

  // Validate metadata values against spec
  const validateMetadata = (spec: MetadataSpec | undefined, values: Record<string, any>): { isValid: boolean; errors: Record<string, string>; errorMessages: string[] } => {
    const errors: Record<string, string> = {};
    const errorMessages: string[] = [];
    
    if (!spec || !spec.properties) {
      return { isValid: true, errors: {}, errorMessages: [] };
    }
    
    const validateProperty = (prop: MetadataSpecProperty, value: any, path: string = '') => {
      const fieldPath = path ? `${path}.${prop.name}` : prop.name;
      const fieldLabel = path ? `${path} > ${prop.displayName}` : prop.displayName;
      
      if (prop.required) {
        // Check if value is missing or empty
        const isEmpty = value === undefined || value === null || value === '';
        // For NUMBER type, also check for NaN
        const isInvalidNumber = prop.type === 'NUMBER' && (typeof value === 'number' && isNaN(value));
        
        if (isEmpty || isInvalidNumber) {
          errors[fieldPath] = `${fieldLabel} is required`;
          errorMessages.push(`${fieldLabel} is required`);
        }
      }
      
      // Validate nested objects
      if (prop.type === 'OBJECT' && prop.properties && prop.properties.length > 0) {
        const nestedValue = value || {};
        prop.properties.forEach((nestedProp) => {
          validateProperty(nestedProp, nestedValue[nestedProp.name], fieldPath);
        });
      }
    };
    
    spec.properties.forEach((prop) => {
      validateProperty(prop, values[prop.name]);
    });
    
    return { isValid: Object.keys(errors).length === 0, errors, errorMessages };
  };

  // Validate metadata whenever values or spec changes
  useEffect(() => {
    if (selectedSpecId && selectedSpecId !== 'none') {
      const validation = validateMetadata(selectedSpec, metadataValues);
      setValidationErrors(validation.errors);
      
      // Notify parent of validation state
      if (onValidationChange) {
        onValidationChange(validation.isValid, validation.errorMessages);
      }
    } else {
      // No spec selected - skip validation
      if (onValidationChange) {
        onValidationChange(true, []);
      }
    }
  }, [metadataValues, selectedSpecId, selectedSpec, onValidationChange]);
  
  // Notify parent of metadata changes (separate from validation to avoid loops)
  useEffect(() => {
    // Always notify parent of metadata changes, even without a spec
    onChange(metadataValues, selectedSpecId || '', selectedSpec || undefined);
  }, [metadataValues, selectedSpecId, selectedSpec]);

  const handleSpecChange = (newSpecId: string) => {
    if (newSpecId === 'none') {
      setSelectedSpecId('none');
      setMetadataValues({});
      onChange({}, '');
    } else {
      setSelectedSpecId(newSpecId);
      setMetadataValues({});
    }
  };

  const handleValueChange = (propertyName: string, newValue: any, parentPath: string = '') => {
    if (parentPath) {
      // Handle nested property update
      setMetadataValues((prev) => {
        const updated = { ...prev };
        const pathParts = parentPath.split('.');
        let current: any = updated;
        
        // Navigate to the parent object
        for (const part of pathParts) {
          if (!current[part]) {
            current[part] = {};
          }
          current = current[part];
        }
        
        // Set the value
        current[propertyName] = newValue;
        return updated;
      });
    } else {
      // Handle top-level property update
      setMetadataValues((prev) => ({
        ...prev,
        [propertyName]: newValue,
      }));
    }
  };

  const getNestedValue = (obj: any, path: string): any => {
    const parts = path.split('.');
    let current = obj;
    for (const part of parts) {
      if (current === undefined || current === null) return undefined;
      current = current[part];
    }
    return current;
  };

  const renderField = (property: MetadataSpecProperty, parentPath: string = '') => {
    const fieldPath = parentPath ? `${parentPath}.${property.name}` : property.name;
    const currentValue = parentPath ? getNestedValue(metadataValues, fieldPath) : metadataValues[property.name];
    const hasError = !!validationErrors[fieldPath];

    switch (property.type) {
      case 'STRING':
        return (
          <div className="space-y-1">
            <Input
              type="text"
              value={currentValue || ''}
              onChange={(e) => handleValueChange(property.name, e.target.value, parentPath)}
              placeholder={property.placeholder || `Enter ${(property.displayName || property.name).toLowerCase()}`}
              data-testid={`input-metadata-${property.name}`}
              className={hasError ? 'border-destructive' : ''}
            />
            {hasError && (
              <p className="text-xs text-destructive flex items-center gap-1">
                <XCircle className="w-3 h-3" />
                {validationErrors[fieldPath]}
              </p>
            )}
          </div>
        );

      case 'NUMBER':
        return (
          <div className="space-y-1">
            <Input
              type="number"
              value={currentValue !== undefined && currentValue !== null && !isNaN(currentValue) ? currentValue : ''}
              onChange={(e) => {
                const val = e.target.value;
                if (val === '') {
                  handleValueChange(property.name, null, parentPath);
                } else {
                  const parsed = parseFloat(val);
                  // Only set value if it's a valid number
                  handleValueChange(property.name, isNaN(parsed) ? null : parsed, parentPath);
                }
              }}
              placeholder={property.placeholder || `Enter ${(property.displayName || property.name).toLowerCase()}`}
              data-testid={`input-metadata-${property.name}`}
              className={hasError ? 'border-destructive' : ''}
            />
            {hasError && (
              <p className="text-xs text-destructive flex items-center gap-1">
                <XCircle className="w-3 h-3" />
                {validationErrors[fieldPath]}
              </p>
            )}
          </div>
        );

      case 'BOOLEAN':
        return (
          <div className="space-y-1">
            <div className="flex items-center">
              <Checkbox
                checked={currentValue === true}
                onCheckedChange={(checked) => handleValueChange(property.name, checked === true, parentPath)}
                data-testid={`checkbox-metadata-${property.name}`}
              />
            </div>
            {hasError && (
              <p className="text-xs text-destructive flex items-center gap-1">
                <XCircle className="w-3 h-3" />
                {validationErrors[fieldPath]}
              </p>
            )}
          </div>
        );

      case 'TAGS':
        return (
          <div className="space-y-1">
            <TagsInput value={currentValue || []} onChange={(tags) => handleValueChange(property.name, tags, parentPath)} />
            {hasError && (
              <p className="text-xs text-destructive flex items-center gap-1">
                <XCircle className="w-3 h-3" />
                {validationErrors[fieldPath]}
              </p>
            )}
          </div>
        );

      case 'ARRAY':
        return (
          <div className="space-y-1">
            <Textarea
              value={Array.isArray(currentValue) ? JSON.stringify(currentValue, null, 2) : '[]'}
              onChange={(e) => {
                try {
                  const parsed = JSON.parse(e.target.value);
                  if (Array.isArray(parsed)) {
                    handleValueChange(property.name, parsed, parentPath);
                  }
                } catch {
                  // Invalid JSON, ignore
                }
              }}
              placeholder={property.placeholder || 'Enter JSON array'}
              className={`font-mono text-sm min-h-[80px] ${hasError ? 'border-destructive' : ''}`}
              data-testid={`textarea-metadata-${property.name}`}
            />
            {hasError && (
              <p className="text-xs text-destructive flex items-center gap-1">
                <XCircle className="w-3 h-3" />
                {validationErrors[fieldPath]}
              </p>
            )}
          </div>
        );

      case 'OBJECT':
        if (property.properties && property.properties.length > 0) {
          // Nested object with defined properties
          return (
            <div className="ml-4 space-y-3 border-l-2 border-muted pl-4">
              {property.properties.map((nestedProp) => (
                <div key={nestedProp.name} className="space-y-2">
                  <Label>
                    {nestedProp.displayName || nestedProp.name}
                    {nestedProp.required && <span className="text-destructive ml-1">*</span>}
                  </Label>
                  {renderField(nestedProp, fieldPath)}
                </div>
              ))}
            </div>
          );
        } else {
          // Generic object - allow JSON input
          return (
            <div className="space-y-1">
              <Textarea
                value={typeof currentValue === 'object' ? JSON.stringify(currentValue, null, 2) : '{}'}
                onChange={(e) => {
                  try {
                    const parsed = JSON.parse(e.target.value);
                    handleValueChange(property.name, parsed, parentPath);
                  } catch {
                    // Invalid JSON, ignore
                  }
                }}
                placeholder={property.placeholder || 'Enter JSON object'}
                className={`font-mono text-sm min-h-[100px] ${hasError ? 'border-destructive' : ''}`}
                data-testid={`textarea-metadata-${property.name}`}
              />
              {hasError && (
                <p className="text-xs text-destructive flex items-center gap-1">
                  <XCircle className="w-3 h-3" />
                  {validationErrors[fieldPath]}
                </p>
              )}
            </div>
          );
        }

      default:
        return (
          <div className="space-y-1">
            <Input
              type="text"
              value={currentValue || ''}
              onChange={(e) => handleValueChange(property.name, e.target.value, parentPath)}
              placeholder={property.placeholder}
              data-testid={`input-metadata-${property.name}`}
              className={hasError ? 'border-destructive' : ''}
            />
            {hasError && (
              <p className="text-xs text-destructive flex items-center gap-1">
                <XCircle className="w-3 h-3" />
                {validationErrors[fieldPath]}
              </p>
            )}
          </div>
        );
    }
  };

  if (specsLoading) {
    return (
      <div className="text-sm text-muted-foreground">Loading metadata specs...</div>
    );
  }

  if (!specs || specs.length === 0) {
    return (
      <Alert>
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>
          No metadata specs available. Please create a metadata spec first.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div className="space-y-4">
      <div className="space-y-2">
        <Label htmlFor="metadata-spec">
          Select Metadata Spec
        </Label>
        <div className="space-y-2">
          <Button
            type="button"
            variant="outline"
            className="w-full justify-between"
            onClick={() => setSpecSearchOpen(true)}
            disabled={mode === 'update' && !!specId}
            data-testid="button-search-metadata-spec"
          >
            <span className="truncate">
              {selectedSpecName || selectedSpecId || 'Select a metadata spec'}
            </span>
            <Search className="w-4 h-4 ml-2 flex-shrink-0" />
          </Button>
          {selectedSpecId && (
            <p className="text-xs text-muted-foreground font-mono">{selectedSpecId}</p>
          )}
        </div>
      </div>

      <ResourceSearchDialog
        open={specSearchOpen}
        onOpenChange={setSpecSearchOpen}
        onSelect={(specId, spec) => {
          handleSpecChange(specId);
          setSelectedSpecName(spec.name);
        }}
        resourceType="metadata-spec"
        endpoint="/api/rest/metadata_spec"
        title="Search Metadata Specs"
        description="Search for a metadata spec to use"
        displayFields={[
          { label: 'Name', key: 'name' },
          { label: 'Type', key: 'type' },
          { label: 'ID', key: 'id' },
        ]}
        searchPlaceholder="Search by name or type..."
        currentResourceId={selectedSpecId}
      />

      {specLoading && selectedSpecId && (
        <div className="text-sm text-muted-foreground">Loading spec details...</div>
      )}

      {!selectedSpecId && (
        <div className="space-y-4 pt-4 border-t">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-medium">Metadata Values (Free-form)</div>
              <p className="text-xs text-muted-foreground mt-1">
                Enter metadata as key-value pairs. No validation will be applied.
              </p>
            </div>
          </div>
          <div className="space-y-2">
            {Object.entries(metadataValues).map(([key, value]) => (
              <div key={key} className="flex gap-2">
                <Input
                  placeholder="Key"
                  value={key}
                  onChange={(e) => {
                    const newKey = e.target.value;
                    const newValues = { ...metadataValues };
                    delete newValues[key];
                    if (newKey) {
                      newValues[newKey] = value;
                    }
                    setMetadataValues(newValues);
                  }}
                  className="flex-1"
                  data-testid={`input-metadata-key-${key}`}
                />
                <Input
                  placeholder="Value"
                  value={typeof value === 'string' ? value : JSON.stringify(value)}
                  onChange={(e) => {
                    setMetadataValues({
                      ...metadataValues,
                      [key]: e.target.value
                    });
                  }}
                  className="flex-1"
                  data-testid={`input-metadata-value-${key}`}
                />
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  onClick={() => {
                    const newValues = { ...metadataValues };
                    delete newValues[key];
                    setMetadataValues(newValues);
                  }}
                  data-testid={`button-remove-metadata-${key}`}
                >
                  <X className="w-4 h-4" />
                </Button>
              </div>
            ))}
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => {
                const newKey = `key${Object.keys(metadataValues).length + 1}`;
                setMetadataValues({
                  ...metadataValues,
                  [newKey]: ''
                });
              }}
              data-testid="button-add-metadata"
            >
              <Plus className="w-4 h-4 mr-2" />
              Add Entry
            </Button>
          </div>
        </div>
      )}

      {selectedSpec && selectedSpec.properties && selectedSpec.properties.length > 0 && (
        <div className="space-y-4 pt-4 border-t">
          <div className="text-sm font-medium">Metadata Values</div>
          {selectedSpec.properties.map((property) => (
            <div key={property.name} className="space-y-2">
              <Label htmlFor={`metadata-${property.name}`}>
                {property.displayName || property.name}
                {property.required && <span className="text-destructive ml-1">*</span>}
              </Label>
              {renderField(property)}
            </div>
          ))}
        </div>
      )}

      {selectedSpec && (!selectedSpec.properties || selectedSpec.properties.length === 0) && (
        <div className="text-sm text-muted-foreground italic">
          This metadata spec has no properties defined
        </div>
      )}
    </div>
  );
}

