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
package com.codenvy.api.resources.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Kevin Pollet
 */
public interface PanelResources extends ClientBundle {

    interface ResourcesCSS extends CssResource {
        String panel();

        String bottomMenuTooltip();

        String bottomMenuTooltipBody();

        String bottomMenuTooltipHeader();

        String memoryIndicator();

        String indicatorBackground();

        String usedMemory();

        String totalMemory();
    }

    @Source({"Resources.css", "org/eclipse/che/ide/api/ui/style.css"})
    ResourcesCSS resourcesCSS();

    @Source("memory.svg")
    SVGResource memory();
}
