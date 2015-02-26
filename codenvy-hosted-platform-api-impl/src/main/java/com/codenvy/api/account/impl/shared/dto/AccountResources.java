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

import com.codenvy.dto.shared.DTO;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface AccountResources extends Resources {
    String getAccountId();

    void setAccountId(String accountId);

    AccountResources withAccountId(String accountId);

    @Override
    AccountResources withPaidAmount(Double amount);

    @Override
    AccountResources withFreeAmount(Double freeAmount);

    @Override
    AccountResources withPrePaidAmount(Double freeAmount);

}
