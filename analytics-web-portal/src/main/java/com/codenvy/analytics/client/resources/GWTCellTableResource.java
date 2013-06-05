/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface GWTCellTableResource extends CellTable.Resources {

    public static final GWTCellTableResource RESOURCES = GWT.create(GWTCellTableResource.class);

    @Source({CellTable.Style.DEFAULT_CSS, "com/codenvy/analytics/client/ui/CellTable.css"})
    TableStyle cellTableStyle();

    @Source("com/codenvy/analytics/client/ui/images/listGrid/header.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource header();

    interface TableStyle extends CellTable.Style {
        String cellTableBox();

        String scrollTable();
    }
}
