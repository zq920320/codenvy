/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.api.subscription.shared.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * Represents tariff plan
 *
 * @author Alexander Garagatyi
 */
@DTO
public interface Plan {
    /* use object instead of primitive to avoid setting the default value on REST framework serialization/deserialization
     * that allow better validate data that was sent
    */
    String getId();

    void setId(String id);

    Plan withId(String id);

    String getServiceId();

    void setServiceId(String serviceId);

    Plan withServiceId(String serviceId);

    Boolean isPaid();

    void setPaid(Boolean paid);

    Plan withPaid(Boolean paid);

    Boolean getSalesOnly();

    void setSalesOnly(Boolean salesOnly);

    Plan withSalesOnly(Boolean salesOnly);

    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    Plan withProperties(Map<String, String> properties);

    String getDescription();

    void setDescription(String description);

    Plan withDescription(String description);

    Integer getBillingCycle();

    void setBillingCycle(Integer cycle);

    Plan withBillingCycle(Integer cycle);

    BillingCycleType getBillingCycleType();

    void setBillingCycleType(BillingCycleType billingCycleType);

    Plan withBillingCycleType(BillingCycleType billingCycleType);

    Integer getBillingContractTerm();

    void setBillingContractTerm(Integer contractTerm);

    Plan withBillingContractTerm(Integer contractTerm);
}
