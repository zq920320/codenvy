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

/**
 * Adding, removing and charging credit card event.
 * @author Max Shaposhnik
 *
 */
@EventOrigin("creditcard")
public class CreditCardRegistrationEvent {

    public enum EventType {
        CREDIT_CARD_ADDED("credit card added"),
        CREDIT_CARD_REMOVED("credit card removed");

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

    private String userId;

    public CreditCardRegistrationEvent(EventType type, String account, String creditCardNumber, String userId) {
        this.type = type;
        this.account = account;
        this.creditCardNumber = creditCardNumber;
        this.userId = userId;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static CreditCardRegistrationEvent creditCardAddedEvent(String account, String creditCardNumber, String userId) {
        return new CreditCardRegistrationEvent(EventType.CREDIT_CARD_ADDED, account, creditCardNumber, userId);
    }

    public static CreditCardRegistrationEvent creditCardRemovedEvent(String account, String creditCardNumber, String userId) {
        return new CreditCardRegistrationEvent(EventType.CREDIT_CARD_REMOVED, account, creditCardNumber, userId);
    }


    @Override
    public String toString() {
        return "CreditCardRegistrationEvent{" +
               "type=" + type +
               ", account='" + account + '\'' +
               ", creditCardNumber='" + creditCardNumber + '\'' +
               ", userId='" + userId + '\'' +
               '}';
    }

}
