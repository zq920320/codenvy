/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.presenter.QueryViewPresenter;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class QueryView extends MainView implements QueryViewPresenter.Display {
    private final ListBox       queryListBox   = new ListBox();
    private final FlexTable     parameterTable = new FlexTable();
    private final Button        runQueryButton = new Button("Run Query");
    private final VerticalPanel resultPanel    = new VerticalPanel();

    public QueryView() {
        super();
        getMainPanel().add(queryListBox);
        getMainPanel().add(parameterTable);
        getMainPanel().add(runQueryButton);
        getMainPanel().add(resultPanel);
    }

    public ListBox getQueryListBox() {
        return queryListBox;
    }

    public FlexTable getParameterTable() {
        return parameterTable;
    }

    public HasClickHandlers getRunQueryButton() {
        return runQueryButton;
    }

    public void setData(String data) {
        resultPanel.clear();
        resultPanel.add(new Label(data));
    }
}
