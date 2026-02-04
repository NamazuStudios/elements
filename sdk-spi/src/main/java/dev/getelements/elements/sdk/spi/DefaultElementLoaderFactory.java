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

        final var isolated = new ElementClassLoader(baseClassLoader, parent);
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

        final var serviceInterfaces = elementServiceRecords
                .stream()
                .flatMap(ElementServiceRecord::exposedTypes)
                .toList();

        final var classGraph = new ClassGraph()
                .ignoreParentClassLoaders()
                .overrideClassLoaders(classLoader)
                .enableAllInfo()
                .acceptClasses(serviceInterfaces
                        .stream()
                        .map(Class::getName)
                        .toArray(String[]::new)
                );

        elementDefinitionRecord.acceptPackages(
                classGraph::acceptPackages,
                classGraph::acceptPackagesNonRecursive
        );

        try (var result = classGraph.scan()) {

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

            final var interfaceMethods = elementServiceRecords.stream()
                    .flatMap(esr -> esr
                            .export()
                            .exposed()
                            .stream()
                            .filter(methods::containsKey)
                            .flatMap(interfaceType -> methods.get(interfaceType)
                                    .stream()
                                    .flatMap(method -> ElementEventConsumerRecord.from(esr, method)))
                    );

            final var implementationExposedMethods = elementServiceRecords.stream()
                    .filter(esr -> methods.containsKey(esr.implementation().type()))
                    .flatMap(esr -> methods.get(esr.implementation().type())
                            .stream()
                            .flatMap(method -> ElementEventConsumerRecord.from(esr, method))
                    );

            return Stream.concat(interfaceMethods, implementationExposedMethods).collect(toList());

        }

    }

    private ElementLoader newSharedLoader(final ElementRecord elementRecord) {

        final var aClass = elementRecord.definition().loader();

        if (ElementLoader.Default.class.equals(aClass)) {
            return new DefaultSharedElementLoader();
        } else {
            try {
                final var ctor = aClass.getConstructor();
                return ctor.newInstance();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
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
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new SdkException(e);
            }
        }

    }

}
