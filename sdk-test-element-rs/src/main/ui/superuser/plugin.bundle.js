(function () {
  var React = window.React;
  if (!React) {
    console.warn('[sdk-test-element-rs] window.React not available, plugin not registered.');
    return;
  }

  function TestElementPlugin() {
    return React.createElement(
      'div',
      { className: 'p-6 max-w-2xl' },
      React.createElement(
        'h1',
        { className: 'text-2xl font-bold mb-2' },
        'Test Element'
      ),
      React.createElement(
        'p',
        { className: 'text-muted-foreground mb-4' },
        'This page is served from the sdk-test-element-rs Element\u2019s UI content directory.'
      ),
      React.createElement(
        'div',
        { className: 'rounded-lg border p-4 text-sm text-muted-foreground' },
        'The Element plugin system allows installed Elements to inject custom UI into the dashboard. ' +
        'This component was loaded dynamically at runtime from the Element\u2019s static content tree.'
      )
    );
  }

  if (window.__elementsPlugins) {
    window.__elementsPlugins.register('sdk-test-element-rs', TestElementPlugin);
    console.log('[sdk-test-element-rs] Plugin registered.');
  } else {
    console.warn('[sdk-test-element-rs] window.__elementsPlugins not available, plugin not registered.');
  }
})();
