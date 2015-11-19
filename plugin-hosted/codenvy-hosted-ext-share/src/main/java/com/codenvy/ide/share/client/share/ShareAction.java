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

import org.eclipse.che.api.factory.dto.Button;
import org.eclipse.che.api.factory.dto.ButtonAttributes;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.api.project.shared.dto.ImportSourceDescriptor;
import org.eclipse.che.api.project.shared.dto.NewProject;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.project.shared.dto.Source;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.git.client.GitRepositoryInitializer;
import com.codenvy.ide.share.client.ShareLocalizationConstant;
import com.codenvy.ide.share.client.ShareResources;
import com.codenvy.ide.share.client.share.social.Item;
import com.codenvy.ide.share.client.share.social.channel.Facebook;
import com.codenvy.ide.share.client.share.social.channel.FactorySnippet;
import com.codenvy.ide.share.client.share.social.channel.GooglePlus;
import com.codenvy.ide.share.client.share.social.channel.Snippet;
import com.codenvy.ide.share.client.share.social.channel.Twitter;

import org.eclipse.che.ide.rest.HTTPHeader;
import org.eclipse.che.ide.ui.zeroClipboard.ClipboardButtonBuilder;
import org.eclipse.che.ide.util.loging.Log;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.eclipse.che.ide.api.notification.Notification.Type.ERROR;
import static com.codenvy.ide.share.client.share.CommitPresenter.CommitActionHandler;
import static com.codenvy.ide.share.client.share.ShareActionView.ActionDelegate;
import static com.google.gwt.http.client.RequestBuilder.POST;
import static java.util.Arrays.asList;

/**
 * Action for sharing project and cloning template.
 *
 * @author Kevin Pollet
 */
@Singleton
public class ShareAction extends Action implements CustomComponentAction, ActionDelegate, CommitActionHandler {

    public static final String SHARE_ACTION_ID = "shareAction";

    private final AppContext                context;
    private final ShareActionView           view;
    private final ShareResources            resources;
    private final ShareLocalizationConstant locale;
    private final DtoFactory                dtoFactory;
    private final GitRepositoryInitializer  gitRepositoryInitializer;
    private final NotificationManager       notificationManager;
    private final CoreLocalizationConstant  coreLocale;
    private final FactoryServiceClient      factoryServiceClient;
    private final ClipboardButtonBuilder    clipboardButtonBuilder;
    private final CommitPresenter           commitPresenter;
    private final ClickHandler              onSharingChannelClickHandler;

