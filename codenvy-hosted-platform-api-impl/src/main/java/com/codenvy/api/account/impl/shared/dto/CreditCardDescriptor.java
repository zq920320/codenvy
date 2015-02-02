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

import com.codenvy.api.core.rest.shared.dto.Hyperlinks;
import com.codenvy.api.core.rest.shared.dto.Link;
import com.codenvy.dto.shared.DTO;

import java.lang.String;import java.util.List;

/**
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/29/15.
 *
 */
@DTO
public interface CreditCardDescriptor extends Hyperlinks {
    String getNumber();

    void setNumber(String number);

    CreditCardDescriptor withNumber(String number);

    String getExpiration();

    void setExpiration(String expiration);

    CreditCardDescriptor withExpiration(String expiration);

    String getAccountId();

    void setAccountId(String accountId);

    CreditCardDescriptor withAccountId(String accountId);

    String getCardholder();

    void setCardholder(String cardholder);

    CreditCardDescriptor withCardholder(String cardholder);

    String getType();

    void setType(String type);

    CreditCardDescriptor withType(String type);

    CreditCardDescriptor withLinks(List<Link> links);
}
