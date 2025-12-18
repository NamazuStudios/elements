package dev.getelements.elements.service.receipt;

import dev.getelements.elements.sdk.dao.ReceiptDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.receipt.CreateReceiptRequest;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.service.receipt.ReceiptService;
import jakarta.inject.Inject;

public class SuperuserReceiptService implements ReceiptService {

    private ReceiptDao receiptDao;

    private UserDao userDao;

    @Override
    public Pagination<Receipt> getReceipts(String userId, int offset, int count, String search) {

        final var user = userId != null && !userId.isEmpty() ? userDao.getUser(userId) : null;

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
    public Receipt createReceipt(CreateReceiptRequest createReceiptRequest) {

        final var user = getUserDao().getUser(createReceiptRequest.getUserId());
        final var receipt = new Receipt();

        receipt.setUser(user);
        receipt.setBody(createReceiptRequest.getBody());
        receipt.setSchema(createReceiptRequest.getSchema());
        receipt.setOriginalTransactionId(createReceiptRequest.getOriginalTransactionId());
        receipt.setPurchaseTime(createReceiptRequest.getPurchaseTime());

        return getReceiptDao().createReceipt(receipt);
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

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
