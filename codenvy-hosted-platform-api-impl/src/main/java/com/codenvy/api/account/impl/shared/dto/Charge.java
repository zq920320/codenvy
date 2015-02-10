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

import java.util.Map;

/**
 * @author Sergii Kabashniuk
 */
@DTO
public interface Charge {
    String getServiceId();

    void setServiceId(String serviceId);

    Charge withServiceId(String serviceId);


    Double getPaidAmount();

    void setPaidAmount(Double amount);

    Charge withPaidAmount(Double amount);


    Double getPaidPrice();

    void setPaidPrice(Double paidPrice);

    Charge withPaidPrice(Double paidPrice);


    Double getFreeAmount();

    void setFreeAmount(Double freeAmount);

    Charge withFreeAmount(Double freeAmount);


    Double getPrePaidAmount();

    void setPrePaidAmount(Double freeAmount);

    Charge withPrePaidAmount(Double freeAmount);


    Map<String, String> getDetails();

    void setDetails(Map<String, String> details);

    Charge withDetails(Map<String, String> details);

}
