import {Schedule} from "../api/models/schedule";

export class ScheduleViewModel implements Schedule {
  name: string;
  description: string;
  id: string;
  displayName: string;
}
