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
package com.codenvy.factory.storage;

import com.codenvy.api.factory.AdvancedFactoryUrlImpl;
import com.codenvy.api.factory.FactoryImage;
import com.codenvy.api.factory.FactoryStore;
import com.codenvy.api.factory.FactoryUrlException;
import com.codenvy.api.factory.dto.AdvancedFactoryUrl;
import com.codenvy.commons.lang.NameGenerator;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InMemoryFactoryStore implements FactoryStore {
    private              Map<String, Set<FactoryImage>>  images    = new HashMap<>();
    private              Map<String, AdvancedFactoryUrl> factories = new HashMap<>();
    private static final ReentrantReadWriteLock          lock      = new ReentrantReadWriteLock();

    @Override
    public String saveFactory(AdvancedFactoryUrl factoryUrl, Set<FactoryImage> images) throws FactoryUrlException {
        lock.writeLock().lock();
        try {
            AdvancedFactoryUrl newFactoryUrl = new AdvancedFactoryUrlImpl(factoryUrl, null);
            newFactoryUrl.setId(NameGenerator.generate("", 16));
            Set<FactoryImage> newImages = new HashSet<>();
            for (FactoryImage image : images) {
                FactoryImage newImage =
                        new FactoryImage(Arrays.copyOf(image.getImageData(), image.getImageData().length), image.getMediaType(),
                                         image.getName());
                newImages.add(newImage);
            }

            factories.put(newFactoryUrl.getId(), newFactoryUrl);
            this.images.put(newFactoryUrl.getId(), newImages);

            return newFactoryUrl.getId();
        } catch (IOException e) {
            throw new FactoryUrlException(e.getLocalizedMessage(), e);
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
    public AdvancedFactoryUrl getFactory(String id) throws FactoryUrlException {
        lock.readLock().lock();
        try {
            return factories.get(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Set<FactoryImage> getFactoryImages(String factoryId, String imageId) throws FactoryUrlException {
        lock.readLock().lock();
        try {
            if (imageId == null)
                return images.get(factoryId);
            for (FactoryImage one : images.get(factoryId)) {
                if (one.getName().equals(imageId))
                    return new HashSet<>(java.util.Arrays.asList(one));
            }
            return Collections.emptySet();
        } finally {
            lock.readLock().unlock();
        }
    }
}
