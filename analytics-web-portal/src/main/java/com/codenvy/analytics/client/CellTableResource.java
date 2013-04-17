/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.client;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;
import com.google.gwt.user.cellview.client.CellTable;

/**
 * @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a>
 */
public interface CellTableResource extends CellTable.Resources
{
    @Source({CellTable.Style.DEFAULT_CSS, "ui/CellTable.css"})
    TableStyle cellTableStyle();

    @Source("ui/images/listGrid/header.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource header();

    interface TableStyle extends CellTable.Style
    {
        String cellTableBox();

        String scrollTable();
    }

}
