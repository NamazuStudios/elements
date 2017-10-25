package com.namazustudios.socialengine.rt.util;

@FunctionalInterface
public interface FinallyAction {

    void perform();

    default FinallyAction andThen(final FinallyAction next) {
        return () -> {
            try {
                perform();
            } finally {
                next.perform();
            }
        };
    }

}
