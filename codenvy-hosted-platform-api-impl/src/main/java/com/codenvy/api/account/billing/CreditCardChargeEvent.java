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
package com.codenvy.api.account.billing;

import com.codenvy.api.core.notification.EventOrigin;

import java.math.BigDecimal;

/**
 * Adding, removing and charging credit card event.
 * @author Max Shaposhnik
 *
 */
@EventOrigin("creditcard")
public class CreditCardChargeEvent {

    public enum EventType {
        CREDIT_CARD_CHARGE_SUCCESS("credit card charged successfully"),
        CREDIT_CARD_CHARGE_FAILED("credit card charge fail");

        private final String value;

        private EventType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private EventType type;

    private String account;

    private String creditCardNumber;

    private String subscriptionId;

    private Double price;

    public CreditCardChargeEvent(EventType type, String account, String creditCardNumber, String subscriptionId, Double price) {
        this.type = type;
        this.account = account;
        this.creditCardNumber = creditCardNumber;
        this.subscriptionId = subscriptionId;
        this.price = price;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public static CreditCardChargeEvent creditCardChargeSuccessEvent(String account, String creditCardNumber, String subscriptionId, Double price) {
        return new CreditCardChargeEvent(EventType.CREDIT_CARD_CHARGE_SUCCESS, account, creditCardNumber, subscriptionId, price);
    }
    public static CreditCardChargeEvent creditCardChargeFailedEvent(String account, String creditCardNumber, String subscriptionId, Double price) {
        return new CreditCardChargeEvent(EventType.CREDIT_CARD_CHARGE_FAILED, account, creditCardNumber, subscriptionId, price);
    }

    @Override
    public String toString() {
        return "CreditCardChargeEvent{" +
               "type=" + type +
               ", account='" + account + '\'' +
               ", creditCardNumber='" + creditCardNumber + '\'' +
               ", price='" + price + '\'' +
               ", subscriptionId='" + subscriptionId + '\'' +
               '}';
    }

}
