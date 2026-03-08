import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from '@/components/ui/command';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Loader2, Plus, X, ChevronsUpDown, Check } from 'lucide-react';
import { TagsInput } from '@/components/TagsInput';
import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { apiRequest } from '@/lib/queryClient';
import { cn } from '@/lib/utils';
import { ItemSearchDialog } from '@/components/ItemSearchDialog';
import { ApplicationSearchDialog } from '@/components/ApplicationSearchDialog';

interface ProductBundleReward {
  itemId: string;
  quantity: number | null;
}

interface ApplicationRef {
  id: string;
  name?: string;
}

interface ProductBundle {
  id: string;
  schema: string;
  application?: ApplicationRef;
  productId: string;
  displayName?: string;
  description?: string;
  productBundleRewards: ProductBundleReward[];
  display?: boolean;
  tags?: string[];
}

interface ProductBundleFormProps {
  mode: 'create' | 'update';
  initialData?: ProductBundle;
  onSubmit: (data: any) => Promise<void>;
  onCancel: () => void;
  onFormChange?: (data: any) => void;
}

const bundleSchema = z.object({
  schema: z.string().min(1, 'Schema is required'),
  application: z.object({
    id: z.string().optional(),
    name: z.string().optional(),
  }).optional(),
  productId: z.string().min(1, 'Product ID is required'),
  displayName: z.string().optional(),
  description: z.string().optional(),
  display: z.boolean().default(false),
  tags: z.array(z.string()).default([]),
  productBundleRewards: z.array(z.object({
    itemId: z.string().min(1, 'Item ID is required'),
    quantity: z.preprocess(
      v => (v === '' || v == null) ? null : Number(v),
      z.number().positive().nullable(),
    ),
  })).min(1, 'At least one reward is required'),
});

