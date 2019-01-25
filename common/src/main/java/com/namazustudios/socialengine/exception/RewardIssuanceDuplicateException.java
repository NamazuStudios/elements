package com.namazustudios.socialengine.exception;

public class RewardIssuanceDuplicateException extends BaseException {

    public RewardIssuanceDuplicateException() {
    }

    public RewardIssuanceDuplicateException(String message) {
        super(message);
    }

    public RewardIssuanceDuplicateException(String message, Throwable cause) {
        super(message, cause);
    }

    public RewardIssuanceDuplicateException(Throwable cause) {
        super(cause);
    }

    public RewardIssuanceDuplicateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public ErrorCode getCode() {
        return ErrorCode.DUPLICATE;
    }
}
