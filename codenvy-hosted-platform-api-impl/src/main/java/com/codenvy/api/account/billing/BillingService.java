/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2015] Codenvy, S.A. 
 *  All Rights Reserved.
 * 
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.api.account.billing;

import com.codenvy.api.account.shared.dto.Receipt;
import com.codenvy.api.core.ServerException;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Sergii Kabashniuk
 */
public interface BillingService {
    void generateReceipts(String billingPeriodId) throws ServerException;

    List<Receipt> getUnpaidReceipt(int limit) throws ServerException;

    void setPaidStatus(long receiptId, int status) throws ServerException;

    List<Receipt> getNotSendReceipt(int limit) throws ServerException;

    void markReceiptAsSent(long receiptId) throws ServerException;
}
