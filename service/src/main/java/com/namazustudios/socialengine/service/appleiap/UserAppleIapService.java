package com.namazustudios.socialengine.service.appleiap;

import com.namazustudios.socialengine.dao.AppleIapReceiptDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.appleiapreceipt.AppleIapReceipt;
import com.namazustudios.socialengine.model.mission.Mission;

import javax.inject.Inject;

public class UserAppleIapService implements AppleIapService {

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
