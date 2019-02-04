package com.namazustudios.socialengine.service.appleiap;

import com.namazustudios.socialengine.dao.AppleIapReceiptDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;

import javax.inject.Inject;

public class UserAppleIapReceiptService implements AppleIapReceiptService {

    protected User user;

    protected AppleIapReceiptDao appleIapReceiptDao;

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public AppleIapReceiptDao getAppleIapReceiptDao() {
        return appleIapReceiptDao;
    }

    @Inject
    public void setAppleIapReceiptDao(AppleIapReceiptDao appleIapReceiptDao) {
        this.appleIapReceiptDao = appleIapReceiptDao;
    }

    @Override
    public Pagination<AppleIapReceipt> getAppleIapReceipts(User user, int offset, int count) {
        return appleIapReceiptDao.getAppleIapReceipts(user, offset, count);
    }

    @Override
    public AppleIapReceipt getAppleIapReceipt(String originalTransactionIdentifier) {
        return appleIapReceiptDao.getAppleIapReceipt(originalTransactionIdentifier);
    }

    @Override
    public AppleIapReceipt createAppleIapReceipt(AppleIapReceipt appleIapReceipt) {
        return appleIapReceiptDao.createAppleIapReceipt(appleIapReceipt);
    }

    @Override
    public void deleteAppleIapReceipt(String originalTransactionIdentifier) {
        appleIapReceiptDao.deleteAppleIapReceipt(originalTransactionIdentifier);
    }

}
