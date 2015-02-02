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

import com.codenvy.dto.shared.DTO;import java.lang.String;

/**
 * Describes the nonce string from client.
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/27/15.
 *
 */
@DTO
public interface NewCreditCard {

    public String getNonce();

    void setNonce(String nonce);

    NewCreditCard withNonce(String nonce);

    public String getStreetAddress();

    void setStreetAddress(String streetAddress);

    NewCreditCard withStreetAddress(String streetAddress);

    public String getCity();

    void setCity(String city);

    NewCreditCard withCity(String city);

    public String getState();

    void setState(String state);

    NewCreditCard withState(String state);

    public String getCountry();

    void setCountry(String country);

    NewCreditCard withCountry(String country);
}
