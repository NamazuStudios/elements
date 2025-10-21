import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { ApplicationConfigurationEditor } from './ApplicationConfigurationEditor';
import { useState, useEffect } from 'react';
import { Loader2 } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';

type ConfigurationType = 'Facebook' | 'Firebase' | 'GooglePlay' | 'iOS' | 'Matchmaking';

interface ApplicationConfigurationDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  value: any;
  configurationType: ConfigurationType | null;
  onSave: (config: { type: ConfigurationType | null; value: any }) => Promise<void>;
}

// Synchronous validation function to check if configuration is valid
function validateConfiguration(configurationType: ConfigurationType | null, value: any): boolean {
  if (!configurationType) {
    return false;
  }
  
  if (configurationType === 'Facebook') {
    return !!(value.applicationId && value.applicationSecret);
  } else if (configurationType === 'Firebase') {
    return !!(value.projectId && value.serviceAccountCredentials);
  } else if (configurationType === 'iOS') {
    return !!(value.applicationId);
  } else if (configurationType === 'Matchmaking') {
    const maxProfiles = value.maxProfiles;
    return maxProfiles !== undefined && maxProfiles !== null && maxProfiles !== '' && maxProfiles >= 2;
  } else if (configurationType === 'GooglePlay') {
    return true; // No required fields
  }
  
  return false;
}

export function ApplicationConfigurationDialog({
  open,
  onOpenChange,
  value,
  configurationType: initialConfigurationType,
  onSave,
}: ApplicationConfigurationDialogProps) {
  const [configurationType, setConfigurationType] = useState<ConfigurationType | null>(initialConfigurationType);
  const [configValue, setConfigValue] = useState<any>(value);
  const [isSaving, setIsSaving] = useState(false);
  const { toast } = useToast();

  // Reset state when dialog opens or props change
  useEffect(() => {
    if (open) {
      setConfigurationType(initialConfigurationType);
      setConfigValue(value);
      setIsSaving(false);
    }
  }, [open, initialConfigurationType, value]);

  // Compute validation synchronously from current state
  const isValid = validateConfiguration(configurationType, configValue);

  const handleSave = async () => {
    // Double-check validation before saving
    if (!isValid) return;
    
    setIsSaving(true);
    try {
      await onSave({ type: configurationType, value: configValue });
      toast({
        title: 'Success',
        description: 'Configuration saved successfully',
      });
      // Close dialog after showing success toast
      onOpenChange(false);
    } catch (error) {
      console.error('Error saving configuration:', error);
      toast({
        title: 'Error',
        description: error instanceof Error ? error.message : 'Failed to save configuration',
        variant: 'destructive',
      });
      // Dialog stays open on error so user can retry
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancel = () => {
    if (!isSaving) {
      onOpenChange(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-3xl max-h-[80vh] flex flex-col" data-testid="dialog-application-configuration">
        <DialogHeader>
          <DialogTitle>Application Configuration</DialogTitle>
          <DialogDescription>
            Configure the application's platform-specific settings
          </DialogDescription>
        </DialogHeader>

        <div className="flex-1 overflow-y-auto py-4">
          <ApplicationConfigurationEditor
            value={configValue}
            onChange={setConfigValue}
            configurationType={configurationType as ConfigurationType}
            onChangeType={setConfigurationType}
          />
        </div>

        <DialogFooter>
          <Button 
            type="button" 
            variant="outline" 
            onClick={handleCancel} 
            disabled={isSaving}
            data-testid="button-cancel-configuration"
          >
            Cancel
          </Button>
          <Button 
            type="button" 
            onClick={handleSave} 
            disabled={!isValid || isSaving}
            data-testid="button-save-configuration"
          >
            {isSaving && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
            {isSaving ? 'Saving...' : 'Save Configuration'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
