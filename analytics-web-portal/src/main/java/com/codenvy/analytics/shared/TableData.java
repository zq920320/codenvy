/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * POJO<br>
 * 
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
@SuppressWarnings("serial")
public class TableData extends ArrayList<List<String>> {
    
    private static final long   serialVersionUID = -4742685800442075709L;

    // possible attributes
    private static final String ATTRIBUTE_WIDGET              = "widget";
    private static final String ATTRIBUTE_TAB_NAME            = "tabName";
    private static final String ATTRIBUTE_TAB_ID              = "tabId";
    private static final String ATTRIBUTE_SORTABLE            = "sortable";
    private static final String ATTRIBUTE_DEFAULT_SORT_COLUMN = "defaultSortColumn";

    private Map<String, String> attributes;

    /**
     * Default {@link TableData} constructor for serialization.
     */
    public TableData() {
    }

    /**
     * {@link TableData} constructor.
     */
    public TableData(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * Getter for {@link #attributes}
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * Setter for {@link #attributes}
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the value of {@link #ATTRIBUTE_WIDGET} attribute
     */
    public WidgetType getWidget() {
        String widgetName = attributes.get(ATTRIBUTE_WIDGET);
        return widgetName == null || widgetName.isEmpty() ? WidgetType.CELL_TABLE : WidgetType.valueOf(widgetName.toUpperCase());
    }

    /**
     * @return the value of {@link #ATTRIBUTE_TAB_ID} attribute
     */
    public String getTabId() {
        return attributes.get(ATTRIBUTE_TAB_ID);
    }

    /**
     * @return the valued of {@link #ATTRIBUTE_TAB_NAME} attribute
     */
    public String getTitle() {
        return attributes.get(ATTRIBUTE_TAB_NAME);
    }

    /**
     * @return the value of {@link #ATTRIBUTE_DEFAULT_SORT_COLUMN} attribute
     */
    public String getDefaultSortColumn() {
        return attributes.get(ATTRIBUTE_DEFAULT_SORT_COLUMN);
    }

    /**
     * @return the value of {@link #ATTRIBUTE_SORTABLE} attribute
     */
    public boolean isSortable() {
        String sortable = attributes.get(ATTRIBUTE_SORTABLE);
        return sortable == null || sortable.isEmpty() ? false : Boolean.valueOf(sortable);
    }

    /**
     * Enumeration of supported widgets.
     */
    public enum WidgetType {
        CELL_TABLE,
        TAB_PANEL,
        DATA_GRID
    }
}