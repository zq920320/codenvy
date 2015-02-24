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
package com.codenvy.api.account;

import com.codenvy.api.core.notification.EventOrigin;

/**
 * Account locking/unlocking event.
 * @author Max Shaposhnik
 *
 */
@EventOrigin("accountlock")
public class AccountLockEvent {

    public enum EventType {
        ACCOUNT_LOCKED("account locked"),
        ACCOUNT_UNLOCKED("account unlocked");

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
    private String    account;

    public AccountLockEvent(EventType type, String account) {
        this.type = type;
        this.account = account;
    }

    public AccountLockEvent() {
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

    public static AccountLockEvent accountLockedEvent(String account) {
        return new AccountLockEvent(EventType.ACCOUNT_LOCKED, account);
    }

    public static AccountLockEvent accountUnlockedEvent(String account) {
        return new AccountLockEvent(EventType.ACCOUNT_UNLOCKED, account);
    }


    @Override
    public String toString() {
        return "AccountLockEvent{" +
               "type=" + type +
               ", account='" + account + '\'' +
               '}';
    }
}
