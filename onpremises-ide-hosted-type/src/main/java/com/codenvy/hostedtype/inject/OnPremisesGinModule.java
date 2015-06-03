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

package com.codenvy.hostedtype.inject;

import com.codenvy.ide.subscriptions.client.OnPremisesChecker;
import com.google.gwt.inject.client.AbstractGinModule;
import com.codenvy.hostedtype.OnPremisesCheckerImpl;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;

import javax.inject.Singleton;

/**
 * This Gin module is initializing in onpremises-ide-compiling-war-ide-codenvy package.
 * It is needed for identifying onpremises version ide.
 *
 * @author Igor Vinokur
 */
@ExtensionGinModule
public class OnPremisesGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        bind(OnPremisesChecker.class).to(OnPremisesCheckerImpl.class).in(Singleton.class);
    }
}