/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.layout;

import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface RowLayout {

    /**
     * Fill list by values depending on implementation.
     * 
     * @param context the execution context
     * @param length
     */
    List<String> fill(Map<String, String> context, int length) throws Exception;
}