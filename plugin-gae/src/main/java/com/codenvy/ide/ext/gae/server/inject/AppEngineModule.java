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
package com.codenvy.ide.ext.gae.server.inject;

import org.eclipse.che.api.project.server.ValueProvider;
import org.eclipse.che.api.project.server.ValueProviderFactory;
import org.eclipse.che.api.project.server.handlers.ProjectHandler;
import org.eclipse.che.api.project.server.type.ProjectType;
import org.eclipse.che.inject.DynaModule;

import com.codenvy.ide.ext.gae.server.AppAdminExceptionMapper;
import com.codenvy.ide.ext.gae.server.generators.JavaGaeProjectGenerator;
import com.codenvy.ide.ext.gae.server.generators.PhpGaeProjectGenerator;
import com.codenvy.ide.ext.gae.server.generators.PythonGaeProjectGenerator;
import com.codenvy.ide.ext.gae.server.inject.factories.AppEngineValueProviderFactory;
import com.codenvy.ide.ext.gae.server.inject.factories.ApplicationFactory;
import com.codenvy.ide.ext.gae.server.inject.factories.IdeAppAdminFactory;
import com.codenvy.ide.ext.gae.server.projecttype.GaeMavenProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.GaePhpProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.GaeProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.GaePythonProjectType;
import com.codenvy.ide.ext.gae.server.projecttype.valueprovider.AppEngineValueProvider;
import com.codenvy.ide.ext.gae.server.projecttype.valueprovider.AppEngineWebXmlValueProviderFactory;
import com.codenvy.ide.ext.gae.server.rest.AppEngineService;
import com.codenvy.ide.ext.gae.server.rest.GAEParametersService;
import com.codenvy.ide.ext.gae.server.rest.GAEValidateService;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.Multibinder;

/**
 * @author Evgen Vidolob
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 * @author Vitalii Parfonov
 */
@DynaModule
public class AppEngineModule extends AbstractModule {
    /** {@inheritDoc} */
    @Override
    protected void configure() {
        bind(AppEngineService.class);
        bind(GAEParametersService.class);
        bind(GAEValidateService.class);

        // It is important to add this bind. In other case mapper will be not in the container.
        //noinspection PointlessBinding
        bind(AppAdminExceptionMapper.class);

        Multibinder.newSetBinder(binder(), ProjectType.class).addBinding().to(GaeProjectType.class);
        Multibinder.newSetBinder(binder(), ProjectType.class).addBinding().to(GaeMavenProjectType.class);
        Multibinder.newSetBinder(binder(), ProjectType.class).addBinding().to(GaePhpProjectType.class);
        Multibinder.newSetBinder(binder(), ProjectType.class).addBinding().to(GaePythonProjectType.class);

        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(JavaGaeProjectGenerator.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(PhpGaeProjectGenerator.class);
        Multibinder.newSetBinder(binder(), ProjectHandler.class).addBinding().to(PythonGaeProjectGenerator.class);

        Multibinder.newSetBinder(binder(), ValueProviderFactory.class).addBinding().to(AppEngineWebXmlValueProviderFactory.class);

        install(new FactoryModuleBuilder().implement(ValueProvider.class, AppEngineValueProvider.class)
                                          .build(AppEngineValueProviderFactory.class));
        install(new FactoryModuleBuilder().build(ApplicationFactory.class));
        install(new FactoryModuleBuilder().build(IdeAppAdminFactory.class));
    }
}