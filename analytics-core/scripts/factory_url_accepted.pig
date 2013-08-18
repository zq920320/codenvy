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

a1 = loadResources('$LOG');
a2 = filterByDate(a1, '$FROM_DATE', '$TO_DATE');
a3 = filterByEvent(a2, 'factory-url-accepted');
a4 = extractWs(a3);
a5 = extractParam(a4, 'REFERRER', 'referrerUrl');
a6 = extractParam(a5, 'FACTORY-URL', 'factoryUrl');

a = FOREACH a6 GENERATE ws, referrerUrl, factoryUrl;
b = LOAD '$LOAD_DIR' USING PigStorage() AS (ws : chararray, referrerUrl : chararray, factoryUrl : chararray);

c1 = UNION a, b;
c = DISTINCT c1;

STORE c INTO '$STORE_DIR' USING PigStorage();


result = countAll(a);
