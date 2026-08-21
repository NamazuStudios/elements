import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { ServerCog, CheckCircle2, AlertCircle, RefreshCw, BookOpen, FileText, Cloud, Rocket, ExternalLink } from 'lucide-react';
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

interface VersionInfo {
  version: string;
  revision: string;
}

// Builds the apidocs URL for the running server's version. SNAPSHOT builds append the
// short git commit (e.g. 3.9.0-SNAPSHOT-e7c18562d) so docs match the exact build in use.
function buildDocsUrl(versionInfo?: VersionInfo): string {
  if (!versionInfo?.version) return 'https://apidocs.namazustudios.com/';

  const versionParam = versionInfo.version.endsWith('-SNAPSHOT') && versionInfo.revision
    ? `${versionInfo.version}-${versionInfo.revision.slice(0, 9)}`
    : versionInfo.version;

  return `https://apidocs.namazustudios.com/?version=${encodeURIComponent(versionParam)}`;
}

const ctaLinks = [
  {
    title: 'Read the Docs',
    description: 'API reference for your running version',
    icon: BookOpen,
    href: (versionInfo?: VersionInfo) => buildDocsUrl(versionInfo),
  },
  {
    title: 'Read the Manual',
    description: 'Guides and concepts for Namazu Elements',
    icon: FileText,
    href: () => 'https://namazustudios.com/docs',
  },
  {
    title: 'Host on AWS',
    description: 'Deploy Elements from the AWS Marketplace',
    icon: Cloud,
    href: () => 'https://aws.amazon.com/marketplace/seller-profile?id=seller-ondtddsmhzhte',
  },
  {
    title: 'Sign up for Namazu Cloud',
    description: 'Fully managed Elements hosting',
    icon: Rocket,
    href: () => 'https://cloud.namazustudios.com?signup',
  },
];

export default function Dashboard() {
  const { data: healthStatus, isLoading: isHealthLoading, isFetching: isHealthFetching, error: healthError } = useQuery<HealthStatus>({
    queryKey: ['/api/rest/health'],
    refetchInterval: 30000, // Refetch every 30 seconds
    retry: false, // Don't retry on failure for health checks
  });

  const { data: versionInfo } = useQuery<VersionInfo>({
    queryKey: ['/api/proxy/api/rest/version'],
    staleTime: Infinity, // Version doesn't change during session
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
    await queryClient.invalidateQueries({ queryKey: ['/api/rest/health'] });
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

      <div className="space-y-3">
        <h2 className="text-lg font-semibold">Get Started</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {ctaLinks.map(({ title, description, icon: Icon, href }) => (
            <a
              key={title}
              href={href(versionInfo)}
              target="_blank"
              rel="noopener noreferrer"
              data-testid={`link-cta-${title.toLowerCase().replace(/\s+/g, '-')}`}
            >
              <Card className="h-full hover-elevate">
                <CardContent className="p-4 space-y-2">
                  <div className="flex items-center justify-between">
                    <Icon className="w-5 h-5 text-muted-foreground" />
                    <ExternalLink className="w-3.5 h-3.5 text-muted-foreground" />
                  </div>
                  <p className="text-sm font-medium">{title}</p>
                  <p className="text-xs text-muted-foreground">{description}</p>
                </CardContent>
              </Card>
            </a>
          ))}
        </div>
      </div>
    </div>
  );
}
