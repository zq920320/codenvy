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
package com.codenvy.factory;

import com.codenvy.api.factory.AdvancedFactoryUrl;
import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.Image;
import com.codenvy.api.factory.store.FactoryStore;
import com.codenvy.api.factory.store.SavedFactoryData;
import com.codenvy.commons.lang.NameGenerator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryFactoryStore implements FactoryStore {
    //private              Map<String, Image>            images    = new HashMap<>();
    private              Map<String, SavedFactoryData> factories = new HashMap<>();
    private static final ReentrantReadWriteLock        lock      = new ReentrantReadWriteLock();

    @Override
    public SavedFactoryData saveFactory(AdvancedFactoryUrl factoryUrl, Set<Image> images) throws FactoryUrlException {
        lock.writeLock().lock();
        try {
            factoryUrl.setId(NameGenerator.generate("", 16));
            Set<Image> newImages = new HashSet<>();
            for (Image image : images) {
                image.setName(NameGenerator.generate("", 16) + image.getName());
                newImages.add(image);
            }

            SavedFactoryData factoryData = new SavedFactoryData(factoryUrl, newImages);
            factories.put(factoryUrl.getId(), factoryData);
            return factoryData;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeFactory(String id) throws FactoryUrlException {
        lock.writeLock().lock();
        try {
            factories.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public SavedFactoryData getFactory(String id) throws FactoryUrlException {
        lock.readLock().lock();
        try {
            return factories.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }
}
