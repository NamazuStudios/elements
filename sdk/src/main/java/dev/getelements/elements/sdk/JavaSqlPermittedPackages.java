package dev.getelements.elements.sdk;

public class JavaSqlPermittedPackages implements PermittedPackages {

    @Override
    public boolean test(final Package aPackage) {
        final var name = aPackage.getName();
        return name.equals("java.sql") || name.startsWith("java.sql.");
    }

    @Override
    public String getDescription() {
        return "Permits the usage of java.sql types (e.g. java.sql.Timestamp, java.sql.Date). Unlike java.base, " +
                "the java.sql platform module is not loaded by the bootstrap class loader, so its types are not " +
                "otherwise visible to Elements without this rule.";
    }

}
