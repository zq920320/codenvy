/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.presenter;

import com.codenvy.analytics.client.AnalysisViewServiceAsync;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalysisViewPresenter extends MainViewPresenter implements Presenter {

    private final AnalysisViewServiceAsync service;

    public interface Display extends MainViewPresenter.Display {
        FlexTable getContentTable();
        void setData(List<TimeLineViewData> result);
    }

    public AnalysisViewPresenter(AnalysisViewServiceAsync service, HandlerManager eventBus, Display view) {
        super(eventBus, view);
        this.service = service;

        update();
    }

    private void update() {
        getDisplay().getContentTable().clear();
        getDisplay().getGWTLoader().show();

        service.getData(new AsyncCallback<List<TimeLineViewData>>() {
            public void onFailure(Throwable caught) {
                getDisplay().getGWTLoader().hide();
                getDisplay().getContentTable().setText(0, 0, caught.getMessage());
            }

            public void onSuccess(List<TimeLineViewData> result) {
                getDisplay().getGWTLoader().hide();
                getDisplay().setData(result);
            }
        });
    }

    private Display getDisplay() {
        return (Display)display;
    }

    public void bind() {
        super.bind();
    }
}
