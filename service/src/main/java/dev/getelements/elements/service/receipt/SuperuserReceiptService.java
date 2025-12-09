package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.receipt.ReceiptService;
import jakarta.inject.Inject;

public class SuperuserReceiptService implements ReceiptService {

    private ReceiptDao receiptDao;

    @Override
    public Pagination<Receipt> getReceipts(User user, int offset, int count, String search) {

        if(search == null || search.isEmpty()) {
            return receiptDao.getReceipts(user, offset, count);
        }

        return receiptDao.getReceipts(user, offset, count, search);
    }

    @Override
    public Receipt getReceiptById(String id) {
        return receiptDao.getReceipt(id);
    }

    @Override
    public Receipt getReceiptBySchemaAndTransactionId(String schema, String originalTransactionId) {
        return receiptDao.getReceipt(schema, originalTransactionId);
    }

    @Override
    public Receipt createReceipt(Receipt receipt) {
        return receiptDao.createReceipt(receipt);
    }

    @Override
    public void deleteReceipt(String receiptId) {
        receiptDao.deleteReceipt(receiptId);
    }

    public ReceiptDao getReceiptDao() {
        return receiptDao;
    }

    @Inject
    public void setReceiptDao(ReceiptDao receiptDao) {
        this.receiptDao = receiptDao;
    }
}
