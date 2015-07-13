/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.jso.Blob;
import com.codenvy.plugin.contribution.client.jso.FormData;
import com.codenvy.plugin.contribution.client.jso.JsBlob;
import com.codenvy.plugin.contribution.client.utils.FactoryHelper;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingService;
import com.codenvy.plugin.contribution.vcs.client.hosting.VcsHostingServiceProvider;
import com.google.gwt.http.client.Header;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.project.shared.dto.ImportSourceDescriptor;
import org.eclipse.che.api.project.shared.dto.RunnerSource;
import org.eclipse.che.api.project.shared.dto.Source;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.commons.exception.ServerException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.rest.HTTPMethod;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.GENERATE_REVIEW_FACTORY;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_MODE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.PULL_REQUEST_ID_VARIABLE_NAME;
import static java.util.Arrays.asList;
import static org.eclipse.che.api.project.shared.Constants.VCS_PROVIDER_NAME;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * Generates a factory for the contribution reviewer.
 */
public class GenerateReviewFactoryStep implements Step {
    private final Step                      addReviewFactoryLinkStep;
    private final ContributeMessages        messages;
    private final ApiUrlTemplate            apiTemplate;
    private final DtoFactory                dtoFactory;
    private final AsyncRequestFactory       asyncRequestFactory;
    private final DtoUnmarshallerFactory    dtoUnmarshallerFactory;
    private final AppContext                appContext;
    private final VcsHostingServiceProvider vcsHostingServiceProvider;
    private final NotificationHelper        notificationHelper;

    @Inject
    public GenerateReviewFactoryStep(@Nonnull final AddReviewFactoryLinkStep addReviewFactoryLinkStep,
                                     @Nonnull final ApiUrlTemplate apiUrlTemplate,
                                     @Nonnull final ContributeMessages messages,
                                     @Nonnull final DtoFactory dtoFactory,
                                     @Nonnull final DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                     @Nonnull final AsyncRequestFactory asyncRequestFactory,
                                     @Nonnull final AppContext appContext,
                                     @Nonnull final VcsHostingServiceProvider vcsHostingServiceProvider,
                                     @Nonnull final NotificationHelper notificationHelper) {
        this.addReviewFactoryLinkStep = addReviewFactoryLinkStep;
        this.apiTemplate = apiUrlTemplate;
        this.messages = messages;
        this.asyncRequestFactory = asyncRequestFactory;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.vcsHostingServiceProvider = vcsHostingServiceProvider;
        this.notificationHelper = notificationHelper;
    }

    /**
     * Sends the request, passing the form data as content.
     *
     * @param xhr
     *         the request
     * @param formData
     *         the form data
     * @return true if the request was sent correctly - Note: doesn't mean the request will be successful
     */
    private static native boolean sendFormData(XMLHttpRequest xhr, FormData formData) /*-{
        try {
            xhr.send(formData);
            return true;
        } catch (e) {
            return false;
        }
    }-*/;

    @Override
    public void execute(@Nonnull final ContributorWorkflow workflow) {
        createFactory(workflow.getContext(), new AsyncCallback<Factory>() {
            @Override
            public void onSuccess(final Factory factory) {
                // find factory URL inside factory
                workflow.getContext().setReviewFactoryUrl(FactoryHelper.getCreateProjectRelUrl(factory));
                workflow.fireStepDoneEvent(GENERATE_REVIEW_FACTORY);
                workflow.setStep(addReviewFactoryLinkStep);
                workflow.executeStep();
            }

            @Override
            public void onFailure(final Throwable exception) {
                notificationHelper.showWarning(messages.stepGenerateReviewFactoryErrorCreateFactory());
                workflow.fireStepDoneEvent(GENERATE_REVIEW_FACTORY);
                workflow.setStep(addReviewFactoryLinkStep);
                workflow.executeStep();
            }
        });
    }

