/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.docker.client.manage;

import com.codenvy.docker.dto.AuthConfig;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.ui.cellview.CellTableResources;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of {@link CredentialsPreferencesView}
 *
 * @author Sergii Leschenko
 */
public class CredentialsPreferencesViewImpl implements CredentialsPreferencesView {
    interface CredentialsPreferencesViewImplUiBinder extends UiBinder<DockLayoutPanel, CredentialsPreferencesViewImpl> {
    }

    private final DockLayoutPanel rootElement;

    private ActionDelegate delegate;

    @UiField
    Button addButton;

    @UiField(provided = true)
    CellTable<AuthConfig> keys;

    @Inject
    public CredentialsPreferencesViewImpl(CredentialsPreferencesViewImplUiBinder uiBinder, CellTableResources res) {
        initCredentialsTable(res);
        rootElement = uiBinder.createAndBindUi(this);
    }

    private void initCredentialsTable(CellTable.Resources res) {
        keys = new CellTable<>(15, res);
        Column<AuthConfig, String> serverAddressColumn = new Column<AuthConfig, String>(new TextCell()) {
            @Override
            public String getValue(AuthConfig object) {
                return object.getServeraddress();
            }

            @Override
            public void render(Cell.Context context, AuthConfig object, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "preferences-docker-credentials-cellTable-serveraddress-"
                                      + context.getIndex() + "\">");
                super.render(context, object, sb);
            }
        };
        serverAddressColumn.setSortable(true);

        Column<AuthConfig, String> editColumn = new Column<AuthConfig, String>(new ButtonCell()) {
            @Override
            public String getValue(AuthConfig object) {
                return "Edit";
            }

            @Override
            public void render(Cell.Context context, AuthConfig object, SafeHtmlBuilder sb) {
                if (object != null) {
                    sb.appendHtmlConstant("<div id=\"" + UIObject.DEBUG_ID_PREFIX + "preferences-docker-credentials-cellTable-edit-"
                                          + context.getIndex() + "\">");
                    super.render(context, object, sb);
                }
            }
        };
        // Creates handler on button clicked
        editColumn.setFieldUpdater(new FieldUpdater<AuthConfig, String>() {
            @Override
            public void update(int index, AuthConfig object, String value) {
                delegate.onEditClicked(object);
            }
        });

        Column<AuthConfig, String> deleteColumn = new Column<AuthConfig, String>(new ButtonCell()) {
            @Override
            public String getValue(AuthConfig object) {
                return "Delete";
            }

            @Override
            public void render(Cell.Context context, AuthConfig object, SafeHtmlBuilder sb) {
                if (object != null) {
                    sb.appendHtmlConstant(
                            "<div id=\"" + UIObject.DEBUG_ID_PREFIX + "preferences-docker-credentials-cellTable-delete-"
                            + context.getIndex() + "\">");
                    super.render(context, object, sb);
                }
            }
        };
        // Creates handler on button clicked
        deleteColumn.setFieldUpdater(new FieldUpdater<AuthConfig, String>() {
            @Override
            public void update(int index, AuthConfig object, String value) {
                delegate.onDeleteClicked(object);
            }
        });

        keys.addColumn(serverAddressColumn, "Server Address");
        keys.addColumn(editColumn, "");//Do not show label for edit column
        keys.addColumn(deleteColumn, "");//Do not show label for delete column
        keys.setColumnWidth(serverAddressColumn, 70, Style.Unit.PCT);
        keys.setColumnWidth(editColumn, 20, Style.Unit.PX);
        keys.setColumnWidth(deleteColumn, 20, Style.Unit.PX);

        // don't show loading indicator
        keys.setLoadingIndicator(null);
    }

    @Override
    public void setKeys(@Nonnull Collection<AuthConfig> keys) {
        List<AuthConfig> appList = new ArrayList<>();
        for (AuthConfig key : keys) {
            appList.add(key);
        }

        this.keys.setRowData(appList);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootElement;
    }

    @UiHandler("addButton")
    void onSaveClicked(ClickEvent event) {
        delegate.onAddClicked();
    }
}
