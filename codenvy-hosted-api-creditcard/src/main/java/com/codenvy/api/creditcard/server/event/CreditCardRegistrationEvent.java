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
package com.codenvy.api.creditcard.server.event;

import com.braintreegateway.CreditCard;

import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Adding and removing credit card event.
 *
 * @author Max Shaposhnik
 */
@EventOrigin("creditcard")
public class CreditCardRegistrationEvent {
    public enum EventType {
        CREDIT_CARD_ADDED("credit card added"),
        CREDIT_CARD_REMOVED("credit card removed");

        private final String value;

        EventType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private EventType  type;
    private String     accountId;
    private String     userId;
    private CreditCard creditCard;

    public CreditCardRegistrationEvent(EventType type, String accountId, String userId, CreditCard creditCard) {
        this.type = type;
        this.accountId = accountId;
        this.creditCard = creditCard;
        this.userId = userId;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }

    public static CreditCardRegistrationEvent creditCardAddedEvent(String account, String userId, CreditCard creditCard) {
        return new CreditCardRegistrationEvent(EventType.CREDIT_CARD_ADDED, account, userId, creditCard);
    }

    public static CreditCardRegistrationEvent creditCardRemovedEvent(String account, String userId, CreditCard creditCard) {
        return new CreditCardRegistrationEvent(EventType.CREDIT_CARD_REMOVED, account, userId, creditCard);
    }
}
