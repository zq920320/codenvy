/*
 *    Copyright (C) 2013 Codenvy.
 *
 */
package com.codenvy.analytics.server.vew.template;


import java.util.List;
import java.util.Map;

/** @author <a href="mailto:abazko@codenvy.com">Anatoliy Bazko</a> */
public interface Row {

    /**
     * Fills row.
     * 
     * @param context the execution context
     * @param length how many columns should be filled
     */
    List<List<String>> fill(Map<String, String> context, int length) throws Exception;
}