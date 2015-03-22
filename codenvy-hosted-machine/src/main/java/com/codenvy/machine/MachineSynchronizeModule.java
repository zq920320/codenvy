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
package com.codenvy.machine;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import org.eclipse.che.inject.DynaModule;

/**
 * @author Alexander Garagatyi
 */
@DynaModule
public class MachineSynchronizeModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                        .implement(org.eclipse.che.api.machine.server.SynchronizeTask.class, SynchronizeTaskImpl.class)
                        .build(org.eclipse.che.api.machine.server.SynchronizeTaskFactory.class));

        bind(com.codenvy.machine.SynchronizeEventListener.class).asEagerSingleton();
    }
}
