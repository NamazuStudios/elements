import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { Trash2, Plus, GripVertical, X, ChevronDown, Search } from 'lucide-react';
import { ResourceSearchDialog } from '@/components/ResourceSearchDialog';
import { apiClient } from '@/lib/api-client';

export interface MissionStepReward {
  itemId: string;  // Internal representation for the form
  quantity: number;
}

export interface MissionStep {
  displayName: string;
  description?: string;
  count?: number;
  metadata?: Record<string, string>;
  rewards?: MissionStepReward[];
}

interface MissionStepsEditorProps {
  value: MissionStep[];
  onChange: (steps: MissionStep[]) => void;
}

export function MissionStepsEditor({ value, onChange }: MissionStepsEditorProps) {
  const steps = value || [];
  const [openSteps, setOpenSteps] = useState<Set<number>>(new Set(steps.map((_, i) => i)));
  const [itemSearchOpen, setItemSearchOpen] = useState(false);
  const [editingReward, setEditingReward] = useState<{ stepIndex: number; rewardIndex: number } | null>(null);

  const { data: items = [] } = useQuery<any[]>({
    queryKey: ['/api/rest/item'],
    queryFn: async () => {
      const response = await apiClient.request<any>('/api/rest/item');
      
      // Elements API returns paginated data: {offset, total, objects: [...]} or {offset, total, content: [...]}
      if (response && typeof response === 'object') {
        if ('objects' in response) {
          return Array.isArray(response.objects) ? response.objects : [];
        }
        if ('content' in response) {
          return Array.isArray(response.content) ? response.content : [];
        }
      }
      return Array.isArray(response) ? response : [];
    },
  });

  const toggleStep = (index: number) => {
    const newOpenSteps = new Set(openSteps);
    if (newOpenSteps.has(index)) {
      newOpenSteps.delete(index);
    } else {
      newOpenSteps.add(index);
    }
    setOpenSteps(newOpenSteps);
  };

  const addStep = () => {
    const newIndex = steps.length;
    onChange([
      ...steps,
      {
        displayName: '',
        description: '',
        count: 1,
        metadata: {},
        rewards: [],
      },
    ]);
    const newOpenSteps = new Set(openSteps);
    newOpenSteps.add(newIndex);
    setOpenSteps(newOpenSteps);
  };

  const removeStep = (index: number) => {
    onChange(steps.filter((_, i) => i !== index));
    // Recompute openSteps: keep indices that are before the removed one,
    // and decrement indices that are after it
    const newOpenSteps = new Set<number>();
    openSteps.forEach(i => {
      if (i < index) {
        newOpenSteps.add(i);
      } else if (i > index) {
        newOpenSteps.add(i - 1);
      }
      // i === index is removed, so we don't add it
    });
    setOpenSteps(newOpenSteps);
  };

  const updateStep = (index: number, field: keyof MissionStep, newValue: any) => {
    const updated = [...steps];
    updated[index] = { ...updated[index], [field]: newValue };
    onChange(updated);
  };

  const addMetadataEntry = (stepIndex: number) => {
    const step = steps[stepIndex];
    const metadata = step.metadata || {};
    updateStep(stepIndex, 'metadata', { ...metadata, '': '' });
  };

  const updateMetadataKey = (stepIndex: number, oldKey: string, newKey: string) => {
    const step = steps[stepIndex];
    const metadata = step.metadata || {};
    const newMetadata = { ...metadata };
    
    if (oldKey !== newKey) {
      const value = newMetadata[oldKey];
      delete newMetadata[oldKey];
      newMetadata[newKey] = value;
    }
    
    updateStep(stepIndex, 'metadata', newMetadata);
  };

  const updateMetadataValue = (stepIndex: number, key: string, value: string) => {
    const step = steps[stepIndex];
    const metadata = step.metadata || {};
    updateStep(stepIndex, 'metadata', { ...metadata, [key]: value });
  };

  const removeMetadataEntry = (stepIndex: number, key: string) => {
    const step = steps[stepIndex];
    const metadata = step.metadata || {};
    const newMetadata = { ...metadata };
    delete newMetadata[key];
    updateStep(stepIndex, 'metadata', newMetadata);
  };

  const addReward = (stepIndex: number) => {
    const step = steps[stepIndex];
    const rewards = step.rewards || [];
    updateStep(stepIndex, 'rewards', [...rewards, { itemId: '', quantity: 1 }]);
  };

  const updateRewardItem = (stepIndex: number, rewardIndex: number, itemId: string) => {
    const step = steps[stepIndex];
    const rewards = step.rewards || [];
    const updated = [...rewards];
    updated[rewardIndex] = { ...updated[rewardIndex], itemId };
    updateStep(stepIndex, 'rewards', updated);
  };

  const updateRewardQuantity = (stepIndex: number, rewardIndex: number, quantity: number) => {
    const step = steps[stepIndex];
    const rewards = step.rewards || [];
    const updated = [...rewards];
    updated[rewardIndex] = { ...updated[rewardIndex], quantity };
    updateStep(stepIndex, 'rewards', updated);
  };

  const removeReward = (stepIndex: number, rewardIndex: number) => {
    const step = steps[stepIndex];
    const rewards = step.rewards || [];
    updateStep(stepIndex, 'rewards', rewards.filter((_, i) => i !== rewardIndex));
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <Label>Mission Steps</Label>
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={addStep}
          data-testid="button-add-step"
        >
          <Plus className="w-4 h-4 mr-2" />
          Add Step
        </Button>
      </div>

      <div className="space-y-3">
        {steps.length === 0 ? (
          <Card>
            <CardContent className="pt-6">
              <p className="text-sm text-muted-foreground text-center">
                No steps added yet. Click "Add Step" to create the first step.
              </p>
            </CardContent>
          </Card>
        ) : (
          steps.map((step, index) => (
            <Collapsible
              key={index}
              open={openSteps.has(index)}
              onOpenChange={() => toggleStep(index)}
            >
              <Card className="relative">
                <CollapsibleTrigger asChild>
                  <CardHeader className="pb-3 hover-elevate cursor-pointer">
                    <div className="flex items-center justify-between gap-2">
                      <div className="flex items-center gap-2 flex-1">
                        <GripVertical className="w-4 h-4 text-muted-foreground" />
                        <CardTitle className="text-base">
                          Step {index + 1}{step.displayName ? `: ${step.displayName}` : ''}
                        </CardTitle>
                        <ChevronDown className={`w-4 h-4 text-muted-foreground transition-transform ${openSteps.has(index) ? '' : '-rotate-90'}`} />
                      </div>
                      <Button
                        type="button"
                        variant="ghost"
                        size="icon"
                        onClick={(e) => {
                          e.stopPropagation();
                          removeStep(index);
                        }}
                        data-testid={`button-remove-step-${index}`}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </CardHeader>
                </CollapsibleTrigger>
                <CollapsibleContent>
                  <CardContent className="space-y-4 pt-0">
                    <div className="grid grid-cols-4 gap-3">
                      <div className="col-span-3">
                        <Label htmlFor={`step-${index}-displayName`}>Display Name *</Label>
                        <Input
                          id={`step-${index}-displayName`}
                          value={step.displayName}
                          onChange={(e) => updateStep(index, 'displayName', e.target.value)}
                          placeholder="Step display name"
                          data-testid={`input-step-displayName-${index}`}
                        />
                      </div>
                      <div className="col-span-1">
                        <Label htmlFor={`step-${index}-count`}>Count</Label>
                        <Input
                          id={`step-${index}-count`}
                          type="number"
                          value={step.count || 1}
                          onChange={(e) => updateStep(index, 'count', parseInt(e.target.value) || 1)}
                          data-testid={`input-step-count-${index}`}
                        />
                      </div>
                    </div>

                    <div>
                      <Label htmlFor={`step-${index}-description`}>Description</Label>
                      <Textarea
                        id={`step-${index}-description`}
                        value={step.description || ''}
                        onChange={(e) => updateStep(index, 'description', e.target.value)}
                        placeholder="Describe what the player needs to do in this step"
                        rows={2}
                        data-testid={`input-step-description-${index}`}
                      />
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <Label>Metadata</Label>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => addMetadataEntry(index)}
                          data-testid={`button-add-metadata-${index}`}
                        >
                          <Plus className="w-3 h-3 mr-1" />
                          Add Entry
                        </Button>
                      </div>
                      <div className="space-y-2">
                        {Object.entries(step.metadata || {}).map(([key, val], metaIndex) => (
                          <div key={metaIndex} className="flex gap-2">
                            <Input
                              placeholder="Key"
                              value={key}
                              onChange={(e) => updateMetadataKey(index, key, e.target.value)}
                              className="flex-1"
                              data-testid={`input-metadata-key-${index}-${metaIndex}`}
                            />
                            <Input
                              placeholder="Value"
                              value={val}
                              onChange={(e) => updateMetadataValue(index, key, e.target.value)}
                              className="flex-1"
                              data-testid={`input-metadata-value-${index}-${metaIndex}`}
                            />
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              onClick={() => removeMetadataEntry(index, key)}
                              data-testid={`button-remove-metadata-${index}-${metaIndex}`}
                            >
                              <X className="w-4 h-4" />
                            </Button>
                          </div>
                        ))}
                        {(!step.metadata || Object.keys(step.metadata).length === 0) && (
                          <p className="text-sm text-muted-foreground text-center py-2">
                            No metadata entries
                          </p>
                        )}
                      </div>
                    </div>

                    <div className="space-y-2">
                      <div className="flex items-center justify-between">
                        <Label>Rewards</Label>
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          onClick={() => addReward(index)}
                          data-testid={`button-add-reward-${index}`}
                        >
                          <Plus className="w-3 h-3 mr-1" />
                          Add Reward
                        </Button>
                      </div>
                      <div className="space-y-2">
                        {(step.rewards || []).map((reward, rewardIndex) => {
                          const selectedItem = items.find((item: any) => item.id === reward.itemId);
                          return (
                            <div key={rewardIndex} className="flex gap-2">
                              <div className="flex-1">
                                <Button
                                  type="button"
                                  variant="outline"
                                  className="w-full justify-start text-left font-normal"
                                  onClick={() => {
                                    setEditingReward({ stepIndex: index, rewardIndex });
                                    setItemSearchOpen(true);
                                  }}
                                  data-testid={`button-select-reward-item-${index}-${rewardIndex}`}
                                >
                                  {selectedItem ? (
                                    <span>{selectedItem.displayName || selectedItem.name || reward.itemId}</span>
                                  ) : reward.itemId ? (
                                    <span className="text-muted-foreground">{reward.itemId}</span>
                                  ) : (
                                    <span className="text-muted-foreground">Select item...</span>
                                  )}
                                  <Search className="ml-auto h-4 w-4 opacity-50" />
                                </Button>
                              </div>
                              <Input
                                type="number"
                                placeholder="Qty"
                                value={reward.quantity}
                                onChange={(e) => updateRewardQuantity(index, rewardIndex, parseInt(e.target.value) || 1)}
                                className="w-24"
                                min={1}
                                data-testid={`input-reward-quantity-${index}-${rewardIndex}`}
                              />
                              <Button
                                type="button"
                                variant="ghost"
                                size="icon"
                                onClick={() => removeReward(index, rewardIndex)}
                                data-testid={`button-remove-reward-${index}-${rewardIndex}`}
                              >
                                <X className="w-4 h-4" />
                              </Button>
                            </div>
                          );
                        })}
                        {(!step.rewards || step.rewards.length === 0) && (
                          <p className="text-sm text-muted-foreground text-center py-2">
                            No rewards
                          </p>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </CollapsibleContent>
              </Card>
            </Collapsible>
          ))
        )}
      </div>

      <ResourceSearchDialog
        open={itemSearchOpen}
        onOpenChange={setItemSearchOpen}
        onSelect={(itemId, item) => {
          if (editingReward) {
            updateRewardItem(editingReward.stepIndex, editingReward.rewardIndex, itemId);
            setItemSearchOpen(false);
            setEditingReward(null);
          }
        }}
        resourceType="item"
        endpoint="/api/rest/item"
        title="Search Items"
        description="Search for items to add as rewards"
        displayFields={[
          { label: 'Name', key: 'displayName' },
          { label: 'ID', key: 'id' },
        ]}
        searchPlaceholder="Search by name or ID..."
        currentResourceId={
          editingReward
            ? steps[editingReward.stepIndex]?.rewards?.[editingReward.rewardIndex]?.itemId
            : undefined
        }
      />
    </div>
  );
}
