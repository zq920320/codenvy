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

import com.codenvy.analytics.client.presenter.TimeLineViewPresenter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineView extends MainView implements TimeLineViewPresenter.Display {

    private final TextBox userEmail;
    private final Button  searchButton;
    private final ListBox timeUnitBox;

    public TimeLineView() {
        super();

        this.userEmail = new TextBox();
        this.searchButton = new Button("Find");
        this.timeUnitBox = new ListBox();

        for (TimeUnit timeUnit : TimeUnit.values()) {
            timeUnitBox.addItem(timeUnit.toString().toLowerCase());
        }
        timeUnitBox.setVisibleItemCount(1);

        constructView();
    }

    private void constructView() {
        DecoratorPanel panel = new DecoratorPanel();
        panel.getElement().setAttribute("align", "center");

        FlexTable table = new FlexTable();

        table.setWidget(0, 0, new Label("Time Unit:"));
        table.setWidget(0, 1, timeUnitBox);

        table.setWidget(1, 0, new Label("E-mail:"));
        table.setWidget(1, 1, userEmail);
        table.setWidget(1, 2, searchButton);
        panel.add(table);

        getSubHeaderPanel().add(panel);
    }

    /** {@inheritDoc} */
    @Override
    public ListBox getTimeUnitBox() {
        return timeUnitBox;
    }

    /** {@inheritDoc} */
    @Override
    public String getUserEmail() {
        return userEmail.getText();
    }

    /** {@inheritDoc} */
    @Override
    public Button getSearchButton() {
        return searchButton;
    }
}
