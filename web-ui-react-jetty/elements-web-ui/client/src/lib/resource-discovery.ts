export interface ResourceDefinition {
  name: string;
  endpoint: string;
  icon: string;
  category: string;
  available: boolean;
}

const POTENTIAL_RESOURCES = [
  // Account Resources
  { name: 'Users', endpoint: '/api/rest/user', icon: 'Users', category: 'Accounts' },
  { name: 'Applications', endpoint: '/api/rest/application', icon: 'AppWindow', category: 'Accounts' },
  { name: 'Profiles', endpoint: '/api/rest/profile', icon: 'User', category: 'Accounts' },
  
  // Game Resources
  { name: 'Items', endpoint: '/api/rest/item', icon: 'Package', category: 'Game' },
  { name: 'Missions', endpoint: '/api/rest/mission', icon: 'Target', category: 'Game' },
  { name: 'Schedules', endpoint: '/api/rest/schedule', icon: 'Calendar', category: 'Game' },
  { name: 'Leaderboards', endpoint: '/api/rest/leaderboard', icon: 'Trophy', category: 'Game' },
  { name: 'Matchmaking', endpoint: '/api/rest/multi_match', icon: 'Users', category: 'Game' },
  
  // Auth
  { name: 'OIDC', endpoint: '/api/rest/auth_scheme/oidc', icon: 'Shield', category: 'Auth' },
  { name: 'OAuth2', endpoint: '/api/rest/auth_scheme/oauth2', icon: 'KeyRound', category: 'Auth' },
  { name: 'Custom', endpoint: '/api/rest/auth_scheme/custom', icon: 'Lock', category: 'Auth' },
  
  // Metadata
  { name: 'Metadata', endpoint: '/api/rest/metadata', icon: 'Database', category: 'Metadata' },
  { name: 'Metadata Spec', endpoint: '/api/rest/metadata_spec', icon: 'FileJson', category: 'Metadata' },
  
  // Web3
  { name: 'Smart Contracts', endpoint: '/api/rest/blockchain/omni/smart_contract', icon: 'FileCode', category: 'Web3' },
  { name: 'Vaults', endpoint: '/api/rest/blockchain/omni/vault', icon: 'Vault', category: 'Web3' },
  
  // Other
  { name: 'Large Objects', endpoint: '/api/rest/large_object', icon: 'HardDrive', category: 'Other' },
];

export async function discoverResources(): Promise<ResourceDefinition[]> {
  // Load visibility settings from localStorage
  const settingsJson = localStorage.getItem('admin-visibility-settings');
  const settings = settingsJson ? JSON.parse(settingsJson) : { resources: {}, categories: {} };
  
  // Return all resources as available without endpoint validation
  // This allows the application to work entirely offline
  // Filter based on visibility settings
  return POTENTIAL_RESOURCES
    .filter(resource => {
      // Check if category is hidden
      if (settings.categories[resource.category] === false) {
        return false;
      }
      // Check if resource is hidden
      if (settings.resources[resource.name] === false) {
        return false;
      }
      return true;
    })
    .map(resource => ({ ...resource, available: true }));
}
