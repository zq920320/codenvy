/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2016] Codenvy, S.A.
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
package com.codenvy.plugin.contribution.client.jso;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.FormElement;

/**
 * Javascript Overlay class over FormData objects.
 */
public class FormData extends JavaScriptObject {

    /** JSO mandated protected constructor. */
    protected FormData() {
    }

    /**
     * Creates a new FormData.
     *
     * @return a FormData
     */
    public static final native FormData create() /*-{
        return new FormData();
    }-*/;

    /**
     * Creates a new FormData linked to the form element
     *
     * @return a FormData
     */
    public static final native FormData create(FormElement form) /*-{
        return new FormData(form);
    }-*/;

    /**
     * Creates a new FormData linked to the form element
     *
     * @return a FormData
     */
    public static final native FormData create(elemental.html.FormElement form) /*-{
        return new FormData(form);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, String value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, int value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, double value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, Blob value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     * @param filename
     *         the file name reported to the server
     */
    public final native void append(String name, Blob value, String filename) /*-{
        this.append(name, value, filename);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     */
    public final native void append(String name, File value) /*-{
        this.append(name, value);
    }-*/;

    /**
     * Appends a key/value pair to the form data.
     *
     * @param name
     *         the key
     * @param value
     *         the value
     * @param filename
     *         the file name reported to the server
     */
    public final native void append(String name, File value, String filename) /*-{
        this.append(name, value, filename);
    }-*/;
}
