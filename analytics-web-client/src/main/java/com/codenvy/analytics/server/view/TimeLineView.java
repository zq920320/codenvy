/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.view;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricType;
import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.scripts.ScriptExecutor;
import com.codenvy.analytics.scripts.ScriptParameters;

import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineView {

    private static final int         HISTORY_LENGTH = 20;

    private static final String      RESOURCE_PATH  = "metrics/time-line.xml";

    private final List<Metric>       metrics;

    private final List<List<String>> rows;

    public TimeLineView(Calendar startDate, TimeUnit timeUnit) throws ParserConfigurationException, SAXException, IOException {
        this.metrics = readMetricsList();
        this.rows = new ArrayList<List<String>>();

        fillRows(startDate, timeUnit);
    }

    private void fillRows(Calendar startDate, TimeUnit timeUnit) throws ParserConfigurationException, SAXException, IOException {
        fillDateRow((Calendar)startDate.clone(), timeUnit);

        for (Metric metric : metrics) {
            fillMetricRow(metric, (Calendar)startDate.clone(), timeUnit);
        }
    }

    private void fillMetricRow(Metric metric, Calendar date, TimeUnit timeUnit) throws IOException {
        Map<String, String> context = new HashMap<String, String>(2);

        // TODO from the end ?

        for (int i = 0; i < HISTORY_LENGTH; i++) {
            List<String> row = new ArrayList<String>(HISTORY_LENGTH + 1);
            row.add(metric.getTitle());

            String fromDate = ScriptExecutor.PARAM_DATE_FORMAT.format(date.getTime());
            String toDate = fromDate;
            
            context.put(ScriptParameters.FROM_DATE.getName(), fromDate);
            context.put(ScriptParameters.TO_DATE.getName(), toDate);
            // context.put(key, value); TODO TimeUnit

            row.add(metric.getValue(context).toString());

            date.add(Calendar.DAY_OF_MONTH, 1);
            rows.add(row);
        }
    }

    private void fillDateRow(Calendar date, TimeUnit timeUnit) {
        List<String> row = new ArrayList<String>(HISTORY_LENGTH + 1);

        row.add(""); // cell(1,1) is blank

        DateFormat df = new SimpleDateFormat("dd/MM");
        for (int i = 0; i < HISTORY_LENGTH; i++) {
            row.add(df.format(date.getTime()));
            date.add(Calendar.DAY_OF_MONTH, 1);
        }

        rows.add(row);
    }

    public Iterator<List<String>> getRows() {
        return rows.iterator();
    }

    private List<Metric> readMetricsList() throws ParserConfigurationException, SAXException, IOException {
        List<Metric> metrics = new ArrayList<Metric>();

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE_PATH);

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String name;
            while ((name = reader.readLine()) != null) {
                if (name.startsWith("#")) {
                    continue;
                }

                try {
                    MetricType metricType = MetricType.valueOf(name.toUpperCase());
                    metrics.add(metricType.getInstance());
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
