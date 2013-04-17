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

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="abazko@codenvy.com">Anatoliy Bazko</a>
 */
public class TimeLineWidget {
    public static Widget createWidget(Iterator<List<String>> iter) {
        CellTable<List<String>> timeline = new CellTable<List<String>>();

        ListDataProvider<List<String>> dataProvider = new ListDataProvider<List<String>>();
        dataProvider.addDataDisplay(timeline);

        List<String> row = iter.next();
        createsColumns(timeline, row.size());

        timeline.setColumnWidth(0, "300pt");
        timeline.setPageSize(Integer.MAX_VALUE);

        List<List<String>> list = dataProvider.getList();
        list.add(row);
        while (iter.hasNext()) {
            list.add(iter.next());
        }

        return timeline;
    }

    private static void createsColumns(CellTable<List<String>> timeline, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            CustomColumn column = new CustomColumn(i);
            timeline.addColumn(column);
        }
    }

    static class CustomColumn extends TextColumn<List<String>> {
        private final int number;

        public CustomColumn(int number) {
            this.number = number;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getValue(List<String> object) {
            return object.get(number);
        }
    }
}
