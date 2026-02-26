import { useState, useRef } from 'react';
import { useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { useToast } from '@/hooks/use-toast';
import { apiClient, getApiPath } from '@/lib/api-client';
import { Loader2, Upload, Search, HardDrive, ChevronDown, ChevronRight } from 'lucide-react';

// ---- Types ------------------------------------------------------------------

interface SystemVersion {
  version: string;
  revision: string;
  timestamp: string;
}

interface ElementManifestMetadata {
  version?: SystemVersion;
  builtinSpis?: string[];
}

interface ElementPathRecordMetadata {
  path: string;
  api: string[];
  spi: string[];
  lib: string[];
  classpath: string[];
  attributes: Record<string, unknown>;
  manifest: ElementManifestMetadata;
}

// ---- Result display ---------------------------------------------------------

const UNKNOWN = 'UNKNOWN';

function isKnown(value: string | null | undefined): value is string {
  return !!value && value !== UNKNOWN;
}

function CollapsiblePaths({ label, description, paths }: { label: string; description: string; paths: string[] }) {
  const [open, setOpen] = useState(false);
  if (paths.length === 0) return null;
  return (
    <div>
      <button
        className="flex items-center gap-1.5 w-full text-left py-1 text-sm hover:text-foreground transition-colors"
        onClick={() => setOpen(v => !v)}
      >
        {open ? <ChevronDown className="w-3 h-3 shrink-0" /> : <ChevronRight className="w-3 h-3 shrink-0" />}
        <span className="font-medium">{label}</span>
        <span className="text-muted-foreground text-xs">{description}</span>
        <Badge variant="secondary" className="ml-auto">{paths.length}</Badge>
      </button>
      {open && (
        <ul className="mt-1 ml-5 space-y-0.5 pb-1">
          {paths.map((p, i) => (
            <li key={i} className="font-mono text-xs text-muted-foreground break-all">{p}</li>
          ))}
        </ul>
      )}
    </div>
  );
}

function ElementResult({ record }: { record: ElementPathRecordMetadata }) {
  const [attrsOpen, setAttrsOpen] = useState(false);
  const { manifest } = record;
  const sv = manifest?.version;
  const attrEntries = Object.entries(record.attributes ?? {});
  const builtinSpis = manifest?.builtinSpis ?? [];

  return (
    <Card className="border border-border">
      <CardHeader className="pb-3">
        <div className="flex items-start gap-2">
          <div className="min-w-0 flex-1">
            <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide mb-1">Element Path</p>
            <CardTitle className="text-sm font-mono break-all">{record.path}</CardTitle>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">

        {/* Manifest */}
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">Manifest</p>
          <div className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 ml-1 text-sm">
            <span className="text-muted-foreground">Version</span>
            <span className="font-mono">
              {isKnown(sv?.version) ? sv!.version : <span className="text-muted-foreground italic">unknown</span>}
            </span>

            <span className="text-muted-foreground">Revision</span>
            <span className="font-mono text-xs break-all">
              {isKnown(sv?.revision) ? sv!.revision : <span className="text-muted-foreground italic">unknown</span>}
            </span>

            <span className="text-muted-foreground">Build Time</span>
            <span className="font-mono text-xs">
              {isKnown(sv?.timestamp) ? sv!.timestamp : <span className="text-muted-foreground italic">unknown</span>}
            </span>

            <span className="text-muted-foreground">Builtin SPIs</span>
            <span>
              {builtinSpis.length > 0
                ? <div className="flex flex-wrap gap-1">{builtinSpis.map(s => <Badge key={s} variant="secondary">{s}</Badge>)}</div>
                : <span className="text-muted-foreground italic text-xs">none</span>}
            </span>
          </div>
        </div>

        {/* JAR paths */}
        <div className="space-y-0.5">
          <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground mb-1">Paths</p>
          <CollapsiblePaths label="API JARs"   description="shared among all elements in this deployment" paths={record.api ?? []} />
          <CollapsiblePaths label="SPI JARs"   description="service-provider interface jars for this element" paths={record.spi ?? []} />
          <CollapsiblePaths label="Lib JARs"   description="implementation jars private to this element" paths={record.lib ?? []} />
          <CollapsiblePaths label="Classpath"  description="additional classpath entries (recursive)" paths={record.classpath ?? []} />
          {(record.api?.length ?? 0) + (record.spi?.length ?? 0) + (record.lib?.length ?? 0) + (record.classpath?.length ?? 0) === 0 && (
            <p className="text-xs text-muted-foreground italic ml-1">No path entries found.</p>
          )}
        </div>

        {/* Attributes */}
        <div>
          <button
            className="flex items-center gap-1.5 w-full text-left py-1 text-xs font-semibold uppercase tracking-wide text-muted-foreground hover:text-foreground transition-colors"
            onClick={() => setAttrsOpen(v => !v)}
          >
            {attrsOpen ? <ChevronDown className="w-3 h-3 shrink-0" /> : <ChevronRight className="w-3 h-3 shrink-0" />}
            Attributes
            <Badge variant="secondary" className="ml-1">{attrEntries.length}</Badge>
            {attrEntries.length === 0 && (
              <span className="font-normal normal-case tracking-normal italic ml-1">(deployment-time overrides; not set in this ELM)</span>
            )}
          </button>
          {attrsOpen && attrEntries.length > 0 && (
            <dl className="mt-1 ml-5 grid grid-cols-[auto_1fr] gap-x-4 gap-y-0.5">
              {attrEntries.map(([k, v]) => (
                <>
                  <dt key={`k-${k}`} className="font-mono text-xs text-muted-foreground">{k}</dt>
                  <dd key={`v-${k}`} className="font-mono text-xs break-all">{String(v)}</dd>
                </>
              ))}
            </dl>
          )}
        </div>

      </CardContent>
    </Card>
  );
}

function ResultsPanel({ results }: { results: ElementPathRecordMetadata[] }) {
  if (results.length === 0) {
    return (
      <p className="text-sm text-muted-foreground text-center py-8">
        No elements found in the inspected artifact.
      </p>
    );
  }
  return (
    <div className="space-y-3">
      <p className="text-sm text-muted-foreground">
        Found <span className="font-medium text-foreground">{results.length}</span> element{results.length !== 1 ? 's' : ''}
      </p>
      {results.map((r, i) => <ElementResult key={i} record={r} />)}
    </div>
  );
}

// ---- Main page --------------------------------------------------------------

export default function ElmInspector() {
  const { toast } = useToast();

  const [results, setResults] = useState<ElementPathRecordMetadata[] | null>(null);

  // -- Coordinates tab state
  const [coordinates, setCoordinates] = useState('');

  // -- Large object tab state
  const [largeObjectId, setLargeObjectId] = useState('');

  // -- File upload tab state
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // -- Mutations ---------------------------------------------------------------

  const coordinatesMutation = useMutation({
    mutationFn: async (coords: string) => {
      return apiClient.request<ElementPathRecordMetadata[]>(
        `/api/rest/elm/inspector/artifact/${encodeURIComponent(coords)}`
      );
    },
    onSuccess: data => {
      setResults(data ?? []);
    },
    onError: (err: Error & { status?: number }) => {
      toast({ title: 'Inspection failed', description: err.message, variant: 'destructive' });
    },
  });

  const largeObjectMutation = useMutation({
    mutationFn: async (id: string) => {
      return apiClient.request<ElementPathRecordMetadata[]>(
        `/api/rest/elm/inspector/large_object/${encodeURIComponent(id)}`
      );
    },
    onSuccess: data => {
      setResults(data ?? []);
    },
    onError: (err: Error & { status?: number }) => {
      toast({ title: 'Inspection failed', description: err.message, variant: 'destructive' });
    },
  });

  const uploadMutation = useMutation({
    mutationFn: async (file: File) => {
      const formData = new FormData();
      formData.append('elm', file);

      const fullPath = await getApiPath('/api/rest/elm/inspector/upload');
      const headers: Record<string, string> = {};
      const token = apiClient.getSessionToken();
      if (token) headers['Elements-SessionSecret'] = token;

      const response = await fetch(fullPath, {
        method: 'POST',
        body: formData,
        headers,
        credentials: 'include',
      });

      if (!response.ok) {
        const text = await response.text().catch(() => '');
        let msg = `Upload failed: ${response.status}`;
        try { msg = JSON.parse(text).message ?? msg; } catch { if (text) msg = text; }
        const err = new Error(msg) as Error & { status: number };
        err.status = response.status;
        throw err;
      }

      return response.json() as Promise<ElementPathRecordMetadata[]>;
    },
    onSuccess: data => {
      setResults(data ?? []);
    },
    onError: (err: Error & { status?: number }) => {
      toast({ title: 'Inspection failed', description: err.message, variant: 'destructive' });
    },
  });

  const isPending =
    coordinatesMutation.isPending ||
    largeObjectMutation.isPending ||
    uploadMutation.isPending;

  // ---- Render ----------------------------------------------------------------

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold">ELM Inspector</h1>
        <p className="text-sm text-muted-foreground mt-1">
          Inspect an Element distribution to view its paths, manifest, and attributes.
        </p>
      </div>

      <Tabs defaultValue="coordinates">
        <TabsList className="mb-4">
          <TabsTrigger value="coordinates">
            <Search className="w-4 h-4 mr-2" />
            Artifact Coordinates
          </TabsTrigger>
          <TabsTrigger value="largeobject">
            <HardDrive className="w-4 h-4 mr-2" />
            Large Object ID
          </TabsTrigger>
          <TabsTrigger value="upload">
            <Upload className="w-4 h-4 mr-2" />
            Upload ELM File
          </TabsTrigger>
        </TabsList>

        {/* Coordinates */}
        <TabsContent value="coordinates">
          <Card>
            <CardContent className="pt-6 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="coordinates">Maven Coordinates</Label>
                <Input
                  id="coordinates"
                  placeholder="e.g. com.example:my-element:elm:1.0.0"
                  value={coordinates}
                  onChange={e => setCoordinates(e.target.value)}
                  onKeyDown={e => {
                    if (e.key === 'Enter' && coordinates.trim()) {
                      coordinatesMutation.mutate(coordinates.trim());
                    }
                  }}
                />
                <p className="text-xs text-muted-foreground">
                  Format: <code>groupId:artifactId:packaging:version</code>
                </p>
              </div>
              <Button
                onClick={() => coordinatesMutation.mutate(coordinates.trim())}
                disabled={!coordinates.trim() || isPending}
              >
                {coordinatesMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                Inspect
              </Button>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Large Object ID */}
        <TabsContent value="largeobject">
          <Card>
            <CardContent className="pt-6 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="loId">Large Object ID</Label>
                <Input
                  id="loId"
                  placeholder="e.g. 64a1f2e3b4c5d6e7f8a9b0c1"
                  value={largeObjectId}
                  onChange={e => setLargeObjectId(e.target.value)}
                  onKeyDown={e => {
                    if (e.key === 'Enter' && largeObjectId.trim()) {
                      largeObjectMutation.mutate(largeObjectId.trim());
                    }
                  }}
                />
              </div>
              <Button
                onClick={() => largeObjectMutation.mutate(largeObjectId.trim())}
                disabled={!largeObjectId.trim() || isPending}
              >
                {largeObjectMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                Inspect
              </Button>
            </CardContent>
          </Card>
        </TabsContent>

        {/* File Upload */}
        <TabsContent value="upload">
          <Card>
            <CardContent className="pt-6 space-y-4">
              <div className="space-y-2">
                <Label>ELM File</Label>
                <div
                  className="border-2 border-dashed border-border rounded-lg p-8 text-center cursor-pointer hover:border-primary transition-colors"
                  onClick={() => fileInputRef.current?.click()}
                  onDragOver={e => e.preventDefault()}
                  onDrop={e => {
                    e.preventDefault();
                    const file = e.dataTransfer.files[0];
                    if (file) setSelectedFile(file);
                  }}
                >
                  <Upload className="w-8 h-8 mx-auto mb-2 text-muted-foreground" />
                  {selectedFile ? (
                    <p className="text-sm font-medium">{selectedFile.name}</p>
                  ) : (
                    <p className="text-sm text-muted-foreground">
                      Drop an <code>.elm</code> file here or click to browse
                    </p>
                  )}
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept=".elm,.zip"
                    className="hidden"
                    onChange={e => {
                      const file = e.target.files?.[0];
                      if (file) setSelectedFile(file);
                    }}
                  />
                </div>
              </div>
              <Button
                onClick={() => selectedFile && uploadMutation.mutate(selectedFile)}
                disabled={!selectedFile || isPending}
              >
                {uploadMutation.isPending && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
                Inspect
              </Button>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Results */}
      {results !== null && (
        <div className="space-y-3">
          <h2 className="text-lg font-semibold">Results</h2>
          <ResultsPanel results={results} />
        </div>
      )}
    </div>
  );
}