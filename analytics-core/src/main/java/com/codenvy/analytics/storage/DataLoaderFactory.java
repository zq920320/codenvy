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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class DataLoaderFactory {

    private static final String DATA_LOADER = "data.loader";

    private static AtomicReference<DataLoader> dataLoaderRef = new AtomicReference<>();

    public static DataLoader createDataLoader() {
        try {
            if (dataLoaderRef.get() == null) {
                synchronized (dataLoaderRef) {
                    if (dataLoaderRef.get() == null) {
                        String clazz = Configurator.getString(DATA_LOADER);
                        Constructor<?> constructor = Class.forName(clazz).getConstructor();
                        DataLoader dataLoader = (DataLoader)constructor.newInstance();

                        dataLoaderRef.set(dataLoader);
                    }
                }
            }
        } catch (MethodNotFoundException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("DataLoader can't be instantiated", e);
        }

        return dataLoaderRef.get();
    }
}
