import { useEffect, useRef, useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Mail, Plus, Ban, Copy, ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import { apiRequest, queryClient } from '@/lib/queryClient';
import {
  describeStatus,
  isActionable,
  buildIssueRequest,
  formatTimestamp,
  expiryUrgency,
  issuerLabel,
  type SuperUserInvite,
} from '@/lib/superuser-invites';

interface InviteListResponse {
  objects: SuperUserInvite[];
  offset: number;
  total: number;
}

const STATUS_BADGE: Record<string, { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }> = {
  PENDING: { label: 'Pending', variant: 'default' },
  REDEEMED: { label: 'Redeemed', variant: 'secondary' },
  REVOKED: { label: 'Revoked', variant: 'outline' },
  EXPIRED: { label: 'Expired', variant: 'destructive' },
};

const URGENCY_BADGE: Record<string, string> = {
  expired: 'text-destructive',
  critical: 'text-destructive',
  warning: 'text-amber-600',
  ok: 'text-muted-foreground',
};

export default function SuperuserInvites() {
  const { toast } = useToast();
  const [isIssueDialogOpen, setIsIssueDialogOpen] = useState(false);
  const [email, setEmail] = useState('');
  const [expiryHours, setExpiryHours] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const emailInputRef = useRef<HTMLInputElement>(null);

  const getPageSize = () => {
    const saved = localStorage.getItem('admin-results-per-page');
    return saved ? parseInt(saved, 10) : 20;
  };
  const pageSize = getPageSize();

  const { data: response, isLoading, isFetching, error } = useQuery<InviteListResponse>({
    queryKey: ['/api/rest/invite/superuser', currentPage, pageSize],
    queryFn: async () => {
      const path = `/api/rest/invite/superuser?offset=${currentPage * pageSize}&count=${pageSize}`;
      const res = await apiRequest('GET', path);
      return res.json();
    },
    retry: false,
  });

  useEffect(() => {
    if (error) {
      toast({
        title: 'Failed to load invitations',
        description: error instanceof Error ? error.message : 'Unexpected error',
        variant: 'destructive',
      });
    }
  }, [error, toast]);

  const issueMutation = useMutation({
    mutationFn: async (request: { email: string; expiryHours?: number }) => {
      return apiRequest('POST', '/api/rest/invite/superuser', request);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/invite/superuser'] });
      setIsIssueDialogOpen(false);
      setEmail('');
      setExpiryHours('');
      setFormError(null);
      toast({
        title: 'Invitation sent',
        description: 'The invitee will receive an email with redemption instructions.',
      });
    },
    onError: (e) => {
      toast({
        title: 'Failed to issue invitation',
        description: e instanceof Error ? e.message : 'Unexpected error',
        variant: 'destructive',
      });
    },
  });

  const revokeMutation = useMutation({
    mutationFn: async (token: string) => {
      return apiRequest('DELETE', `/api/rest/invite/superuser/${token}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['/api/rest/invite/superuser'] });
      toast({ title: 'Invitation revoked', description: 'The invite can no longer be redeemed.' });
    },
    onError: (e) => {
      toast({
        title: 'Failed to revoke invitation',
        description: e instanceof Error ? e.message : 'Unexpected error',
        variant: 'destructive',
      });
    },
  });

  const openIssueDialog = () => {
    setFormError(null);
    setIsIssueDialogOpen(true);
    setTimeout(() => emailInputRef.current?.focus(), 0);
  };

  const submitIssue = () => {
    const { error: validationError, request } = buildIssueRequest(email, expiryHours);
    if (validationError || !request) {
      setFormError(validationError ?? 'Unable to build the invitation.');
      return;
    }
    setFormError(null);
    issueMutation.mutate(request);
  };

  const copyToken = (invite: SuperUserInvite) => {
    navigator.clipboard?.writeText(invite.token).then(() => {
      toast({ title: 'Token copied', description: 'The invitation token is on the clipboard.' });
    });
  };

  const invites = response?.objects ?? [];
  const total = response?.total ?? 0;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  return (
    <div className="min-h-screen bg-background p-8" data-testid="page-superuser-invites">
      <div className="max-w-6xl mx-auto space-y-6">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Mail className="w-8 h-8" />
            <div>
              <h1 className="text-2xl font-semibold">Superuser Invitations</h1>
              <p className="text-sm text-muted-foreground">
                Invite administrators by email. Redeeming the invite elevates the invited account to superuser.
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="icon"
              onClick={() => queryClient.invalidateQueries({ queryKey: ['/api/rest/invite/superuser'] })}
              data-testid="button-refresh-invites"
            >
              <RefreshCw className={`w-4 h-4 ${isFetching ? 'animate-spin' : ''}`} />
            </Button>
            <Button onClick={openIssueDialog} data-testid="button-issue-invite">
              <Plus className="w-4 h-4 mr-2" />
              Invite Superuser
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Issued Invitations</CardTitle>
            <CardDescription>
              Newest first. A pending invitation can be revoked until it is redeemed; redeemed and revoked
              invitations are kept for the audit trail.
            </CardDescription>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <p className="text-sm text-muted-foreground" data-testid="text-loading">Loading…</p>
            ) : invites.length === 0 ? (
              <p className="text-sm text-muted-foreground" data-testid="text-empty">
                No invitations have been issued yet.
              </p>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Invited Email</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Issued By</TableHead>
                    <TableHead>Issued</TableHead>
                    <TableHead>Expires</TableHead>
                    <TableHead>Token</TableHead>
                    <TableHead>Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {invites.map((invite) => {
                    const status = describeStatus(invite);
                    const badge = STATUS_BADGE[status] ?? STATUS_BADGE.PENDING;
                    const actionable = isActionable(invite);
                    const urgency = expiryUrgency(invite);
                    return (
                      <TableRow key={invite.token} data-testid={`row-invite-${invite.token}`}>
                        <TableCell className="font-medium">{invite.invitedEmail}</TableCell>
                        <TableCell>
                          <Badge variant={badge.variant}>{badge.label}</Badge>
                        </TableCell>
                        <TableCell>{issuerLabel(invite)}</TableCell>
                        <TableCell>{formatTimestamp(invite.createdAt)}</TableCell>
                        <TableCell>
                          <span className={URGENCY_BADGE[urgency] ?? ''}>{formatTimestamp(invite.expiry)}</span>
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="ghost"
                            size="sm"
                            onClick={() => copyToken(invite)}
                            data-testid={`button-copy-token-${invite.token}`}
                          >
                            <Copy className="w-4 h-4" />
                          </Button>
                        </TableCell>
                        <TableCell>
                          <Button
                            variant="outline"
                            size="sm"
                            disabled={!actionable || revokeMutation.isPending}
                            onClick={() => revokeMutation.mutate(invite.token)}
                            data-testid={`button-revoke-${invite.token}`}
                          >
                            <Ban className="w-4 h-4 mr-2" />
                            Revoke
                          </Button>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            )}

            {totalPages > 1 && (
              <div className="flex items-center justify-between mt-4">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                  data-testid="button-prev-page"
                >
                  <ChevronLeft className="w-4 h-4 mr-1" /> Previous
                </Button>
                <span className="text-sm text-muted-foreground" data-testid="text-page-indicator">
                  Page {currentPage + 1} of {totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage((p) => p + 1)}
                  data-testid="button-next-page"
                >
                  Next <ChevronRight className="w-4 h-4 ml-1" />
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog open={isIssueDialogOpen} onOpenChange={setIsIssueDialogOpen}>
        <DialogContent data-testid="dialog-issue-invite">
          <DialogHeader>
            <DialogTitle>Invite a Superuser</DialogTitle>
            <DialogDescription>
              An invitation email is sent to the address below. The invitee signs in with an account whose
              verified email matches, then redeems the token to elevate that account to superuser.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="invite-email">Email address</Label>
              <Input
                id="invite-email"
                ref={emailInputRef}
                type="email"
                placeholder="admin@example.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                data-testid="input-invite-email"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="invite-expiry">Expiry (hours, optional)</Label>
              <Input
                id="invite-expiry"
                type="text"
                inputMode="numeric"
                placeholder="Server default (24)"
                value={expiryHours}
                onChange={(e) => setExpiryHours(e.target.value)}
                data-testid="input-invite-expiry-hours"
              />
            </div>
            {formError && (
              <p className="text-sm text-destructive" data-testid="text-invite-form-error">{formError}</p>
            )}
            <div className="flex justify-end gap-2">
              <Button variant="outline" onClick={() => setIsIssueDialogOpen(false)} data-testid="button-cancel-invite">
                Cancel
              </Button>
              <Button onClick={submitIssue} disabled={issueMutation.isPending} data-testid="button-send-invite">
                {issueMutation.isPending ? 'Sending…' : 'Send Invitation'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
}
