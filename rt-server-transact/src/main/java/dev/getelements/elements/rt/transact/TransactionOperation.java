package dev.getelements.elements.rt.transact;

@FunctionalInterface
interface TransactionOperation<ReadWriteTransaction, ReturnT> {

    ReturnT apply(ReadWriteTransaction txn) throws TransactionConflictException;

}
