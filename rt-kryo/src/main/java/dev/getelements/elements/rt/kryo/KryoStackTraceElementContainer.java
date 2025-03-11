package dev.getelements.elements.rt.kryo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KryoStackTraceElementContainer implements Serializable {

    private String fileName;

    private String declaringClass;

    private int lineNumber;

    private String methodName;

    public KryoStackTraceElementContainer() {}

    public KryoStackTraceElementContainer(
            final String fileName,
            final String declaringClass,
            final int lineNumber,
            final String methodName) {
        this.fileName = fileName;
        this.declaringClass = declaringClass;
        this.lineNumber = lineNumber;
        this.methodName = methodName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public StackTraceElement toStackTraceElement() {
        return new StackTraceElement(
                getDeclaringClass(),
                getMethodName(),
                getFileName(),
                getLineNumber()
        );
    }

    public static KryoStackTraceElementContainer from(final StackTraceElement stackTraceElement) {
        return new KryoStackTraceElementContainer(
                stackTraceElement.getFileName(),
                stackTraceElement.getClassName(),
                stackTraceElement.getLineNumber(),
                stackTraceElement.getMethodName()
        );
    }

    public static List<KryoStackTraceElementContainer> from(final StackTraceElement[] stackTraceElements) {
        return Stream.of(stackTraceElements)
                .map(KryoStackTraceElementContainer::from)
                .collect(Collectors.toCollection(ArrayList::new));
    }

}
