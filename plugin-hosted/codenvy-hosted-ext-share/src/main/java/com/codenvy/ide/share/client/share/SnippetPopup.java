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
package com.codenvy.ide.share.client.share;

import com.codenvy.ide.share.client.ShareResources;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.ui.zeroClipboard.ClipboardButtonBuilder;
import org.eclipse.che.ide.util.browser.UserAgent;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;

import static com.google.gwt.dom.client.Style.Unit.PX;
import static com.google.gwt.dom.client.Style.VerticalAlign.MIDDLE;
import static com.google.gwt.event.dom.client.KeyCodes.KEY_C;

/**
 * Popup for displaying snippets with content selected for easy copy operation.
 *
 * @author Ann Shumilova
 * @author Kevin Pollet
 */
public class SnippetPopup extends Window {

    public SnippetPopup(String title,
                        String content,
                        ClipboardButtonBuilder clipboardButtonBuilder,
                        CoreLocalizationConstant coreLocale,
                        ShareResources shareResources) {
        setTitle(title);

        final TextArea textArea = new TextArea();
        textArea.setStyleName(shareResources.shareCSS().input());
        textArea.getElement().getStyle().setWidth(330, PX);
        textArea.getElement().getStyle().setHeight(80, PX);
        textArea.getElement().getStyle().setProperty("display", "table-cell");
        textArea.getElement().getStyle().setVerticalAlign(MIDDLE);
        textArea.getElement().getStyle().setMargin(10, PX);
        textArea.getElement().getStyle().setPadding(5, PX);
        textArea.setValue(content);
        textArea.addKeyDownHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (((UserAgent.isMac() && event.isMetaKeyDown()) || event.isControlKeyDown()) && event.getNativeKeyCode() == KEY_C) {
                    hide();
                }
            }
        });

        final FlowPanel clipboard = new FlowPanel();
        clipboard.getElement().getStyle().setProperty("display", "table-cell");
        clipboard.getElement().getStyle().setVerticalAlign(MIDDLE);
        clipboardButtonBuilder.withParentWidget(clipboard)
                              .withResourceWidget(textArea)
                              .build();


        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.getElement().getStyle().setProperty("display", "table");
        flowPanel.add(textArea);
        flowPanel.add(clipboard);

        final Button btnOk = createButton(coreLocale.ok(), "snippet-popup-ok", new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        btnOk.addStyleName(resources.centerPanelCss().blueButton());

        setWidget(flowPanel);
        getFooter().add(btnOk);
        show();

        new Timer() {
            @Override
            public void run() {
                textArea.setFocus(true);
                textArea.selectAll();
            }
        }.schedule(200);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
    }
}
