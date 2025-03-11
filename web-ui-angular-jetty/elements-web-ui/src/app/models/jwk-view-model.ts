import {Oauth2AuthScheme} from "../api/models/auth/auth-scheme-oauth2";
import {JWK} from "../api/models/auth/jwk";

export class JwkViewModel implements JWK {
  alg: string;
  e: string;
  kid: string;
  kty: string;
  n: string;
  use: string;
}
