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
package com.codenvy.api.creditcard.shared.dto;

import org.eclipse.che.dto.shared.DTO;

/**
 * Describes the new credit card request.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com) on 1/27/15.
 */
@DTO
public interface NewCreditCard {
    String getNonce();

    void setNonce(String nonce);

    NewCreditCard withNonce(String nonce);

    String getStreetAddress();

    void setStreetAddress(String streetAddress);

    NewCreditCard withStreetAddress(String streetAddress);

    String getCity();

    void setCity(String city);

    NewCreditCard withCity(String city);

    String getState();

    void setState(String state);

    NewCreditCard withState(String state);

    String getCountry();

    void setCountry(String country);

    NewCreditCard withCountry(String country);
}
