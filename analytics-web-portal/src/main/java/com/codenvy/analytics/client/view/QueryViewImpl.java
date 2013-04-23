package com.codenvy.analytics.client.view;

import com.codenvy.analytics.client.AnalyticsApplication;
import com.codenvy.analytics.client.GWTLoader;
import com.codenvy.analytics.metrics.TimeUnit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryViewImpl extends Composite implements View {
    private final AnalyticsApplication portal;
    private final Map<String, String>  parameters          = new HashMap<String, String>();
    private Map<String, String>        metrics;
    private String                     metricTitle;
    private final FlexTable            flexTableMain       = new FlexTable();
    private final FlexTable            flexTableParameters = new FlexTable();
    private final GWTLoader            gwtLoader           = new GWTLoader();

    public QueryViewImpl(final AnalyticsApplication portal) {
        this.portal = portal;

        flexTableMain.setStyleName("flexTableMain");

        this.portal.getQueryService().getMetricTypes(new AsyncCallback<Map<String, String>>() {

            public void onSuccess(final Map<String, String> result) {
                metrics = result;
                flexTableMain.setWidget(0, 0, new ParameterListBox());
            }

            public void onFailure(Throwable caught) {
                flexTableMain.setText(0, 0, caught.getMessage());
            }
        });

        flexTableMain.setWidget(2, 0, new QueryButton("Run Query"));

        initWidget(flexTableMain);
    }


    /**
     * {@inheritDoc}
     */
    public void update(TimeUnit timeUnit) {
        // do nothing
    }

    private class QueryButton extends Button {
        public QueryButton(String name) {
            super(name);
            setStyleName("mainmenu");
            this.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    gwtLoader.show();
                    portal.getQueryService().calculateMetric(metrics.get(metricTitle), parameters, new AsyncCallback<String>() {
                        public void onSuccess(String result) {
                            gwtLoader.hide();
                            flexTableMain.setText(3, 0, result);
                        }

                        public void onFailure(Throwable caught) {
                            gwtLoader.hide();
                            flexTableMain.setText(3, 0, caught.getMessage());
                        }
                    });
                }
            });
        }
    }

    private class ParameterTextBox extends TextBox {
        public ParameterTextBox(final String parameterName, String dafaultValue) {
            super();
            this.setText(dafaultValue);
            parameters.put(parameterName, dafaultValue);
            this.addValueChangeHandler(new ValueChangeHandler<String>() {
                public void onValueChange(ValueChangeEvent<String> event) {
                    parameters.put(parameterName, event.getValue());
                }
            });
        }
    }

    private class ParameterListBox extends ListBox {
        public ParameterListBox() {
            super();
            List<String> metricTitlesList = new ArrayList<String>(metrics.keySet());
            for (String metricTitle : metricTitlesList) {
                this.addItem(metricTitle);
            }

            this.setVisibleItemCount(1);
            this.addChangeHandler(new ParameterListBoxChangeHandler(this, metricTitlesList));

            generateParametersBox(this, metricTitlesList);
        }
    }

    private class ParameterListBoxChangeHandler implements ChangeHandler {
        private ListBox      listBox;

        private List<String> metricTitles;

        public ParameterListBoxChangeHandler(ListBox listBox, List<String> metricTitles) {
            super();
            this.listBox = listBox;
            this.metricTitles = metricTitles;
        }

        public void onChange(ChangeEvent event) {
            generateParametersBox(listBox, metricTitles);
        }
    }

    private void generateParametersBox(ListBox listBox, List<String> metricTitles) {
        flexTableMain.setWidget(1, 0, flexTableParameters);
        metricTitle = metricTitles.get(listBox.getSelectedIndex());

        portal.getQueryService().getMetricParametersList(metrics.get(metricTitle),
                                                         new AsyncCallback<List<ArrayList<String>>>() {

                                                             public void onFailure(Throwable caught) {
                                                                 flexTableParameters.setText(0, 0, caught.getMessage());
                                                             }

                                                             public void onSuccess(List<ArrayList<String>> result) {
                                                                 flexTableParameters.removeAllRows();
                                                                 int rowCounter = 0;

                                                                 for (final List<String> parameterEntry : result) {
                                                                     flexTableParameters.setText(rowCounter, 0, parameterEntry.get(3));
                                                                     flexTableParameters.setWidget(rowCounter,
                                                                                                   1,
                                                                                                   new ParameterTextBox(
                                                                                                                        parameterEntry.get(0),
                                                                                                                        parameterEntry.get(1)));
                                                                     flexTableParameters.setText(rowCounter, 2, parameterEntry.get(2));

                                                                     rowCounter++;
                                                                 }

                                                             }
                                                         });
    }
}
