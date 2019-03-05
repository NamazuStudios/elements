import {MissionStep} from '../api/models/mission-step';
import {Reward} from '../api/models/reward';

export class MissionStepViewModel implements MissionStep {
  displayName: string;
  description: string;
  count: number;
  rewards: Array<Reward>;
  metadata?: {[key: string]: any};

  constructor() {
    this.metadata = {};
  };
}
