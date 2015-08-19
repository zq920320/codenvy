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
package com.codenvy.ide.factory.client.utils;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.accept.Authenticator;
import com.codenvy.ide.factory.shared.Constants;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.factory.dto.Factory;
import org.eclipse.che.api.project.gwt.client.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ImportProject;
import org.eclipse.che.api.project.shared.dto.ImportResponse;
import org.eclipse.che.api.project.shared.dto.ImportSourceDescriptor;
import org.eclipse.che.api.project.shared.dto.NewProject;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.project.shared.dto.ProjectReference;
import org.eclipse.che.api.project.shared.dto.ProjectUpdate;
import org.eclipse.che.api.project.shared.dto.Source;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.project.wizard.ImportProjectNotificationSubscriber;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Sergii Leschenko
 * @author Valeriy Svydenko
 */
public class FactoryProjectImporter {
    public static final String DEFAULT_PROJECT_NAME       = "Unnamed";
    public static final String DEFAULT_PROJECT_VISIBILITY = "private";
    public static final String DEFAULT_KEEP_VCS           = "false";

    private final ProjectServiceClient                projectServiceClient;
    private final DtoUnmarshallerFactory              dtoUnmarshallerFactory;
    private final FactoryLocalizationConstant         localization;
    private final DtoFactory                          dtoFactory;
    private final Authenticator                       authenticator;
    private final ImportProjectNotificationSubscriber notificationSubscriber;

    private Factory                          factory;
    private Notification                     notification;
    private AsyncCallback<ProjectDescriptor> callback;


    @Inject
    public FactoryProjectImporter(ProjectServiceClient projectServiceClient,
                                  DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                  FactoryLocalizationConstant localization,
                                  Authenticator authenticator,
                                  DtoFactory dtoFactory,
                                  ImportProjectNotificationSubscriber notificationSubscriber) {
        this.projectServiceClient = projectServiceClient;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.localization = localization;
        this.dtoFactory = dtoFactory;
        this.authenticator = authenticator;
        this.notificationSubscriber = notificationSubscriber;
    }

    public void startImporting(Notification notification, Factory factory, AsyncCallback<ProjectDescriptor> callback) {
        this.callback = callback;
        this.notification = notification;
        this.factory = factory;

        prepareFactory();

        if (isValidFactoryJson(factory)) {
            selectAvailableProjectName();
        }
    }

    private void prepareFactory() {
        if (factory.getProject() == null) {
            factory.setProject(dtoFactory.createDto(NewProject.class)
                                         .withName(DEFAULT_PROJECT_NAME)
                                         .withVisibility(DEFAULT_PROJECT_VISIBILITY));
        } else {
            if (factory.getProject().getName() == null) {
                factory.getProject().setName(DEFAULT_PROJECT_NAME);
            }

            if (factory.getProject().getVisibility() == null) {
                factory.getProject().setVisibility(DEFAULT_PROJECT_VISIBILITY);
            }
        }

        if (!factory.getProject().getMixinTypes().contains(Constants.FACTORY_PROJECT_TYPE_ID)) {
            factory.getProject().getMixinTypes().add(Constants.FACTORY_PROJECT_TYPE_ID);
        }

        //setting keepVcs to true by default
        if (factory.getSource() == null) {
            factory.setSource(dtoFactory.createDto(Source.class));
        }

        if (factory.getSource().getProject() == null) {
            factory.getSource().setProject(dtoFactory.createDto(ImportSourceDescriptor.class));
        }

        if (factory.getSource().getProject().getParameters() == null) {
            factory.getSource().getProject().setParameters(new HashMap<String, String>());
            factory.getSource().getProject().getParameters().put("keepVcs", DEFAULT_KEEP_VCS);
        } else if (factory.getSource().getProject().getParameters().get("keepVcs") == null) {
            factory.getSource().getProject().getParameters().put("keepVcs", DEFAULT_KEEP_VCS);
        }
    }

    private boolean isValidFactoryJson(Factory factory) {
        if (factory != null) {
            if (factory.getSource() != null) {
                return true;
            } else {
                callback.onFailure(new Exception("Config file doesn't contain information about source provider"));
            }
        } else {
            callback.onFailure(new Exception("Config file is missed"));
        }
        return false;
    }

