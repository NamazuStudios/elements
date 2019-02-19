import {Mission} from '../api/models/mission';
import {MissionStep} from '../api/models/mission-step';

export class MissionViewModel implements Mission{
  name: string;
  description: string;
  id: string;
  tags: Array<string>;
  displayName: string;
  metadata: {[key: string]: any};
  steps: Array<MissionStep>;
  finalRepeatStep: MissionStep;
}
