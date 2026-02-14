package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.annotation.*;
import dev.getelements.elements.sdk.exception.SdkElementNotFoundException;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.*;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import dev.getelements.elements.sdk.util.reflection.ElementReflectionUtils;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.FieldInfo;
import io.github.classgraph.MethodInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class DefaultElementLoaderFactory implements ElementLoaderFactory {

    private final ElementReflectionUtils reflectionUtils = new ElementReflectionUtils();

    @Override
    public ElementLoader getIsolatedLoaderWithParent(
            final Attributes attributes,
            final ClassLoader baseClassLoader,
            final ClassLoaderConstructor classLoaderCtor,
            final ClassLoader parent,
            final Predicate<ElementDefinitionRecord> selector) {

        final var isolated = new ElementImplementationClassLoader(baseClassLoader, parent);
        final var classLoader = classLoaderCtor.apply(isolated);
        final var elementDefinitionRecord = scanForModuleDefinition(classLoader, selector);

        // Partially initializes the classloaders with the ElementDefinitionRecord
        reflectionUtils.injectBeanProperties(isolated, elementDefinitionRecord);
        reflectionUtils.injectBeanProperties(classLoader, elementDefinitionRecord);

        final var elementRecord = loadElementRecord(
                attributes,
                classLoader,
                elementDefinitionRecord);

        // Fully initializes the classloaders with the ElementDefinitionRecord
        reflectionUtils.injectBeanProperties(isolated, elementRecord);
        reflectionUtils.injectBeanProperties(classLoader, elementRecord);

        final var elementLoader = newIsolatedLoader(classLoader, elementRecord);
        return reflectionUtils.injectBeanProperties(elementLoader, elementRecord);

    }

    private ElementRecord loadElementRecord(
            final Attributes attributes,
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord) {

        final var elementServices = scanForElementServices(classLoader, elementDefinitionRecord);
        final var elementProducedEvents = scanForProducedEvents(classLoader, elementDefinitionRecord);
        final var elementConsumedEvents = scanForConsumedEvents(classLoader, elementDefinitionRecord, elementServices);
        final var elementDefaultAttributes = scanForDefaultAttributes(classLoader, elementDefinitionRecord);
        final var elementDependencies = ElementDependencyRecord.fromPackage(elementDefinitionRecord.pkg()).toList();

        // The Module Records and Services
        final var elementResolvedAttributes = new SimpleAttributes.Builder()
                .from(elementDefaultAttributes)
                .from(attributes)
                .build()
                .immutableCopy();

        return new ElementRecord(
                ElementType.ISOLATED_CLASSPATH,
                elementDefinitionRecord,
                elementServices,
                elementProducedEvents,
                elementConsumedEvents,
                elementDependencies,
                elementResolvedAttributes,
                elementDefaultAttributes,
                classLoader
        );

    }

    @Override
    public Optional<ElementDefinitionRecord> findElementDefinitionRecord(
            final ClassLoader classLoader,
            final Attributes attributes,
            final Predicate<ElementDefinitionRecord> selector) {

        final var cg = new ClassGraph()
                .overrideClassLoaders(classLoader)
                .ignoreParentClassLoaders()
                .enableClassInfo()
                .enableAnnotationInfo();

        try (final var result = cg.scan()) {
            return result
                    .getPackageInfo()
                    .stream()
                    .filter(nfo -> nfo.hasAnnotation(ElementDefinition.class))
                    .map(nfo -> {
                        try {
                            final var packageInfoClass = nfo.getName() + ".package-info";
                            return classLoader.loadClass(packageInfoClass);
                        } catch (ClassNotFoundException ex) {
                            throw new SdkException("Unable to find package-info: " + nfo.getName(), ex);
                        }
                    })
                    .map(Class::getPackage)
                    .map(ElementDefinitionRecord::fromPackage)
                    .filter(selector)
                    .reduce((a, b) -> {
                        throw new SdkException("Found more than one element definition.");
                    });
        }

    }

    @Override
    public ElementRecord getElementRecordFromPackage(Attributes attributes, final Package aPackage) {
        return loadElementRecord(attributes, aPackage);
    }

    @Override
    public Stream<ElementServiceRecord> getExposedServices(final Package aPackage) {
        final var localClassLoader = getClass().getClassLoader();
        final var elementDefinitionRecord = ElementDefinitionRecord.fromPackage(aPackage);
        return scanForElementServices(localClassLoader, elementDefinitionRecord).stream();
    }

    @Override
    public ElementLoader getSharedLoader(final ElementRecord elementRecord,
                                         final ServiceLocator serviceLocator) {

        final var elementLoader = newSharedLoader(elementRecord);

        return reflectionUtils.injectBeanProperties(elementLoader,
                elementRecord,
                elementRecord.definition(),
                serviceLocator);

    }

    private ElementRecord loadElementRecord(final Attributes attributes, final Package aPackage) {

        final var localClassLoader = getClass().getClassLoader();
        final var elementDefinitionRecord = ElementDefinitionRecord.fromPackage(aPackage);
        final var elementServices = scanForElementServices(localClassLoader, elementDefinitionRecord);
        final var elementProducedEvents = scanForProducedEvents(localClassLoader, elementDefinitionRecord);
        final var elementConsumedEvents = scanForConsumedEvents(localClassLoader, elementDefinitionRecord, elementServices);
        final var elementDefaultAttributes = scanForDefaultAttributes(localClassLoader, elementDefinitionRecord);
        final var elementDependencies = ElementDependencyRecord.fromPackage(aPackage).toList();

        // The Module Records and Services
        final var elementResolvedAttributes = new SimpleAttributes.Builder()
                .from(elementDefaultAttributes)
                .from(attributes)
                .build()
                .immutableCopy();

        return new ElementRecord(
                ElementType.SHARED_CLASSPATH,
                elementDefinitionRecord,
                elementServices,
                elementProducedEvents,
                elementConsumedEvents,
                elementDependencies,
                elementResolvedAttributes,
                elementDefaultAttributes,
                localClassLoader
        );

    }

    private List<ElementDefaultAttributeRecord> scanForDefaultAttributes(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord) {

        final var cg = new ClassGraph()
                .enableClassInfo()
                .enableFieldInfo()
                .enableAnnotationInfo()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader);

        elementDefinitionRecord.acceptPackages(
                cg::acceptPackages,
                cg::acceptPackagesNonRecursive
        );

        try (final var result = cg.scan()) {

            return result
                    .getClassesWithFieldAnnotation(ElementDefaultAttribute.class)
                    .stream()
                    .flatMap(classInfo -> classInfo
                            .getDeclaredFieldInfo()
                            .stream()
                            .filter(fieldInfo ->
                                    fieldInfo.hasAnnotation(ElementDefaultAttribute.class) &&
                                    fieldInfo.isStatic() &&
                                    fieldInfo.isFinal())
                            .map(FieldInfo::loadClassAndGetField)
                    )
                    .map(ElementDefaultAttributeRecord::from)
                    .collect(toList());

        }

    }

    private ElementDefinitionRecord scanForModuleDefinition(
            final ClassLoader classLoader,
            final Predicate<ElementDefinitionRecord> selector) {

        final var cg = new ClassGraph()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .enableAnnotationInfo();

        try (final var result = cg.scan()) {

            final var elementDefinitionRecords = result
                    .getPackageInfo()
                    .stream()
                    .filter(nfo -> nfo.hasAnnotation(ElementDefinition.class))
                    .map(nfo -> {
                        try {
                            final var packageInfoClass = nfo.getName() + ".package-info";
                            return classLoader.loadClass(packageInfoClass);
                        } catch (ClassNotFoundException ex) {
                            throw new SdkException("Unable to find package-info: " + nfo.getName(), ex);
                        }
                    })
                    .map(Class::getPackage)
                    .map(ElementDefinitionRecord::fromPackage)
                    .filter(selector)
                    .toList();

            if (elementDefinitionRecords.size() > 1) {
                throw new SdkException("Found more than one element definition: " + elementDefinitionRecords);
            } else if (elementDefinitionRecords.isEmpty()) {
                throw new SdkElementNotFoundException("Found no element definition in " + classLoader);
            }

            return elementDefinitionRecords.getFirst();

        }

    }

    private List<ElementServiceRecord> scanForElementServices(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord) {

        final var classGraph = new ClassGraph()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .enableAnnotationInfo();

        elementDefinitionRecord.acceptPackages(
                classGraph::acceptPackages,
                classGraph::acceptPackagesNonRecursive
        );

        try (final var result = classGraph.scan()) {

            final var fromPackage = ElementServiceRecord.fromPackage(elementDefinitionRecord.pkg());

            final var fromClasses = result.getClassesWithAnnotation(ElementServiceExport.class)
                    .stream()
                    .map(ClassInfo::loadClass)
                    .flatMap(ElementServiceRecord::fromClass);

            return Stream.concat(fromPackage, fromClasses).collect(toList());

        }

    }

    private List<ElementEventProducerRecord> scanForProducedEvents(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord) {

        final var classGraph = new ClassGraph()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .enableClassInfo()
                .enableMethodInfo()
                .enableAnnotationInfo();

        if (elementDefinitionRecord.recursive()) {
            classGraph.acceptPackages(elementDefinitionRecord.pkgName());
        } else {
            classGraph.acceptPackagesNonRecursive(elementDefinitionRecord.pkgName());
        }

        try (var result = classGraph.scan()) {
            return result.getClassesWithAnnotation(ElementEventProducer.class)
                    .stream()
                    .map(ClassInfo::loadClass)
                    .flatMap(aClass -> Stream.of(aClass.getAnnotationsByType(ElementEventProducer.class)))
                    .map(ElementEventProducerRecord::from)
                    .toList();
        }

    }

    private List<ElementEventConsumerRecord<?>> scanForConsumedEvents(
            final ClassLoader classLoader,
            final ElementDefinitionRecord elementDefinitionRecord,
            final List<ElementServiceRecord> elementServiceRecords) {

        // Build service lookup structures for fast access and validation
        final var serviceRecordByClass = new java.util.HashMap<Class<?>, ElementServiceRecord>();
        elementServiceRecords.forEach(esr -> {
            // Add all exposed interfaces
            esr.export().exposed().forEach(exposedClass ->
                    serviceRecordByClass.put(exposedClass, esr));
            // Add implementation class
            serviceRecordByClass.put(esr.implementation().type(), esr);
        });
        final var validServiceClasses = serviceRecordByClass.keySet();

        // Scan ALL classes in Element package (no restriction to service classes)
        final var classGraph = new ClassGraph()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .enableAllInfo();

        elementDefinitionRecord.acceptPackages(
                classGraph::acceptPackages,
                classGraph::acceptPackagesNonRecursive
        );

        try (var result = classGraph.scan()) {

            // Find all methods with @ElementEventConsumer annotation
            final var methods = result
                    .getClassesWithMethodAnnotation(ElementEventConsumer.class)
                    .stream()
                    .collect(toMap(ClassInfo::loadClass, classInfo -> classInfo
                            .getMethodInfo()
                            .filter(methodInfo -> methodInfo.hasAnnotation(ElementEventConsumer.class))
                            .stream()
                            .map(MethodInfo::loadClassAndGetMethod)
                            .toList()
                    ));

            // Process all found methods and route appropriately
            return methods.entrySet().stream()
                    .flatMap(entry -> {
                        final var declaringClass = entry.getKey();
                        final var classMethods = entry.getValue();

                        return classMethods.stream()
                                .flatMap(method -> processEventConsumer(
                                        method,
                                        declaringClass,
                                        serviceRecordByClass,
                                        validServiceClasses
                                ));
                    })
                    .collect(toList());

        }

    }

    /**
     * Processes a single event consumer method and creates appropriate ElementEventConsumerRecord(s).
     * Handles both direct service methods and methods that route via another service.
     *
     * @param method the method with @ElementEventConsumer annotation
     * @param declaringClass the class that declares the method
     * @param serviceRecordByClass map of service classes to their records
     * @param validServiceClasses set of all valid service classes for validation
     * @return stream of ElementEventConsumerRecord instances
     */
    private Stream<ElementEventConsumerRecord<?>> processEventConsumer(
            final java.lang.reflect.Method method,
            final Class<?> declaringClass,
            final java.util.Map<Class<?>, ElementServiceRecord> serviceRecordByClass,
            final java.util.Set<Class<?>> validServiceClasses) {

        final var annotation = method.getAnnotation(ElementEventConsumer.class);
        final var viaRef = annotation.via();
        final var viaClass = viaRef.value();

        // Check if method uses 'via' routing
        if (viaClass != ElementEventConsumer.None.class) {
            // Route via specified service
            return createConsumerViaService(
                    method,
                    viaRef,
                    serviceRecordByClass,
                    validServiceClasses
            );
        } else {
            // Default: Method must be on an exported service
            return createConsumerDirect(
                    method,
                    declaringClass,
                    serviceRecordByClass
            );
        }
    }

    /**
     * Creates event consumer record for a method that routes via another service.
     * Validates that the via service exists and creates a consumer record using that service's key.
     *
     * @param method the consumer method
     * @param viaRef the service reference to route through
     * @param serviceRecordByClass map of service classes to records
     * @param validServiceClasses set of valid service classes
     * @return stream of ElementEventConsumerRecord
     */
    private Stream<ElementEventConsumerRecord<?>> createConsumerViaService(
            final java.lang.reflect.Method method,
            final ElementServiceReference viaRef,
            final java.util.Map<Class<?>, ElementServiceRecord> serviceRecordByClass,
            final java.util.Set<Class<?>> validServiceClasses) {

        final var viaClass = viaRef.value();
        final var viaName = viaRef.name();

        // Validate: via class must be an exported service
        if (!validServiceClasses.contains(viaClass)) {
            throw new SdkException(String.format(
                    "Method %s.%s specifies via=%s, but %s is not an exported service in this Element. " +
                    "Available services: %s",
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    viaClass.getName(),
                    viaClass.getName(),
                    validServiceClasses.stream().map(Class::getName).collect(toList())
            ));
        }

        // Lookup: Find the service record for the via class
        final var serviceRecord = serviceRecordByClass.get(viaClass);
        if (serviceRecord == null) {
            throw new SdkException(String.format(
                    "Service record not found for via class: %s (method: %s.%s)",
                    viaClass.getName(),
                    method.getDeclaringClass().getName(),
                    method.getName()
            ));
        }

        // Validate: If viaName is specified, verify the service record has that name
        if (!viaName.isBlank()) {
            final var exportName = serviceRecord.export().name();
            if (!viaName.equals(exportName)) {
                throw new SdkException(String.format(
                        "Method %s.%s specifies via service name '%s', but service %s has name '%s'",
                        method.getDeclaringClass().getName(),
                        method.getName(),
                        viaName,
                        viaClass.getName(),
                        exportName.isEmpty() ? "(unnamed)" : exportName
                ));
            }
        }

        // Create consumer record using the via service's record
        // This will cause dispatch to look up via serviceLocator.findInstance(viaClass)
        return ElementEventConsumerRecord.from(serviceRecord, method);
    }

    /**
     * Creates event consumer record for a method directly on an exported service.
     * This is the original behavior - the declaring class must be an exported service.
     *
     * @param method the consumer method
     * @param declaringClass the class declaring the method
     * @param serviceRecordByClass map of service classes to records
     * @return stream of ElementEventConsumerRecord
     */
    private Stream<ElementEventConsumerRecord<?>> createConsumerDirect(
            final java.lang.reflect.Method method,
            final Class<?> declaringClass,
            final java.util.Map<Class<?>, ElementServiceRecord> serviceRecordByClass) {

        // Lookup: Find service record for declaring class
        final var serviceRecord = serviceRecordByClass.get(declaringClass);

        if (serviceRecord == null) {
            throw new SdkException(String.format(
                    "Method %s.%s has @ElementEventConsumer but %s is not an exported service. " +
                    "Either add @ElementServiceExport to %s or use via=@ElementServiceReference(...) " +
                    "to route through an exported service.",
                    declaringClass.getName(),
                    method.getName(),
                    declaringClass.getName(),
                    declaringClass.getName()
            ));
        }

        // Create standard consumer record
        return ElementEventConsumerRecord.from(serviceRecord, method);
    }

    private ElementLoader newSharedLoader(final ElementRecord elementRecord) {

        final var aClass = elementRecord.definition().loader();

        if (ElementLoader.Default.class.equals(aClass)) {
            return new DefaultSharedElementLoader();
        } else {
            try {
                final var ctor = aClass.getConstructor();
                return ctor.newInstance();
            } catch (InvocationTargetException |
                     NoSuchMethodException |
                     InstantiationException |
                     IllegalAccessException e) {
                throw new SdkException(e);
            }
        }

    }

    private ElementLoader newIsolatedLoader(
            final ClassLoader classLoader,
            final ElementRecord elementRecord) {

        final var aClass = elementRecord.definition().loader();

        if (ElementLoader.Default.class.equals(aClass)) {

            final var loader = ServiceLoader.load(ElementLoader.class, classLoader);

            return loader.findFirst().orElseThrow(() -> new SdkException(
                    "No SPI (Service Provider Implementation) for " + ElementLoader.class.getName())
            );

        } else {
            try {
                final var ctor = aClass.getConstructor();
                return ctor.newInstance();
            } catch (InvocationTargetException |
                     NoSuchMethodException |
                     InstantiationException |
                     IllegalAccessException e) {
                throw new SdkException(e);
            }
        }

    }

}
