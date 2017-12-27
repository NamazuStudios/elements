package com.namazustudios.socialengine.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class MultiException extends InternalException {

    private final List<Throwable> additionalCauses;

    public MultiException(final List<? extends Throwable> causes) {
        this(null, causes);
    }

    public MultiException(final String message, final List<? extends Throwable> causes) {
        super(message, causes == null || causes.isEmpty() ? null : causes.get(0));
        this.additionalCauses = causes == null ? emptyList() : new ArrayList<>(causes.subList(1, causes.size()));
    }

    public List<Throwable> getAdditionalCauses() {
        return additionalCauses;
    }

    @Override
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        walk((i, th) -> {
            s.printf("Additional Cause #%d\n", i.intValue() + 1);
            th.printStackTrace(s);
        });
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        walk((i, th) -> {
            s.printf("Additional Cause #%d: ", i.intValue() + 1);
            th.printStackTrace(s);
        });
    }

    private void walk(final BiConsumer<Integer, Throwable> biConsumer) {
        final ListIterator<Throwable> itr = additionalCauses.listIterator();

        while (itr.hasNext()) {
            biConsumer.accept(itr.nextIndex(), itr.next());
        }

    }

}
