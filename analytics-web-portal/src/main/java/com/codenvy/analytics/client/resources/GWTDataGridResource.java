/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.DataGrid.Style;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface GWTDataGridResource extends DataGrid.Resources {

     public static final GWTDataGridResource RESOURCES = GWT.create(GWTDataGridResource.class);
    
    @Source({DataGrid.Style.DEFAULT_CSS, "com/codenvy/analytics/client/ui/DataGrid.css"})
    Style dataGridStyle();

    @Source("com/codenvy/analytics/client/ui/images/listGrid/header.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource header();
}
