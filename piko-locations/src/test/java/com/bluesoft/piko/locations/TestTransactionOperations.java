package com.bluesoft.piko.locations;

import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

public class TestTransactionOperations implements TransactionOperations {

    public static final TestTransactionOperations INSTANCE = new TestTransactionOperations();

    private TestTransactionOperations() {
    }

    @Override
    public <T> T execute(TransactionCallback<T> transactionCallback) throws TransactionException {
        return transactionCallback.doInTransaction(new SimpleTransactionStatus(false));
    }

}
