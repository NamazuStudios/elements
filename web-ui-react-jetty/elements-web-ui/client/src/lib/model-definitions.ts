/**
 * Local Model Schema Definitions
 * 
 * This file contains all model schemas for Elements resources
 * to enable offline functionality without fetching from GitHub.
 */

import { ModelSchema } from './schema-parser';

export const MODEL_DEFINITIONS: Record<string, ModelSchema> = {
  // User models
  'user/User': {
    name: 'User',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'firstName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'lastName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'email', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'primaryPhoneNb', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'password', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'level', type: 'enum', required: false, enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'], isArray: false, isMap: false },
      { name: 'profiles', type: 'string', required: false, isArray: true, isMap: false },
    ],
  },
  'user/UserCreateRequest': {
    name: 'UserCreateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'firstName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'lastName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'email', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'primaryPhoneNb', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'password', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'confirmPassword', type: 'string', required: true, isArray: false, isMap: false, uiOnly: true },
      { name: 'level', type: 'enum', required: false, enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'], isArray: false, isMap: false },
    ],
  },
  'user/UserUpdateRequest': {
    name: 'UserUpdateRequest',
    fields: [
      { name: 'id', type: 'string', required: true, isArray: false, isMap: false, validationGroups: { update: 'notNull' } },
      { name: 'name', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'firstName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'lastName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'email', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'primaryPhoneNb', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'password', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'confirmPassword', type: 'string', required: false, isArray: false, isMap: false, uiOnly: true },
      { name: 'level', type: 'enum', required: false, enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'], isArray: false, isMap: false },
    ],
  },

  // Application models
  'application/Application': {
    name: 'Application',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'gitBranch', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'scriptRepoUrl', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'httpDocumentationUrl', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'httpDocumentationUiUrl', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'httpTunnelEndpointUrl', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'attributes', type: 'object', required: false, isArray: false, isMap: true },
      { name: 'applicationConfiguration', type: 'object', required: false, isArray: true, isMap: false },
    ],
  },
  'application/ApplicationCreateRequest': {
    name: 'ApplicationCreateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'gitBranch', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'scriptRepoUrl', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'attributes', type: 'object', required: false, isArray: false, isMap: true },
      { name: 'applicationConfiguration', type: 'object', required: false, isArray: true, isMap: false },
    ],
  },
  'application/ApplicationUpdateRequest': {
    name: 'ApplicationUpdateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'attributes', type: 'object', required: false, isArray: false, isMap: true },
    ],
  },

  // Application Configuration models
  'application/FacebookApplicationConfiguration': {
    name: 'FacebookApplicationConfiguration',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'applicationId', type: 'string', required: true, isArray: false, isMap: false, description: 'The AppID as it appears in the Facebook Developer Console' },
      { name: 'applicationSecret', type: 'string', required: true, isArray: false, isMap: false, description: 'The App Secret as it appears in the Facebook Developer Console' },
      { name: 'builtinApplicationPermissions', type: 'string', required: false, isArray: true, isMap: false, description: 'The set of built-in permissions connected clients will need to request' },
    ],
  },
  'application/FirebaseApplicationConfiguration': {
    name: 'FirebaseApplicationConfiguration',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'projectId', type: 'string', required: true, isArray: false, isMap: false, description: 'The Firebase project ID' },
      { name: 'serviceAccountCredentials', type: 'string', required: true, isArray: false, isMap: false, description: 'The contents of the serviceAccountCredentials.json file' },
    ],
  },
  'application/GooglePlayApplicationConfiguration': {
    name: 'GooglePlayApplicationConfiguration',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'applicationId', type: 'string', required: false, isArray: false, isMap: false, description: 'Application ID as defined in Google Play (com.mycompany.app)' },
      { name: 'jsonKey', type: 'object', required: false, isArray: false, isMap: true, description: 'Google Play service account JSON key' },
      { name: 'productBundles', type: 'object', required: false, isArray: true, isMap: false, description: 'Product bundles that may be rewarded upon successful IAP transactions' },
    ],
  },
  'application/IosApplicationConfiguration': {
    name: 'IosApplicationConfiguration',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'applicationId', type: 'string', required: true, isArray: false, isMap: false, description: 'Application ID as defined in the AppStore (com.mycompany.app)' },
      { name: 'productBundles', type: 'object', required: false, isArray: true, isMap: false, description: 'Product bundles that may be rewarded upon successful IAP transactions' },
    ],
  },
  'application/MatchmakingApplicationConfiguration': {
    name: 'MatchmakingApplicationConfiguration',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'success', type: 'object', required: false, isArray: false, isMap: false, description: 'The callback definition for when a successful match is made' },
      { name: 'matchmaker', type: 'object', required: false, isArray: false, isMap: false, description: 'The matchmaker service reference to use' },
      { name: 'maxProfiles', type: 'number', required: false, isArray: false, isMap: false, description: 'The maximum number of profiles that can be matched in a single match' },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false, description: 'Metadata for this matchmaking configuration, copied to the match when created' },
    ],
  },

  // Supporting types for Application Configurations
  'application/ProductBundle': {
    name: 'ProductBundle',
    fields: [
      { name: 'productId', type: 'string', required: true, isArray: false, isMap: false, description: 'Platform-specific unique SKU/product identifier' },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false, description: 'Title of the product bundle to display to end users' },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false, description: 'Description of the product bundle to display to end users' },
      { name: 'productBundleRewards', type: 'object', required: true, isArray: true, isMap: false, description: 'List of rewards issued to the user upon purchase' },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false, description: 'Application-specific metadata' },
      { name: 'display', type: 'boolean', required: true, isArray: false, isMap: false, description: 'Whether to display this product bundle to end users' },
    ],
  },
  'application/ProductBundleReward': {
    name: 'ProductBundleReward',
    fields: [
      { name: 'itemId', type: 'string', required: true, isArray: false, isMap: false, description: 'The ID of the item to be rewarded' },
      { name: 'quantity', type: 'number', required: true, isArray: false, isMap: false, description: 'The quantity of the item to be rewarded' },
    ],
  },
  'application/CallbackDefinition': {
    name: 'CallbackDefinition',
    fields: [
      { name: 'method', type: 'string', required: true, isArray: false, isMap: false, description: 'The method to invoke' },
      { name: 'service', type: 'object', required: true, isArray: false, isMap: false, description: 'The module/service to invoke' },
    ],
  },
  'application/ElementServiceReference': {
    name: 'ElementServiceReference',
    fields: [
      { name: 'elementName', type: 'string', required: true, isArray: false, isMap: false, description: 'The name of the Element to reference' },
      { name: 'serviceType', type: 'string', required: false, isArray: false, isMap: false, description: 'The type of the service within the Element' },
      { name: 'serviceName', type: 'string', required: false, isArray: false, isMap: false, description: 'The name of the service within the Element' },
    ],
  },

  // Profile models
  'profile/Profile': {
    name: 'Profile',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'user', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'application', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'imageUrl', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false },
    ],
  },
  'profile/ProfileCreateRequest': {
    name: 'ProfileCreateRequest',
    fields: [
      { name: 'user', type: 'object', required: true, isArray: false, isMap: false, description: 'User reference with id' },
      { name: 'application', type: 'object', required: true, isArray: false, isMap: false, description: 'Application reference with id' },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'imageUrl', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false },
    ],
  },
  'profile/ProfileUpdateRequest': {
    name: 'ProfileUpdateRequest',
    fields: [
      { name: 'id', type: 'string', required: true, isArray: false, isMap: false, validationGroups: { update: 'notNull' } },
      { name: 'user', type: 'object', required: false, isArray: false, isMap: false, description: 'User reference with id' },
      { name: 'application', type: 'object', required: false, isArray: false, isMap: false, description: 'Application reference with id' },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'imageUrl', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false },
    ],
  },

  // Item models
  'goods/Item': {
    name: 'Item',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'category', type: 'enum', required: true, enumValues: ['FUNGIBLE', 'DISTINCT'], isArray: false, isMap: false },
      { name: 'tags', type: 'string', required: false, isArray: true, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false },
      { name: 'metadataSpec', type: 'string', required: false, isArray: false, isMap: false },
    ],
  },
  'goods/ItemCreateRequest': {
    name: 'ItemCreateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'category', type: 'enum', required: true, enumValues: ['FUNGIBLE', 'DISTINCT'], isArray: false, isMap: false },
      { name: 'tags', type: 'string', required: false, isArray: true, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false },
      { name: 'metadataSpec', type: 'string', required: false, isArray: false, isMap: false },
    ],
  },

  // Mission models
  'mission/Mission': {
    name: 'Mission',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'steps', type: 'object', required: false, isArray: true, isMap: false },
    ],
  },
  'mission/MissionCreateRequest': {
    name: 'MissionCreateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'steps', type: 'object', required: false, isArray: true, isMap: false },
    ],
  },

  // Schedule models
  'mission/Schedule': {
    name: 'Schedule',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'description', type: 'string', required: false, isArray: false, isMap: false },
    ],
  },

  // ScheduleEvent models
  'mission/ScheduleEvent': {
    name: 'ScheduleEvent',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, validationGroups: { update: 'notNull' } },
      { name: 'begin', type: 'number', required: false, isArray: false, isMap: false },
      { name: 'end', type: 'number', required: false, isArray: false, isMap: false },
      { name: 'schedule', type: 'object', required: false, isArray: false, isMap: false },
      { name: 'missions', type: 'object', required: true, isArray: true, isMap: false },
    ],
  },
  'mission/ScheduleEventCreateRequest': {
    name: 'ScheduleEventCreateRequest',
    fields: [
      { name: 'begin', type: 'number', required: true, isArray: false, isMap: false },
      { name: 'end', type: 'number', required: true, isArray: false, isMap: false },
      { name: 'missionNamesOrIds', type: 'string', required: true, isArray: true, isMap: false },
    ],
  },

  // Leaderboard models
  'leaderboard/Leaderboard': {
    name: 'Leaderboard',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false, description: 'The name of the leaderboard. This must be unique across all leaderboards.' },
      { name: 'title', type: 'string', required: true, isArray: false, isMap: false, description: 'The user-presentable name or title for the leaderboard.' },
      { name: 'scoreStrategyType', type: 'enum', required: true, enumValues: ['OVERWRITE_IF_GREATER', 'ACCUMULATE'], isArray: false, isMap: false, description: 'The score strategy for the leaderboard. Current options are OVERWRITE_IF_GREATER and ACCUMULATE.' },
      { name: 'scoreUnits', type: 'string', required: true, isArray: false, isMap: false, description: 'The units-of measure for the score type of the leaderboard.' },
      { name: 'timeStrategyType', type: 'enum', required: true, enumValues: ['ALL_TIME', 'EPOCHAL'], isArray: false, isMap: false, description: 'The time strategy for the leaderboard. Current options are ALL_TIME and EPOCHAL.' },
      { name: 'firstEpochTimestamp', type: 'number', required: false, isArray: false, isMap: false, validationGroups: { update: 'null' }, conditionalVisibility: { dependsOn: 'timeStrategyType', showWhen: 'EPOCHAL' }, description: 'The time at which the leaderboard epoch intervals should begin. If null, then the leaderboard is all-time and not epochal.' },
      { name: 'epochInterval', type: 'number', required: false, isArray: false, isMap: false, validationGroups: { update: 'null' }, conditionalVisibility: { dependsOn: 'timeStrategyType', showWhen: 'EPOCHAL' }, description: 'The duration for a leaderboard epoch interval. If null, then the leaderboard is all-time and not epochal.' },
    ],
  },

  // Auth Scheme models
  'auth/OidcAuthScheme': {
    name: 'OidcAuthScheme',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, validationGroups: { insert: 'null', create: 'null', update: 'notNull' } },
      { name: 'name', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'issuer', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'keys', type: 'object', required: true, isArray: true, isMap: false },
      { name: 'keysUrl', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'mediaType', type: 'string', required: false, isArray: false, isMap: false },
    ],
  },
  'auth/OAuth2AuthScheme': {
    name: 'OAuth2AuthScheme',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, validationGroups: { insert: 'null', create: 'null', update: 'notNull' } },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'validationUrl', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'headers', type: 'object', required: false, isArray: true, isMap: false },
      { name: 'params', type: 'object', required: false, isArray: true, isMap: false },
      { name: 'responseIdMapping', type: 'string', required: false, isArray: false, isMap: false },
    ],
  },
  'auth/CustomAuthScheme': {
    name: 'CustomAuthScheme',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, validationGroups: { insert: 'null', create: 'null', update: 'notNull' } },
      { name: 'audience', type: 'string', required: true, isArray: false, isMap: false, description: 'The JWT audience for the scheme. Must be unique.' },
      { name: 'publicKey', type: 'string', required: false, isArray: false, isMap: false, description: 'The Base64 encoded public key. Optional - if not provided, the system will generate one. Must be valid Base64 format if provided.' },
      { name: 'algorithm', type: 'enum', required: true, enumValues: ['RSA_256', 'RSA_384', 'RSA_512'], isArray: false, isMap: false, description: 'The signing algorithm for the auth scheme.' },
      { name: 'userLevel', type: 'enum', required: true, enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'], isArray: false, isMap: false, description: 'The highest permitted user level this particular scheme will authorize.' },
      { name: 'tags', type: 'string', required: true, isArray: true, isMap: false, description: 'A list of tags used to index the auth scheme.' },
      { name: 'allowedIssuers', type: 'string', required: true, isArray: true, isMap: false, description: 'The list of issuers allowed to use this scheme.' },
    ],
  },

  // Blockchain models
  'blockchain/SmartContract': {
    name: 'SmartContract',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false, pattern: 'NO_WHITE_SPACE', description: 'The unique symbolic name of the smart contract.' },
      { name: 'displayName', type: 'string', required: true, isArray: false, isMap: false, description: 'The name given to this contract for display purposes.' },
      { name: 'addresses', type: 'object', required: true, isArray: false, isMap: true, description: 'The address of the contract from the blockchain. Map of BlockchainNetwork to SmartContractAddress.' },
      { name: 'vaultId', type: 'string', required: true, isArray: false, isMap: false, description: 'The Elements database id of the vault containing the default account to be used for contract related requests.' },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: true, description: 'Any metadata for this contract.' },
    ],
  },
  'blockchain/SmartContractCreateRequest': {
    name: 'SmartContractCreateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false, pattern: 'NO_WHITE_SPACE', description: 'The unique symbolic name of the smart contract.' },
      { name: 'displayName', type: 'string', required: true, isArray: false, isMap: false, description: 'The name given to this contract for display purposes.' },
      { name: 'addresses', type: 'object', required: true, isArray: false, isMap: true, description: 'The address of the contract from the blockchain. Map of BlockchainNetwork to SmartContractAddress.' },
      { name: 'vaultId', type: 'string', required: true, isArray: false, isMap: false, description: 'The Elements database id of the vault containing the default account to be used for contract related requests.' },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: true, description: 'Any metadata for this contract.' },
    ],
  },
  'blockchain/SmartContractUpdateRequest': {
    name: 'SmartContractUpdateRequest',
    fields: [
      { name: 'id', type: 'string', required: true, isArray: false, isMap: false, validationGroups: { update: 'notNull' } },
      { name: 'name', type: 'string', required: false, isArray: false, isMap: false, pattern: 'NO_WHITE_SPACE', description: 'The unique symbolic name of the smart contract.' },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false, description: 'The name given to this contract for display purposes.' },
      { name: 'addresses', type: 'object', required: false, isArray: false, isMap: true, description: 'The address of the contract from the blockchain. Map of BlockchainNetwork to SmartContractAddress.' },
      { name: 'vaultId', type: 'string', required: false, isArray: false, isMap: false, description: 'The Elements database id of the vault containing the default account to be used for contract related requests.' },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: true, description: 'Any metadata for this contract.' },
    ],
  },
  'blockchain/Vault': {
    name: 'Vault',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'userId', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'algorithm', type: 'enum', required: false, enumValues: ['RSA_256', 'RSA_384', 'RSA_512'], isArray: false, isMap: false },
      { name: 'vaultKey', type: 'object', required: false, isArray: false, isMap: false, readOnly: true, description: 'The vault key information including encryption status and keys' },
    ],
  },
  'blockchain/VaultCreateRequest': {
    name: 'VaultCreateRequest',
    fields: [
      { name: 'passphrase', type: 'string', required: false, isArray: false, isMap: false, description: 'Optional passphrase for encrypting the vault. If provided, the vault will be encrypted.' },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false, description: 'Display name for the vault' },
      { name: 'userId', type: 'string', required: false, isArray: false, isMap: false, description: 'User ID to associate with this vault' },
      { name: 'algorithm', type: 'enum', required: true, enumValues: ['RSA_256', 'RSA_384', 'RSA_512'], isArray: false, isMap: false, description: 'Encryption algorithm for the vault' },
    ],
  },
  'blockchain/VaultUpdateRequest': {
    name: 'VaultUpdateRequest',
    fields: [
      { name: 'id', type: 'string', required: true, isArray: false, isMap: false, validationGroups: { update: 'notNull' } },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'passphrase', type: 'string', required: false, isArray: false, isMap: false, description: 'New passphrase for the vault' },
    ],
  },

  // Inventory models
  'inventory/AdvancedInventory': {
    name: 'AdvancedInventory',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'profile', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'item', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'quantity', type: 'number', required: true, isArray: false, isMap: false },
    ],
  },
  'inventory/DistinctInventory': {
    name: 'DistinctInventory',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'profile', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'item', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false },
    ],
  },

  // Metadata models
  'metadata/Metadata': {
    name: 'Metadata',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'metadataSpec', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'accessLevel', type: 'enum', required: true, enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'], isArray: false, isMap: false, description: 'The minimum level of access required to view this metadata' },
    ],
  },
  'metadata/MetadataCreateRequest': {
    name: 'MetadataCreateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'metadataSpec', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'accessLevel', type: 'enum', required: true, enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'], isArray: false, isMap: false, description: 'The minimum level of access required to view this metadata' },
    ],
  },
  'metadata/MetadataUpdateRequest': {
    name: 'MetadataUpdateRequest',
    fields: [
      // Note: name field is NOT included in update - it cannot be updated
      { name: 'metadataSpec', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: false },
      { name: 'accessLevel', type: 'enum', required: false, enumValues: ['UNPRIVILEGED', 'USER', 'SUPERUSER'], isArray: false, isMap: false, description: 'The minimum level of access required to view this metadata' },
    ],
  },

  // MetadataSpec models
  'schema/MetadataSpec': {
    name: 'MetadataSpec',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'type', type: 'enum', enumValues: ['STRING', 'NUMBER', 'BOOLEAN', 'OBJECT'], required: true, isArray: false, isMap: false },
      { name: 'properties', type: 'object', required: true, isArray: true, isMap: false },
    ],
  },
  'schema/MetadataSpecCreateRequest': {
    name: 'MetadataSpecCreateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'type', type: 'enum', enumValues: ['STRING', 'NUMBER', 'BOOLEAN', 'OBJECT'], required: true, isArray: false, isMap: false },
      { name: 'properties', type: 'object', required: true, isArray: true, isMap: false },
    ],
  },
  'schema/MetadataSpecUpdateRequest': {
    name: 'MetadataSpecUpdateRequest',
    fields: [
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'type', type: 'enum', enumValues: ['STRING', 'NUMBER', 'BOOLEAN', 'OBJECT'], required: true, isArray: false, isMap: false },
      { name: 'properties', type: 'object', required: true, isArray: true, isMap: false },
    ],
  },

  // Matchmaking models
  'matchmaking/Matchmaking': {
    name: 'Matchmaking',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'name', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'displayName', type: 'string', required: false, isArray: false, isMap: false },
    ],
  },

  // Notification models
  'notification/Notification': {
    name: 'Notification',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'title', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'message', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'type', type: 'string', required: false, isArray: false, isMap: false },
    ],
  },

  // Setting models
  'setting/Setting': {
    name: 'Setting',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false },
      { name: 'key', type: 'string', required: true, isArray: false, isMap: false },
      { name: 'value', type: 'string', required: true, isArray: false, isMap: false },
    ],
  },

  // MultiMatch models
  'match/MultiMatch': {
    name: 'MultiMatch',
    fields: [
      { name: 'id', type: 'string', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'status', type: 'enum', required: true, enumValues: ['OPEN', 'FULL', 'CLOSED', 'ENDED'], isArray: false, isMap: false },
      { name: 'configuration', type: 'object', required: true, isArray: false, isMap: false },
      { name: 'metadata', type: 'object', required: false, isArray: false, isMap: true },
      { name: 'count', type: 'number', required: false, isArray: false, isMap: false, readOnly: true },
      { name: 'expiry', type: 'number', required: false, isArray: false, isMap: false },
      { name: 'created', type: 'number', required: false, isArray: false, isMap: false, readOnly: true },
    ],
  },
};

/**
 * Get a local model schema by path and name
 */
export function getLocalModel(resourcePath: string, modelName: string): ModelSchema | null {
  const key = `${resourcePath}/${modelName}`;
  return MODEL_DEFINITIONS[key] || null;
}
