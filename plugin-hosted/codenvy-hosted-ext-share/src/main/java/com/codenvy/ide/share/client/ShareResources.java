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
package com.codenvy.ide.share.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Factory extension resources (css styles, images).
 *
 * @author Ann Shumilova
 */
public interface ShareResources extends ClientBundle {
    interface ShareCSS extends CssResource {
        String link();

        String input();

        String tooltip();

        String social();

        String mainMenuBarButton();

        String mainMenuBarButtonClicked();

        String buttonTooltip();

        String shareButton();

        String shareButtonTooltip();

        String shareDropDown();

        String shareDropDownWithHeader();

        String shareDropDownHeader();

        String shareDropDownContent();
    }

    @Source({"Share.css", "org/eclipse/che/ide/api/ui/style.css"})
    ShareCSS shareCSS();

    @Source("share/clone.svg")
    SVGResource cloneIcon();

    @Source("share/html.svg")
    SVGResource html();

    @Source("share/github.svg")
    SVGResource github();

    @Source("share/facebook.svg")
    SVGResource facebook();

    @Source("share/google-plus.svg")
    SVGResource googlePlus();

    @Source("share/twitter.svg")
    SVGResource twitter();

    @Source("share/email.svg")
    SVGResource email();

    @Source("share/share.svg")
    SVGResource shareButton();

    @Source("share/back.svg")
    SVGResource backButton();

    @Source("share/share-project.svg")
    SVGResource shareProject();

    @Source("share/bitbucket.svg")
    SVGResource bitbucket();

    @Source("share/iframe.svg")
    SVGResource iFrame();
}
