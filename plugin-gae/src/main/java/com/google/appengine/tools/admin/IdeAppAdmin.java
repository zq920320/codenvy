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
package com.google.appengine.tools.admin;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import javax.validation.constraints.NotNull;
import java.io.PrintWriter;
import java.io.Writer;

import static com.google.appengine.tools.admin.AppAdminFactory.ApplicationProcessingOptions;
import static com.google.appengine.tools.admin.AppAdminFactory.ConnectOptions;

/**
 * The application administration class of our App Engine applications. Use this class to get App Engine application and server connection.
 *
 * @author Andrey Parfonov
 * @author Valeriy Svydenko
 * @author Dmitry Shnurenko
 */
public class IdeAppAdmin extends AppAdminImpl {
    private static final Writer DUMMY_WRITER = new DummyWriter();
    private final GenericApplication app;

    @Inject
    public IdeAppAdmin(@Assisted ConnectOptions options, @Assisted GenericApplication app) {
        super(options, app, new PrintWriter(DUMMY_WRITER), new ApplicationProcessingOptions(), AppVersionUpload.class);
        this.app = app;
    }

    /** {@inheritDoc} */
    @Override
    protected ServerConnection getServerConnection(ConnectOptions options) {
        return new OAuth2ServerConnection(options);
    }

    /** @return an application that has an ability to be deployed to GAE */
    public GenericApplication getApplication() {
        return app;
    }

    private static class DummyWriter extends Writer {
        public void close() {
            // do nothing
        }

        public void flush() {
            // do nothing
        }

        public void write(@NotNull char[] cBuf, int off, int len) {
            // do nothing
        }
    }
}
