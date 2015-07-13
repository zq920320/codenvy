/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client;

import com.google.gwt.resources.client.ClientBundle;

import org.eclipse.che.ide.ui.Styles;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Contributor plugin resources.
 */
public interface ContributeResources extends ClientBundle {
    @Source({"Contribute.css", "org/eclipse/che/ide/api/ui/style.css", "org/eclipse/che/ide/ui/Styles.css"})
    ContributeCss contributeCss();

    @Source("images/ok.svg")
    SVGResource statusOkIcon();

    @Source("images/error.svg")
    SVGResource statusErrorIcon();

    @Source("images/refresh.svg")
    SVGResource refreshIcon();

    interface ContributeCss extends Styles {
        String blueButton();

        String errorMessage();
    }
}
