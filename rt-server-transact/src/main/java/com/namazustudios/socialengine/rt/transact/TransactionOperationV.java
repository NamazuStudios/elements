package com.namazustudios.socialengine.rt.transact;

@FunctionalInterface
interface TransactionOperationV {

    void apply(ReadWriteTransaction txn) throws TransactionConflictException;

}
