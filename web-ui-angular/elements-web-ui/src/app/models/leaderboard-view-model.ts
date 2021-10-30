import { Leaderboard } from "../api/models";

export class LeaderboardViewModel implements Leaderboard {
  name: string;
  title: string;
  timeStrategyType: "ALL_TIME" | "EPOCHAL";
  scoreStrategyType: "OVERWRITE_IF_GREATER" | "ACCUMULATE";
  scoreUnits: string;
  days?: number;
  hours?: number;
  minutes?: number;
  seconds?: number;
  firstEpochTimestampView?: string;
  
}
