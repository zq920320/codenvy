/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.presenter.UserViewPresenter;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class UserView extends MainView implements UserViewPresenter.Display {

    private final Button  searchButton;
    private final TextBox userEmail;

    /**
     * {@link UserView} constructor.
     */
    public UserView() {
        super();

        userEmail = new TextBox();
        searchButton = new Button("Find");

        constructView();
    }

    private void constructView() {
        DecoratorPanel panel = new DecoratorPanel();
        panel.getElement().setAttribute("align", "center");

        FlexTable table = new FlexTable();
        
        table.setWidget(0, 0, new Label("E-mail:"));
        table.setWidget(0, 1, userEmail);
        table.setWidget(0, 2, searchButton);
        panel.add(table);

        getSubHeaderPanel().add(panel);
    }

    /** {@inheritDoc} */
    @Override
    public Button getSearchButton() {
        return searchButton;
    }

    /** {@inheritDoc} */
    @Override
    public String getUserEmail() {
        return userEmail.getText();
    }
}
