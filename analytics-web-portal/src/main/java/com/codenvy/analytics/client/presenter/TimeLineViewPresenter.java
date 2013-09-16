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

import com.codenvy.analytics.client.TimeLineServiceAsync;
import com.codenvy.analytics.metrics.MetricFilter;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.shared.TableData;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class TimeLineViewPresenter extends MainViewPresenter implements Presenter {
    private final TimeLineServiceAsync timelineService;

    public interface Display extends MainViewPresenter.Display {
        ListBox getTimeUnitBox();

        ListBox getSearchCategoryBox();

        TextBox getSearchField();

        Button getFindBtn();
    }

    private TimeUnit       currentTimeUnit = TimeUnit.DAY;
    private SearchCategory searchCategory  = SearchCategory.EMAIL;

    public TimeLineViewPresenter(TimeLineServiceAsync timelineService, HandlerManager eventBus, Display view) {
        super(eventBus, view);
        this.timelineService = timelineService;

        update();
    }

    public void bind() {
        getDisplay().getTimeUnitBox().addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                TimeUnit newCurrentTimeUnit = TimeUnit.values()[getDisplay().getTimeUnitBox().getSelectedIndex()];
                if (newCurrentTimeUnit != currentTimeUnit) {
                    currentTimeUnit = newCurrentTimeUnit;

                    update();
                }
            }
        });

        getDisplay().getSearchCategoryBox().addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                SearchCategory newSearchCategory =
                        SearchCategory.values()[getDisplay().getSearchCategoryBox().getSelectedIndex()];
                if (newSearchCategory != searchCategory) {
                    searchCategory = newSearchCategory;

                    getDisplay().getSearchField().setText("");

                    update();
                }
            }
        });

        getDisplay().getFindBtn().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                update();
            }
        });

        super.bind();
    }

    private void update() {
        getDisplay().getGWTLoader().show();

        timelineService.getData(currentTimeUnit, prepareFilterContext(), new AsyncCallback<List<TableData>>() {
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

    private Map<String, String> prepareFilterContext() {
        String filterValue = getDisplay().getSearchField().getText();

        if (!filterValue.isEmpty()) {
            Map<String, String> filterContext = new HashMap<String, String>(1);

            switch (searchCategory) {
                case EMAIL:
                    filterContext.put(MetricFilter.USERS.name(), filterValue);
                    break;
                case DOMAIN:
                    filterContext.put(MetricFilter.DOMAINS.name(), filterValue);
                    break;
                case COMPANY:
                    filterContext.put(MetricFilter.COMPANY.name(), filterValue);
                    break;
            }

            return filterContext;
        } else {
            return new HashMap<String, String>(0);
        }
    }

    private Display getDisplay() {
        return (Display)display;
    }

    public static enum SearchCategory {
        EMAIL,
        DOMAIN,
        COMPANY
    }
}
