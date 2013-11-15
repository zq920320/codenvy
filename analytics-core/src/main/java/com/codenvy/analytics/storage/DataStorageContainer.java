/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.analytics.storage;

import com.codenvy.analytics.Configurator;

import javax.el.MethodNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class DataStorageContainer {

    private static final String ANALYTICS_STORAGE          = "analytics.storage";
    private static final String ANALYTICS_STORAGE_EMBEDDED = "analytics.storage.embedded";

    private static final AtomicReference<DataStorage> dataStorageRef;

    static {
        dataStorageRef = new AtomicReference<>();
    }

    /** {@link com.codenvy.analytics.storage.DataStorage#createDataLoader()} */
    public static DataLoader createDataLoader() {
        initDataStorage();
        try {
            return dataStorageRef.get().createDataLoader();
        } catch (IOException e) {
            throw new IllegalStateException("DataLoader can't be instantiated", e);
        }
    }

    /** {@link com.codenvy.analytics.storage.DataStorage#getStorageUrl()} */
    public static String getStorageUrl() {
        initDataStorage();
        return dataStorageRef.get().getStorageUrl();
    }

    private static void initDataStorage() {
        if (dataStorageRef.get() == null) {
            synchronized (dataStorageRef) {
                if (dataStorageRef.get() == null) {
                    try {
                        String clazz = Configurator.getString(ANALYTICS_STORAGE);
                        Constructor<?> constructor = Class.forName(clazz).getConstructor();
                        DataStorage dataStorage = (DataStorage)constructor.newInstance();

                        dataStorageRef.set(dataStorage);

                        if (Configurator.getBoolean(ANALYTICS_STORAGE_EMBEDDED)) {
                            dataStorage.initEmbeddedStorage();
                        }
                    } catch (MethodNotFoundException | ClassNotFoundException | NoSuchMethodException |
                            InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        throw new IllegalStateException("DataStorage can't be instantiated", e);
                    }
                }
            }
        }
    }
}
