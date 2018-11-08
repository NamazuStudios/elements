import {Prize} from "../api/models/prize";

export class PrizeViewModel implements Prize {
  description: string;
  imageUrl: string;
  prizeInfo: string;
  title: string;
}
