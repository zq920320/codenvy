/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import com.codenvy.analytics.metrics.Metric;
import com.codenvy.analytics.metrics.MetricFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public class LayoutReader {

    private final String resource;

    public LayoutReader(String resource) {
        this.resource = resource;
    }

    /**
     * Reads resource layout.
     */
    public ViewLayout read() throws IOException, SAXException, ParserConfigurationException {
        InputStream in = readResource();

        try {
            Node rootNode = getRooNode(in);

            Map<String, String> attributes = fetchAttributes(rootNode);
            List<List<RowLayout>> layout = fetchLayout(rootNode);

            return new ViewLayout(attributes, layout);
        } finally {
            in.close();
        }
    }

    protected List<List<RowLayout>> fetchLayout(Node rootNode) throws IOException {
        NodeList viewNodes = ((Element)rootNode).getElementsByTagName("view");

        List<List<RowLayout>> layout = new ArrayList<List<RowLayout>>();

        for (int i = 0; i < viewNodes.getLength(); i++) {
            NodeList rowNodes = viewNodes.item(i).getChildNodes();
            layout.add(readRowLayout(rowNodes));
        }

        return layout;
    }

    protected Map<String, String> fetchAttributes(Node rootNode) {
        Map<String, String> attributes = new HashMap<String, String>();

        NamedNodeMap nodeMap = rootNode.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node item = nodeMap.item(i);
            attributes.put(item.getNodeName(), item.getNodeValue());
        }
        return attributes;
    }

    protected InputStream readResource() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

    protected Node getRooNode(InputStream in) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(in);

        return doc.getElementsByTagName("views").item(0);
    }

    protected List<RowLayout> readRowLayout(NodeList rowNodes) throws IOException {
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
                    layout.add(new EmptyRowLayoutImpl());
                } else if (nodeName.equals("total")) {
                    layout.add(createTotalRow(element));
                }
            }
        }
        return layout;
    }

    private RowLayout createTotalRow(Element element) throws IOException {
        String format = element.getAttribute("format");
        String types = element.getAttribute("types");

        return new TotalRowLayoutImpl(types, format);
    }

    protected RowLayout createDateRow(Element element) {
        String section = element.getAttribute("section");
        String formatDay = element.getAttribute("formatDay");
        String formatWeek = element.getAttribute("formatWeek");
        String formatMonth = element.getAttribute("formatMonth");
        return new DateRowLayoutImpl(section, formatDay, formatWeek, formatMonth);
    }

    protected AbstractRow createMetricRow(Element element) throws IOException {
        Metric metric = MetricFactory.createMetric(element.getAttribute("type").toUpperCase());
        String format = element.getAttribute("format");
        String title = element.getAttribute("title");

        return new MetricRowLayoutImpl(metric, title, format);
    }
}
