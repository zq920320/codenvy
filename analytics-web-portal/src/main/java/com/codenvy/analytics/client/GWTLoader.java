/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.client;

import com.codenvy.analytics.client.resources.GWTResources;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 */
public class GWTLoader {
    private static final String LOADING_DATA_MESSAGE = "Loading data ...";
    private static final String LOADER_ID            = "GWTLoaderId";
    private PopupPanel          loader;

    public GWTLoader() {
        GWTResources.INSTANCE.css().ensureInjected();

        FlowPanel loaderContent = new FlowPanel();
        loaderContent.getElement().setId(LOADER_ID);
        DOM.setStyleAttribute(loaderContent.getElement(), "backgroundImage", "url("
                                                                             + GWTResources.INSTANCE.getLoaderBackgroudElement()
                                                                                                    .getURL() + ")");
        loaderContent.setStyleName(GWTResources.INSTANCE.css().loaderCenteredContent());

        Image image = new Image(GWTResources.INSTANCE.getLoaderImage());
        image.setStyleName(GWTResources.INSTANCE.css().loaderImage());

        Element messageElement = DOM.createSpan();
        messageElement.setInnerHTML(LOADING_DATA_MESSAGE);

        loaderContent.add(image);
        loaderContent.getElement().appendChild(com.google.gwt.dom.client.Document.get().createBRElement());
        loaderContent.getElement().appendChild(messageElement);

        loader = new PopupPanel();
        loader.setWidget(loaderContent);
        loader.setStyleName(GWTResources.INSTANCE.css().loaderBackground());
    }

    /**
     * Showing loader.
     */
    public void show() {
        loader.show();
        DOM.setStyleAttribute(loader.getElement(), "zIndex", (Integer.MAX_VALUE - 1) + "");
    }

    /**
     * Hiding loader.
     */
    public void hide() {
        loader.hide();
    }
}
