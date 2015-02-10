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
 * @author Sergii Kabashniuk
 */
@DTO
public interface Charge {
    String getServiceId();

    void setServiceId(String serviceId);

    Charge withServiceId(String serviceId);


    String getType();

    void setType(String type);

    Charge withType(String type);


    Double getAmount();

    void setAmount(Double amount);

    Charge withAmount(Double amount);


    Double getPrice();

    void setPrice(Double usedPrice);

    Charge withPrice(Double usedPrice);


}
