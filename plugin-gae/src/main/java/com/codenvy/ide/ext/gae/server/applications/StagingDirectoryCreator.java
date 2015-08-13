/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.server.applications;

import com.google.appengine.tools.admin.AppAdminFactory;
import com.google.appengine.tools.admin.Application;
import com.google.appengine.tools.admin.ResourceLimits;

import org.apache.commons.codec.binary.Base64;

import java.lang.reflect.Method;

/**
 * @author Sergii Kabashniuk
 * @author Sergii Leschenko
 */
public class StagingDirectoryCreator {
    public static ResourceLimits getLimits() throws Exception {
        Method method = ResourceLimits.class.getDeclaredMethod("newDefaultResourceLimits");
        method.setAccessible(true);
        return (ResourceLimits)method.invoke(null);
    }

    public static void main(String[] args) throws Exception {
        Application app = Application.readApplication(args[0]);
        AppAdminFactory.ApplicationProcessingOptions options = new AppAdminFactory.ApplicationProcessingOptions();
        final String stagDirectoryPath = app.createStagingDirectory(options, getLimits()).getAbsolutePath();
        System.out.println(Base64.encodeBase64String(stagDirectoryPath.getBytes()));
        System.out.println(Base64.encodeBase64String(app.getApiVersion().getBytes()));
        System.out.println(Base64.encodeBase64String(app.getAppYaml().getBytes()));
    }
}
