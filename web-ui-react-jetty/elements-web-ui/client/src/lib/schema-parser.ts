/**
 * Java SDK Model Parser
 * 
 * Parses Java model files from the Elements SDK to extract field metadata
 * including types, validation constraints, and schema descriptions.
 */

export interface FieldSchema {
  name: string;
  type: string;
  required: boolean;
  pattern?: string;
  description?: string;
  enumValues?: string[];
  isArray?: boolean;
  isMap?: boolean;
  readOnly?: boolean;
  uiOnly?: boolean;
  validationGroups?: {
    insert?: boolean | 'null' | 'notNull';
    update?: boolean | 'null' | 'notNull';
    read?: boolean | 'null' | 'notNull';
    create?: boolean | 'null' | 'notNull';
  };
  conditionalVisibility?: {
    dependsOn: string;
    showWhen: string | string[];
  };
}

export interface ModelSchema {
  name: string;
  fields: FieldSchema[];
}

/**
 * Parse a Java model file to extract field schemas
 */
export function parseJavaModel(javaCode: string): ModelSchema {
  const fields: FieldSchema[] = [];
  const className = extractClassName(javaCode);
  
  // Extract field declarations with their annotations
  const fieldPattern = /(?:@[^\n]+\n)*\s*private\s+(\w+(?:<[^>]+>)?(?:\[\])?)\s+(\w+);/g;
  const matches = Array.from(javaCode.matchAll(fieldPattern));
  
  for (const match of matches) {
    const fullMatch = match[0];
    const fieldType = match[1];
    const fieldName = match[2];
    
    // Parse annotations for this field
    const annotations = extractAnnotations(fullMatch);
    
    // Extract validation groups first
    const validationGroups = extractValidationGroups(annotations);
    
    // Determine if field is required (only if @NotNull has no groups at all)
    const notNullAnnotation = annotations.find(a => a.name === 'NotNull');
    const required = !!(notNullAnnotation && !notNullAnnotation.params.groups);
    
    // Extract pattern constraint
    const patternAnnotation = annotations.find(a => a.name === 'Pattern');
    const pattern = patternAnnotation?.params.regexp;
    
    // Extract schema description
    const schemaAnnotation = annotations.find(a => a.name === 'Schema');
    const description = schemaAnnotation?.params.description;
    
    // Determine field type details
    const isArray = fieldType.includes('[]') || fieldType.includes('List<');
    const isMap = fieldType.includes('Map<');
    
    // Map Java types to simple types
    let simpleType = mapJavaType(fieldType);
    
    // Check if this is an enum by looking for nested enum definition
    const enumValues = extractEnumValues(javaCode, fieldType);
    if (enumValues) {
      simpleType = 'enum';
    }
    
    fields.push({
      name: fieldName,
      type: simpleType,
      required,
      pattern,
      description,
      enumValues,
      isArray,
      isMap,
      validationGroups,
    });
  }
  
  return {
    name: className,
    fields,
  };
}

interface Annotation {
  name: string;
  params: Record<string, any>;
}

function extractClassName(javaCode: string): string {
  const match = javaCode.match(/(?:public\s+)?class\s+(\w+)/);
  return match ? match[1] : 'Unknown';
}

function extractAnnotations(fieldCode: string): Annotation[] {
  const annotations: Annotation[] = [];
  const annotationPattern = /@(\w+)(?:\(([^)]+)\))?/g;
  
  const matches = Array.from(fieldCode.matchAll(annotationPattern));
  for (const match of matches) {
    const name = match[1];
    const paramsStr = match[2];
    const params: Record<string, any> = {};
    
    if (paramsStr) {
      // Parse simple key=value pairs
      const paramPattern = /(\w+)\s*=\s*(?:"([^"]*)"|([^,\s]+)|{([^}]+)})/g;
      const paramMatches = Array.from(paramsStr.matchAll(paramPattern));
      
      for (const paramMatch of paramMatches) {
        const key = paramMatch[1];
        let value: string | string[] = paramMatch[2] || paramMatch[3] || paramMatch[4];
        
        // Handle array values
        if (paramMatch[4]) {
          value = paramMatch[4].split(',').map((v: string) => v.trim());
        }
        
        params[key] = value;
      }
    }
    
    annotations.push({ name, params });
  }
  
  return annotations;
}

function hasValidationGroup(annotation: Annotation, group: string): boolean {
  if (!annotation.params.groups) return false;
  const groups = Array.isArray(annotation.params.groups) 
    ? annotation.params.groups 
    : [annotation.params.groups];
  return groups.some(g => g.includes(group));
}

function extractValidationGroups(annotations: Annotation[]) {
  const groups: FieldSchema['validationGroups'] = {};
  
  for (const annotation of annotations) {
    if (annotation.name === 'Null' && annotation.params.groups) {
      const groupsStr = annotation.params.groups;
      if (groupsStr.includes('Insert')) groups.insert = 'null';
      if (groupsStr.includes('Create')) groups.create = 'null';
    }
    
    if (annotation.name === 'NotNull' && annotation.params.groups) {
      const groupsStr = annotation.params.groups;
      if (groupsStr.includes('Update')) groups.update = 'notNull';
      if (groupsStr.includes('Read')) groups.read = 'notNull';
      if (groupsStr.includes('Insert')) groups.insert = 'notNull';
      if (groupsStr.includes('Create')) groups.create = 'notNull';
    }
  }
  
  return Object.keys(groups).length > 0 ? groups : undefined;
}

