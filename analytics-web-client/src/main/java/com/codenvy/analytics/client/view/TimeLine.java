/*
 * Copyright (C) 2013 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.codenvy.analytics.client.view;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import java.util.List;

/**
 * @author <a href="abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLine {
    public static Widget createWidget() {
        CellTable<String> timeline = new CellTable<String>();

        // Create name column.
        TextColumn<String> nameColumn = new TextColumn<String>() {
            @Override
            public String getValue(String value) {
                return value;
            }
        };

        timeline.addColumn(nameColumn);

        ListDataProvider<String> dataProvider = new ListDataProvider<String>();
        dataProvider.addDataDisplay(timeline);

        // Add the data to the data provider, which automatically pushes it to the
        // widget.
        List<String> list = dataProvider.getList();
        list.add("a");
        list.add("b");
        list.add("c");

        return timeline;
    }
}
