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
package com.codenvy.ide.factory.client.configure;

import com.codenvy.ide.factory.client.FactoryLocalizationConstant;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.factory.gwt.client.FactoryServiceClient;
import org.eclipse.che.api.factory.shared.dto.Factory;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.Pair;

import java.util.Collections;
import java.util.List;

/**
 * @author Anton Korneta
 */
@Singleton
public class CreateFactoryPresenter implements CreateFactoryView.ActionDelegate {
    private final CreateFactoryView           view;
    private final AppContext                  appContext;
    private final FactoryServiceClient        factoryService;
    private final FactoryLocalizationConstant locale;
    private final DtoFactory dtoFactory;


    @Inject
    public CreateFactoryPresenter(CreateFactoryView view,
                                  AppContext appContext,
                                  FactoryServiceClient factoryService,
                                  FactoryLocalizationConstant locale,
                                  DtoFactory dtoFactory) {
        this.view = view;
        this.appContext = appContext;
        this.factoryService = factoryService;
        this.locale = locale;
        this.dtoFactory = dtoFactory;
        view.setDelegate(this);
    }

    public void showDialog() {
        view.showDialog();
    }

    @Override
    public void onCreateClicked() {
        final String factoryName = view.getFactoryName();
        factoryService.getFactoryJson(appContext.getWorkspace().getId(), null)
                      .then(new Operation<Factory>() {
                          @Override
                          public void apply(final Factory factory) throws OperationException {
                              factoryService.findFactory(null, null, Collections.singletonList(Pair.of("name", factoryName)))
                                            .then(saveFactory(factory, factoryName))
                                            .catchError(logError());
                          }
                      })
                      .catchError(logError());
    }

    @Override
    public void onFactoryNameChanged(String factoryName) {
        view.enableCreateFactoryButton(isValidFactoryName(factoryName));
    }

    @Override
    public void onCancelClicked() {
        view.close();
    }

    private Operation<List<Factory>> saveFactory(final Factory factory, final String factoryName) {
        return new Operation<List<Factory>>() {
            @Override
            public void apply(List<Factory> factories) throws OperationException {
                if (!factories.isEmpty()) {
                    view.showFactoryNameError(locale.createFactoryAlreadyExist(), null);
                } else {
                    factoryService.saveFactory(factory.withName(factoryName))
                                  .then(new Operation<Factory>() {
                                      @Override
                                      public void apply(Factory factory) throws OperationException {
                                          for (Link link : factory.getLinks()) {
                                              if (link.getRel() != null && link.getRel().equals("create-project")) {
                                                  view.setFactoryLink(link.getHref());
                                              }
                                          }
                                      }
                                  })
                                  .catchError(logError());
                }
            }
        };
    }

    private Operation<PromiseError> logError() {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError err) throws OperationException {
                final String errorMessage = dtoFactory.createDtoFromJson(err.getMessage(), ServiceError.class).getMessage();
                view.showFactoryNameError(locale.createFactoryFromCurrentWorkspaceFailed(), errorMessage);
            }
        };
    }

    private boolean isValidFactoryName(String name) {
        if (name.length() == 0 || name.length() >= 125) {
            return false;
        }
        view.hideFactoryNameError();
        return true;
    }
}
