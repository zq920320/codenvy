/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.metrics;

import com.codenvy.analytics.metrics.value.ValueData;
import com.codenvy.analytics.metrics.value.ValueDataFactory;

import org.w3c.dom.DOMException;
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
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class InitialValueContainer {

    /** Runtime parameter name. Contains the directory where script are located. */
    public static final String                      ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY =
                                                                                                "analytics.metrics.initial.values";

    /** The value of {@value #ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY} runtime parameter. */
    private static final String                     METRICS_INITIAL_VALUES                    =
                                                                                                System.getProperty(ANALYTICS_METRICS_INITIAL_VALUES_PROPERTY);

    private Map<MetricType, Map<String, ValueData>> initialValues;
    private Map<String, Map<String, String>>        uuids;
    private static final InitialValueContainer      INSTANCE                                  = new InitialValueContainer();

    private InitialValueContainer() {
    }

    /**
     * Factory method. Returns singleton instance.
     */
    public static InitialValueContainer getInstance() {
        return INSTANCE;
    }

    /**
     * Gets initial value for give metric and context.
     * 
     * @return initial value
     * @throws InitialValueNotFoundException if initial value not found
     * @throws IOException if another something gone wrong
     */
    public ValueData getInitalValue(MetricType metricType, String uuid) throws InitialValueNotFoundException, IOException {
        initialize();
        Map<String, ValueData> values = getValues(metricType);

        ValueData valueData = values.get(uuid);
        if (valueData == null) {
            throw new InitialValueNotFoundException("Initial value not found for " + metricType + " and context " + uuid);
        }

        return valueData;
    }

    /**
     * Checks if container contains initial value for given metric below or equal to the give date from context.
     * 
     * @throws InitialValueNotFoundException
     */
    public void validateExistenceInitialValueBefore(MetricType metricType, Map<String, String> context) throws InitialValueNotFoundException,
                                                                                                      IOException {
        initialize();
        Map<String, ValueData> values = getValues(metricType);

        Calendar toDate = Utils.getToDate(context);
        for (String uuid : values.keySet()) {
            Calendar inititalToDate = Utils.getToDate(uuids.get(uuid));

            if (inititalToDate.before(toDate) || inititalToDate.equals(toDate)) {
                return;
            }
        }

        throw new InitialValueNotFoundException("There is no initial value below given date");
    }

    /**
     * Get all initial values for give metric
     * 
     * @throws InitialValueNotFoundException if there are no values
     */
    private Map<String, ValueData> getValues(MetricType metricType) throws InitialValueNotFoundException {
        Map<String, ValueData> values = initialValues.get(metricType);
        if (values == null) {
            throw new InitialValueNotFoundException("Initial value not found for " + metricType);
        }

        return values;
    }

    protected void readInitialValues() throws ParserConfigurationException,
                                      SAXException,
                                      IOException,
                                      IllegalArgumentException,
                                      NoSuchMethodException,
                                      SecurityException,
                                      InstantiationException,
                                      IllegalAccessException,
                                      InvocationTargetException,
                                      DOMException {

        initialValues = new HashMap<MetricType, Map<String, ValueData>>();
        uuids = new HashMap<String, Map<String, String>>();

        InputStream in = readResource();
        try {
            NodeList nodes = parseDocument(in);

            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element)node;

                    MetricType metricType = MetricType.valueOf(element.getAttribute("type").toUpperCase());
                    extractInitialValues(metricType, element.getElementsByTagName("initial-value"));
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

    private void extractInitialValues(MetricType metricType, NodeList nodes) throws IOException,
                                                                            NoSuchMethodException,
                                                                            SecurityException,
                                                                            InstantiationException,
                                                                            IllegalAccessException,
                                                                            IllegalArgumentException,
                                                                            InvocationTargetException,
                                                                            DOMException {
        for (int j = 0; j < nodes.getLength(); j++) {
            Node node = nodes.item(j);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)node;

                Map<String, String> context = prepareContext(element.getAttributes());
                AbstractMetric metric = prepareMetric(metricType);

                Map<String, String> uuid = metric.makeUUID(context);
                ValueData valueData = ValueDataFactory.createValueData(metric.getValueDataClass(), element.getTextContent());

                Map<String, ValueData> values = initialValues.get(metricType);
                if (values == null) {
                    values = new HashMap<String, ValueData>();
                    initialValues.put(metricType, values);
                }

                values.put(uuid.toString(), valueData);
                uuids.put(uuid.toString(), uuid);
            }
        }
    }

    protected AbstractMetric prepareMetric(MetricType metricType) throws IOException {
        return (AbstractMetric)MetricFactory.createMetric(metricType);
    }

    private Map<String, String> prepareContext(NamedNodeMap nodeMap) {
        Map<String, String> context = new HashMap<String, String>();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            String key = nodeMap.item(i).getNodeName();
            String value = nodeMap.item(i).getNodeValue();

            context.put(key, value);
        }

        return context;
    }

    private void initialize() {
        if (initialValues != null) {
            return;
        }

        try {
            readInitialValues();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