function mapJavaType(javaType: string): string {
  const cleanType = javaType.replace(/List<|Map<|>|\[\]/g, '').trim();
  
  if (cleanType === 'String') return 'string';
  if (cleanType === 'Integer' || cleanType === 'int' || cleanType === 'Long' || cleanType === 'long') {
    return 'number';
  }
  if (cleanType === 'Boolean' || cleanType === 'boolean') return 'boolean';
  if (cleanType === 'Double' || cleanType === 'double' || cleanType === 'Float' || cleanType === 'float') {
    return 'number';
  }
  if (cleanType.includes('Map')) return 'object';
  
  // Default to object for custom types
  return 'object';
}

function extractEnumValues(javaCode: string, typeName: string): string[] | undefined {
  // Remove generic markers
  const cleanType = typeName.replace(/List<|Map<|>|\[\]/g, '').trim();
  
  // Check for known external enum types
  const externalEnums: Record<string, string[]> = {
    'ItemCategory': ['FUNGIBLE', 'DISTINCT'],
    // Add more external enums here as needed
  };
  
  if (externalEnums[cleanType]) {
    return externalEnums[cleanType];
  }
  
  // Look for enum definition within the class
  const enumPattern = new RegExp(`enum\\s+${cleanType}\\s*\\{([^}]+)\\}`, 's');
  const match = javaCode.match(enumPattern);
  
  if (match) {
    const enumBody = match[1];
    // Extract enum constants (words before commas or semicolons)
    const values = enumBody
      .split(/[,;]/)
      .map(v => v.trim())
      .filter(v => v && /^[A-Z_]+$/.test(v));
    
    return values.length > 0 ? values : undefined;
  }
  
  return undefined;
}

/**
 * Get a local model schema (offline-capable)
 */
export async function fetchJavaModel(
  resourcePath: string,
  modelName: string
): Promise<ModelSchema | null> {
  // Use local model definitions instead of fetching from GitHub
  // This enables offline functionality
  const { getLocalModel } = await import('./model-definitions');
  return getLocalModel(resourcePath, modelName);
}

/**
 * Get the appropriate schema for a resource operation
 * Tries CreateRequest/UpdateRequest first, falls back to base model
 */
export async function getResourceSchema(
  resourceName: string,
  operation: 'create' | 'update'
): Promise<ModelSchema | null> {
  // Map resource names to Java package paths
  const resourcePathMap: Record<string, string> = {
    'Users': 'user',
    'Applications': 'application',
    'Profiles': 'profile',
    'Items': 'goods',
    'Missions': 'mission',
    'Schedules': 'mission',
    'Schedule Events': 'mission',
    'Leaderboards': 'leaderboard',
    'OIDC': 'auth',
    'OAuth2': 'auth',
    'Custom': 'auth',
    'Custom Auth': 'auth',
    'Smart Contracts': 'blockchain',
    'Vaults': 'blockchain',
    'Fungible': 'inventory',
    'Distinct': 'inventory',
    'Advanced Inventory': 'inventory',
    'Distinct Inventory': 'inventory',
    'Metadata': 'metadata',
    'Metadata Spec': 'schema',
    'Matchmaking': 'matchmaking',
    'Notifications': 'notification',
    'Settings': 'setting',
  };
  
  // Map resource names to Java model names
  const modelNameMap: Record<string, string> = {
    'Users': 'User',
    'Applications': 'Application',
    'Profiles': 'Profile',
    'Items': 'Item',
    'Missions': 'Mission',
    'Schedules': 'Schedule',
    'Schedule Events': 'ScheduleEvent',
    'Leaderboards': 'Leaderboard',
    'OIDC': 'OidcAuthScheme',
    'OAuth2': 'OAuth2AuthScheme',
    'Custom': 'CustomAuthScheme',
    'Custom Auth': 'CustomAuthScheme',
    'Smart Contracts': 'SmartContract',
    'Vaults': 'Vault',
    'Fungible': 'AdvancedInventory',
    'Distinct': 'DistinctInventory',
    'Advanced Inventory': 'AdvancedInventory',
    'Distinct Inventory': 'DistinctInventory',
    'Metadata': 'Metadata',
    'Metadata Spec': 'MetadataSpec',
    'Matchmaking': 'Matchmaking',
    'Notifications': 'Notification',
    'Settings': 'Setting',
  };
  
  const resourcePath = resourcePathMap[resourceName];
  const baseModelName = modelNameMap[resourceName];
  
  if (!resourcePath || !baseModelName) {
    console.warn(`Unknown resource: ${resourceName}`);
    return null;
  }
  
  // Try specific request model first
  const requestSuffix = operation === 'create' ? 'CreateRequest' : 'UpdateRequest';
  const requestModel = await fetchJavaModel(resourcePath, `${baseModelName}${requestSuffix}`);
  
  if (requestModel) {
    return requestModel;
  }
  
  // Fall back to base model
  return await fetchJavaModel(resourcePath, baseModelName);
}
