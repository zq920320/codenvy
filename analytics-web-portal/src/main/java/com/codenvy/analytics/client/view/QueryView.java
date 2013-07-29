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
