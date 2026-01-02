import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { Trash2, Plus } from 'lucide-react';
import { Card } from '@/components/ui/card';

interface HeaderParam {
  key: string;
  value: string;
  fromClient: boolean;
  userId?: boolean;
}

interface OAuth2HeaderParamEditorProps {
  value: HeaderParam[];
  onChange: (value: HeaderParam[]) => void;
  placeholder?: string;
  showUserIdCheckbox?: boolean;
  hasUserIdElsewhere?: boolean;
}

export function OAuth2HeaderParamEditor({ 
  value = [], 
  onChange, 
  placeholder = 'Add item',
  showUserIdCheckbox = false,
  hasUserIdElsewhere = false,
}: OAuth2HeaderParamEditorProps) {
  const [items, setItems] = useState<HeaderParam[]>([]);

  useEffect(() => {
    // Ensure value is an array and has the correct structure
    const normalizedValue = Array.isArray(value) ? value : [];
    setItems(normalizedValue);
  }, [value]);

  // Check if any item in this list has userId set to true
  const hasUserIdInThisList = items.some(item => item.userId === true);

  const handleAdd = () => {
    // Add new item to the top for better visibility
    const newItems = [{ key: '', value: '', fromClient: false, userId: false }, ...items];
    setItems(newItems);
    onChange(newItems);
  };

  const handleRemove = (index: number) => {
    const newItems = items.filter((_, i) => i !== index);
    setItems(newItems);
    onChange(newItems);
  };

  const handleChange = (index: number, field: keyof HeaderParam, fieldValue: string | boolean) => {
    const newItems = [...items];
    newItems[index] = { ...newItems[index], [field]: fieldValue };
    setItems(newItems);
    onChange(newItems);
  };

  return (
    <div className="space-y-3">
      <div className="flex items-center justify-end">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={handleAdd}
          data-testid={`button-add-item`}
        >
          <Plus className="w-4 h-4 mr-1" />
          Add
        </Button>
      </div>

      {items.length === 0 ? (
        <Card className="p-4 text-center text-muted-foreground text-sm">
          {placeholder}
        </Card>
      ) : (
        <div className="space-y-2">
          {items.map((item, index) => (
            <Card key={index} className="p-3">
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <Label className="text-xs text-muted-foreground">Key</Label>
                    <Input
                      value={item.key}
                      onChange={(e) => handleChange(index, 'key', e.target.value)}
                      placeholder="Header/Param key"
                      className="mt-1"
                      data-testid={`input-item-key-${index}`}
                    />
                  </div>
                  <div>
                    <Label className="text-xs text-muted-foreground">Value</Label>
                    <Input
                      value={item.value}
                      onChange={(e) => handleChange(index, 'value', e.target.value)}
                      placeholder="Header/Param value"
                      className="mt-1"
                      data-testid={`input-item-value-${index}`}
                    />
                  </div>
                </div>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id={`fromClient-${index}`}
                        checked={item.fromClient}
                        onCheckedChange={(checked) => handleChange(index, 'fromClient', !!checked)}
                        data-testid={`checkbox-item-fromclient-${index}`}
                      />
                      <Label 
                        htmlFor={`fromClient-${index}`} 
                        className="text-sm font-normal cursor-pointer"
                      >
                        From Client
                      </Label>
                    </div>

                    {showUserIdCheckbox && (
                      <div className="flex items-center space-x-2">
                        <Checkbox
                          id={`userId-${index}`}
                          checked={item.userId === true}
                          onCheckedChange={(checked) => handleChange(index, 'userId', !!checked)}
                          disabled={!item.userId && (hasUserIdElsewhere || hasUserIdInThisList)}
                          data-testid={`checkbox-item-userid-${index}`}
                        />
                        <Label 
                          htmlFor={`userId-${index}`} 
                          className={`text-sm font-normal cursor-pointer ${!item.userId && (hasUserIdElsewhere || hasUserIdInThisList) ? 'text-muted-foreground' : ''}`}
                        >
                          User ID
                        </Label>
                      </div>
                    )}
                  </div>

                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => handleRemove(index)}
                    data-testid={`button-remove-item-${index}`}
                  >
                    <Trash2 className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
