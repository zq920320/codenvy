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

a1 = filterByEvent(l, 'factory-created');
a2 = extractUrlParam(a1, 'FACTORY-URL', 'factoryUrl');
a3 = extractQueryParam(a2, 'factoryUrl', 'orgid', 'ORG-ID', 'orgId');
a4 = removeEmptyField(a3, 'orgId');
a = FOREACH a4 GENERATE orgId, factoryUrl;


result = setByField(a, 'orgId', 'factoryUrl');
