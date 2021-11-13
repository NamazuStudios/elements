export interface APIError {

    /**
     * Describes the error type.
     */
    code: string;
  
    /**
     * Error message returned from server.
     */
    message?: string;
  
  }