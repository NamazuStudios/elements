package dev.getelements.elements.rt.kryo;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public final class KryoThrowableContainer implements Serializable {

    private String className;

    private String message;

    private KryoThrowableContainer cause;

    private List<KryoStackTraceElementContainer> stackTrace;

    public KryoThrowableContainer() {}

    public KryoThrowableContainer(
            final String className,
            final String message,
            final List<KryoStackTraceElementContainer> stackTrace,
            final KryoThrowableContainer cause) {
        this.className = className;
        this.message = message;
        this.stackTrace = stackTrace;
        this.cause = cause;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public KryoThrowableContainer getCause() {
        return cause;
    }

    public void setCause(KryoThrowableContainer cause) {
        this.cause = cause;
    }

    public List<KryoStackTraceElementContainer> getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(List<KryoStackTraceElementContainer> stackTrace) {
        this.stackTrace = stackTrace;
    }

    public Throwable toThrowable(final ClassLoader classLoader) throws
            ClassNotFoundException,
            NoSuchMethodException,
            InvocationTargetException,
            InstantiationException,
            IllegalAccessException {

        final var cls = classLoader.loadClass(getClassName());

        if (!Throwable.class.isAssignableFrom(cls)) {
            throw new IllegalArgumentException(format("%s is not %s", cls.getName(), Throwable.class.getName()));
        }

        final var cause = getCause() == null
                ? null
                : getCause().toThrowable(classLoader);

        final var stackTraceContainers = getStackTrace() == null
                ? new ArrayList<KryoStackTraceElementContainer>()
                : getStackTrace();

        final var stackTrace = stackTraceContainers
                .stream()
                .map(KryoStackTraceElementContainer::toStackTraceElement)
                .toArray(StackTraceElement[]::new);

        final var constructor = cls.getConstructor(String.class, Throwable.class);
        final var throwable = (Throwable) constructor.newInstance(getMessage(), cause);
        throwable.setStackTrace(stackTrace);

        return throwable;

    }

    public static KryoThrowableContainer from(final Throwable throwable) {

        final var message = throwable.getMessage();
        final var className = throwable.getClass().getName();
        final var stackTrace = throwable.getStackTrace() == null
                ? null
                : KryoStackTraceElementContainer.from(throwable.getStackTrace());

        final var cause = throwable.getCause() == null
                ? null
                : from(throwable.getCause());

        return new KryoThrowableContainer(className, message, stackTrace, cause);

    }

}
