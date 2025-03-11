import {Mission} from './mission';
import {Schedule} from "./schedule";

export interface ScheduleEvent {
  id?: string;
  begin: number
  end: number
  schedule: Schedule
  missions: Array<Mission>
}
