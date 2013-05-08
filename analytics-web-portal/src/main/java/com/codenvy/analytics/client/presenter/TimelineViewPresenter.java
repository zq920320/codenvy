package com.codenvy.analytics.client.presenter;

import com.codenvy.analytics.client.TimeLineViewServiceAsync;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.shared.TimeLineViewData;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;

import java.util.Date;
import java.util.List;

public class TimelineViewPresenter extends MainViewPresenter implements Presenter {
    private final TimeLineViewServiceAsync timelineService;

    public interface Display extends MainViewPresenter.Display {
        ListBox getTimeUnitBox();

        FlexTable getContentTable();

        void setData(List<TimeLineViewData> result);
    }

    private TimeUnit currentTimeUnit = TimeUnit.DAY;

    public TimelineViewPresenter(TimeLineViewServiceAsync timelineService, HandlerManager eventBus, Display view) {
        super(eventBus, view);
        this.timelineService = timelineService;

        update(currentTimeUnit);
    }

    public void bind() {
        getDisplay().getTimeUnitBox().addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                TimeUnit newCurrentTimeUnit = TimeUnit.values()[getDisplay().getTimeUnitBox().getSelectedIndex()];
                if (newCurrentTimeUnit != currentTimeUnit) {
                    currentTimeUnit = newCurrentTimeUnit;
                    update(currentTimeUnit);
                }
            }
        });

        super.bind();
    }

    private void update(TimeUnit timeUnit) {
        getDisplay().getContentTable().clear();
        getDisplay().getGWTLoader().show();

        timelineService.getViews(new Date(), timeUnit, new AsyncCallback<List<TimeLineViewData>>() {
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

    private Display getDisplay()
    {
        return (Display)display;
    }
}
