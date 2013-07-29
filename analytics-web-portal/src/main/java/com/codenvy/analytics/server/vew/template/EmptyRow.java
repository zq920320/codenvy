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


import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
class EmptyRow extends AbstractRow {

    /** {@link EmptyRow} constructor. */
    private EmptyRow() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    protected String doRetrieve(Map<String, String> context, int columnNumber) throws IOException {
        return "";
    }

    /** Factory method */
    public static EmptyRow initialize() {
        return new EmptyRow();
    }
}