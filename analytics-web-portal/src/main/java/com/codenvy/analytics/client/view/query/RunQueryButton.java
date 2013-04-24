package com.codenvy.analytics.client.view.query;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

class RunQueryButton extends Button {

    final private QueryViewImpl queryView;

    public RunQueryButton(String name, QueryViewImpl queryView) {
        super(name);
        this.queryView = queryView;
        setVisible(true);
        this.setStyleName("mainmenu");

        this.addClickHandler(new QueryRunClickHandler());
    }

    private class QueryRunClickHandler implements ClickHandler {
        public void onClick(ClickEvent event) {
            queryView.getGwtLoader().show();
            queryView.getPortal()
                     .getQueryService()
                     .calculateMetric(queryView.getSelectedMetricName(), queryView.getSelectedMetricParameters(),
                                      new CalculateMetricAsyncCallback(queryView));
        }
    }

    public QueryViewImpl getQueryView() {
        return queryView;
    }
}
