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

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.utils.FactoryHelper;
import com.codenvy.plugin.contribution.client.utils.NotificationHelper;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.app.AppContext;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;

import static com.codenvy.plugin.contribution.client.steps.events.StepEvent.Step.GENERATE_REVIEW_FACTORY;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_MODE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.CONTRIBUTE_VARIABLE_NAME;
import static com.codenvy.plugin.contribution.projecttype.shared.ContributionProjectTypeConstants.PULL_REQUEST_ID_VARIABLE_NAME;
import static java.util.Collections.singletonList;

/**
 * Generates a factory for the contribution reviewer.
 */
public class GenerateReviewFactoryStep implements Step {
    private final Step                      addReviewFactoryLinkStep;
    private final ContributeMessages        messages;
    private final AppContext                appContext;
    private final NotificationHelper        notificationHelper;
    private final FactoryServiceClient      factoryService;

    @Inject
    public GenerateReviewFactoryStep(final AddReviewFactoryLinkStep addReviewFactoryLinkStep,
                                     final ContributeMessages messages,
                                     final AppContext appContext,
                                     final NotificationHelper notificationHelper,
                                     final FactoryServiceClient factoryService) {
        this.addReviewFactoryLinkStep = addReviewFactoryLinkStep;
        this.messages = messages;
        this.appContext = appContext;
        this.notificationHelper = notificationHelper;
        this.factoryService = factoryService;
    }

    @Override
    public void execute(@NotNull final ContributorWorkflow workflow) {
        factoryService.getFactoryJson(appContext.getWorkspaceId(), null)
                      .then(updateProjectAttributes())
                      .then(new Operation<Factory>() {
                          @Override
                          public void apply(Factory factory) throws OperationException {
                              factoryService.saveFactory(factory)
                                            .then(new Operation<Factory>() {
                                                @Override
                                                public void apply(Factory factory) throws OperationException {
                                                    workflow.getContext()
                                                            .setReviewFactoryUrl(FactoryHelper.getCreateProjectRelUrl(factory));
                                                    workflow.fireStepDoneEvent(GENERATE_REVIEW_FACTORY);
                                                    workflow.setStep(addReviewFactoryLinkStep);
                                                    workflow.executeStep();
                                                }
                                            })
                                            .catchError(new Operation<PromiseError>() {
                                                @Override
                                                public void apply(PromiseError arg) throws OperationException {
                                                    notificationHelper.showWarning(messages.stepGenerateReviewFactoryErrorCreateFactory());
                                                    workflow.fireStepDoneEvent(GENERATE_REVIEW_FACTORY);
                                                    workflow.setStep(addReviewFactoryLinkStep);
                                                    workflow.executeStep();
                                                }
                                            });
                          }
                      })
                      .catchError(new Operation<PromiseError>() {
                          @Override
                          public void apply(PromiseError arg) throws OperationException {
                              notificationHelper.showWarning(messages.stepGenerateReviewFactoryErrorCreateFactory());
                              workflow.fireStepDoneEvent(GENERATE_REVIEW_FACTORY);
                              workflow.setStep(addReviewFactoryLinkStep);
                              workflow.executeStep();
                          }
                      });
    }

    private Function<Factory, Factory> updateProjectAttributes() {
        return new Function<Factory, Factory>() {
            @Override
            public Factory apply(Factory factory) throws FunctionException {
                final Optional<ProjectConfigDto> projectOpt = FluentIterable.from(factory.getWorkspace().getProjects())
                                                                            .filter(new Predicate<ProjectConfigDto>() {
                                                                                @Override
                                                                                public boolean apply(ProjectConfigDto project) {
                                                                                    return project.getPath()
                                                                                                  .equals(appContext.getCurrentProject()
                                                                                                                    .getRootProject()
                                                                                                                    .getPath());
                                                                                }
                                                                            })
                                                                            .first();
                if (projectOpt.isPresent()) {
                    final ProjectConfigDto project = projectOpt.get();
                    // new factory is not a 'contribute workflow factory'
                    project.getAttributes().remove(CONTRIBUTE_VARIABLE_NAME);
                    // new factory is in a review mode
                    project.getAttributes().put(CONTRIBUTE_MODE_VARIABLE_NAME, singletonList("review"));
                    // remember the related pull request id
                    project.getAttributes().put(PULL_REQUEST_ID_VARIABLE_NAME, singletonList("notUsed"));
                }
                return factory;
            }
        };
    }
}
