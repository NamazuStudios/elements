package dev.getelements.elements.rt.transact;

@FunctionalInterface
interface TransactionOperationV {

    void apply(ReadWriteTransaction txn) throws TransactionConflictException;

}