    @Inject
    public ShareAction(AppContext context,
                       final ShareActionView view,
                       ShareResources resources,
                       ShareLocalizationConstant locale,
                       DtoFactory dtoFactory,
                       GitRepositoryInitializer gitRepositoryInitializer,
                       NotificationManager notificationManager,
                       CoreLocalizationConstant coreLocale,
                       FactoryServiceClient factoryServiceClient,
                       ClipboardButtonBuilder clipboardButtonBuilder,
                       CommitPresenter commitPresenter) {
        this.context = context;
        this.view = view;
        this.resources = resources;
        this.locale = locale;
        this.dtoFactory = dtoFactory;
        this.gitRepositoryInitializer = gitRepositoryInitializer;
        this.notificationManager = notificationManager;
        this.coreLocale = coreLocale;
        this.factoryServiceClient = factoryServiceClient;
        this.clipboardButtonBuilder = clipboardButtonBuilder;
        this.commitPresenter = commitPresenter;
        this.onSharingChannelClickHandler = new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                view.hideDropDown();
            }
        };

        this.commitPresenter.setCommitActionHandler(this);
        this.view.setDelegate(this);
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setVisible(context.getCurrentProject() != null && !context.getWorkspace().isTemporary());
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        return view.asWidget();
    }

    @Override
    public void onMouseOver() {
        if (!view.isDropDownVisible()) {
            view.showTooltip();
        }
    }

    @Override
    public void onMouseOut() {
        view.hideTooltip();
    }

    @Override
    public void onClick() {
        if (!view.isDropDownVisible()) {
            view.hideTooltip();

            if (commitPresenter.hasUncommittedChanges()) {
                commitPresenter.showView();
            } else {
                view.showItemsToShareDropDown(getItemsToShare());
            }

        } else {
            view.hideDropDown();
            view.showTooltip();
        }
    }

    @Override
    public void onCommitAction(CommitAction action) {
        view.showItemsToShareDropDown(getItemsToShare());
    }

    @Override
    public void onItemToShareClick(@NotNull final Item item) {
        final CurrentProject currentProject = context.getCurrentProject();
        if (currentProject != null) {

            final ProjectDescriptor projectDescriptor = currentProject.getRootProject();
            if (item instanceof Project) {
                final String projectButtonURL = new UrlBuilder().setProtocol(Window.Location.getProtocol())
                                                                .setHost(Window.Location.getHost())
                                                                .setPath("factory/resources/factory-white.png")
                                                                .buildString();

                final String projectShareURL = new UrlBuilder().setProtocol(Window.Location.getProtocol())
                                                               .setHost(Window.Location.getHost())
                                                               .setPath("ide-resources/share/project/" +
                                                                        projectDescriptor.getWorkspaceName() + "/" +
                                                                        projectDescriptor.getName())
                                                               .buildString();

                view.showSharingChannelsDropDown(item, projectDescriptor.getName(), projectShareURL, projectButtonURL);

            } else {
                getFactory(new AsyncCallback<Factory>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        handleError(caught);
                    }

                    @Override
                    public void onSuccess(Factory factory) {
                        final String factoryShareURL = new UrlBuilder().setProtocol(Window.Location.getProtocol())
                                                                       .setHost(Window.Location.getHost())
                                                                       .setPath("ide-resources/share/factory/" + factory.getId())
                                                                       .buildString();

                        view.showSharingChannelsDropDown(item, projectDescriptor.getTypeName(), factoryShareURL, factory.getId());
                    }
                });
            }

        } else {
            view.hideDropDown();
        }
    }

    @Override
    public void onSharingChannelsBackClick() {
        view.showItemsToShareDropDown(getItemsToShare());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private void getFactory(final AsyncCallback<Factory> callback) {
        final CurrentProject currentProject = context.getCurrentProject();
        if (currentProject == null) {
            Log.error(getClass(), "Open the project before sharing a factory");
            return;
        }

        final NewProject factoryProject = dtoFactory.createDto(NewProject.class);
        factoryProject.setName(currentProject.getRootProject().getName());
        factoryProject.setType(currentProject.getRootProject().getType());
        factoryProject.setRunners(currentProject.getRootProject().getRunners());
        factoryProject.setBuilders(currentProject.getRootProject().getBuilders());
        factoryProject.setVisibility(currentProject.getRootProject().getVisibility());
        factoryProject.setMixins(currentProject.getRootProject().getMixins());

        final Factory factory = dtoFactory.createDto(Factory.class);
        factory.setV("2.0");
        factory.setProject(factoryProject);

        final ButtonAttributes buttonAttributes = dtoFactory.createDto(ButtonAttributes.class)
                                                            .withColor("white")
                                                            .withCounter(false)
                                                            .withStyle("vertical");

        factory.setButton(dtoFactory.createDto(Button.class)
                                    .withType(Button.ButtonType.nologo)
                                    .withAttributes(buttonAttributes));

        gitRepositoryInitializer.getGitUrlWithAutoInit(currentProject.getRootProject(), new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                final ImportSourceDescriptor importSourceDescriptor = dtoFactory.createDto(ImportSourceDescriptor.class);
                importSourceDescriptor.setLocation(result);
                importSourceDescriptor.setType("git");

                factory.setSource(dtoFactory.createDto(Source.class).withProject(importSourceDescriptor));

                final String boundary = Long.toString(new Random().nextLong(), 36);
                final String data = "--" + boundary + "\r\n"
                                    + "Content-Disposition: form-data; name=\"factoryUrl\"\r\n\r\n"
                                    + dtoFactory.toJson(factory)
                                    + "\r\n--" + boundary + "--";

                final RequestBuilder request = new RequestBuilder(POST, "/api/factory");
                request.setHeader(HTTPHeader.CONTENT_TYPE, "multipart/form-data; boundary=" + boundary);

                try {

                    request.sendRequest(data, new RequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            callback.onSuccess(dtoFactory.createDtoFromJson(response.getText(), Factory.class));
                        }

                        @Override
                        public void onError(Request request, Throwable exception) {
                            handleError(exception);
                        }
                    });

                } catch (RequestException e) {
                    handleError(e);
                }
            }

            @Override
            public void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private List<Item> getItemsToShare() {
        final List<Item> itemsToShare = new ArrayList<>();
        final CurrentProject currentProject = context.getCurrentProject();

        if (currentProject != null) {
            itemsToShare.add(new Project());

            if (!currentProject.isReadOnly()) {
                itemsToShare.add(new CloningTemplate());
            }
        }

        return itemsToShare;
    }

    private void handleError(@NotNull Throwable e) {
        final Notification notification = new Notification(e.getMessage(), ERROR);
        notificationManager.showNotification(notification);
    }

    /*
     * Items to share
     */
    private class Project extends Item {

        public Project() {
            super(
                    locale.shareButtonDropDownShareProjectName(),
                    resources.shareProject(),
                    locale.shareButtonDropDownShareProjectText(),
                    asList(new Facebook(locale.socialShareProjectFacebookMessage(), resources, locale,
                                        onSharingChannelClickHandler),
                           new GooglePlus(locale.socialShareProjectMessage(), resources, locale, onSharingChannelClickHandler),
                           new Twitter(locale.socialShareProjectMessage(), resources, locale, onSharingChannelClickHandler),
                           newSnippet(resources.iFrame(), locale.socialShareChannelIFrameText(),
                                      locale.socialShareChannelIFrameSnippetTitle(),
                                      locale.socialShareChannelIFrameSnippetTemplate()),
                           newSnippet(resources.html(), locale.socialShareChannelHtmlText(),
                                      locale.socialShareChannelHtmlSnippetTitle(),
                                      locale.socialShareChannelHtmlSnippetTemplate()),
                           newSnippet(resources.github(), locale.socialShareChannelGitHubText(),
                                      locale.socialShareChannelGitHubSnippetTitle(),
                                      locale.socialShareChannelGitHubSnippetTemplate()),
                           newSnippet(resources.bitbucket(), locale.socialShareChannelBitbucketText(),
                                      locale.socialShareChannelBitbucketSnippetTitle(),
                                      locale.socialShareChannelBitbucketSnippetTemplate()))
                 );
        }
    }

    private Snippet newSnippet(@NotNull SVGResource icon, @NotNull String label, @NotNull String title, @NotNull String type) {
        return new Snippet(icon, label, title, type, onSharingChannelClickHandler, resources, clipboardButtonBuilder, coreLocale);
    }

    private class CloningTemplate extends Item {

        public CloningTemplate() {
            super(
                    locale.shareButtonDropDownShareCloningTemplateName(),
                    resources.cloneIcon(),
                    locale.shareButtonDropDownShareCloningTemplateText(),
                    asList(new Facebook(locale.socialShareCloningTemplateFacebookMessage(), resources, locale,
                                        onSharingChannelClickHandler),
                           new GooglePlus(locale.socialShareCloningTemplateMessage(), resources, locale,
                                          onSharingChannelClickHandler),
                           new Twitter(locale.socialShareCloningTemplateMessage(), resources, locale,
                                       onSharingChannelClickHandler),
                           newFactorySnippet(resources.iFrame(), locale.socialShareChannelIFrameText(),
                                             locale.socialShareChannelIFrameSnippetTitle(), FactorySnippet.IFRAME),
                           newFactorySnippet(resources.html(), locale.socialShareChannelHtmlText(),
                                             locale.socialShareChannelHtmlSnippetTitle(), FactorySnippet.HTML),
                           newFactorySnippet(resources.github(), locale.socialShareChannelGitHubText(),
                                             locale.socialShareChannelGitHubSnippetTitle(), FactorySnippet.MARKDOWN),
                           newFactorySnippet(resources.bitbucket(), locale.socialShareChannelBitbucketText(),
                                             locale.socialShareChannelBitbucketSnippetTitle(), FactorySnippet.MARKDOWN))
                 );
        }
    }

    private FactorySnippet newFactorySnippet(@NotNull SVGResource icon, @NotNull String label, @NotNull String title,
                                             @NotNull String type) {
        return new FactorySnippet(icon, label, title, type, resources, clipboardButtonBuilder, coreLocale, onSharingChannelClickHandler,
                                  factoryServiceClient, notificationManager);
    }
}