    private void createFactory(final Context context, final AsyncCallback<Factory> callback) {
        getFactory(context, new AsyncCallback<Factory>() {
            @Override
            public void onSuccess(final Factory factory) {
                final String factoryJson = dtoFactory.toJson(factory);
                final FormData formData = FormData.create();

                final Blob blob = JsBlob.create(factoryJson);
                formData.append("factoryUrl", blob);
                saveFactory(formData, callback);
            }

            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private void getFactory(final Context context, final AsyncCallback<Factory> callback) {
        exportProject(new AsyncCallback<Factory>() {
            @Override
            public void onSuccess(final Factory factory) {
                getSource(context, new AsyncCallback<Source>() {
                    @Override
                    public void onFailure(final Throwable exception) {
                        callback.onFailure(exception);
                    }

                    @Override
                    public void onSuccess(final Source source) {
                        factory.setSource(source);

                        // project must be public to be shared
                        factory.getProject().setVisibility("public");

                        // new factory is not a 'contribute workflow factory'
                        factory.getProject().getAttributes().remove(CONTRIBUTE_VARIABLE_NAME);

                        // new factory is in a review mode
                        factory.getProject().getAttributes().put(CONTRIBUTE_MODE_VARIABLE_NAME, asList("review"));

                        // remember the related pull request id
                        factory.getProject().getAttributes().put(PULL_REQUEST_ID_VARIABLE_NAME, asList("notUsed"));

                        callback.onSuccess(factory);
                    }
                });
            }

            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    private void exportProject(final AsyncCallback<Factory> callback) {
        final String workspaceId = appContext.getWorkspace().getId();

        final CurrentProject currentProject = appContext.getCurrentProject();
        if (currentProject != null) {
            final String projectName = currentProject.getRootProject().getName();
            final String requestUrl = apiTemplate.getFactoryJson(workspaceId, projectName);

            asyncRequestFactory.createGetRequest(requestUrl)
                               .header(ACCEPT, APPLICATION_JSON)
                               .send(new AsyncRequestCallback<Factory>(dtoUnmarshallerFactory.newUnmarshaller(Factory.class)) {

                                   @Override
                                   protected void onSuccess(final Factory result) {
                                       callback.onSuccess(result);
                                   }

                                   @Override
                                   protected void onFailure(final Throwable exception) {
                                       callback.onFailure(exception);
                                   }
                               });
        }
    }

    private void getSource(final Context context, final AsyncCallback<Source> callback) {
        vcsHostingServiceProvider.getVcsHostingService(new AsyncCallback<VcsHostingService>() {
            @Override
            public void onFailure(final Throwable exception) {
                callback.onFailure(exception);
            }

            @Override
            public void onSuccess(final VcsHostingService vcsHostingService) {
                final Source source = dtoFactory.createDto(Source.class);
                final ImportSourceDescriptor importSourceDescriptor = dtoFactory.createDto(ImportSourceDescriptor.class);

                final String forkRepoUrl =
                        vcsHostingService.makeSSHRemoteUrl(context.getHostUserLogin(), context.getForkedRepositoryName());
                importSourceDescriptor.setLocation(forkRepoUrl);

                final String vcsType = context.getProject().getAttributes().get(VCS_PROVIDER_NAME).get(0);
                importSourceDescriptor.setType(vcsType);

                importSourceDescriptor.setParameters(new HashMap<String, String>());

                // keep some origin factory settings
                if (appContext.getFactory() != null) {
                    final Factory originFactory = appContext.getFactory();

                    final String keepDirectory = originFactory.getSource().getProject().getParameters().get("keepDirectory");
                    if (keepDirectory != null) {
                        importSourceDescriptor.getParameters().put("keepDirectory", keepDirectory);
                    }

                    final Map<String, RunnerSource> runners = originFactory.getSource().getRunners();
                    if (runners != null && !runners.isEmpty()) {
                        source.setRunners(runners);
                    }
                }

                // keep VCS information
                importSourceDescriptor.getParameters().put("keepVcs", "true");

                // Use the contribution branch
                importSourceDescriptor.getParameters().put("branch", context.getWorkBranchName());

                callback.onSuccess(source.withProject(importSourceDescriptor));
            }
        });
    }

    private void saveFactory(final FormData formData, final AsyncCallback<Factory> callback) {
        final String requestUrl = apiTemplate.saveFactory();

        final XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.open(HTTPMethod.POST, requestUrl);
        xhr.setRequestHeader(ACCEPT, APPLICATION_JSON);
        xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {

            @Override
            public void onReadyStateChange(final XMLHttpRequest request) {
                if (request.getReadyState() == XMLHttpRequest.DONE) {
                    if (request.getStatus() == Response.SC_OK) {
                        request.clearOnReadyStateChange();
                        final String payLoad = request.getResponseText();
                        final Factory createdFactory = dtoFactory.createDtoFromJson(payLoad, Factory.class);

                        if (createdFactory.getId() == null || createdFactory.getId().isEmpty()) {
                            final ServiceError error = dtoFactory.createDtoFromJson(payLoad, ServiceError.class);
                            callback.onFailure(new Exception(error.getMessage()));
                        } else {
                            callback.onSuccess(createdFactory);
                        }
                    } else {
                        final Response response = new ResponseImpl(request);
                        callback.onFailure(new ServerException(response));
                    }
                }

            }
        });

        if (!sendFormData(xhr, formData)) {
            callback.onFailure(new Exception("Could not call service"));
        }
    }

    /**
     * Template for building api urls.
     */
    interface ApiUrlTemplate extends Messages {
        /**
         * Returns a 'getFactoryJson' call URL.
         *
         * @param workspaceId
         *         the workspace id
         * @param projectName
         *         the project name
         * @return the call URL
         */
        @DefaultMessage("/api/factory/{0}/{1}")
        String getFactoryJson(String workspaceId, String projectName);

        /**
         * Returns a 'getFactoryJson'/saveFactory call URL.
         *
         * @return the call URL
         */
        @DefaultMessage("/api/factory")
        String saveFactory();
    }

    /**
     * Concrete {@link Response}, {@link #getHeaders()} copied from GWT internal.
     */
    private class ResponseImpl extends Response {
        private final XMLHttpRequest request;

        ResponseImpl(final XMLHttpRequest request) {
            this.request = request;
        }

        @Override
        public String getText() {
            return request.getResponseText();
        }

        @Override
        public String getStatusText() {
            return request.getStatusText();
        }

        @Override
        public int getStatusCode() {
            return request.getStatus();
        }

        @Override
        public String getHeadersAsString() {
            return request.getAllResponseHeaders();
        }

        @Override
        public Header[] getHeaders() {
            final String allHeaders = request.getAllResponseHeaders();
            final String[] unparsedHeaders = allHeaders.split("\n");
            final Header[] parsedHeaders = new Header[unparsedHeaders.length];

            for (int i = 0, n = unparsedHeaders.length; i < n; ++i) {
                final String unparsedHeader = unparsedHeaders[i];

                if (unparsedHeader.length() == 0) {
                    continue;
                }

                final int endOfNameIdx = unparsedHeader.indexOf(':');
                if (endOfNameIdx < 0) {
                    continue;
                }

                final String name = unparsedHeader.substring(0, endOfNameIdx).trim();
                final String value = unparsedHeader.substring(endOfNameIdx + 1).trim();
                final Header header = new Header() {
                    @Override
                    public String getName() {
                        return name;
                    }

                    @Override
                    public String getValue() {
                        return value;
                    }

                    @Override
                    public String toString() {
                        return name + " : " + value;
                    }
                };

                parsedHeaders[i] = header;
            }

            return parsedHeaders;
        }

        @Override
        public String getHeader(final String header) {
            return request.getResponseHeader(header);
        }
    }
}
