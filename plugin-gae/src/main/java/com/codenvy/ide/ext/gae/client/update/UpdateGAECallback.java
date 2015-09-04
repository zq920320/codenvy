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
package com.codenvy.ide.ext.gae.client.update;

import javax.annotation.Nonnull;

/**
 * Callback is used for returning information about updating of project.
 *
 * @author Dmitry Shnurenko
 */
public interface UpdateGAECallback {

    /**
     * Method is called when build or deploy operation are success.
     *
     * @param message
     *         info message about build or deploy
     */
    void onSuccess(@Nonnull String message);

    /**
     * Method is called when build or deploy operation are failed.
     *
     * @param errorMessage
     *         info error message
     */
    void onFailure(@Nonnull String errorMessage);

}