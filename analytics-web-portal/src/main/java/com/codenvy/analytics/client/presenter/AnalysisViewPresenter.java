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

import com.codenvy.analytics.client.AnalysisServiceAsync;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class AnalysisViewPresenter extends MainViewPresenter implements Presenter {

    private final AnalysisServiceAsync service;

    public interface Display extends MainViewPresenter.Display {
        void setData(List<TableData> result);
    }

    public AnalysisViewPresenter(AnalysisServiceAsync service, HandlerManager eventBus, Display view) {
        super(eventBus, view);
        this.service = service;

        update();
    }

    private void update() {
        getDisplay().getContentTable().clear();
        getDisplay().getGWTLoader().show();

        service.getData(new AsyncCallback<List<TableData>>() {
            public void onFailure(Throwable caught) {
                getDisplay().getGWTLoader().hide();
                getDisplay().getContentTable().setText(0, 0, caught.getMessage());
            }

            public void onSuccess(List<TableData> result) {
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
