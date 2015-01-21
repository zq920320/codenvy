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
/*
 Predefined greeting pages
 */

if (window['IDE'] && window['IDE']['config']) {
    window.IDE.config.greetings = {
        // user anonymous
        "anonymous": "/ide-resources/_app/greetings/persistent/welcome.html",

        // user authenticated
        "authenticated": "/ide-resources/_app/greetings/persistent/welcome.html",

        // anonymous user in temporary workspace
        "anonymous-workspace-temporary": "/ide-resources/_app/greetings/temporary-workspace-rightpane-not-authenticated.html",

        // anonymous user in temporary private workspace
        "anonymous-workspace-temporary-private": "/ide-resources/_app/greetings/temporary-private-workspace-rightpane-not-authenticated.html",

        // authenticated user in temporary workspace
        "authenticated-workspace-temporary": "/ide-resources/_app/greetings/temporary-workspace-rightpane-authenticated.html",

        // authenticated user in temporary private workspace
        "authenticated-workspace-temporary-private": "/ide-resources/_app/greetings/temporary-private-workspace-rightpane-authenticated.html"
    };
}
