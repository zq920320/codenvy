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


package com.codenvy.analytics.client.presenter;


import com.codenvy.analytics.client.QueryServiceAsync;
import com.codenvy.analytics.client.resources.ParameterDateBox;
import com.codenvy.analytics.client.resources.ParameterTextBox;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class QueryViewPresenter extends MainViewPresenter {

    private final QueryServiceAsync queryService;

    public interface Display extends MainViewPresenter.Display {
        ListBox getQueryListBox();

        FlexTable getParameterTable();

        HasClickHandlers getRunQueryButton();

        void setData(String data);
    }

    private String              selectedMetricTitle;

    private Map<String, String> metrics;

    private List<String>        metricTitles;

    private Map<String, String> selectedParameters = new HashMap<String, String>();


    public QueryViewPresenter(QueryServiceAsync queryService, HandlerManager eventBus, Display view) {
        super(eventBus, view);
        this.queryService = queryService;
        generateMetricsList();
    }

    public void bind() {

        getDisplay().getQueryListBox().addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {
                defineSelectedMetricTitle();
                generateMetricParameters();
            }
        });

        getDisplay().getRunQueryButton().addClickHandler(new ClickHandler() {
            
            public void onClick(ClickEvent event) {
                getDisplay().getGWTLoader().show();

                queryService.calculateMetric(metrics.get(selectedMetricTitle), selectedParameters, new AsyncCallback<String>() {
                    
                    public void onSuccess(String result) {
                        getDisplay().setData(result);
                        getDisplay().getGWTLoader().hide();
                    }
                    
                    public void onFailure(Throwable caught) {
                        getDisplay().getGWTLoader().hide();
                    }
                });
            }
        });

        super.bind();
    }

    private void generateMetricsList()
    {
        queryService.getMetricTypes(new AsyncCallback<Map<String, String>>() {

            public void onSuccess(Map<String, String> result) {
                metrics = result;
                metricTitles = new ArrayList<String>(metrics.keySet());

                for (String metricTitle : metricTitles) {
                    getDisplay().getQueryListBox().addItem(metricTitle);
                }

                selectedMetricTitle = metricTitles.get(0);

                generateMetricParameters();
            }


            public void onFailure(Throwable caught) {
            }
        });
    }

    private void generateMetricParameters()
    {
        queryService.getMetricParametersList(metrics.get(selectedMetricTitle), new AsyncCallback<List<ArrayList<String>>>() {

            public void onSuccess(List<ArrayList<String>> result) {
                getDisplay().getParameterTable().removeAllRows();
                selectedParameters.clear();
                int rowCounter = 0;

                for (final List<String> parameterEntry : result) {
                    String name = parameterEntry.get(0);
                    String defaultValue = parameterEntry.get(1);
                    String title = parameterEntry.get(2);

                    getDisplay().getParameterTable().setText(rowCounter, 0, title);
                    if ("fromDate".equals(name) || "toDate".equals(name)) {
                        getDisplay().getParameterTable().setWidget(rowCounter,
                                                                   1,
                                                                   new ParameterDateBox(selectedParameters, name, defaultValue));
                    }
                    else {
                        getDisplay().getParameterTable().setWidget(rowCounter,
                                                                   1,
                                                                   new ParameterTextBox(selectedParameters, name, defaultValue));
                    }

                    rowCounter++;
                }
            }

            public void onFailure(Throwable caught) {
            }
        });
    }

    private void defineSelectedMetricTitle() {
        selectedMetricTitle = metricTitles.get(getDisplay().getQueryListBox().getSelectedIndex());
    }

    private Display getDisplay() {
        return (Display)display;
    }
}
