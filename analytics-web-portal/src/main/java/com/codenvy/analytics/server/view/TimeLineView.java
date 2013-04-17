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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineView {

    private static int                DEFAULT_HISTORY_LENGTH            = 20;

    private int                       historyLength                     = DEFAULT_HISTORY_LENGTH;

    /** Runtime parameter name. Contains the path to time-line view configuration. */
    private static final String       ANALYTICS_TIME_LINE_VIEW_PROPERTY = "analytics.view.time-line";

    /** The value of {@value #ANALYTICS_TIME_LINE_VIEW_PROPERTY} runtime parameter. */
    public static final String        ANALYTICS_TIME_LIVE_VIEW          =
                                                                          System.getProperty(ANALYTICS_TIME_LINE_VIEW_PROPERTY);

    /**
     * Actually contains data to display.
     */
    private List<List<String>>        filledRows;
    private List<Row>                 rowsLayout;
    private final Map<String, String> initContext;

    /**
     * @param initContext contains the first time interval for which data have to be calculated
     */
    public TimeLineView(Map<String, String> initContext) {
        this.initContext = initContext;
    }

    /**
     * TOOD
     */
    public List<List<String>> getRows() throws Exception {
        this.filledRows = new ArrayList<List<String>>();
        this.rowsLayout = readRowsLayout();

        for (Row row : rowsLayout) {
            filledRows.add(row.fill(new HashMap<String, String>(initContext)));
        }

        return filledRows;
    }

    /**
     * TODO
     */
    public int getHistoryLength() {
        return historyLength;
    }

    protected List<Row> readRowsLayout() throws ParserConfigurationException, SAXException, IOException {
        List<Row> rowsLayout = new ArrayList<Row>();


        InputStream in = readResource();
        try {
            NodeList nodes = parseDocument(in);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)node;

                    String nodeName = element.getNodeName();
                    if (nodeName.equals("date")) {
                        rowsLayout.add(createDateRow(element));
                    } else if (nodeName.equals("metric")) {
                        rowsLayout.add(createMetricRow(element));
                    } else if (nodeName.equals("empty")) {
                        rowsLayout.add(new EmptyRow());
                    } else if (nodeName.equals("section")) {
                        rowsLayout.add(createSectionRow(element));
                    }
                }
            }
        } finally {
            in.close();
        }

        return rowsLayout;
    }

    protected Row createDateRow(Element element) {
        String section = element.getAttribute("section");
        String format = element.getAttribute("format");
        return new DateRow(section, format);
    }

    protected Row createSectionRow(Element element) {
        String name = element.getAttribute("name");
        return new SectionRow(name);
    }

    protected MetricRow createMetricRow(Element element) throws IOException {
        Metric metric = MetricFactory.createMetric(element.getAttribute("type").toUpperCase());
        String format = element.getAttribute("format");
        return new MetricRow(metric, format);
    }

    private NodeList parseDocument(InputStream in) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(in);

        Node node = doc.getElementsByTagName("view").item(0);

        try {
            historyLength = Integer.valueOf(node.getAttributes().getNamedItem("history_length").getNodeValue());
        } catch (NumberFormatException e) {
            historyLength = DEFAULT_HISTORY_LENGTH;
        }

        return node.getChildNodes();
    }

    protected InputStream readResource() throws FileNotFoundException {
        return new FileInputStream(new File(ANALYTICS_TIME_LIVE_VIEW));
    }

    /**
     * TODO
     */
    protected interface Row {
        List<String> fill(Map<String, String> context) throws Exception;
    }

    protected class DateRow implements Row {
        private final String sectionName;
        private final String format;

        DateRow(String sectionName, String format) {
            this.sectionName = sectionName;
            this.format = format;
        }

        public List<String> fill(Map<String, String> context) throws Exception {
            List<String> row = new ArrayList<String>(getHistoryLength() + 1);
            row.add(sectionName);

            DateFormat df = new SimpleDateFormat(format);

            for (int i = 0; i < getHistoryLength(); i++) {
                Date date = ScriptExecutor.PARAM_DATE_FORMAT.parse(context.get(ScriptParameters.TO_DATE.getName()));
                row.add(df.format(date));

                context = TimeIntervalUtil.prevDateInterval(context);
            }

            return row;
        }
    }

    protected class MetricRow implements Row {
        private final Metric metric;
        private final String format;

        MetricRow(Metric metric, String format) {
            this.metric = metric;
            this.format = format == null || format.isEmpty() ? "%d" : format;
        }

        public List<String> fill(Map<String, String> context) throws Exception {
            List<String> row = new ArrayList<String>(getHistoryLength() + 1);
            row.add(metric.getTitle());

            for (int i = 0; i < getHistoryLength(); i++) {
                Object value = metric.getValue(new HashMap<String, String>(context));

                row.add(String.format(format, value));
                context = TimeIntervalUtil.prevDateInterval(context);
            }

            return row;
        }
    }

    protected class EmptyRow implements Row {
        public List<String> fill(Map<String, String> context) {
            List<String> row = new ArrayList<String>(getHistoryLength() + 1);
            for (int i = 0; i < getHistoryLength() + 1; i++) {
                row.add("");
            }
            return row;
        }
    }

    protected class SectionRow implements Row {
        private final String name;

        protected SectionRow(String name) {
            this.name = name;
        }

        public List<String> fill(Map<String, String> context) {
            List<String> row = new ArrayList<String>(getHistoryLength() + 1);
            row.add(name);

            for (int i = 1; i < getHistoryLength() + 1; i++) {
                row.add("");
            }
            return row;
        }
    }
}
