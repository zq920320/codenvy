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

l = LOAD '$LOAD_DIR' USING PigStorage() AS (ws : chararray, user : chararray, project : chararray, type : chararray, repoUrl : chararray, factoryUrl : chararray);
r1 = FILTER l BY INDEXOF('$PARAM', $FIELD, 0) >= 0;
r2 = FOREACH r1 GENERATE factoryUrl;
result = DISTINCT r2;
