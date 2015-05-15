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
package com.codenvy.api.account.impl.shared.dto;

import com.codenvy.api.account.billing.Bonus;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface NewBonus {
    String getAccountId();

    void setAccountId(String accountId);

    NewBonus withAccountId(String accountId);

    long getFromDate();

    void setFromDate(long fromDate);

    NewBonus withFromDate(long fromDate);

    long getTillDate();

    void setTillDate(long tillDate);

    NewBonus withTillDate(long tillDate);

    Double getResources();

    void setResources(Double resources);

    NewBonus withResources(Double resources);

    String getCause();

    void setCause(String cause);

    NewBonus withCause(String cause);
}
