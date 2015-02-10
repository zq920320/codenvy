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
    WAITING_EXECUTOR("waiting"),

    EXECUTING("executing"),

    PAYMENT_FAIL("fail"),

    CREDIT_CARD_MISSING("nocc"),

    PAID_SUCCESSFULLY("successful");


    private final String state;

    private PaymentState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public PaymentState fromState(String state) {
        switch (state) {
            case "waiting":
                return WAITING_EXECUTOR;
            case "executing":
                return EXECUTING;
            case "fail":
                return PAYMENT_FAIL;
            case "nocc":
                return CREDIT_CARD_MISSING;
            case "successful":
                return PAID_SUCCESSFULLY;
        }
        throw new RuntimeException("Unknown Payment state : " + state);
    }
}
