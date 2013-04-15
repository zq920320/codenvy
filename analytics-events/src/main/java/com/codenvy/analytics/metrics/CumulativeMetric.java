/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.scripts.LongValueManager;
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
    private static final String       ANALYTICS_METRICS_DEFAULT_VALUES_PROPERTY = "analytics.metrics.default.values";


    /** The value of {@value #ANALYTICS_METRICS_DEFAULT_VALUES_PROPERTY} runtime parameter. */
    public static final String        METRICS_DEFAULT_VALUES                    =
                                                                                  System.getProperty(ANALYTICS_METRICS_DEFAULT_VALUES_PROPERTY);

    private final Metric              addedMetric;
    private final Metric              removedMetric;
    private final Map<String, Object> defaults                                  = new ConcurrentHashMap<String, Object>();

    CumulativeMetric(MetricType metricType, Metric addedMetric, Metric removedMetric) throws IOException {
        super(metricType);

        this.addedMetric = addedMetric;
        this.removedMetric = removedMetric;

        try {
            readDefaultsValues();
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

        Object value = defaults.get(key);
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
    protected Long evaluateValue(Map<String, String> context) throws IOException {
        long addedEntities = (Long)addedMetric.getValue(context);
        long removedEntities = (Long)removedMetric.getValue(context);

        try {
            TimeIntervalUtil.prevDateInterval(context);
        } catch (ParseException e) {
            throw new IOException(e);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }

        long previousEntities = (Long)getValue(context);

        return new Long(previousEntities + addedEntities - removedEntities);
    }

    protected void readDefaultsValues() throws ParserConfigurationException, SAXException, IOException {
        InputStream in = readResource();
        try {
            NodeList nodes = parseDocument(in);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)node;

                    if (metricType.toString().equalsIgnoreCase(element.getAttribute("type"))) {
                        extractDefaultValues(element.getElementsByTagName("default-value"));
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
        return new FileInputStream(new File(METRICS_DEFAULT_VALUES));
    }

    private void extractDefaultValues(NodeList nodes) throws IOException {
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

                defaults.put(key, value);
            }
        }
    }
}
