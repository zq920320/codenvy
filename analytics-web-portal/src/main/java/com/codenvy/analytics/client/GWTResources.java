/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface GWTResources extends ClientBundle {
    
    public static final GWTResources INSTANCE = GWT.create(GWTResources.class);

    @Source("com/codenvy/analytics/client/ui/gwt-style.css")
    Style css();

    /**
     * CSS styles for GWT components.
     */
    public interface Style extends CssResource
    {
        /**
         * @return {@link String} checkbox style
         */
        String checkBox();

        /**
         * @return {@link String} checkbox disabled state style
         */
        String checkBoxDisabled();

        /**
         * @return {@link String} checkbox when title is at the left side
         */
        String checkBoxTitleLeft();

        /**
         * @return {@link String} radio button's style
         */
        String radioButton();

        /**
         * @return {@link String} radio button's style when disabled
         */
        String radioButtonDisabled();

        /**
         * @return {@link String} text box's style
         */
        String textBox();

        /**
         * @return {@link String} text box's style when has focus
         */
        String textBoxFocused();

        /**
         * @return {@link String} text box's style in disabled state
         */
        String textBoxDisabled();

        String transparent();

        String loaderBackground();

        String loaderCenteredContent();

        String loaderImage();
    }


    @Source("com/codenvy/analytics/client/ui/images/component/loader/ajax-loader.gif")
    ImageResource getLoaderImage();

    @Source("com/codenvy/analytics/client/ui/images/component/loader/loader-background-element.png")
    ImageResource getLoaderBackgroudElement();
}
