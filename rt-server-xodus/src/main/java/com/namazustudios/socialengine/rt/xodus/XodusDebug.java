package com.namazustudios.socialengine.rt.xodus;

import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Store;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.namazustudios.socialengine.rt.xodus.XodusResourceService.*;
import static java.util.Collections.unmodifiableSet;
import static jetbrains.exodus.bindings.IntegerBinding.entryToInt;
import static jetbrains.exodus.bindings.StringBinding.entryToString;
import static jetbrains.exodus.env.StoreConfig.USE_EXISTING;

public class XodusDebug {

    public static final Set<String> TEXT_STORES;

    public static final Set<String> BINARY_STORES;

    public static final Set<String> INTEGER_STORES;

    static {

        final Set<String> binaryStores = new HashSet<>();
        binaryStores.add(STORE_RESOURCES);
        BINARY_STORES = unmodifiableSet(binaryStores);

        final Set<String> integerStores = new HashSet<>();
        integerStores.add(STORE_ACQUIRES);
        INTEGER_STORES = unmodifiableSet(integerStores);

        final Set<String> textStores = new HashSet<>();
        textStores.add(STORE_PATHS);
        textStores.add(STORE_RESOURCE_IDS);
        TEXT_STORES = unmodifiableSet(textStores);

    }

    /**
     * Accepting a {@link StringBuilder}, this will completely dump the store contents to the builder.  Useful and
     * strongly only recommended for debugging and testing purposes, this can potentially use enough memory to
     * cause an {@link OutOfMemoryError}.  Use it wisely.
     *
     * @param environment the {@link Environment}
     * @param stringBuilder the output {@link StringBuilder}
     * @return the {@link StringBuilder} that was supplied to the method.
     */
    public static StringBuilder dumpStoreData(final Environment environment, final StringBuilder stringBuilder) {
        return environment.computeInReadonlyTransaction(txn -> {

            final List<String> stores = environment.getAllStoreNames(txn);

            stores.stream().filter(BINARY_STORES::contains).forEach(storeName -> {

                final Store store = environment.openStore(storeName, USE_EXISTING, txn);

                stringBuilder.append("Binary Store: ").append(store.getName()).append('\n')
                             .append("Configuration: ").append(store.getConfig()).append('\n');

                int count = 0;

                try (final Cursor cursor  = store.openCursor(txn)) {
                    while (cursor.getNext()) {
                        final String key = entryToString(cursor.getKey());
                        stringBuilder.append("Record # ").append(count++).append(": ")
                                     .append(key).append(" -> ").append("<binary>").append('\n');
                    }
                }

            });

            stringBuilder.append('\n');

            stores.stream().filter(INTEGER_STORES::contains).forEach(storeName -> {

                final Store store = environment.openStore(storeName, USE_EXISTING, txn);

                stringBuilder.append("Integer Store: ").append(store.getName()).append('\n')
                             .append("Configuration: ").append(store.getConfig()).append('\n');

                int count = 0;

                try (final Cursor cursor  = store.openCursor(txn)) {
                    while (cursor.getNext()) {
                        final String key = entryToString(cursor.getKey());
                        final Integer value = entryToInt(cursor.getValue());
                        stringBuilder.append("Record # ").append(count++).append(": ")
                                     .append(key).append(" -> ").append(value).append('\n');
                    }
                }

            });

            stringBuilder.append('\n');

            stores.stream().filter(TEXT_STORES::contains).forEach(storeName -> {

                final Store store = environment.openStore(storeName, USE_EXISTING, txn);

                stringBuilder.append("Text Store: ").append(store.getName()).append('\n')
                             .append("Configuration: ").append(store.getConfig()).append('\n');

                int count = 0;

                try (final Cursor cursor  = store.openCursor(txn)) {
                    while (cursor.getNext()) {
                        final String key = entryToString(cursor.getKey());
                        final String value = entryToString(cursor.getValue());
                        stringBuilder.append("Record # ").append(count++).append(": ")
                                     .append(key).append(" -> ").append(value).append('\n');
                    }
                }

            });

            stringBuilder.append('\n');

            return stringBuilder;

        });
    }

    public static void main(final String[] args) throws FileNotFoundException {
        try (final Environment environment = Environments.newInstance(args[0]);
             final PrintStream printWriter = getWriter(args)) {
            final StringBuilder builder = dumpStoreData(environment, new StringBuilder());
            printWriter.println(builder);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Must specify environment as first argument.");
        }
    }

    private static PrintStream getWriter(final String[] args) throws FileNotFoundException {
        try {
            final String path = args[1];
            final FileOutputStream fos = new FileOutputStream(path);
            return new PrintStream(fos);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return new PrintStream(System.out) {
                @Override
                public void close() {}
            };
        }
    }

}
