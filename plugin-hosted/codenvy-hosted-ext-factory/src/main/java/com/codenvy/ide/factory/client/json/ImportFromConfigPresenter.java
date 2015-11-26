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
package com.codenvy.ide.factory.client.json;

import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.ide.api.event.project.OpenProjectEvent;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.dto.DtoFactory;
import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.codenvy.ide.factory.client.utils.FactoryProjectImporter;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Imports project from factory.json file
 *
 * @author Sergii Leschenko
 */
public class ImportFromConfigPresenter implements ImportFromConfigView.ActionDelegate {
    private final FactoryLocalizationConstant      factoryLocalization;
    private final ImportFromConfigView             view;
    private final NotificationManager              notificationManager;
    private final DtoFactory                       dtoFactory;
    private final FactoryProjectImporter           projectImporter;
    private final AsyncCallback<ProjectDescriptor>    importerCallback;
    private       Notification                     importNotification;

    @Inject
    public ImportFromConfigPresenter(final FactoryLocalizationConstant factoryLocalization,
                                     FactoryProjectImporter projectImporter,
                                     ImportFromConfigView view,
                                     NotificationManager notificationManager,
                                     final EventBus eventBus,
                                     DtoFactory dtoFactory) {
        this.factoryLocalization = factoryLocalization;
        this.notificationManager = notificationManager;
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.view.setDelegate(this);
        this.projectImporter = projectImporter;

        importerCallback = new AsyncCallback<ProjectDescriptor>() {
            @Override
            public void onSuccess(ProjectDescriptor result) {
                importNotification.setMessage(factoryLocalization.clonedSource());
                importNotification.setType(Notification.Type.INFO);
                importNotification.setStatus(Notification.Status.FINISHED);
                eventBus.fireEvent(new OpenProjectEvent(result));
            }

            @Override
            public void onFailure(Throwable throwable) {
                importNotification.setMessage(throwable.getMessage());
                importNotification.setType(Notification.Type.ERROR);
                importNotification.setStatus(Notification.Status.FINISHED);
                Log.error(getClass(), throwable.getMessage());
            }
        };
    }

    /** Show dialog. */
    public void showDialog() {
        view.setEnabledImportButton(false);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.closeDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onImportClicked() {
        view.closeDialog();
        Factory factoryJson;
        try {
            factoryJson = dtoFactory.createDtoFromJson(view.getFileContent(), Factory.class);
        } catch (JSONException jsonException) {
            showErrorMessage("Error parsing factory object.", jsonException);
            return;
        }

        importNotification = new Notification(factoryLocalization.cloningSource(), Notification.Type.INFO, Notification.Status.PROGRESS);
        notificationManager.showNotification(importNotification);
        //projectImporter.startImporting(importNotification, factoryJson, importerCallback);
    }


    @Override
    public void onErrorReadingFile(String errorMessage) {
        view.setEnabledImportButton(false);
        showErrorMessage(errorMessage);
    }

    private void showErrorMessage(String messagePrefix, Throwable error) {
        showErrorMessage(messagePrefix + error.getMessage());
    }

    private void showErrorMessage(String message) {
        notificationManager.showNotification(new Notification(message, Notification.Type.ERROR));
        Log.error(getClass(), message);
    }
}
