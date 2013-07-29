/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2013] Codenvy, S.A.
 * All Rights Reserved.
 * NOTICE: All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */


package com.codenvy.analytics.server.vew.template;

import com.codenvy.analytics.metrics.TimeUnit;
import com.codenvy.analytics.metrics.Utils;
import com.codenvy.analytics.shared.TableData;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class Display {

    private final Map<String, String> attributes;
    private final List<Table> templates;

    /**
     * {@link Display} constructor. The class is supposed to be initialized via {@link #initialize(String)} factory method.
     */
    Display(Map<String, String> attributes, List<Table> templates) {
        this.attributes = new HashMap<>(attributes);
        this.templates = new ArrayList<>(templates);
    }

    /**
     * @return unmodifiable {@link #attributes}
     */
    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * @return unmodifiable {@link #templates}
     */
    public List<Table> getLayout() {
        return Collections.unmodifiableList(templates);
    }

    /**
     * Display initialization by reading data from the file.
     * 
     * @param resourcePath the path to resource containing {@link Table}
     * @return {@link Display}
     */
    public static Display initialize(String resourcePath) {
        try {
            return TemplateReader.read(resourcePath);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Retrieving {@link TableData}.
     * 
     * @param context the execution context
     * @return the list of {@link TableData}
     * @throws Exception if something gone wrong
     */
    public List<TableData> retrieveData(Map<String, String> context) throws Exception {
        List<TableData> result = new ArrayList<>(templates.size());

        for (Table table : templates) {
            result.add(table.retrieveData(context));
        }

        return result;
    }
}
