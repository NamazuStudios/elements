import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ServerCog, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { queryClient } from '@/lib/queryClient';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';

interface HealthStatus {
  checksFailed: number;
  checksPerformed: number;
  overallHealth: number;
  problems: any[];
  instanceStatus?: any;
  databaseStatus?: any[];
  discoveryHealthStatus?: any;
  routingHealthStatus?: any;
  invokerHealthStatus?: any;
}

export default function Dashboard() {
  const { data: healthStatus, isLoading: isHealthLoading, isFetching: isHealthFetching, error: healthError } = useQuery<HealthStatus>({
    queryKey: ['/api/proxy/api/rest/health'],
    refetchInterval: 30000, // Refetch every 30 seconds
    retry: false, // Don't retry on failure for health checks
  });

  const getHealthColor = () => {
    if (isHealthLoading || !healthStatus) return 'muted';
    if (healthError) return 'destructive';
    if (healthStatus.overallHealth >= 80) return 'green';
    if (healthStatus.overallHealth >= 50) return 'yellow';
    return 'destructive';
  };

  const healthColor = getHealthColor();

  const handleRefresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['/api/proxy/api/rest/health'] });
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold">Dashboard</h1>
          <p className="text-muted-foreground mt-1">Overview of your Elements platform</p>
        </div>
        <Button 
          onClick={handleRefresh} 
          variant="outline" 
          size="sm"
          disabled={isHealthFetching}
          data-testid="button-refresh-health"
        >
          <RefreshCw className={`w-4 h-4 mr-2 ${isHealthFetching ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>

      <div className="max-w-2xl">
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="flex items-center gap-2">
                  <ServerCog className="w-5 h-5" />
                  Server Health
                </CardTitle>
                <CardDescription>
                  Real-time health monitoring of the Elements backend
                </CardDescription>
              </div>
              {isHealthLoading ? (
                <Badge variant="outline" data-testid="badge-health-loading">
                  Checking...
                </Badge>
              ) : healthError ? (
                <Badge variant="destructive" data-testid="badge-health-error">
                  <AlertCircle className="w-3 h-3 mr-1" />
                  Error
                </Badge>
              ) : healthStatus ? (
                <Badge 
                  variant={healthStatus.overallHealth >= 80 ? 'default' : healthStatus.overallHealth >= 50 ? 'outline' : 'destructive'}
                  className={healthStatus.overallHealth >= 80 ? 'bg-green-500 hover:bg-green-600' : healthStatus.overallHealth >= 50 ? 'bg-yellow-500 text-black hover:bg-yellow-600' : ''}
                  data-testid="badge-health-status"
                >
                  <CheckCircle2 className="w-3 h-3 mr-1" />
                  {Math.round(healthStatus.overallHealth)}%
                </Badge>
              ) : null}
            </div>
          </CardHeader>
          <CardContent className="space-y-4">
            {isHealthLoading ? (
              <p className="text-sm text-muted-foreground">Loading health status...</p>
            ) : healthError ? (
              <div className="space-y-2">
                <p className="text-sm text-destructive font-medium">Failed to fetch health status</p>
                <p className="text-xs text-muted-foreground">The server may be offline or unreachable.</p>
              </div>
            ) : healthStatus ? (
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <p className="text-xs text-muted-foreground">Checks Performed</p>
                    <p className="text-2xl font-semibold" data-testid="text-checks-performed">
                      {healthStatus.checksPerformed}
                    </p>
                  </div>
                  <div className="space-y-1">
                    <p className="text-xs text-muted-foreground">Checks Failed</p>
                    <p className="text-2xl font-semibold" data-testid="text-checks-failed">
                      {healthStatus.checksFailed}
                    </p>
                  </div>
                </div>
                
                {healthStatus.problems && healthStatus.problems.length > 0 && (
                  <div className="pt-3 border-t">
                    <p className="text-sm font-medium mb-2">Issues Detected:</p>
                    <ul className="space-y-1">
                      {healthStatus.problems.map((problem: any, idx: number) => (
                        <li key={idx} className="text-xs text-muted-foreground">
                          • {JSON.stringify(problem)}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
                
                {(!healthStatus.problems || healthStatus.problems.length === 0) && healthStatus.overallHealth >= 80 && (
                  <div className="pt-3 border-t">
                    <p className="text-sm text-green-600 dark:text-green-400 flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4" />
                      All systems operational
                    </p>
                  </div>
                )}
              </div>
            ) : null}
          </CardContent>
        </Card>
      </div>

      <div className="text-sm text-muted-foreground">
        <p>More dashboard metrics will be available once the appropriate endpoints are created.</p>
      </div>
    </div>
  );
}
