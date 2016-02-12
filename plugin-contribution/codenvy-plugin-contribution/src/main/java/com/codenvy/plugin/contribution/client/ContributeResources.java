/*
 *  [2012] - [2016] Codenvy, S.A.
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
