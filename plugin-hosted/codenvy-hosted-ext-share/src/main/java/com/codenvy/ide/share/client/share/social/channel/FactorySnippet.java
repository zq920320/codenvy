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
package com.codenvy.ide.share.client.share.social.channel;

import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import com.codenvy.ide.share.client.ShareResources;
import com.codenvy.ide.share.client.share.SnippetPopup;
import com.codenvy.ide.share.client.share.social.SharingChannel;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.StringUnmarshaller;
import org.eclipse.che.ide.ui.zeroClipboard.ClipboardButtonBuilder;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;

/**
 * Factory snippet channel.
 *
 * @author Kevin Pollet
 */
public class FactorySnippet extends SharingChannel {
    public static final String HTML     = "html";
    public static final String IFRAME   = "iframe";
    public static final String MARKDOWN = "markdown";

    private final String                   title;
    private final String                   type;
    private final ShareResources           resources;
    private final ClipboardButtonBuilder   clipboardButtonBuilder;
    private final CoreLocalizationConstant coreLocale;
    private final FactoryServiceClient     factoryServiceClient;
    private final NotificationManager      notificationManager;

    public FactorySnippet(@NotNull SVGResource icon,
                          @NotNull String label,
                          @NotNull String title,
                          @NotNull String type,
                          @NotNull ShareResources resources,
                          @NotNull ClipboardButtonBuilder clipboardButtonBuilder,
                          @NotNull CoreLocalizationConstant coreLocale,
                          ClickHandler clickHandler,
                          @NotNull FactoryServiceClient factoryServiceClient,
                          @NotNull NotificationManager notificationManager) {
        super(icon, label, clickHandler);

        this.title = title;
        this.type = type;
        this.resources = resources;
        this.clipboardButtonBuilder = clipboardButtonBuilder;
        this.coreLocale = coreLocale;
        this.factoryServiceClient = factoryServiceClient;
        this.notificationManager = notificationManager;
    }

    @Override
    public void decorateWidget(@NotNull Widget element, @NotNull final String... params) {
        element.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                factoryServiceClient.getFactorySnippet(params[2], type, new AsyncRequestCallback<String>(new StringUnmarshaller()) {
                    @Override
                    protected void onSuccess(String snippet) {
                        new SnippetPopup(title, snippet, clipboardButtonBuilder, coreLocale, resources);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        handleError(exception);
                    }
                });
            }

        }, ClickEvent.getType());
    }

    private void handleError(@NotNull Throwable e) {
        final Notification notification = new Notification(e.getMessage(), ERROR);
        notificationManager.showNotification(notification);
    }
}
