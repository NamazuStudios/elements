import { Component, type ReactNode, type ErrorInfo } from 'react';

interface Props {
  pluginName: string;
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class PluginErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, info: ErrorInfo) {
    console.error(`[PluginErrorBoundary] Plugin "${this.props.pluginName}" threw an error:`, error, info);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="p-6">
          <h2 className="text-lg font-semibold text-destructive mb-2">Plugin Error</h2>
          <p className="text-muted-foreground mb-3">
            The plugin <strong>{this.props.pluginName}</strong> encountered an error and could not be rendered.
          </p>
          <pre className="text-xs text-destructive/80 bg-destructive/10 rounded p-3 whitespace-pre-wrap break-all">
            {this.state.error?.message}
          </pre>
        </div>
      );
    }
    return this.props.children;
  }
}
