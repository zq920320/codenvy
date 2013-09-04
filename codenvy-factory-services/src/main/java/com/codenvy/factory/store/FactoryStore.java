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
package com.codenvy.factory.store;

import com.codenvy.factory.commons.AdvancedFactoryUrl;
import com.codenvy.factory.commons.FactoryUrlException;
import com.codenvy.factory.commons.Image;

import java.util.Map;

public interface FactoryStore {
    public Map<String, Object> saveFactory(AdvancedFactoryUrl factoryUrl, Image image) throws FactoryUrlException;

    public Map<String, Object> getFactory(String id) throws FactoryUrlException;

    public Image getImage(String id) throws FactoryUrlException;
}
