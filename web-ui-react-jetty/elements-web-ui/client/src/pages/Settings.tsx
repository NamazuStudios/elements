import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useToast } from '@/hooks/use-toast';
import { Settings as SettingsIcon, Eye, EyeOff } from 'lucide-react';
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from '@/components/ui/accordion';

interface VisibilitySettings {
  resources: Record<string, boolean>;
  categories: Record<string, boolean>;
}

interface AppSettings {
  visibility: VisibilitySettings;
  resultsPerPage: number;
}

const RESOURCE_CATEGORIES = [
  'Core',
  'Game',
  'Auth',
  'Metadata',
  'Web3',
  'Other',
];

const RESOURCES_BY_CATEGORY: Record<string, string[]> = {
  Core: ['Users', 'Applications', 'Profiles'],
  Game: ['Items', 'Missions', 'Schedules', 'Leaderboards', 'Matchmaking'],
  Auth: ['OIDC', 'OAuth2', 'Custom'],
  Metadata: ['Metadata', 'Metadata Spec'],
  Web3: ['Smart Contracts', 'Vaults'],
  Other: ['Large Objects'],
};

export default function Settings() {
  const { toast } = useToast();
  const [settings, setSettings] = useState<AppSettings>({
    visibility: {
      resources: {},
      categories: {},
    },
    resultsPerPage: 20,
  });

  useEffect(() => {
    // Load settings from localStorage
    const savedVisibility = localStorage.getItem('admin-visibility-settings');
    const savedResultsPerPage = localStorage.getItem('admin-results-per-page');
    
    setSettings({
      visibility: savedVisibility ? JSON.parse(savedVisibility) : { resources: {}, categories: {} },
      resultsPerPage: savedResultsPerPage ? parseInt(savedResultsPerPage, 10) : 20,
    });
  }, []);

  const saveSettings = () => {
    localStorage.setItem('admin-visibility-settings', JSON.stringify(settings.visibility));
    localStorage.setItem('admin-results-per-page', settings.resultsPerPage.toString());
    toast({
      title: 'Settings saved',
      description: 'Your preferences have been saved. Refresh the page to apply changes.',
    });
  };

  const toggleCategory = (category: string) => {
    setSettings(prev => ({
      ...prev,
      visibility: {
        ...prev.visibility,
        categories: {
          ...prev.visibility.categories,
          [category]: !prev.visibility.categories[category],
        },
      },
    }));
  };

  const toggleResource = (resource: string) => {
    setSettings(prev => ({
      ...prev,
      visibility: {
        ...prev.visibility,
        resources: {
          ...prev.visibility.resources,
          [resource]: !prev.visibility.resources[resource],
        },
      },
    }));
  };

  const resetSettings = () => {
    setSettings({ 
      visibility: { resources: {}, categories: {} },
      resultsPerPage: 20,
    });
    localStorage.removeItem('admin-visibility-settings');
    localStorage.removeItem('admin-results-per-page');
    toast({
      title: 'Settings reset',
      description: 'All settings have been reset to defaults. Refresh the page to apply changes.',
    });
  };

  const isCategoryHidden = (category: string) => settings.visibility.categories[category] === false;
  const isResourceHidden = (resource: string) => settings.visibility.resources[resource] === false;

  return (
    <div className="container mx-auto p-6 max-w-4xl" data-testid="page-settings">
      <div className="flex items-center gap-3 mb-6">
        <SettingsIcon className="h-8 w-8" />
        <div>
          <h1 className="text-3xl font-bold">Settings</h1>
          <p className="text-muted-foreground">Configure visibility and preferences for the admin interface</p>
        </div>
      </div>

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>General Settings</CardTitle>
            <CardDescription>
              Configure general preferences for the admin interface
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-3">
              <Label htmlFor="results-per-page">Results per page</Label>
              <Input
                id="results-per-page"
                type="number"
                min="5"
                max="100"
                value={settings.resultsPerPage}
                onChange={(e) => setSettings(prev => ({ ...prev, resultsPerPage: parseInt(e.target.value, 10) || 20 }))}
                data-testid="input-results-per-page"
                className="max-w-xs"
              />
              <p className="text-sm text-muted-foreground">
                Number of items to display per page in all resource lists
              </p>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Advanced Settings</CardTitle>
            <CardDescription>
              Additional configuration options
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Accordion type="single" collapsible className="w-full">
              <AccordionItem value="resource-visibility">
                <AccordionTrigger data-testid="accordion-resource-visibility">
                  Resource Visibility
                </AccordionTrigger>
                <AccordionContent>
                  <div className="space-y-4 pt-2">
                    <p className="text-sm text-muted-foreground mb-4">
                      Control which resource groups and individual resources are visible in the sidebar. 
                      Changes require a page refresh to take effect.
                    </p>
                    {RESOURCE_CATEGORIES.map((category) => (
                      <div key={category} className="space-y-3">
                        <div className="flex items-center justify-between p-3 bg-muted/50 rounded-lg">
                          <div className="flex items-center gap-2">
                            {isCategoryHidden(category) ? (
                              <EyeOff className="h-4 w-4 text-muted-foreground" />
                            ) : (
                              <Eye className="h-4 w-4 text-muted-foreground" />
                            )}
                            <Label htmlFor={`category-${category}`} className="font-semibold cursor-pointer">
                              {category}
                            </Label>
                          </div>
                          <Switch
                            id={`category-${category}`}
                            checked={!isCategoryHidden(category)}
                            onCheckedChange={() => toggleCategory(category)}
                            data-testid={`toggle-category-${category}`}
                          />
                        </div>
                        
                        <div className="ml-6 space-y-2">
                          {RESOURCES_BY_CATEGORY[category]?.map((resource) => (
                            <div key={resource} className="flex items-center justify-between py-2">
                              <div className="flex items-center gap-2">
                                {isResourceHidden(resource) ? (
                                  <EyeOff className="h-3 w-3 text-muted-foreground" />
                                ) : (
                                  <Eye className="h-3 w-3 text-muted-foreground" />
                                )}
                                <Label htmlFor={`resource-${resource}`} className="text-sm cursor-pointer">
                                  {resource}
                                </Label>
                              </div>
                              <Switch
                                id={`resource-${resource}`}
                                checked={!isResourceHidden(resource)}
                                onCheckedChange={() => toggleResource(resource)}
                                disabled={isCategoryHidden(category)}
                                data-testid={`toggle-resource-${resource}`}
                              />
                            </div>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          </CardContent>
        </Card>

        <div className="flex gap-3">
          <Button onClick={saveSettings} data-testid="button-save-settings">
            Save Settings
          </Button>
          <Button variant="outline" onClick={resetSettings} data-testid="button-reset-settings">
            Reset to Defaults
          </Button>
        </div>

        <Card className="border-muted-foreground/20">
          <CardHeader>
            <CardTitle className="text-sm">Note</CardTitle>
          </CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            <p>Settings are stored locally in your browser. After saving changes, refresh the page to see updated sidebar navigation.</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