export function ProductBundleForm({ mode, initialData, onSubmit, onCancel, onFormChange }: ProductBundleFormProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [schemaPopoverOpen, setSchemaPopoverOpen] = useState(false);
  const [schemaSearch, setSchemaSearch] = useState('');
  const [appSearchOpen, setAppSearchOpen] = useState(false);

  // Per-row item metadata: { id, name, category }
  const [rewardItems, setRewardItems] = useState<Record<number, { id: string; name: string; category: string }>>(() => {
    if (!initialData?.productBundleRewards) return {};
    return Object.fromEntries(
      initialData.productBundleRewards.map((r, i) => [i, { id: r.itemId, name: r.itemId, category: '' }])
    );
  });

  // Fetch item details for pre-loaded rewards so names and categories are correct
  useEffect(() => {
    const rewards = initialData?.productBundleRewards;
    if (!rewards?.length) return;
    rewards.forEach((reward, index) => {
      if (!reward.itemId) return;
      apiRequest('GET', `/api/proxy/api/rest/item/${reward.itemId}`)
        .then(r => r.json())
        .then(item => setRewardItems(prev => ({
          ...prev,
          [index]: { id: item.id, name: item.name || item.id, category: item.category || '' },
        })))
        .catch(() => {});
    });
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Which row's item search dialog is open (-1 = none)
  const [itemSearchIndex, setItemSearchIndex] = useState(-1);

  const form = useForm({
    resolver: zodResolver(bundleSchema),
    defaultValues: {
      schema: initialData?.schema ?? '',
      application: initialData?.application ?? { id: '', name: '' },
      productId: initialData?.productId ?? '',
      displayName: initialData?.displayName ?? '',
      description: initialData?.description ?? '',
      display: initialData?.display ?? false,
      tags: initialData?.tags ?? [],
      productBundleRewards: initialData?.productBundleRewards?.length
        ? initialData.productBundleRewards.map(r => ({ itemId: r.itemId, quantity: r.quantity ?? ('' as any) }))
        : [{ itemId: '', quantity: '' as any }],
    },
  });

  const { fields, append, remove } = useFieldArray({
    control: form.control,
    name: 'productBundleRewards',
  });

  useEffect(() => {
    if (!onFormChange) return;
    const subscription = form.watch((data) => onFormChange(data));
    return () => subscription.unsubscribe();
  }, [form, onFormChange]);

  // Fetch registered schemas for the combobox
  const { data: schemasData } = useQuery({
    queryKey: ['/api/rest/product/sku/schema'],
    queryFn: async () => {
      const response = await apiRequest('GET', '/api/proxy/api/rest/product/sku/schema?offset=0&count=100');
      return response.json();
    },
    enabled: schemaPopoverOpen && mode === 'create',
  });

  const schemaOptions: string[] = schemasData?.objects?.map((s: any) => s.schema) ?? [];

  const handleSubmit = async (data: any) => {
    setIsSubmitting(true);
    try {
      const payload = {
        ...data,
        application: data.application?.id ? data.application : null,
      };
      await onSubmit(payload);
    } finally {
      setIsSubmitting(false);
    }
  };

  const setRewardItem = (index: number, item: any) => {
    setRewardItems(prev => ({
      ...prev,
      [index]: { id: item.id, name: item.name || item.id, category: item.category || '' },
    }));
  };

  const handleRemoveReward = (index: number) => {
    remove(index);
    setRewardItems(prev => {
      const next: Record<number, { id: string; name: string; category: string }> = {};
      Object.entries(prev).forEach(([key, val]) => {
        const k = Number(key);
        if (k < index) next[k] = val;
        else if (k > index) next[k - 1] = val;
      });
      return next;
    });
  };

  const selectedAppName = form.watch('application.name') || form.watch('application.id');

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-6">

        {/* Schema */}
        <FormField
          control={form.control}
          name="schema"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Schema *</FormLabel>
              {mode === 'update' ? (
                <FormControl>
                  <Input {...field} disabled className="bg-muted" data-testid="input-schema" />
                </FormControl>
              ) : (
                <Popover open={schemaPopoverOpen} onOpenChange={setSchemaPopoverOpen}>
                  <PopoverTrigger asChild>
                    <FormControl>
                      <Button
                        variant="outline"
                        role="combobox"
                        aria-expanded={schemaPopoverOpen}
                        className={cn('w-full justify-between font-normal', !field.value && 'text-muted-foreground')}
                        data-testid="input-schema"
                      >
                        {field.value || 'Select or type a schema…'}
                        <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
                      </Button>
                    </FormControl>
                  </PopoverTrigger>
                  <PopoverContent className="w-[--radix-popover-trigger-width] p-0" align="start">
                    <Command>
                      <CommandInput
                        placeholder="Search or type a schema…"
                        value={schemaSearch}
                        onValueChange={val => {
                          setSchemaSearch(val);
                          field.onChange(val);
                        }}
                      />
                      <CommandList>
                        <CommandEmpty>
                          {schemaSearch
                            ? <span className="text-xs px-2">Press Enter to use &quot;{schemaSearch}&quot;</span>
                            : 'No schemas found.'}
                        </CommandEmpty>
                        <CommandGroup>
                          {schemaOptions.map(s => (
                            <CommandItem
                              key={s}
                              value={s}
                              onSelect={val => {
                                field.onChange(val);
                                setSchemaSearch(val);
                                setSchemaPopoverOpen(false);
                              }}
                            >
                              <Check className={cn('mr-2 h-4 w-4', field.value === s ? 'opacity-100' : 'opacity-0')} />
                              {s}
                            </CommandItem>
                          ))}
                        </CommandGroup>
                      </CommandList>
                    </Command>
                  </PopoverContent>
                </Popover>
              )}
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Application */}
        <FormField
          control={form.control}
          name="application.id"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Application</FormLabel>
              <FormControl>
                <Input
                  {...field}
                  readOnly
                  placeholder="Select an application…"
                  value={selectedAppName || field.value}
                  className={cn('bg-muted', mode === 'create' ? 'cursor-pointer' : 'cursor-default')}
                  disabled={mode === 'update'}
                  onClick={() => { if (mode === 'create') setAppSearchOpen(true); }}
                  data-testid="input-application"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Display Name */}
        <FormField
          control={form.control}
          name="displayName"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Display Name</FormLabel>
              <FormControl>
                <Input {...field} placeholder="100 Gold Coins" data-testid="input-displayName" />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Description */}
        <FormField
          control={form.control}
          name="description"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Description</FormLabel>
              <FormControl>
                <Textarea
                  {...field}
                  placeholder="Optional description shown to end users…"
                  rows={3}
                  data-testid="input-description"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Tags */}
        <FormField
          control={form.control}
          name="tags"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Tags</FormLabel>
              <FormControl>
                <TagsInput value={field.value ?? []} onChange={field.onChange} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Product ID */}
        <FormField
          control={form.control}
          name="productId"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Product ID *</FormLabel>
              <FormControl>
                <Input
                  {...field}
                  placeholder="com.example.coins_100"
                  disabled={mode === 'update'}
                  className={mode === 'update' ? 'bg-muted' : ''}
                  data-testid="input-productId"
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        {/* Display toggle */}
        <FormField
          control={form.control}
          name="display"
          render={({ field }) => (
            <FormItem className="flex flex-row items-center gap-3">
              <FormControl>
                <Checkbox
                  checked={field.value}
                  onCheckedChange={field.onChange}
                  data-testid="checkbox-display"
                />
              </FormControl>
              <FormLabel className="!mt-0 cursor-pointer">Show to end users</FormLabel>
            </FormItem>
          )}
        />

        {/* Rewards */}
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <FormLabel>Rewards *</FormLabel>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => append({ itemId: '', quantity: '' as any })}
              data-testid="button-add-reward"
            >
              <Plus className="w-4 h-4 mr-1" />
              Add Reward
            </Button>
          </div>

          {form.formState.errors.productBundleRewards?.root?.message && (
            <p className="text-sm text-destructive">{form.formState.errors.productBundleRewards.root.message}</p>
          )}
          {typeof form.formState.errors.productBundleRewards?.message === 'string' && (
            <p className="text-sm text-destructive">{form.formState.errors.productBundleRewards.message}</p>
          )}

          {fields.map((field, index) => {
            const rewardItem = rewardItems[index];
            const isFungible = rewardItem?.category === 'FUNGIBLE';

            return (
              <div key={field.id} className="flex gap-2 items-start">
                {/* Item picker */}
                <FormField
                  control={form.control}
                  name={`productBundleRewards.${index}.itemId`}
                  render={({ field: itemField }) => (
                    <FormItem className="flex-1">
                      {index === 0 && <FormLabel className="text-xs text-muted-foreground">Item</FormLabel>}
                      <div className="flex gap-1">
                        <FormControl>
                          <Input
                            {...itemField}
                            readOnly
                            placeholder="Select an item…"
                            value={rewardItem ? rewardItem.name : itemField.value}
                            className="bg-muted cursor-pointer"
                            onClick={() => setItemSearchIndex(index)}
                            data-testid={`input-reward-itemId-${index}`}
                          />
                        </FormControl>
                        {rewardItem && (
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => {
                              itemField.onChange('');
                              form.setValue(`productBundleRewards.${index}.quantity`, null);
                              setRewardItems(prev => {
                                const next = { ...prev };
                                delete next[index];
                                return next;
                              });
                            }}
                            data-testid={`button-clear-item-${index}`}
                          >
                            <X className="w-4 h-4" />
                          </Button>
                        )}
                      </div>
                      <FormMessage />
                    </FormItem>
                  )}
                />

                {/* Quantity — only for FUNGIBLE items */}
                {isFungible && (
                  <FormField
                    control={form.control}
                    name={`productBundleRewards.${index}.quantity`}
                    render={({ field: qtyField }) => (
                      <FormItem className="w-28">
                        {index === 0 && <FormLabel className="text-xs text-muted-foreground">Qty</FormLabel>}
                        <FormControl>
                          <Input
                            {...qtyField}
                            type="number"
                            min={1}
                            placeholder="—"
                            value={qtyField.value ?? ''}
                            onChange={e => qtyField.onChange(e.target.value)}
                            data-testid={`input-reward-quantity-${index}`}
                          />
                        </FormControl>
                        <FormMessage />
                      </FormItem>
                    )}
                  />
                )}

                <div className={index === 0 ? 'mt-6' : ''}>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    onClick={() => handleRemoveReward(index)}
                    disabled={fields.length === 1}
                    data-testid={`button-remove-reward-${index}`}
                  >
                    <X className="w-4 h-4" />
                  </Button>
                </div>
              </div>
            );
          })}
        </div>

        {/* Form actions */}
        <div className="flex gap-2 justify-end pt-4">
          <Button
            type="button"
            variant="outline"
            onClick={onCancel}
            disabled={isSubmitting}
            data-testid="button-cancel"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            disabled={isSubmitting}
            data-testid="button-submit"
          >
            {isSubmitting && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
            {mode === 'create' ? 'Create Bundle' : 'Update Bundle'}
          </Button>
        </div>
      </form>

      {/* Item search dialog */}
      <ItemSearchDialog
        open={itemSearchIndex >= 0}
        onOpenChange={open => { if (!open) setItemSearchIndex(-1); }}
        onSelect={(itemId, item) => {
          if (itemSearchIndex < 0) return;
          form.setValue(`productBundleRewards.${itemSearchIndex}.itemId`, itemId);
          form.setValue(`productBundleRewards.${itemSearchIndex}.quantity`, null);
          setRewardItem(itemSearchIndex, item);
          setItemSearchIndex(-1);
        }}
        title="Select Item"
        description="Choose the item to award when this bundle is purchased."
      />

      {/* Application search dialog */}
      <ApplicationSearchDialog
        open={appSearchOpen}
        onOpenChange={setAppSearchOpen}
        onSelect={(appId, app) => {
          form.setValue('application.id', appId);
          form.setValue('application.name', app.name || appId);
          setAppSearchOpen(false);
        }}
      />
    </Form>
  );
}
