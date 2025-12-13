package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.receipt.ReceiptService;
import jakarta.inject.Inject;

public class UserReceiptService implements ReceiptService {

    private ReceiptDao receiptDao;

    //We only want to use the current user, so we ignore anything passed in for this permission level
    private User user;

    @Override
    public Pagination<Receipt> getReceipts(String ignored, int offset, int count, String search) {

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
        throw new ForbiddenException("User receipt creation not allowed via this API");
    }

    @Override
    public void deleteReceipt(String receiptId) {
        throw new ForbiddenException("User receipt deletion not allowed via this API");
    }

    public ReceiptDao getReceiptDao() {
        return receiptDao;
    }

    @Inject
    public void setReceiptDao(ReceiptDao receiptDao) {
        this.receiptDao = receiptDao;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }
}
