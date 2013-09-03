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

t1 = loadResources('$LOG', '$FROM_DATE', '$TO_DATE', '$USER', '$WS');
t2 = FOREACH t1 GENERATE *, '' AS id; -- it is required 'id' field to be in scheme
t3 = combineClosestEvents(t2, '$EVENT-started', '$EVENT-finished');
t = GROUP t3 ALL;

result = FOREACH t GENERATE SUM(t3.delta);
