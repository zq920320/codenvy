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
package com.codenvy.api.subscription.saas.shared.dto;

import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * @author Sergii Leschenko
 */
@DTO
public interface BonusDescriptor extends Hyperlinks {
    Long getId();

    void setId(Long id);

    BonusDescriptor withId(Long Id);

    String getAccountId();

    void setAccountId(String accountId);

    BonusDescriptor withAccountId(String accountId);

    long getFromDate();

    void setFromDate(long fromDate);

    BonusDescriptor withFromDate(long fromDate);

    long getTillDate();

    void setTillDate(long tillDate);

    BonusDescriptor withTillDate(long tillDate);

    Double getResources();

    void setResources(Double resources);

    BonusDescriptor withResources(Double resources);

    String getCause();

    void setCause(String cause);

    BonusDescriptor withCause(String cause);

    @Override
    BonusDescriptor withLinks(List<Link> links);
}
