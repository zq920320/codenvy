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

/**
 * @author Sergii Kabashniuk
 */
public enum PaymentState {
    WAITING_EXECUTOR(1),

    EXECUTING(2),

    PAYMENT_FAIL(8),

    CREDIT_CARD_MISSING(16),

    PAID_SUCCESSFULLY(32);


    private final int state;

    private PaymentState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public PaymentState fromState(int state) {
        switch (state) {
            case 1:
                return WAITING_EXECUTOR;
            case 2:
                return EXECUTING;
            case 8:
                return PAYMENT_FAIL;
            case 16:
                return CREDIT_CARD_MISSING;
            case 32:
                return PAID_SUCCESSFULLY;
        }
        throw new RuntimeException("Unknown Payment state : " + state);
    }
}
