/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.LongValueManager;
import com.codenvy.analytics.scripts.MapValueManager;
import com.codenvy.analytics.scripts.ScriptParameters;
import com.codenvy.analytics.scripts.ValueManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * The value of the metric will be calculated as: previous value + added value - removed value.
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public abstract class CumulativeMetric extends AbstractMetric {

    /** Runtime parameter name. Contains the directory where script are located. */
    private static final String       ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY = "analytics.metrics.initial.values";


    /** The value of {@value #ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY} runtime parameter. */
    public static final String        METRICS_INITIAL_VALUES                    =
                                                                                  System.getProperty(ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY);

    private final Metric              addedMetric;
    private final Metric              removedMetric;
    private final Map<String, Object> initialValues                             = new ConcurrentHashMap<String, Object>();

    CumulativeMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) throws IOException {
        super(metricType);

        this.addedMetric = addedMetric;
        this.removedMetric = removedMetric;

        try {
            readInitialValues();
        } catch (ParserConfigurationException e) {
            throw new IOException(e);
        } catch (SAXException e) {
            throw new IOException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getMandatoryParams() {
        Set<ScriptParameters> params = addedMetric.getMandatoryParams();
        params.addAll(removedMetric.getMandatoryParams());

        params.remove(ScriptParameters.FROM_DATE);

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ScriptParameters> getAdditionalParams() {
        Set<ScriptParameters> params = addedMetric.getAdditionalParams();
        params.addAll(removedMetric.getAdditionalParams());
        params.removeAll(getMandatoryParams());

        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Object loadValue(Map<String, String> context) throws IOException {
        String key = makeKeys(context).toString();

        Object value = initialValues.get(key);
        if (value != null) {
            return value;
        }

        return super.loadValue(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueManager getValueManager() {
        return new LongValueManager();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long evaluateValue(Map<String, String> context) throws InitialValueNotFoundException, IOException {
        Map<String, String> prevContext = new LinkedHashMap<String, String>(context);
        prevContext.put(ScriptParameters.TIME_UNIT.getName(), TimeUnit.DAY.toString());
        prevContext.put(ScriptParameters.FROM_DATE.getName(), context.get(ScriptParameters.TO_DATE.getName()));

        validateDatePeriod(prevContext);

        long addedEntities = (Long)addedMetric.getValue(prevContext);
        long removedEntities = (Long)removedMetric.getValue(prevContext);
        
        try {
            prevContext = TimeIntervalUtil.prevDateInterval(prevContext);
        } catch (ParseException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }

        long previousEntities = (Long)getValue(prevContext);

        return new Long(previousEntities + addedEntities - removedEntities);
    }

    protected void readInitialValues() throws ParserConfigurationException, SAXException, IOException {
        InputStream in = readResource();
        try {
            NodeList nodes = parseDocument(in);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)node;

                    if (metricType.toString().equalsIgnoreCase(element.getAttribute("type"))) {
                        extractInitialValues(element.getElementsByTagName("initial-value"));
                    }
                }
            }
        } finally {
            in.close();
        }
    }

    private NodeList parseDocument(InputStream in) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(in);

        return doc.getElementsByTagName("metric");
    }

    protected InputStream readResource() throws FileNotFoundException {
        return new FileInputStream(new File(METRICS_INITIAL_VALUES));
    }

    private void extractInitialValues(NodeList nodes) throws IOException {
        for (int j = 0; j < nodes.getLength(); j++) {
            Node node = nodes.item(j);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;
                NamedNodeMap nodeMap = element.getAttributes();

                Map<String, String> context = new HashMap<String, String>();
                for (int i = 0; i < nodeMap.getLength(); i++) {
                    String key = nodeMap.item(i).getNodeName();
                    String value = nodeMap.item(i).getNodeValue();

                    context.put(key, value);
                }

                String key = makeKeys(context).toString();
                Object value = getValueManager().valueOf(element.getTextContent());

                if (initialValues.containsKey(key)) {
                    throw new IOException("Wrong cumulative metric configuration");
                }

                initialValues.put(key, value);
            }
        }
    }

    private void validateDatePeriod(Map<String, String> context) throws IOException {
        long toDate = Long.valueOf(context.get(ScriptParameters.TO_DATE.getName()));
        for (String key : initialValues.keySet()) {
            long initialToDate = new MapValueManager().valueOf(key).get(ScriptParameters.TO_DATE.getName());

            if (initialToDate > toDate) {
                throw new InitialValueNotFoundException("Wrong cumulative metric configuration");
            }
        }
    }
}
