/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.ext.gae.server.applications.yaml;

import com.google.appengine.repackaged.net.sourceforge.yamlbeans.YamlException;
import com.google.appengine.repackaged.net.sourceforge.yamlbeans.YamlReader;
import com.google.appengine.repackaged.net.sourceforge.yamlbeans.YamlWriter;
import com.google.apphosting.utils.config.AppEngineConfigException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Java representation of application yaml file. It is important for adopting Python/PHP project to Java project and deploy every project
 * with using GAE Java SDK.
 *
 * @author Andrey Parfonov
 * @author Andrey Plotnikov
 * @author Valeriy Svydenko
 */
public class YamlAppInfo {
    // https://developers.google.com/appengine/docs/python/config/appconfig#Skipping_Files
    private static List<String> DEFAULT_SKIP_FILES = Arrays.asList("^(.*/)?#.*#",
                                                                   "^(.*/)?.*~",
                                                                   "^(.*/)?.*\\.py[co]",
                                                                   "^(.*/)?.*/RCS/.*",
                                                                   "^(.*/)?\\..*",
                                                                   "^(.*/)?.*\\.bak",
                                                                   "^(.*/)?\\.codenvy");

    // It is important that the following fields to have public (these fields set when configuration file is parsing) scope and these
    // names (equivalent name to app YAML tags)
    public String                                                                              application;
    public String                                                                              version;
    public String                                                                              runtime;
    public String                                                                              api_version;
    public List<Map<String, String>>                                                           builtins;
    public List<String>                                                                        includes;
    public List<Map<String, String>>                                                           handlers;
    public List<Map<String, String>>                                                           libraries;
    public List<String>                                                                        inbound_services;
    public String                                                                              default_expiration;
    public List<String>                                                                        skip_files;
    public Map<String, List<Map<String, List<Map<Map<String, String>, Map<String, String>>>>>> admin_console;
    public List<Map<String, String>>                                                           error_handlers;
    public List<Map<String, String>>                                                           backends;
    public String threadsafe = "false";
    public Map<String, String>                                                                 env_variables;
    public String                                                                              module;
    public String                                                                              instance_class;
    public Map<String, Integer>                                                                automatic_scaling;
    public Map<String, List<String>>                                                           pagespeed;

    /**
     * Transform content of app YAML file to abstract presentation in Java object.
     *
     * @param reader
     *         reader that read given YAML file
     * @return an instance with configuration of YAML file
     * @throws IOException
     *         if some problem happens with reading/writing operation
     */
    @NotNull
    public static YamlAppInfo parse(@NotNull Reader reader) throws IOException {
        YamlReader yamlReader = new YamlReader(reader);
        try {
            return yamlReader.read(YamlAppInfo.class);
        } catch (YamlException e) {
            throw new AppEngineConfigException(e.getMessage(), e);
        } finally {
            yamlReader.close();
        }
    }

    /**
     * Convert parameters of the element to String format.
     *
     * @return string format of configuration
     */
    @NotNull
    public String toYaml() {
        if (skip_files == null) {
            skip_files = DEFAULT_SKIP_FILES;
        }

        try {
            StringWriter buf = new StringWriter();
            YamlWriter yamlWriter = new YamlWriter(buf);

            yamlWriter.write(this);
            yamlWriter.close();

            return buf.toString()
                      .replace("!", "")
                      .replace(YamlAppInfo.class.getName(), "")
                      .replaceFirst("\n", "")
                      .replace("java.util.Arrays$ArrayList", "");
        } catch (YamlException e) {
            throw new AppEngineConfigException(e.getMessage(), e);
        }
    }

}