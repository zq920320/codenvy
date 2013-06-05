/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.presenter.TimeLineViewPresenter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineView extends MainView implements TimeLineViewPresenter.Display {

    private final TextBox   userNameFilter    = new TextBox();
    private final Button    applyFilterButton = new Button("Go");
    private final ListBox   timeUnitBox       = new ListBox();

    public TimeLineView() {
        super();

        HorizontalPanel timeUnitPanel = new HorizontalPanel();
        Label label = new Label("Time Unit:");
        timeUnitPanel.add(new Label("Time Unit: "));
        timeUnitPanel.add(timeUnitBox);

        HorizontalPanel domainPanel = new HorizontalPanel();
        label = new Label("Users email: ");
        label.getElement().setAttribute("vertical-align", "midle");
        domainPanel.add(label);
        domainPanel.add(userNameFilter);
        domainPanel.add(applyFilterButton);

        VerticalPanel verticalPanel = new VerticalPanel();
        verticalPanel.add(timeUnitPanel);
        verticalPanel.add(domainPanel);
        verticalPanel.getElement().setAttribute("align", "center");

        getSubHeaderPanel().add(verticalPanel);
        for (TimeUnit timeUnit : TimeUnit.values()) {
            timeUnitBox.addItem(timeUnit.toString().toLowerCase());
        }

        timeUnitBox.setVisibleItemCount(1);
    }

    public ListBox getTimeUnitBox() {
        return timeUnitBox;
    }

    public String getUserFilter() {
        return userNameFilter.getText();
    }

    public Button getApplyFilterButton() {
        return applyFilterButton;
    }
}
