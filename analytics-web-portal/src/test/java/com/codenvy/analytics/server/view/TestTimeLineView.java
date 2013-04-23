/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.view;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.codenvy.analytics.server.manager.TimeLineViewManager;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.TimeIntervalUtil;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.shared.TimeLineViewData;

import org.testng.annotations.Test;
import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TestTimeLineView {

    @Test
    public void testTimeViewByDay() throws Exception {
        String resouce = "<views history-length=\"2\">" +

                         "<view>" +
                         "<date section=\"WORKSPACES\" format=\"dd/MM\"/>" +
                         "<metric type=\"workspaces_created\"/>" +
                         "<empty />" +
                         "</view>" +

                         "<view>" +
                         "<date section=\"PROJECTS\" format=\"dd/MM\"/>" +
                         "<metric type=\"projects_created\"/>" +
                         "<empty />" +
                         "</view>" +

                         "</views>";
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse("20130310"));

        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.DAY.toString());
        TimeIntervalUtil.initDateInterval(cal, context);

        Metric mockedMetrick = mock(Metric.class);
        when(mockedMetrick.getValue(anyMap())).thenReturn(2L).thenReturn(1L);
        when(mockedMetrick.getTitle()).thenReturn("title");

        TimeLineViewManager tView = spy(new TimeLineViewManager(context));
        doReturn(new ByteArrayInputStream(resouce.getBytes())).when(tView).readResource();
        doReturn(tView.new MetricRowLayout(mockedMetrick, "%d")).when(tView).createMetricRow(any(Element.class));

        List<TimeLineViewData> views = tView.getTimeLineViewData();
        assertEquals(tView.getHistoryLength(), 2);
        assertEquals(views.size(), 2);

        Iterator<List<String>> rows = views.get(0).iterator();

        List<String> row = rows.next();
        assertEquals(row.get(0), "WORKSPACES");
        assertEquals(row.get(1), "10/03");
        assertEquals(row.get(2), "09/03");

        row = rows.next();
        assertEquals(row.get(0), "title");
        assertEquals(row.get(1), "2");
        assertEquals(row.get(2), "1");

        row = rows.next();
        assertEquals(row.get(0), "");
        assertEquals(row.get(1), "");
        assertEquals(row.get(2), "");

        rows = views.get(1).iterator();

        row = rows.next();
        assertEquals(row.get(0), "PROJECTS");
        assertEquals(row.get(1), "10/03");
        assertEquals(row.get(2), "09/03");

        row = rows.next();
        assertEquals(row.get(0), "title");
        assertEquals(row.get(1), "1");
        assertEquals(row.get(2), "1");

        row = rows.next();
        assertEquals(row.get(0), "");
        assertEquals(row.get(1), "");
        assertEquals(row.get(2), "");

    }

    @Test
    public void testTimeViewByMonth() throws Exception {
        String resouce = "<views history-length=\"2\">" +

                         "<view>" +
                         "<date section=\"WORKSPACES\" format=\"dd/MM\"/>" +
                         "<metric type=\"workspaces_created\"/>" +
                         "<empty />" +
                         "</view>" +

                         "<view>" +
                         "<date section=\"PROJECTS\" format=\"dd/MM\"/>" +
                         "<metric type=\"projects_created\"/>" +
                         "<empty />" +
                         "</view>" +

                         "</views>";

        Calendar cal = Calendar.getInstance();
        cal.setTime(ScriptExecutor.PARAM_DATE_FORMAT.parse("20130310"));

        Map<String, String> context = new HashMap<String, String>();
        context.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.MONTH.toString());
        TimeIntervalUtil.initDateInterval(cal, context);

        Metric mockedMetrick = mock(Metric.class);
        when(mockedMetrick.getValue(anyMap())).thenReturn(2L).thenReturn(1L);
        when(mockedMetrick.getTitle()).thenReturn("title");

        TimeLineViewManager tView = spy(new TimeLineViewManager(context));
        doReturn(new ByteArrayInputStream(resouce.getBytes())).when(tView).readResource();
        doReturn(tView.new MetricRowLayout(mockedMetrick, "%d")).when(tView).createMetricRow(any(Element.class));

        List<TimeLineViewData> views = tView.getTimeLineViewData();
        assertEquals(tView.getHistoryLength(), 2);
        assertEquals(views.size(), 2);

        Iterator<List<String>> rows = views.get(0).iterator();

        List<String> row = rows.next();
        assertEquals(row.get(0), "WORKSPACES");
        assertEquals(row.get(1), "31/03");
        assertEquals(row.get(2), "28/02");

        row = rows.next();
        assertEquals(row.get(0), "title");
        assertEquals(row.get(1), "2");
        assertEquals(row.get(2), "1");

        row = rows.next();
        assertEquals(row.get(0), "");
        assertEquals(row.get(1), "");
        assertEquals(row.get(2), "");

        rows = views.get(1).iterator();

        row = rows.next();
        assertEquals(row.get(0), "PROJECTS");
        assertEquals(row.get(1), "31/03");
        assertEquals(row.get(2), "28/02");

        row = rows.next();
        assertEquals(row.get(0), "title");
        assertEquals(row.get(1), "1");
        assertEquals(row.get(2), "1");

        row = rows.next();
        assertEquals(row.get(0), "");
        assertEquals(row.get(1), "");
        assertEquals(row.get(2), "");

    }
}
