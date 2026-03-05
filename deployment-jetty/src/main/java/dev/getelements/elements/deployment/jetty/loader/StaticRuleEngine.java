package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.record.ElementStaticContentRecord;
import org.eclipse.jetty.http.MimeTypes;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Parses static-file header rules from element attributes, applies them to the pre-enumerated file list provided by
 * {@link ElementStaticContentRecord}, and produces a {@link StaticServingConfig} containing the file index, the
 * resolved index file, and any configured error pages.
 *
 * <h2>Rule attribute format</h2>
 * <pre>
 * dev.getelements.{ns}.rule.&lt;name&gt;.regex                     → pattern ([ and ] are converted to capture groups)
 * dev.getelements.{ns}.rule.&lt;name&gt;.header.&lt;Header&gt;.value    → header value template
 * </pre>
 *
 * <h2>Index file</h2>
 * <pre>
 * dev.getelements.{ns}.index    → relative path of the file served at the context root (default: index.html)
 * </pre>
 *
 * <h2>Error pages</h2>
 * <pre>
 * dev.getelements.{ns}.error.&lt;code&gt;    → relative path of the file served for that HTTP status code
 * </pre>
 *
 * <p>Where {@code {ns}} is the namespace passed at construction time ({@code "static"} for the Standard tree,
 * {@code "ui"} for the UI tree). Rules, index files, and error pages are therefore independent per tree.</p>
 *
 * <h2>Header value template variables</h2>
 * <ul>
 *   <li>{@code $filename} – the file's name (last path component)</li>
 *   <li>{@code $path} – the relative path string</li>
 *   <li>{@code $[0]} – the entire match (group 0)</li>
 *   <li>{@code $[N]} – capture group N (N ≥ 1)</li>
 * </ul>
 *
 * <p>Rule names are sorted alphabetically. Within a single file, if multiple rules match and both set the same header,
 * the last rule wins and a {@link Loader.PendingDeployment#logWarningf warning} is emitted.</p>
 *
 * <p>Rules with zero file matches also produce a warning.</p>
 */
class StaticRuleEngine {

    static final String DEFAULT_INDEX = "index.html";

    private final ElementStaticContentRecord contentRecord;

    private final Attributes attributes;

    private final String rulePrefix;

    private final String indexAttr;

    private final String errorPrefix;

    private final List<StaticContentRule> rules;

    private final Loader.PendingDeployment pending;

    StaticRuleEngine(
            final ElementStaticContentRecord contentRecord,
            final Attributes attributes,
            final String namespace,
            final Loader.PendingDeployment pending) {
        this.contentRecord = contentRecord;
        this.attributes = attributes;
        this.pending = pending;
        this.rulePrefix  = "dev.getelements." + namespace + ".rule.";
        this.indexAttr   = "dev.getelements." + namespace + ".index";
        this.errorPrefix = "dev.getelements." + namespace + ".error.";
        this.rules = parseRules(attributes, rulePrefix, pending);
    }

    /**
     * Builds the serving configuration. The file index maps each relative path string (forward-slash separated) to
     * its pre-resolved {@link StaticFileMetadata}. The index file and error pages are resolved from the same index.
     *
     * @return the immutable {@link StaticServingConfig}
     */
    StaticServingConfig buildIndex() {

        final var matchCounts = new LinkedHashMap<String, Integer>();
        for (final var rule : rules) {
            matchCounts.put(rule.name(), 0);
        }

        final var index = new LinkedHashMap<String, StaticFileMetadata>();
        final var root = contentRecord.root();

        for (final var absolutePath : contentRecord.contents()) {

            final var relPath = root.relativize(absolutePath);
            final var relPathStr = toForwardSlash(relPath);
            final var filename = absolutePath.getFileName().toString();

            // Apply rules: collect headers; last rule wins per header key; warn on collision.
            final var resolvedHeaders = new LinkedHashMap<String, String>();
            final Set<String> headersFromPreviousRules = new LinkedHashSet<>();
            String mimeFromRule = null;

            for (final var rule : rules) {
                final var matcher = rule.pattern().matcher(relPathStr);
                if (!matcher.find()) continue;

                matchCounts.merge(rule.name(), 1, Integer::sum);

                // Check for header collision with a previous rule
                final var thisRuleHeaders = rule.headerTemplates().keySet();
                for (final var header : thisRuleHeaders) {
                    if (headersFromPreviousRules.contains(header)) {
                        pending.logWarningf(
                                "Static rule collision: both rule '%s' and an earlier rule set header '%s' for file '%s'.",
                                rule.name(), header, relPathStr
                        );
                    }
                }

                for (final var entry : rule.headerTemplates().entrySet()) {
                    final var resolved = substitute(entry.getValue(), filename, relPathStr, matcher);
                    resolvedHeaders.put(entry.getKey(), resolved);
                }

                headersFromPreviousRules.addAll(thisRuleHeaders);

                if (rule.headerTemplates().containsKey("content-type")) {
                    mimeFromRule = resolvedHeaders.get("content-type");
                }
            }

            // Resolve MIME type
            final String mimeType;
            if (mimeFromRule != null) {
                mimeType = mimeFromRule;
            } else {
                final var detected = MimeTypes.DEFAULTS.getMimeByExtension(filename);
                if (detected != null) {
                    mimeType = detected;
                } else {
                    pending.logf("No known MIME type for '%s'; defaulting to application/octet-stream.", relPathStr);
                    mimeType = "application/octet-stream";
                }
            }

            index.put(relPathStr, new StaticFileMetadata(absolutePath, mimeType, Map.copyOf(resolvedHeaders)));
        }

        // Warn about dead rules
        for (final var entry : matchCounts.entrySet()) {
            if (entry.getValue() == 0) {
                pending.logWarningf("Static rule '%s' matched zero files.", entry.getKey());
            }
        }

        final var immutableIndex = Collections.unmodifiableMap(index);

        // Resolve index file
        final var rawIndex = attributes.getAttribute(indexAttr);
        final var indexPath = (rawIndex instanceof String s && !s.isBlank()) ? s.strip() : DEFAULT_INDEX;
        final var indexFile = immutableIndex.get(indexPath);
        if (indexFile == null) {
            pending.logf("Index file '%s' not found in content tree; root requests will return 404.", indexPath);
        }

        // Resolve error pages
        final var errorPages = new LinkedHashMap<Integer, StaticFileMetadata>();
        attributes.getAttributeNames().stream()
                .filter(name -> name.startsWith(errorPrefix))
                .forEach(name -> {
                    final var suffix = name.substring(errorPrefix.length());
                    try {
                        final int code = Integer.parseInt(suffix);
                        final var errorFilename = String.valueOf(attributes.getAttribute(name)).strip();
                        final var meta = immutableIndex.get(errorFilename);
                        if (meta != null) {
                            errorPages.put(code, meta);
                        } else {
                            pending.logWarningf(
                                    "Error page '%s' for status %d not found in content tree; skipping.",
                                    errorFilename, code
                            );
                        }
                    } catch (final NumberFormatException ex) {
                        pending.logWarningf(
                                "Invalid error-page attribute '%s': '%s' is not a valid HTTP status code.",
                                name, suffix
                        );
                    }
                });

        return new StaticServingConfig(immutableIndex, indexFile, Collections.unmodifiableMap(errorPages));
    }

    // ---- Private helpers ----------------------------------------------------------------

    private static List<StaticContentRule> parseRules(
            final Attributes attributes,
            final String rulePrefix,
            final Loader.PendingDeployment pending) {

        // Collect all rule-related attributes into: ruleName -> (key-suffix -> value)
        final var ruleData = new TreeMap<String, Map<String, String>>();

        attributes.getAttributeNames().stream()
                .filter(name -> name.startsWith(rulePrefix))
                .forEach(name -> {
                    final var suffix = name.substring(rulePrefix.length()); // e.g. "images.regex"
                    final var dot = suffix.indexOf('.');
                    if (dot < 0) return; // malformed – skip
                    final var ruleName = suffix.substring(0, dot);
                    final var keySuffix = suffix.substring(dot + 1); // e.g. "regex" or "header.Cache-Control.value"
                    ruleData.computeIfAbsent(ruleName, k -> new LinkedHashMap<>())
                            .put(keySuffix, String.valueOf(attributes.getAttribute(name)));
                });

        final var rules = new ArrayList<StaticContentRule>();

        for (final var entry : ruleData.entrySet()) {
            final var ruleName = entry.getKey();
            final var kv = entry.getValue();

            final var rawRegex = kv.get("regex");
            if (rawRegex == null || rawRegex.isBlank()) {
                pending.logWarningf("Static rule '%s' has no regex; skipping.", ruleName);
                continue;
            }

            // Convert property-file-safe bracket notation to Java capture groups
            final var javaRegex = rawRegex.replace("[", "(").replace("]", ")");

            final Pattern pattern;
            try {
                pattern = Pattern.compile(javaRegex);
            } catch (PatternSyntaxException ex) {
                pending.logWarningf("Static rule '%s' has invalid regex '%s': %s", ruleName, javaRegex, ex.getMessage());
                continue;
            }

            // Collect header templates: key suffix "header.<Name>.value" → header name = <Name>
            final var headerTemplates = new LinkedHashMap<String, String>();
            kv.forEach((k, v) -> {
                if (k.startsWith("header.") && k.endsWith(".value")) {
                    // strip "header." prefix and ".value" suffix
                    final var headerName = k.substring("header.".length(), k.length() - ".value".length()).toLowerCase();
                    if (!headerName.isBlank()) {
                        headerTemplates.put(headerName, v);
                    }
                }
            });

            rules.add(new StaticContentRule(ruleName, pattern, Map.copyOf(headerTemplates)));
        }

        return Collections.unmodifiableList(rules);
    }

    private static String substitute(
            final String template,
            final String filename,
            final String relPathStr,
            final java.util.regex.Matcher matcher) {

        var result = template;
        result = result.replace("$filename", filename);
        result = result.replace("$path", relPathStr);

        // Replace $[0], $[1], ...$[N] with capture group references
        // Use regex replacement to find all occurrences of $[<digits>]
        final var groupPattern = Pattern.compile("\\$\\[(\\d+)\\]");
        final var sb = new StringBuilder();
        final var m = groupPattern.matcher(result);
        while (m.find()) {
            final int groupIndex = Integer.parseInt(m.group(1));
            String groupValue;
            try {
                groupValue = groupIndex <= matcher.groupCount() ? matcher.group(groupIndex) : "";
            } catch (IndexOutOfBoundsException ex) {
                groupValue = "";
            }
            m.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(groupValue != null ? groupValue : ""));
        }
        m.appendTail(sb);

        return sb.toString();
    }

    private static String toForwardSlash(final Path path) {
        final var str = path.toString();
        return str.replace(java.io.File.separatorChar, '/');
    }

}