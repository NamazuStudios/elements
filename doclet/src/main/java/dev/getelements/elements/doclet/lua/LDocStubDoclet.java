package dev.getelements.elements.doclet.lua;

import dev.getelements.elements.doclet.*;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;

public class LDocStubDoclet implements Doclet {

    private static final Pattern INDENTATION = Pattern.compile("\\s+");

    private Locale locale;

    private Reporter reporter;

    private final List<String> authors = new ArrayList<>();

    private final DirectoryDocWriter.Builder directoryDocWriterBuilder = new DirectoryDocWriter.Builder();

    @Override
    public void init(final Locale locale, final Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return "luadoc";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {

        final var now = LocalDate.now();

        final var defaultCopyrightNotice =format(
            "© %s %d",
            now.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()),
            now.getYear()
        );

        directoryDocWriterBuilder.withCopyrightNotice(defaultCopyrightNotice);

        return Set.of(
            new LDocAbstractOption(1, "Output Directory", "<path>", "--directory", "-d") {
                @Override
                public boolean process(final String option, final List<String> arguments) {

                    final var directory = arguments.get(0);

                    try {
                        final var path = Paths.get(directory);
                        directoryDocWriterBuilder.withRoot(path);
                        return true;
                    } catch (InvalidPathException ex) {
                        reporter.print(Diagnostic.Kind.ERROR, "Invalid Path: " + directory + " " + ex.getMessage());
                        return false;
                    }

                }
            },
            new LDocAbstractOption(1, "Indentation", "<string>", "--indent", "-i") {
                @Override
                public boolean process(final String option, final List<String> arguments) {

                    final var indentation = arguments.get(0);

                    if (INDENTATION.matcher(indentation).matches()) {
                        directoryDocWriterBuilder.withIndentation(indentation);
                        return true;
                    } else {
                        reporter.print(Diagnostic.Kind.ERROR,
                                "Invalid indentation: " + indentation +
                                        " (must match " + INDENTATION + ")");
                        return false;
                    }

                }
            },
            new LDocAbstractOption(1, "Copyright Notice", "<string>", "--copyright", "-c") {
                @Override
                public boolean process(final String option, final List<String> arguments) {
                    final var notice = arguments.get(0);
                    directoryDocWriterBuilder.withCopyrightNotice(notice);
                    return true;
                }
            },
            new LDocAbstractOption(1, "Max Columns", "<int>", "--max-columns", "-m") {
                @Override
                public boolean process(final String option, final List<String> arguments) {
                    try {

                        final var maxColumns = Integer.parseInt(arguments.get(0));

                        if (maxColumns > 0) {
                            directoryDocWriterBuilder.withMaxColumns(maxColumns);
                            return true;
                        } else {
                            reporter.print(Diagnostic.Kind.ERROR, "Columns count must be > 0");
                            return false;
                        }

                    } catch (NumberFormatException nfe) {
                        reporter.print(Diagnostic.Kind.ERROR, "Invalid column count: " + arguments.get(0));
                        return false;
                    }
                }
            },
            new LDocAbstractOption(1, "Line Ending", format("<%s>", Newline.getPossibleValues()), "--newline", "-n") {
                @Override
                public boolean process(final String option, final List<String> arguments) {

                    final Newline newline;

                    try {
                        newline = Newline.valueOf(arguments.get(0));
                    } catch (IllegalArgumentException ex) {
                        reporter.print(Diagnostic.Kind.ERROR, "Invalid newline: " + arguments.get(0));
                        return false;
                    }

                    directoryDocWriterBuilder.withNewline(newline.getNewline());
                    return true;

                }
            },
            new LDocAbstractOption(1, "Author", "<author>", "-author", "-a") {
                @Override
                public boolean process(final String option, final List<String> arguments) {
                    authors.add(arguments.get(0));
                    return true;
                }
            }
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

    @Override
    public boolean run(final DocletEnvironment environment) {

        final var authors = unmodifiableList(this.authors);

        final var cxt = new DocContext() {

            @Override
            public List<String> getAuthors() {
                return authors;
            }

            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public Reporter getReporter() {
                return reporter;
            }

            @Override
            public DocletEnvironment getEnvironment() {
                return environment;
            }

            @Override
            public Object getFileForTypename() throws IOException {
                final var jfo = environment
                    .getJavaFileManager()
                    .getJavaFileForInput(StandardLocation.SOURCE_PATH, "", null);

                return null;
            }

        };

        try (var docWriter = directoryDocWriterBuilder.withDocContext(cxt).build()) {
            for (var type : cxt.getIncludedElements()) {

                int i = 0;

                for (var processor : DocProcessor.get(cxt, type)) {
                    final var stubs = processor.process();
                    write(docWriter, stubs);
                }
            }
        } catch (IOException ex) {
            reporter.print(Diagnostic.Kind.ERROR, "IOException Writing docs: " + ex.getMessage());
            return false;
        }

        return true;

    }

    private void write(final DocWriter docWriter, final List<? extends DocRoot> stubs) throws IOException {
        for (var stub : stubs) {

            try (var docRootWriter = docWriter.open(stub)) {
                stub.write(docRootWriter);
            }

            reporter.print(Diagnostic.Kind.NOTE, "Processed stub: " + stub);

        }
    }


    public enum Newline {

        /**
         * Line feed.
         */
        LF("\n"),

        /**
         * Carriage return. Line feed.
         */
        CRLF("\r\n");

        private final String newline;

        Newline(final String newline) {
            this.newline = newline;
        }

        public String getNewline() {
            return newline;
        }

        public static String getPossibleValues() {
            return Stream.of(values()).map(Enum::toString).collect(joining(","));
        }

    }

}
