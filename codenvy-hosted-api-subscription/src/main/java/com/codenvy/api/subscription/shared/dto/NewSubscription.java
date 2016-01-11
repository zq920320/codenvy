/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
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

import com.codenvy.api.subscription.server.AbstractSubscriptionService;
import com.wordnik.swagger.annotations.ApiModelProperty;

import org.eclipse.che.dto.shared.DTO;

import java.util.Map;

/**
 * Describes subscription - a link between {@link AbstractSubscriptionService} and {@link
 * org.eclipse.che.api.account.server.dao.Account}
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@DTO
public interface NewSubscription {
    /* use object instead of primitive to avoid setting the default value on REST framework serialization/deserialization
     * that allow better validate data that was sent
    */

    @ApiModelProperty(value = "Account ID")
    String getAccountId();

    void setAccountId(String orgId);

    NewSubscription withAccountId(String orgId);

    @ApiModelProperty(value = "Plan ID")
    String getPlanId();

    void setPlanId(String id);

    NewSubscription withPlanId(String id);

    @ApiModelProperty(value = "Is payment system used")
    Boolean getUsePaymentSystem();

    void setUsePaymentSystem(Boolean usePaymentSystem);

    NewSubscription withUsePaymentSystem(Boolean usePaymentSystem);

    @ApiModelProperty(value = "Properties of subscription")
    Map<String, String> getProperties();

    void setProperties(Map<String, String> properties);

    NewSubscription withProperties(Map<String, String> properties);
}
