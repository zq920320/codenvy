/*
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.utils.FactoryHelper;
import com.codenvy.plugin.contribution.client.workflow.Configuration;
import com.codenvy.plugin.contribution.client.workflow.Context;
import com.codenvy.plugin.contribution.client.workflow.Step;
import com.codenvy.plugin.contribution.client.workflow.WorkflowExecutor;
import com.codenvy.plugin.contribution.vcs.client.hosting.dto.PullRequest;
import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;

/**
 * Used for toggling last step mark.
 *
 * @author Yevhenii Voevodin
 * @author Anton Korneta
 */
@Singleton
public class UpdatePullRequestStep implements Step {
    private static final RegExp SCHEMA = RegExp.compile("[^\\.]*(f\\?id=)([a-zA-Z0-9]*)[^\\.]*");

    private final FactoryServiceClient factoryService;
    private final AppContext           appContext;

    @Inject
    public UpdatePullRequestStep(FactoryServiceClient factoryService,
                                 AppContext appContext) {
        this.factoryService = factoryService;
        this.appContext = appContext;
    }

    @Override
    public void execute(final WorkflowExecutor executor, final Context context) {
        final PullRequest pullRequest = context.getPullRequest();
        factoryService.getFactoryJson(appContext.getWorkspaceId(), null)
                      .then(new Operation<Factory>() {
                          @Override
                          public void apply(Factory currentFactory) throws OperationException {
                              final String factoryId = extractFactoryId(pullRequest.getDescription());
                              if (!Strings.isNullOrEmpty(factoryId)) {
                                  updateFactory(executor, context, factoryId, currentFactory);
                              } else {
                                  addReviewUrl(executor, currentFactory, pullRequest, context, context.getConfiguration());
                              }
                          }
                      })
                      .catchError(handleError(executor, context));
    }

    private Promise<Factory> updateFactory(final WorkflowExecutor executor,
                                           final Context context,
                                           final String factoryId,
                                           final Factory currentFactory) {
        return factoryService.updateFactory(factoryId, currentFactory)
                             .then(new Operation<Factory>() {
                                 @Override
                                 public void apply(Factory updatedFactory) throws OperationException {
                                     context.setReviewFactoryUrl(FactoryHelper.getAcceptFactoryUrl(updatedFactory));
                                     executor.done(UpdatePullRequestStep.this, context);
                                 }
                             });
    }

    private void addReviewUrl(final WorkflowExecutor executor,
                              final Factory currentFactory,
                              final PullRequest pullRequest,
                              final Context context,
                              final Configuration configuration) {
        factoryService.saveFactory(currentFactory)
                      .then(new Operation<Factory>() {
                          @Override
                          public void apply(Factory factory) throws OperationException {
                              final String reviewUrl = context.getVcsHostingService()
                                                              .formatReviewFactoryUrl(FactoryHelper.getAcceptFactoryUrl(factory));
                              context.setReviewFactoryUrl(reviewUrl);
                              final String comment = reviewUrl + "\n" + configuration.getContributionComment();
                              configuration.withContributionComment(comment);
                              context.getVcsHostingService()
                                     .updatePullRequest(context.getOriginRepositoryOwner(),
                                                        context.getUpstreamRepositoryName(),
                                                        pullRequest.withDescription(comment))
                                     .then(new Operation<PullRequest>() {
                                         @Override
                                         public void apply(PullRequest pr) throws OperationException {
                                             executor.done(UpdatePullRequestStep.this, context);
                                         }
                                     })
                                     .catchError(handleError(executor, context));
                          }
                      });
    }

    private String extractFactoryId(String description) {
        if (SCHEMA.test(description)) {
            return SCHEMA.exec(description).getGroup(2);
        }
        return null;
    }

    private Operation<PromiseError> handleError(final WorkflowExecutor executor, final Context context) {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError err) throws OperationException {
                context.getViewState().setStatusMessage(err.getMessage(), true);
                executor.fail(UpdatePullRequestStep.this, context, err.getMessage());
            }
        };
    }
}