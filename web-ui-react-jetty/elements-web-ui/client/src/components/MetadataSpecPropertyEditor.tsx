import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Checkbox } from '@/components/ui/checkbox';
import { Card } from '@/components/ui/card';
import { Trash2, Plus, ChevronDown, ChevronRight } from 'lucide-react';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';

const PROPERTY_TYPES = ['STRING', 'NUMBER', 'BOOLEAN', 'OBJECT'];

interface MetadataSpecPropertyData {
  name: string;
  displayName: string;
  type: string;
  required: boolean;
  placeholder?: string;
  defaultValue?: any;
  properties?: MetadataSpecPropertyData[];
}

interface MetadataSpecPropertyEditorProps {
  value?: MetadataSpecPropertyData[];
  onChange: (properties: MetadataSpecPropertyData[]) => void;
}

export function MetadataSpecPropertyEditor({ value = [], onChange }: MetadataSpecPropertyEditorProps) {
  const [properties, setProperties] = useState<MetadataSpecPropertyData[]>(value);

  useEffect(() => {
    if (value && JSON.stringify(value) !== JSON.stringify(properties)) {
      setProperties(value);
    }
  }, [value]);

  useEffect(() => {
    onChange(properties);
  }, [properties]);

  const addProperty = () => {
    setProperties([
      ...properties,
      {
        name: '',
        displayName: '',
        type: 'STRING',
        required: false,
      },
    ]);
  };

  const removeProperty = (index: number) => {
    setProperties(properties.filter((_, i) => i !== index));
  };

  const updateProperty = (index: number, field: string, value: any) => {
    const updated = [...properties];
    updated[index] = { ...updated[index], [field]: value };
    
    // If type changes from OBJECT, remove nested properties
    if (field === 'type' && value !== 'OBJECT' && updated[index].properties) {
      delete updated[index].properties;
    }
    
    // If type changes to OBJECT, initialize empty properties array
    if (field === 'type' && value === 'OBJECT' && !updated[index].properties) {
      updated[index].properties = [];
    }
    
    setProperties(updated);
  };

  const updateNestedProperties = (index: number, nestedProperties: MetadataSpecPropertyData[]) => {
    const updated = [...properties];
    updated[index] = { ...updated[index], properties: nestedProperties };
    setProperties(updated);
  };

  return (
    <div className="space-y-4">
      {properties.map((property, index) => (
        <PropertyEditor
          key={index}
          property={property}
          onUpdate={(field, value) => updateProperty(index, field, value)}
          onUpdateNested={(nested) => updateNestedProperties(index, nested)}
          onRemove={() => removeProperty(index)}
          level={0}
        />
      ))}
      
      <Button
        type="button"
        variant="outline"
        onClick={addProperty}
        className="w-full"
        data-testid="button-add-property"
      >
        <Plus className="w-4 h-4 mr-2" />
        Add Property
      </Button>
    </div>
  );
}

interface PropertyEditorProps {
  property: MetadataSpecPropertyData;
  onUpdate: (field: string, value: any) => void;
  onUpdateNested: (properties: MetadataSpecPropertyData[]) => void;
  onRemove: () => void;
  level: number;
}

