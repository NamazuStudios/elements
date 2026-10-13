export interface AdminUiState {
  location: string;
  openGroups: Record<string, boolean>;
}

export function adminUiStateKey(username: string | null): string {
  return `elements-admin-ui-state:${username ?? 'unknown'}`;
}

export function loadAdminUiState(username: string | null): AdminUiState {
  try {
    const raw = localStorage.getItem(adminUiStateKey(username));
    if (!raw) return { location: '/dashboard', openGroups: {} };
    const parsed = JSON.parse(raw) as Partial<AdminUiState>;
    return {
      location: typeof parsed.location === 'string' ? parsed.location : '/dashboard',
      openGroups: parsed.openGroups && typeof parsed.openGroups === 'object' ? parsed.openGroups : {},
    };
  } catch {
    return { location: '/dashboard', openGroups: {} };
  }
}

export function saveAdminUiState(username: string | null, state: AdminUiState): void {
  try {
    localStorage.setItem(adminUiStateKey(username), JSON.stringify(state));
  } catch {
    // Ignore storage errors (quota, disabled) rather than breaking navigation.
  }
}

export const CORE_ELEMENTS_GROUP_KEY = 'core-elements';
export const EXPLORER_GROUP_KEY = 'explorer';
export const ELEMENT_MANAGEMENT_GROUP_KEY = 'element-management';
export const ELEMENT_PLUGINS_GROUP_KEY = 'element-plugins';

export function categoryGroupKey(category: string): string {
  return `category:${category}`;
}

export function pluginGroupKey(qualifier: string): string {
  return `plugin-group:${qualifier}`;
}