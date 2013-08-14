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

a1 = loadResources('$log');
a2 = filterByDate(a1, '$FROM_DATE', '$TO_DATE');
a3 = filterByEvent(a2, '$EVENT');
a4 = extractWs(a3);
a5 = FOREACH a4 GENERATE ws;
a = FILTER a5 BY ws != 'default';

result = countByField(a, 'ws');
