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

// Validation: name must match pattern [^_]\w+ (no leading underscore, word characters only)
function validateConfigName(name: string): boolean {
  if (!name) return true; // Empty is valid (optional field)
  const pattern = /^[^_]\w*$/; // First char not underscore, then word chars (letters, digits, underscores)
  return pattern.test(name);
}

// Synchronous validation function to check if configuration is valid
function validateConfiguration(configurationType: ConfigurationType | null, value: any): boolean {
  if (!configurationType) {
    return false;
  }
  
  // Validate name field (common to all configurations)
  if (!validateConfigName(value.name)) {
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
    // jsonKey must be an object (not a string) if provided
    if (value.jsonKey && typeof value.jsonKey === 'string') {
      return false; // Invalid JSON - stored as string means parse failed
    }
    return true; // No required fields beyond name validation and valid jsonKey
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
    } catch (error: any) {
      console.error('Error saving configuration:', error);
      
      // Extract detailed error message
      let errorMessage = 'Failed to save configuration';
      if (error instanceof Error) {
        errorMessage = error.message;
      }
      
      // If there are additional details, append them
      if (error?.details) {
        const details = Array.isArray(error.details) 
          ? error.details.join(', ') 
          : typeof error.details === 'string' 
            ? error.details 
            : JSON.stringify(error.details);
        errorMessage += `: ${details}`;
      }
      
      toast({
        title: 'Error',
        description: errorMessage,
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
