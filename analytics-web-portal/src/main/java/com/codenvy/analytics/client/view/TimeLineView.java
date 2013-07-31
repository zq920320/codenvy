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

    private final TextBox searchField;
    private final Button findBtn;
    private final ListBox timeUnitBox;
    private final ListBox searchCategoryBox;

    public TimeLineView() {
        super();

        this.searchField = new TextBox();
        this.findBtn = new Button("Go");

        this.timeUnitBox = new ListBox();
        for (TimeUnit timeUnit : TimeUnit.values()) {
            timeUnitBox.addItem(timeUnit.toString().toLowerCase());
        }
        timeUnitBox.setVisibleItemCount(1);

        this.searchCategoryBox = new ListBox();
        for (TimeLineViewPresenter.SearchCategory category : TimeLineViewPresenter.SearchCategory.values()) {
            searchCategoryBox.addItem(category.toString().toLowerCase());
        }
        searchCategoryBox.setVisibleItemCount(1);

        constructView();
    }

    private void constructView() {
        DecoratorPanel panel = new DecoratorPanel();
        panel.getElement().setAttribute("align", "center");

        FlexTable table = new FlexTable();

        table.setWidget(0, 0, new Label("Time Unit:"));
        table.setWidget(0, 1, timeUnitBox);

        table.setWidget(1, 0, new Label("Search by:"));
        table.setWidget(1, 1, searchCategoryBox);

        table.setWidget(2, 0, new Label(""));
        table.setWidget(2, 1, searchField);
        table.setWidget(2, 2, findBtn);
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
    public ListBox getSearchCategoryBox() {
        return searchCategoryBox;
    }

    /** {@inheritDoc} */
    @Override
    public TextBox getSearchField() {
        return searchField;
    }

    /** {@inheritDoc} */
    @Override
    public Button getFindBtn() {
        return findBtn;
    }

}
