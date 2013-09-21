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

IMPORT 'macros.pig';

l = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');

a1 = filterByEvent(l, 'factory-url-accepted');
a2 = extractUrlParam(a1, 'FACTORY-URL', 'factoryUrl');
a3 = removeEmptyField(a2, 'factoryUrl');
a4 = FOREACH a3 GENERATE ws, factoryUrl AS factoryUrl;
a = FOREACH a4 GENERATE ws, (INDEXOF(factoryUrl, '&ptype=', 0) > 0 ? REGEX_EXTRACT(factoryUrl, '(.*)&ptype=.*', 1) : factoryUrl) AS factoryUrl;

result = setByField(a, 'factoryUrl', 'ws');

