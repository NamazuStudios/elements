/* tslint:disable */
export interface GameOnPrizeBundle {

  /**
   * The title of the prize bundle.
   */
  title?: string;

  /**
   * The description of the prize bundle.
   */
  description?: string;

  /**
   * The image URL of the prize bundle.
   */
  imageUrl?: string;

  /**
   * The list of Prize IDs awareded by this bundle.
   */
  prizeIds?: Array<string>;

  /**
   * The minimum rank needed to win the prize bundle.
   */
  rankFrom?: number;

  /**
   * The maximum rank needed to win the prize bundle.
   */
  rankTo?: number;
}
