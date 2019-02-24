import {Mission} from '../api/models/mission';
import {MissionStep} from '../api/models/mission-step';
import {MissionStepViewModel} from './mission-step-view-model';

export class MissionViewModel implements Mission {
  name: string;
  description: string;
  id: string;
  tags: Array<string>;
  displayName: string;
  metadata: {[key: string]: any};
  steps: Array<MissionStep>;
  finalRepeatStep: MissionStep;

  constructor() {
    this.steps = [];
  }
}
