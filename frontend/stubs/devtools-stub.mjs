// Complete stub for Vue DevTools in production
// This prevents SSR errors when devtools are accidentally included

// Create a no-op function factory
const noop = () => {};
const noopObj = {};
const noopProxy = new Proxy({}, { get: () => noop });

// Export all commonly used DevTools functions as no-ops
export default {};
export const setupDevToolsPlugin = noop;
export const setupDevtoolsPlugin = noop;
export const devtools = noopProxy;
export const onDevToolsClientConnected = noop;
export const onDevToolsConnected = noop;
export const createDevToolsApi = () => noopProxy;
export const registerDevToolsPlugin = noop;
export const createComponentsDevToolsPlugin = noop;
export const initDevTools = noop;
export const toggleComponentHighLighter = noop;
export const inspectComponentHighLighter = noop;
export const cancelInspectComponentHighLighter = noop;
export const highlight = noop;
export const unhighlight = noop;
export const getComponentInspector = () => noopProxy;
export const devtoolsContext = noopProxy;
export const devtoolsState = noopProxy;
export const devtoolsAppRecords = noopProxy;
export const devtoolsPluginBuffer = [];
export const activeAppRecord = noopProxy;

// Export all other functions as no-ops using a Proxy
export const addCustomCommand = noop;
export const addCustomTab = noop;
export const addDevToolsAppRecord = noop;
export const addDevToolsPluginToBuffer = noop;
export const addInspector = noop;
export const callConnectedUpdatedHook = noop;
export const callDevToolsPluginSetupFn = noop;
export const callInspectorUpdatedHook = noop;
export const callStateUpdatedHook = noop;
export const createDevToolsCtxHooks = () => noopProxy;
export const createRpcClient = () => noopProxy;
export const createRpcProxy = () => noopProxy;
export const createRpcServer = () => noopProxy;
export const getActiveInspectors = () => [];
export const getDevToolsEnv = () => noopProxy;
export const getExtensionClientContext = () => noopProxy;
export const getInspector = () => noopProxy;
export const getInspectorActions = () => [];
export const getInspectorInfo = () => noopProxy;
export const getInspectorNodeActions = () => [];
export const openInEditor = noop;
export const removeCustomCommand = noop;
export const removeDevToolsAppRecord = noop;
export const resetDevToolsState = noop;
export const scrollToComponent = noop;
export const setActiveAppRecord = noop;
export const setActiveAppRecordId = noop;
export const setDevToolsEnv = noop;
export const toggleClientConnected = noop;
export const updateDevToolsClientDetected = noop;
export const updateDevToolsState = noop;
