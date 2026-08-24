import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card } from '@/components/ui/card';
import { Trash2, Plus } from 'lucide-react';

const KEY_TYPES = ['RSA', 'EC'];

const EC_CURVES = ['P-256', 'P-384', 'P-521'];

interface JwkData {
  alg?: string;
  kid?: string;
  kty: string;
  use?: string;
  e?: string;
  n?: string;
  crv?: string;
  x?: string;
  y?: string;
}

interface JwkListEditorProps {
  value?: JwkData[];
  onChange: (keys: JwkData[]) => void;
}

// A JWK's field set is fixed by RFC 7518, and differs by key type -- RSA uses "e"/"n", EC uses "crv"/"x"/"y".
// Editing this as freeform key/value pairs would let a user type a field name the server's JWK model has no
// setter for, which gets silently dropped -- so each card exposes exactly the fields valid for its own "kty".
export function JwkListEditor({ value = [], onChange }: JwkListEditorProps) {
  const [keys, setKeys] = useState<JwkData[]>(value);

  useEffect(() => {
    if (value && JSON.stringify(value) !== JSON.stringify(keys)) {
      setKeys(value);
    }
  }, [value]);

  useEffect(() => {
    onChange(keys);
  }, [keys]);

  const addKey = () => {
    setKeys([...keys, { kty: 'RSA', use: 'sig' }]);
  };

  const removeKey = (index: number) => {
    setKeys(keys.filter((_, i) => i !== index));
  };

  const updateKey = (index: number, field: keyof JwkData, fieldValue: string) => {
    const updated = [...keys];
    const next = { ...updated[index], [field]: fieldValue };

    // Switching key type clears the fields that don't apply to the new type, rather than leaving stale
    // RSA values behind on an EC key (or vice versa).
    if (field === 'kty') {
      delete next.e;
      delete next.n;
      delete next.crv;
      delete next.x;
      delete next.y;
    }

    updated[index] = next;
    setKeys(updated);
  };

  return (
    <div className="space-y-3">
      {keys.length === 0 ? (
        <Card className="p-4 text-center text-muted-foreground text-sm">
          No keys configured
        </Card>
      ) : (
        <div className="space-y-3">
          {keys.map((key, index) => (
            <Card key={index} className="p-4">
              <div className="space-y-3">
                <div className="grid grid-cols-2 md:grid-cols-4 gap-2">
                  <div>
                    <Label className="text-xs text-muted-foreground">Key Type</Label>
                    <Select
                      value={key.kty}
                      onValueChange={(v) => updateKey(index, 'kty', v)}
                    >
                      <SelectTrigger className="mt-1" data-testid={`select-jwk-kty-${index}`}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {KEY_TYPES.map((kty) => (
                          <SelectItem key={kty} value={kty}>{kty}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div>
                    <Label className="text-xs text-muted-foreground">Algorithm</Label>
                    <Input
                      value={key.alg ?? ''}
                      onChange={(e) => updateKey(index, 'alg', e.target.value)}
                      placeholder={key.kty === 'EC' ? 'ES256' : 'RS256'}
                      className="mt-1"
                      data-testid={`input-jwk-alg-${index}`}
                    />
                  </div>
                  <div>
                    <Label className="text-xs text-muted-foreground">Key ID</Label>
                    <Input
                      value={key.kid ?? ''}
                      onChange={(e) => updateKey(index, 'kid', e.target.value)}
                      placeholder="kid"
                      className="mt-1"
                      data-testid={`input-jwk-kid-${index}`}
                    />
                  </div>
                  <div>
                    <Label className="text-xs text-muted-foreground">Use</Label>
                    <Input
                      value={key.use ?? ''}
                      onChange={(e) => updateKey(index, 'use', e.target.value)}
                      placeholder="sig"
                      className="mt-1"
                      data-testid={`input-jwk-use-${index}`}
                    />
                  </div>
                </div>

                {key.kty === 'EC' ? (
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-2">
                    <div>
                      <Label className="text-xs text-muted-foreground">Curve</Label>
                      <Select
                        value={key.crv ?? ''}
                        onValueChange={(v) => updateKey(index, 'crv', v)}
                      >
                        <SelectTrigger className="mt-1" data-testid={`select-jwk-crv-${index}`}>
                          <SelectValue placeholder="Select curve" />
                        </SelectTrigger>
                        <SelectContent>
                          {EC_CURVES.map((crv) => (
                            <SelectItem key={crv} value={crv}>{crv}</SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div>
                      <Label className="text-xs text-muted-foreground">X Coordinate</Label>
                      <Input
                        value={key.x ?? ''}
                        onChange={(e) => updateKey(index, 'x', e.target.value)}
                        placeholder="Base64url encoded x"
                        className="mt-1 font-mono text-sm"
                        data-testid={`input-jwk-x-${index}`}
                      />
                    </div>
                    <div>
                      <Label className="text-xs text-muted-foreground">Y Coordinate</Label>
                      <Input
                        value={key.y ?? ''}
                        onChange={(e) => updateKey(index, 'y', e.target.value)}
                        placeholder="Base64url encoded y"
                        className="mt-1 font-mono text-sm"
                        data-testid={`input-jwk-y-${index}`}
                      />
                    </div>
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
                    <div>
                      <Label className="text-xs text-muted-foreground">Modulus (n)</Label>
                      <Input
                        value={key.n ?? ''}
                        onChange={(e) => updateKey(index, 'n', e.target.value)}
                        placeholder="Base64url encoded modulus"
                        className="mt-1 font-mono text-sm"
                        data-testid={`input-jwk-n-${index}`}
                      />
                    </div>
                    <div>
                      <Label className="text-xs text-muted-foreground">Exponent (e)</Label>
                      <Input
                        value={key.e ?? ''}
                        onChange={(e) => updateKey(index, 'e', e.target.value)}
                        placeholder="Base64url encoded exponent"
                        className="mt-1 font-mono text-sm"
                        data-testid={`input-jwk-e-${index}`}
                      />
                    </div>
                  </div>
                )}

                <div className="flex justify-end">
                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => removeKey(index)}
                    data-testid={`button-remove-jwk-${index}`}
                  >
                    <Trash2 className="w-4 h-4 mr-1" />
                    Remove
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}

      <Button
        type="button"
        variant="outline"
        onClick={addKey}
        className="w-full"
        data-testid="button-add-jwk"
      >
        <Plus className="w-4 h-4 mr-2" />
        Add Key
      </Button>
    </div>
  );
}
