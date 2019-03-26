import {MissionStep} from './mission-step';

export interface Mission {
  id?: string;
  name: string;
  tags?: Array<string>;
  displayName: string;
  description: string;
  steps?: Array<MissionStep>;
  finalRepeatStep?: MissionStep;
  metadata?: {[key: string]: any};
}
