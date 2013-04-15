/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.view;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;
import com.codenvy.analytics.metrics.TimeIntervalUtil;
import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineView {

    public static final int           HISTORY_LENGTH = 20;

    /** Runtime parameter name. Contains the path to time-line view configuration. */
    private static final String       ANALYTICS_TIME_LINE_VIEW_PROPERTY = "analytics.view.time-line";


    /** The value of {@value #ANALYTICS_TIME_LINE_VIEW_PROPERTY} runtime parameter. */
    public static final String        ANALYTICS_TIME_LIVE_VIEW          =
                                                                          System.getProperty(ANALYTICS_TIME_LINE_VIEW_PROPERTY);

    private List<List<String>>        rows;

    private final Map<String, String> initContext;

    private List<Metric>              metrics;

    /**
     * @param initContext contains the first time interval for which data have to be calculated
     */
    public TimeLineView(Map<String, String> initContext) {
        this.initContext = initContext;
    }

    private void fillRows(Map<String, String> initContext) throws ParserConfigurationException,
                                                      SAXException,
                                                      IOException,
                                                      IllegalArgumentException,
                                                      ParseException {
        fillDateRow(initContext);

        for (Metric metric : metrics) {
            fillMetricRow(metric, initContext);
        }
    }

    private void fillMetricRow(Metric metric, Map<String, String> initContext) throws IOException, IllegalArgumentException, ParseException {
        Map<String, String> currentContext = new HashMap<String, String>(initContext);

        // rolling back to the last time interval
        List<String> row = new ArrayList<String>(HISTORY_LENGTH + 1);

        row.add(metric.getTitle());
        for (int i = 0; i < HISTORY_LENGTH; i++) {
            row.add(metric.getValue(new HashMap<String, String>(currentContext)).toString());
            TimeIntervalUtil.prevDateInterval(currentContext);
        }

        rows.add(row);
    }

    private void fillDateRow(Map<String, String> initContext) throws IllegalArgumentException, IOException, ParseException {
        List<String> row = new ArrayList<String>(HISTORY_LENGTH + 1);

        row.add(""); // cell(1,1) is blank

        Map<String, String> currentContext = new HashMap<String, String>(initContext);
        DateFormat df = new SimpleDateFormat("dd/MM");

        for (int i = 0; i < HISTORY_LENGTH; i++) {
            Date date = ScriptExecutor.PARAM_DATE_FORMAT.parse(currentContext.get(ScriptParameters.TO_DATE.getName()));
            row.add(df.format(date));

            TimeIntervalUtil.prevDateInterval(currentContext);
        }

        rows.add(row);
    }

    public List<List<String>> getRows() throws ParserConfigurationException,
                                           SAXException,
                                           IOException,
                                           IllegalArgumentException,
                                           ParseException {
        this.rows = new ArrayList<List<String>>();
        this.metrics = readMetricsList();

        fillRows(initContext);

        return rows;
    }

    protected List<Metric> readMetricsList() throws ParserConfigurationException, SAXException, IOException {
        List<Metric> metrics = new ArrayList<Metric>();

        InputStream in = new FileInputStream(new File(ANALYTICS_TIME_LIVE_VIEW));

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String name;
            while ((name = reader.readLine()) != null) {
                if (name.startsWith("#")) {
                    continue;
                }

                try {
                    metrics.add(MetricFactory.createMetric(name));
                } catch (IllegalArgumentException e) {
                    throw new IOException("Metric " + name + " not found");
                }
            }

        } catch (Exception e) {
            reader.close();
        }

        return metrics;
    }
}