function PropertyEditor({ property, onUpdate, onUpdateNested, onRemove, level }: PropertyEditorProps) {
  const [isOpen, setIsOpen] = useState(level < 2); // Auto-expand first 2 levels

  const hasNestedProperties = property.type === 'OBJECT';
  const indent = level * 16;

  return (
    <Card className="p-4" style={{ marginLeft: `${indent}px` }}>
      <div className="space-y-4">
        <div className="flex items-start gap-2">
          <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor={`name-${level}-${property.name}`}>
                Name <span className="text-destructive">*</span>
              </Label>
              <Input
                id={`name-${level}-${property.name}`}
                value={property.name}
                onChange={(e) => onUpdate('name', e.target.value)}
                placeholder="fieldName"
                data-testid={`input-property-name-${level}`}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor={`displayName-${level}-${property.name}`}>
                Display Name <span className="text-destructive">*</span>
              </Label>
              <Input
                id={`displayName-${level}-${property.name}`}
                value={property.displayName}
                onChange={(e) => onUpdate('displayName', e.target.value)}
                placeholder="Field Name"
                data-testid={`input-property-displayname-${level}`}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor={`type-${level}-${property.name}`}>
                Type <span className="text-destructive">*</span>
              </Label>
              <Select
                value={property.type}
                onValueChange={(value) => onUpdate('type', value)}
              >
                <SelectTrigger id={`type-${level}-${property.name}`} data-testid={`select-property-type-${level}`}>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {PROPERTY_TYPES.map((type) => (
                    <SelectItem key={type} value={type}>
                      {type}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Only show Placeholder for STRING and NUMBER types */}
            {(property.type === 'STRING' || property.type === 'NUMBER') && (
              <div className="space-y-2">
                <Label htmlFor={`placeholder-${level}-${property.name}`}>Placeholder</Label>
                <Input
                  id={`placeholder-${level}-${property.name}`}
                  value={property.placeholder || ''}
                  onChange={(e) => onUpdate('placeholder', e.target.value || undefined)}
                  placeholder="Enter placeholder text"
                  data-testid={`input-property-placeholder-${level}`}
                />
              </div>
            )}

            {/* Only show Default Value for STRING and NUMBER types */}
            {(property.type === 'STRING' || property.type === 'NUMBER') && (
              <div className="space-y-2">
                <Label htmlFor={`defaultValue-${level}-${property.name}`}>Default Value</Label>
                <Input
                  id={`defaultValue-${level}-${property.name}`}
                  value={property.defaultValue !== undefined ? String(property.defaultValue) : ''}
                  onChange={(e) => {
                    const val = e.target.value;
                    if (val === '') {
                      onUpdate('defaultValue', undefined);
                    } else if (property.type === 'NUMBER') {
                      onUpdate('defaultValue', parseFloat(val) || val);
                    } else {
                      onUpdate('defaultValue', val);
                    }
                  }}
                  placeholder="Default value"
                  data-testid={`input-property-defaultvalue-${level}`}
                />
              </div>
            )}

            {/* Show checkbox for BOOLEAN Default Value */}
            {property.type === 'BOOLEAN' && (
              <div className="space-y-2">
                <Label htmlFor={`defaultValue-${level}-${property.name}`}>Default Value</Label>
                <div className="flex items-center h-9">
                  <Checkbox
                    id={`defaultValue-${level}-${property.name}`}
                    checked={property.defaultValue === true}
                    onCheckedChange={(checked) => onUpdate('defaultValue', checked === true)}
                    data-testid={`checkbox-property-defaultvalue-${level}`}
                  />
                </div>
              </div>
            )}

            <div className="flex items-center space-x-2 pt-6">
              <Checkbox
                id={`required-${level}-${property.name}`}
                checked={property.required}
                onCheckedChange={(checked) => onUpdate('required', checked === true)}
                data-testid={`checkbox-property-required-${level}`}
              />
              <Label htmlFor={`required-${level}-${property.name}`} className="cursor-pointer">
                Required
              </Label>
            </div>
          </div>

          <Button
            type="button"
            variant="ghost"
            size="icon"
            onClick={onRemove}
            className="shrink-0"
            data-testid={`button-remove-property-${level}`}
          >
            <Trash2 className="w-4 h-4 text-destructive" />
          </Button>
        </div>

        {hasNestedProperties && (
          <Collapsible open={isOpen} onOpenChange={setIsOpen}>
            <CollapsibleTrigger asChild>
              <Button
                type="button"
                variant="ghost"
                className="w-full justify-start"
                data-testid={`button-toggle-nested-${level}`}
              >
                {isOpen ? (
                  <ChevronDown className="w-4 h-4 mr-2" />
                ) : (
                  <ChevronRight className="w-4 h-4 mr-2" />
                )}
                Nested Properties ({property.properties?.length || 0})
              </Button>
            </CollapsibleTrigger>
            <CollapsibleContent className="space-y-4 mt-4">
              <MetadataSpecPropertyEditor
                value={property.properties || []}
                onChange={onUpdateNested}
              />
            </CollapsibleContent>
          </Collapsible>
        )}
      </div>
    </Card>
  );
}
