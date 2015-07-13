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
package com.codenvy.ide.subscriptions.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Kevin Pollet
 */
public interface SubscriptionsResources extends ClientBundle {
    interface SubscriptionsCSS extends CssResource {
        String queueTypeIndicator();

        String panel();

        String tooltip();

        String tooltipHeader();

        String tooltipBody();

        String bottomMenuTooltip();

        String bottomMenuTooltipBody();

        String bottomMenuTooltipHeader();

        String centerContent();

        String subscriptionTitle();

        String memoryIndicator();

        String indicatorBackground();

        String usedMemory();

        String totalMemory();
    }

    @Source({"Subscriptions.css", "org/eclipse/che/ide/api/ui/style.css"})
    SubscriptionsCSS subscriptionsCSS();

    @Source("subscriptions/memory.svg")
    SVGResource memory();

    @Source("subscriptions/dedicated-queue.svg")
    SVGResource dedicatedQueue();

    @Source("subscriptions/shared-queue.svg")
    SVGResource sharedQueue();
}
