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

u = usersCreatedFromFactory(l);
f = LOAD '$LOAD_DIR' USING PigStorage() AS (tmpWs : chararray, referrer : chararray, factoryUrl : chararray);

j1 = JOIN u BY tmpWs, f BY tmpWs;
j2 = FOREACH j1 GENERATE u::user AS user, f::factoryUrl AS url;
j3 = GROUP j2 BY url;
result = FOREACH j3 {
    t1 = FOREACH j2 GENERATE user;
    t = DISTINCT t1;

    GENERATE group, t;
}
