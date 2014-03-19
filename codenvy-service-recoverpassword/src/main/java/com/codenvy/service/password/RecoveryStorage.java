/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2013] Codenvy, S.A.
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
package com.codenvy.service.password;

import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;

@Singleton
public class RecoveryStorage {
    public static final long VALIDATION_MAX_AGE_IN_MILLISECONDS = TimeUnit.HOURS.toMillis(1);

    private final ConcurrentMap<String, Map<String, String>> storage;
    private final Timer                                      timer;

    public RecoveryStorage() {
        this.storage = new ConcurrentHashMap<>();

        // Remove all invalid validation data once per hour;
        timer = new Timer("recovery-storage-timer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for (String uuid : storage.keySet()) {
                    if (!isValid(uuid)) {
                        storage.remove(uuid);
                    }
                }
            }
        }, 0, TimeUnit.HOURS.toMillis(1));
    }

    /**
     * Verify that data corresponds to uuid are in the storage and it added
     * not earlier then validationAgeInHours ago. If validationAgeInHours <=
     * 0, verify that data corresponds to uuid are in the storage only.
     *
     * @param uuid
     *         - unique identifier of validation data
     * @return - true if there is valid data, false otherwise
     */
    public boolean isValid(String uuid) {
        if (!storage.containsKey(uuid)) {
            return false;
        }

        if (VALIDATION_MAX_AGE_IN_MILLISECONDS > 0) {
            // verify token's age
            long creationTime = Long.valueOf(storage.get(uuid).get("creation.time"));
            long currentTime = System.currentTimeMillis();

            return (creationTime + VALIDATION_MAX_AGE_IN_MILLISECONDS) > currentTime;
        } else {
            return true;
        }
    }

    /**
     * Add validation data to storage.
     *
     * @return - uuid of stored data
     */
    public String setValidationData(String userName) {
        String uuid = UUID.randomUUID().toString();

        Map<String, String> validationData = new HashMap<>();

        validationData.put("user.name", userName);
        // save start validation time
        validationData.put("creation.time", Long.toString(System.currentTimeMillis()));

        storage.put(uuid, validationData);

        return uuid;
    }

    /**
     * Remove recovery data from storage by its uuid.
     *
     * @param uuid
     *         - unique identifier of validation data
     */
    public void remove(String uuid) {
        storage.remove(uuid);
    }

    /**
     * Get map with fields names as keys and fields values as values of the
     * map.
     *
     * @param uuid
     *         - unique identifier of validation data
     * @return - map with validation's parameters
     */
    public Map<String, String> get(String uuid) {
        return storage.get(uuid);
    }

    /**
     * Terminate storage and all stared threads.
     */
    public void suspend() {
        timer.cancel();
        storage.clear();
    }
}
