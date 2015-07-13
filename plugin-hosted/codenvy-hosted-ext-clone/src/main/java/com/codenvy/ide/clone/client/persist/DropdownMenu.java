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
package com.codenvy.ide.clone.client.persist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LIElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.UListElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.List;

/**
 * Simple dropdown menu. It can be used as popup menu also.
 *
 * @author Vitaliy Guliy
 */
public class DropdownMenu {

    interface DropdownMenuUiBinder extends UiBinder<Widget, DropdownMenu> {
    }

    DropdownMenuUiBinder dropdownMenuUiBinder = GWT.create(DropdownMenuUiBinder.class);

    public interface Styles extends CssResource {
        String divider();
    }

    @UiField
    Styles style;

    @UiField
    DivElement glassElement;

    @UiField
    UListElement ulElement;

    Widget widget;

    HandlerRegistration previewHandler;

    public DropdownMenu(List<String> items, int left, int top) {
        widget = dropdownMenuUiBinder.createAndBindUi(this);

        ulElement.removeAllChildren();
        for (String item : items) {
            LIElement liElement = Document.get().createLIElement();
            if (item.isEmpty()) {
                liElement.setClassName(style.divider());
            } else {
                liElement.setInnerHTML(item);
            }

            ulElement.appendChild(liElement);
        }

        RootLayoutPanel.get().add(widget);

        ulElement.getStyle().setLeft(left, Style.Unit.PX);
        ulElement.getStyle().setTop(top, Style.Unit.PX);

        previewHandler = Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
            @Override
            public void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                if (Event.ONKEYDOWN == event.getTypeInt()) {
                    close();
                }
            }
        });

        Event.sinkEvents(glassElement, Event.ONMOUSEDOWN);
        Event.setEventListener(glassElement, new EventListener() {
            @Override
            public void onBrowserEvent(Event event) {
                if (Event.ONMOUSEDOWN == event.getTypeInt()) {
                    close();
                }
            }
        });
    }

    private void close() {
        widget.removeFromParent();
        previewHandler.removeHandler();
    }
}
