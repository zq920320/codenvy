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
package com.codenvy.ide.hosted.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.extension.Extension;


/**
 * Extension which adds menu items for help menu, depending on the SubscriptionDescriptor
 *
 * @author Vitaly Parfonov
 */
@Singleton
@Extension(title = "HostedExtension", version = "1.0.0")
public class HostedExtension {
    /*not use here need for initialize*/
    private final HostedLocalizationConstant localizationConstant;

    /** Create extension. */
    @Inject
    public HostedExtension(HostedLocalizationConstant localizationConstant) {
        this.localizationConstant = localizationConstant;
    }


}
