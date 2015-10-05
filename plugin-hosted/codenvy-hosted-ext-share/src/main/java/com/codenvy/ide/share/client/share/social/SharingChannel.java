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
package com.codenvy.ide.share.client.share.social;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * Sharing channel.
 */
public abstract class SharingChannel {
    private final SVGResource  icon;
    private final String       label;
    private final ClickHandler clickHandler;

    /**
     * Constructs an instance of {@link SharingChannel}.
     *
     * @param icon
     *         the sharing channel icon, must not be {@code null}.
     * @param label
     *         the sharing channel label, must not be {@code null}.
     * @param clickHandler
     *         handler called when the sharing channel is clicked.
     */
    public SharingChannel(@NotNull SVGResource icon, @NotNull String label, ClickHandler clickHandler) {
        this.icon = icon;
        this.label = label;
        this.clickHandler = clickHandler;
    }

    /**
     * Returns the sharing channel icon, never {@code null}.
     *
     * @return the sharing channel icon.
     */
    public
    @NotNull
    SVGResource getIcon() {
        return icon;
    }

    /**
     * Returns the sharing channel label, never {@code null}.
     *
     * @return the sharing channel label.
     */
    public
    @NotNull
    String getLabel() {
        return label;
    }

    /**
     * Can be used to decorate the sharing channel widget.
     *
     * @param element
     *         the sharing channel widget, never {@code null}.
     * @param params
     *         parameters used for decoration.
     */
    public final void decorate(@NotNull Widget element, @NotNull String... params) {
        if (clickHandler != null) {
            element.addDomHandler(clickHandler, ClickEvent.getType());
        }
        decorateWidget(element, params);
    }

    /**
     * Can be used to decorate the sharing channel widget.
     *
     * @param element
     *         the sharing channel widget, never {@code null}.
     * @param params
     *         parameters used for decoration.
     */
    protected void decorateWidget(@NotNull Widget element, @NotNull String... params) {
    }

    /**
     * Utility method used to render a template.
     *
     * @param template
     *         the template to render, must not be {@code null}.
     * @param params
     *         the template parameters.
     * @return the rendered template.
     */
    protected String render(@NotNull String template, String... params) {
        String renderedTemplate = template;
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                renderedTemplate = renderedTemplate.replaceAll("\\{" + i + "\\}", params[i]);
            }
        }
        return renderedTemplate;
    }
}
