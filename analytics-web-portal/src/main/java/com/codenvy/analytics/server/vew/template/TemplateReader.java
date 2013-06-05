/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;

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
public class TemplateReader {

    private static final String ROW_TITLE    = "title";
    private static final String ROW_TOTAL    = "total";
    private static final String ROW_EMPTY    = "empty";
    private static final String ROW_METRIC   = "metric";
    private static final String ROW_LIST     = "list";
    private static final String ROW_DATE     = "date";

    private static final String NODE_DISPLAY = "display";
    private static final String NODE_TABLE   = "table";

    /** Reads templates from the file and initializes {@link Display}. */
    public static Display read(String resourcePath) throws IOException, SAXException, ParserConfigurationException {
        InputStream in = openResource(resourcePath);

        try {
            Node displayNode = getDisplayNode(in);

            Map<String, String> attributes = readAttributes(displayNode);
            List<Table> tables = readTables(displayNode);

            return new Display(attributes, tables);
        } finally {
            in.close();
        }
    }

    /** @return the root node of the XML scheme */
    protected static Node getDisplayNode(InputStream in) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc = dbFactory.newDocumentBuilder().parse(in);

        return doc.getElementsByTagName(NODE_DISPLAY).item(0);
    }

    /** Reads tables template */
    protected static List<Table> readTables(Node displayNode) throws IOException {
        NodeList viewNodes = ((Element)displayNode).getElementsByTagName(NODE_TABLE);

        List<Table> tables = new ArrayList<Table>();

        for (int i = 0; i < viewNodes.getLength(); i++) {
            Node tableNode = viewNodes.item(i);

            Map<String, String> attributes = readAttributes(tableNode);
            List<Row> rows = readRows(tableNode.getChildNodes());
            
            tables.add(new Table(attributes, rows));
        }

        return tables;
    }

    /** Fetches node's attributes */
    protected static Map<String, String> readAttributes(Node node) {
        Map<String, String> attributes = new HashMap<String, String>();

        NamedNodeMap nodeMap = node.getAttributes();
        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node item = nodeMap.item(i);
            attributes.put(item.getNodeName(), item.getNodeValue());
        }

        return attributes;
    }

    /** Open stream on the resource */
    protected static InputStream openResource(String resourcePath) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
    }

    /** Reads rows template */
    protected static List<Row> readRows(NodeList rowNodes) throws IOException {
        List<Row> rows = new ArrayList<Row>();

        for (int j = 0; j < rowNodes.getLength(); j++) {
            Node rowNode = rowNodes.item(j);

            if (rowNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)rowNode;

                Row row;
                switch (element.getNodeName()) {
                    case ROW_DATE:
                        row = DateRow.initialize(element);
                        break;

                    case ROW_METRIC:
                        row = MetricRow.initialize(element);
                        break;

                    case ROW_EMPTY:
                        row = EmptyRow.initialize(element);
                        break;

                    case ROW_TOTAL:
                        row = TotalRow.initialize(element);
                        break;

                    case ROW_TITLE:
                        row = TitleRow.initialize(element);
                        break;

                    case ROW_LIST:
                        row = ListRow.initialize(element);
                        break;

                    default:
                        continue;
                }

                rows.add(row);
            }
        }

        return rows;
    }
}
