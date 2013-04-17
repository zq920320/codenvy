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
import com.codenvy.analytics.shared.DataView;

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
public class TimeLineViewManager {

    /**
     * Default value for {@link #historyLength}.
     */
    private static int                DEFAULT_HISTORY_LENGTH            = 20;

    /**
     * How many time intervals have to be included in resulted data view.
     */
    private int                       historyLength                     = DEFAULT_HISTORY_LENGTH;

    /** Runtime parameter name. Contains the path to time-line view configuration. */
    private static final String       ANALYTICS_TIME_LINE_VIEW_PROPERTY = "analytics.view.time-line";

    /** The value of {@value #ANALYTICS_TIME_LINE_VIEW_PROPERTY} runtime parameter. */
    public static final String        ANALYTICS_TIME_LIVE_VIEW          =
                                                                          System.getProperty(ANALYTICS_TIME_LINE_VIEW_PROPERTY);
    /**
     * Actually contains data to display.
     */
    private List<DataView>            dataViews;

    /**
     * Context.
     */
    private final Map<String, String> initContext;

    /**
     * @param initContext contains the first time interval for which data have to be calculated
     */
    public TimeLineViewManager(Map<String, String> initContext) {
        this.initContext = initContext;
    }

    /**
     * Return {@link #dataViews}.
     */
    public List<DataView> getDataView() throws Exception {
        this.dataViews = new ArrayList<DataView>();

        List<List<RowLayout>> rowsLayout = readRowsLayout();
        for (int i = 0; i < rowsLayout.size(); i++) {
            dataViews.add(prepareDataView(rowsLayout.get(i)));
        }

        return dataViews;
    }

    private DataView prepareDataView(List<RowLayout> list) throws Exception {
        DataView dataView = new DataView();

        for (RowLayout row : list) {
            dataView.add(row.fill(new HashMap<String, String>(initContext)));
        }
        
        return dataView;
    }

    /**
     * Returns {@link #historyLength}.
     */
    public int getHistoryLength() {
        return historyLength;
    }

    protected List<List<RowLayout>> readRowsLayout() throws ParserConfigurationException, SAXException, IOException {
        List<List<RowLayout>> rowsLayout = new ArrayList<List<RowLayout>>();

        InputStream in = readResource();
        try {
            NodeList viewNodes = parseDocument(in);
            for (int i = 0; i < viewNodes.getLength(); i++) {
                NodeList rowNodes = viewNodes.item(i).getChildNodes();
                rowsLayout.add(readRowLayout(rowNodes));
            }
        } finally {
            in.close();
        }

        return rowsLayout;
    }

    private List<RowLayout> readRowLayout(NodeList rowNodes) throws IOException {
        List<RowLayout> layout = new ArrayList<RowLayout>();
        for (int j = 0; j < rowNodes.getLength(); j++) {
            Node rowNode = rowNodes.item(j);

            if (rowNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)rowNode;

                String nodeName = element.getNodeName();
                if (nodeName.equals("date")) {
                    layout.add(createDateRow(element));
                } else if (nodeName.equals("metric")) {
                    layout.add(createMetricRow(element));
                } else if (nodeName.equals("empty")) {
                    layout.add(new EmptyRowLayout());
                } else if (nodeName.equals("section")) {
                    layout.add(createSectionRow(element));
                }
            }
        }
        return layout;
    }

    protected RowLayout createDateRow(Element element) {
        String section = element.getAttribute("section");
        String format = element.getAttribute("format");
        return new DateRowLayout(section, format);
    }

    protected RowLayout createSectionRow(Element element) {
        String name = element.getAttribute("name");
        return new SectionRowLayout(name);
    }

    protected MetricRowLayout createMetricRow(Element element) throws IOException {
        Metric metric = MetricFactory.createMetric(element.getAttribute("type").toUpperCase());
        String format = element.getAttribute("format");
        return new MetricRowLayout(metric, format);
    }

    private NodeList parseDocument(InputStream in) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(in);

        Node node = doc.getElementsByTagName("views").item(0);

        try {
            historyLength = Integer.valueOf(node.getAttributes().getNamedItem("history-length").getNodeValue());
        } catch (NumberFormatException e) {
            historyLength = DEFAULT_HISTORY_LENGTH;
        }

        return ((Element)node).getElementsByTagName("view");
    }

    protected InputStream readResource() throws FileNotFoundException {
        return new FileInputStream(new File(ANALYTICS_TIME_LIVE_VIEW));
    }

    protected interface RowLayout {
        List<String> fill(Map<String, String> context) throws Exception;
    }

    protected class DateRowLayout implements RowLayout {
        private final String sectionName;
        private final String format;

        DateRowLayout(String sectionName, String format) {
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

    protected class MetricRowLayout implements RowLayout {
        private final Metric metric;
        private final String format;

        MetricRowLayout(Metric metric, String format) {
            this.metric = metric;
            this.format = format == null || format.isEmpty() ? "%d" : format;
        }

        public List<String> fill(Map<String, String> context) throws Exception {
            List<String> row = new ArrayList<String>(getHistoryLength() + 1);
            row.add(metric.getTitle());

            for (int i = 0; i < getHistoryLength(); i++) {
                Object value = metric.getValue(new HashMap<String, String>(context));

                if (value instanceof Double && ((Double)value).isNaN()) {
                    row.add("");
                } else {
                    row.add(String.format(format, value));
                }

                context = TimeIntervalUtil.prevDateInterval(context);
            }

            return row;
        }
    }

    protected class EmptyRowLayout implements RowLayout {
        public List<String> fill(Map<String, String> context) {
            List<String> row = new ArrayList<String>(getHistoryLength() + 1);
            for (int i = 0; i < getHistoryLength() + 1; i++) {
                row.add("");
            }
            return row;
        }
    }

    protected class SectionRowLayout implements RowLayout {
        private final String name;

        protected SectionRowLayout(String name) {
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