    private void selectAvailableProjectName() {
        projectServiceClient.getProjects(new AsyncRequestCallback<List<ProjectReference>>(
                dtoUnmarshallerFactory.newListUnmarshaller(ProjectReference.class)) {
            @Override
            protected void onSuccess(List<ProjectReference> result) {
                String projectName = factory.getProject().getName();

                if (!result.isEmpty()) {
                    Set<String> names = new HashSet<>();
                    for (ProjectReference projectReference : result) {
                        names.add(projectReference.getName());
                    }

                    if (names.contains(projectName)) {
                        projectName += "-";

                        int postfixCounter = 1;
                        while (names.contains(projectName + postfixCounter)) {
                            ++postfixCounter;
                        }

                        projectName += postfixCounter;
                        factory.getProject().setName(projectName);
                    }
                }

                importProject();
            }

            @Override
            protected void onFailure(Throwable exception) {
                callback.onFailure(exception);
            }
        });
    }

    /**
     * Imports source to project
     */
    private void importProject() {
        notification.setMessage(localization.cloningSource());
        notificationSubscriber.subscribe(factory.getProject().getName(), notification);
        ImportProject importProject = dtoFactory.createDto(ImportProject.class)
                                                .withSource(factory.getSource())
                                                .withProject(factory.getProject());

        projectServiceClient.importProject(factory.getProject().getName(), true, importProject,
                                           new AsyncRequestCallback<ImportResponse>(
                                                   dtoUnmarshallerFactory.newUnmarshaller(ImportResponse.class)) {
                                               @Override
                                               protected void onSuccess(ImportResponse importedProject) {
                                                   if (importedProject.getProjectDescriptor().getMixins()
                                                                      .contains(Constants.FACTORY_PROJECT_TYPE_ID)) {
                                                       updateProjectAttributes(importedProject.getProjectDescriptor());
                                                   } else {
                                                       callback.onSuccess(importedProject.getProjectDescriptor());
                                                   }
                                               }

                                               @Override
                                               protected void onFailure(Throwable exception) {
                                                   if (exception instanceof UnauthorizedException) {
                                                       rerunWithAuthImport();
                                                   } else {
                                                       callback.onFailure(new Exception("Unable to import source of project. " + dtoFactory
                                                               .createDtoFromJson(exception.getMessage(), ServiceError.class)
                                                               .getMessage()));
                                                   }
                                               }
                                           });
    }

    private void rerunWithAuthImport() {
        notification.setMessage(localization.needToAuthorizeBeforeAcceptMessage());
        authenticator.showOAuthWindow(factory.getSource().getProject().getLocation(),
                                      new Authenticator.AuthCallback() {
                                          @Override
                                          public void onAuthenticated() {
                                              notification.setMessage(localization.oauthSuccess());
                                              importProject();
                                          }

                                          @Override
                                          public void onError(String message) {
                                              notification.setMessage(localization.oauthFailed() + " " + message);
                                              notification.setType(Notification.Type.ERROR);
                                              notification.setStatus(Notification.Status.FINISHED);
                                          }
                                      });
    }

    private void updateProjectAttributes(ProjectDescriptor projectDescriptor) {
        Map<String, List<String>> attributes = projectDescriptor.getAttributes();
        attributes.put(Constants.FACTORY_ID_ATTRIBUTE_NAME, Collections.singletonList(factory.getId()));
        ProjectUpdate update = dtoFactory.createDto(ProjectUpdate.class)
                                         .withType(projectDescriptor.getType())
                                         .withMixinTypes(projectDescriptor.getMixins())
                                         .withAttributes(attributes)
                                         .withBuilders(projectDescriptor.getBuilders())
                                         .withDescription(projectDescriptor.getDescription())
                                         .withRunners(projectDescriptor.getRunners());
        projectServiceClient.updateProject(projectDescriptor.getPath(), update,
                                           new AsyncRequestCallback<ProjectDescriptor>(
                                                   dtoUnmarshallerFactory.newUnmarshaller(ProjectDescriptor.class)) {
                                               @Override
                                               protected void onSuccess(ProjectDescriptor projectDescriptor) {
                                                   callback.onSuccess(projectDescriptor);
                                               }

                                               @Override
                                               protected void onFailure(Throwable exception) {
                                                   callback.onFailure(new Exception("Unable to set properties of project. " + dtoFactory
                                                           .createDtoFromJson(exception.getMessage(), ServiceError.class)
                                                           .getMessage()));
                                               }
                                           }
                                          );
    }

    public Factory getFactory() {
        return factory;
    }
}
