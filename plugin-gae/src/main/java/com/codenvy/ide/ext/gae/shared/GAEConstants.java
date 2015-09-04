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
package com.codenvy.ide.ext.gae.shared;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
public interface GAEConstants {

    //maven project type
    String GAE_JAVA_ID      = "GAEJava";
    String GAE_JAVA_PROJECT = "Google App Engine Project";

    //php project type
    String GAE_PHP_ID      = "GAEPhp";
    String GAE_PHP_PROJECT = "PHP App Engine Project";

    //python project type
    String GAE_PYTHON_ID      = "GAEPython";
    String GAE_PYTHON_PROJECT = "Python App Engine Project";

    //wizard gae constants
    String APPLICATION_ID = "applicationId";

    //java project structure path
    String SOURCE_FOLDER           = "src/main/java";
    String TEST_SOURCE_FOLDER      = "src/test/java";
    String WEB_INF_FOLDER          = "src/main/webapp/WEB-INF";
    String APP_ENGINE_WEB_XML_PATH = "src/main/webapp/WEB-INF/appengine-web.xml";

    //error validate constants
    String ERROR_WEB_ENGINE_VALIDATE = "webEngineNotExist";
    String ERROR_YAML_VALIDATE       = "yamlNotExist";
}