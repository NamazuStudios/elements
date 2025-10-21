import { useEffect, useCallback } from 'react';

interface UseDraftOptions {
  resourceName: string;
  mode: 'create' | 'update';
  itemId?: string;
  onSaveDraft?: (data: any) => void;
}

export function useFormDraft({ resourceName, mode, itemId }: UseDraftOptions) {
  const draftKey = `draft_${resourceName}_${mode}_${itemId || 'new'}`;

  const saveDraft = useCallback((data: any) => {
    try {
      localStorage.setItem(draftKey, JSON.stringify({
        data,
        timestamp: Date.now(),
      }));
    } catch (error) {
      console.error('Failed to save draft:', error);
    }
  }, [draftKey]);

  const loadDraft = useCallback((): any | null => {
    try {
      const stored = localStorage.getItem(draftKey);
      if (!stored) return null;
      
      const parsed = JSON.parse(stored);
      // Only load drafts from the last 24 hours
      const ageInHours = (Date.now() - parsed.timestamp) / (1000 * 60 * 60);
      if (ageInHours > 24) {
        localStorage.removeItem(draftKey);
        return null;
      }
      
      return parsed.data;
    } catch (error) {
      console.error('Failed to load draft:', error);
      return null;
    }
  }, [draftKey]);

  const clearDraft = useCallback(() => {
    try {
      localStorage.removeItem(draftKey);
    } catch (error) {
      console.error('Failed to clear draft:', error);
    }
  }, [draftKey]);

  const hasDraft = useCallback((): boolean => {
    const draft = loadDraft();
    return draft !== null;
  }, [loadDraft]);

  return {
    saveDraft,
    loadDraft,
    clearDraft,
    hasDraft,
  };
}
