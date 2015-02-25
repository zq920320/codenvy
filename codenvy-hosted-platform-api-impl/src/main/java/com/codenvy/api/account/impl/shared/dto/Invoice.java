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

import java.util.List;

/**
 * Describe invoice
 *
 * @author Sergii Kabashniuk
 */
@DTO
public interface Invoice {
    String getAccountId();

    void setAccountId(String accountId);

    Invoice withAccountId(String accountId);


    Long getId();

    void setId(Long id);

    Invoice withId(Long id);


    Double getTotal();

    void setTotal(Double total);

    Invoice withTotal(Double total);


    long getCreationDate();

    void setCreationDate(long creationDate);

    Invoice withCreationDate(long creationDate);


    long getFromDate();

    void setFromDate(long fromDate);

    Invoice withFromDate(long fromDate);


    long getUntilDate();

    void setUntilDate(long untilDate);

    Invoice withUntilDate(long untilDate);


    String getCreditCardId();

    void setCreditCardId(String creditCardId);

    Invoice withCreditCardId(String creditCardId);


    String getPaymentState();

    void setPaymentState(String paymentState);

    Invoice withPaymentState(String paymentState);


    Long getPaymentDate();

    void setPaymentDate(Long paymentDate);

    Invoice withPaymentDate(Long paymentDate);


    Long getMailingDate();

    void setMailingDate(Long mailingDate);

    Invoice withMailingDate(Long mailingDate);


    List<Charge> getCharges();

    void setCharges(List<Charge> charges);

    Invoice withCharges(List<Charge> charges);

}
