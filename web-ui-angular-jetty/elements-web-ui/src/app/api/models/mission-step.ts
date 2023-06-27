import {Reward} from './reward';

export interface MissionStep {
  displayName: string;
  description: string;
  count: number;
  rewards: Array<Reward>;
  metadata?: {[key: string]: any};
}
