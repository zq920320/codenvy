/*
 * CODENVY CONFIDENTIAL
 * __________________
 * 
 *  [2012] - [2014] Codenvy, S.A. 
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
package com.codenvy.subscription;

import com.codenvy.api.account.server.SubscriptionAttributesValidator;
import com.codenvy.api.account.shared.dto.NewBilling;
import com.codenvy.api.account.shared.dto.NewSubscriptionAttributes;
import com.codenvy.api.core.ConflictException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Validates {@link com.codenvy.api.account.server.dao.SubscriptionAttributes}
 *
 * @author Alexander Garagatyi
 */
public class SubscriptionAttributesValidatorImpl implements SubscriptionAttributesValidator {
    @Override
    public void validate(NewSubscriptionAttributes subscriptionAttributes) throws ConflictException {
        if (null == subscriptionAttributes) {
            throw new ConflictException("Subscription attributes required");
        }
        final NewBilling billing = subscriptionAttributes.getBilling();
        if (null == billing) {
            throw new ConflictException("Subscription attribute billing required");
        }

        if (billing.getUsePaymentSystem() == null || billing.getUsePaymentSystem().isEmpty()) {
            throw new ConflictException("Billing attribute usePaymentSystem required");
        }

        if (!"true".equals(billing.getUsePaymentSystem()) && !"false".equals(billing.getUsePaymentSystem())) {
            throw new ConflictException("Value of billing attribute usePaymentSystem is invalid");
        }

        if (subscriptionAttributes.getDescription() == null || subscriptionAttributes.getDescription().isEmpty()) {
            throw new ConflictException("Subscription attribute description required");
        }

        // if usePaymentSystem is true this field will be filled from payment system response
        if ("false".equals(billing.getUsePaymentSystem()) && subscriptionAttributes.getTrialDuration() == null) {
            throw new ConflictException("Subscription attribute trial duration required");
        }

        if (subscriptionAttributes.getStartDate() == null || subscriptionAttributes.getStartDate().isEmpty()) {
            throw new ConflictException("Subscription attribute startDate required");
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(subscriptionAttributes.getStartDate());
        } catch (ParseException e) {
            throw new ConflictException("Value of subscription attribute startDate is invalid");
        }

        if (subscriptionAttributes.getEndDate() == null || subscriptionAttributes.getEndDate().isEmpty()) {
            throw new ConflictException("Subscription attribute endDate required");
        }
        try {
            dateFormat.parse(subscriptionAttributes.getEndDate());
        } catch (ParseException e) {
            throw new ConflictException("Value of subscription attribute endDate is invalid");
        }

        if ("true".equals(billing.getUsePaymentSystem()) && (billing.getPaymentToken() == null || billing.getPaymentToken().isEmpty())) {
            throw new ConflictException("Billing attribute paymentToken required");
        }

        // if usePaymentSystem is true this field will be filled from payment system response
        if ("false".equals(billing.getUsePaymentSystem())) {
            if ((billing.getStartDate() == null || billing.getStartDate().isEmpty())) {
                throw new ConflictException("Billing attribute startDate required");
            }
            try {
                dateFormat.parse(billing.getStartDate());
            } catch (ParseException e) {
                throw new ConflictException("Value of billing attribute startDate is invalid");
            }
        }

        if (billing.getEndDate() == null || billing.getEndDate().isEmpty()) {
            throw new ConflictException("Billing attribute endDate required");
        }
        try {
            dateFormat.parse(billing.getEndDate());
        } catch (ParseException e) {
            throw new ConflictException("Value of billing attribute endDate is invalid");
        }

        if (billing.getCycle() == null) {
            throw new ConflictException("Billing attribute cycle required");
        }

        if (billing.getCycleType() == null) {
            throw new ConflictException("Billing attribute cycleType required");
        }

        if (billing.getContractTerm() == null) {
            throw new ConflictException("Billing attribute contractTerm required");
        }
    }
}
