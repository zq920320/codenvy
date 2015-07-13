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

import org.eclipse.che.ide.api.DocumentTitleDecorator;

/**
 * @author Vitaly Parfonov
 */
public class HostedEnvDocumentTitleDecorator implements DocumentTitleDecorator {

    private HostedLocalizationConstant localizationConstant;

    @Inject
    public HostedEnvDocumentTitleDecorator(HostedLocalizationConstant localizationConstant) {
        this.localizationConstant = localizationConstant;
    }

    @Override
    public String getDocumentTitle() {
        return localizationConstant.codenvyTabTitle();
    }

    @Override
    public String getDocumentTitle(String project) {
        return localizationConstant.codenvyTabTitle(project);
    }

}
