/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.presenter;

import com.codenvy.analytics.client.UserServiceAsync;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;

import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UserViewPresenter extends MainViewPresenter implements Presenter {

    private final UserServiceAsync userService;
    
    /** @see MainViewPresenter.Display */
    public interface Display extends MainViewPresenter.Display {

        /**
         * @return the search button
         */
        Button getSearchButton();

        /**
         * @return the use email from input box.
         */
        String getUserEmail();
    }

    public UserViewPresenter(UserServiceAsync userService, HandlerManager eventBus, Display view) {
        super(eventBus, view);
        this.userService = userService;
    }

    public void bind() {
        super.bind();
        
        getDisplay().getSearchButton().addClickHandler(new ClickHandler() {
            /** {@inheritDoc} */
            @Override
            public void onClick(ClickEvent event) {
                getDisplay().getGWTLoader().show();

                retrieveData(getDisplay().getUserEmail());

                getDisplay().getGWTLoader().hide();
            }
        });
    }

    /**
     * Retrieves data from the server.
     */
    private void retrieveData(String userEmail) {

        userService.getData(userEmail, new AsyncCallback<List<TableData>>() {
            /** {@inheritDoc} */
            @Override
            public void onSuccess(List<TableData> result) {
                getDisplay().setData(result);
            }

            /** {@inheritDoc} */
            @Override
            public void onFailure(Throwable caught) {
                getDisplay().setErrorMessage(caught.getMessage());
            }
        });
    }

    private Display getDisplay() {
        return (Display)display;
    }
}
