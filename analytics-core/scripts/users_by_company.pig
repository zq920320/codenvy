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

l = LOAD '$RESULT_DIR/PROFILES' USING PigStorage() AS (user : chararray, firstName: chararray, lastName: chararray, company: chararray, phone : chararray, job : chararray);

r1 = FOREACH l GENERATE *, REGEX_EXTRACT(UPPER(company), CONCAT(CONCAT('(', REPLACE(UPPER('$PARAM'), '\\?', '.')), ')'), 1) AS rexp;
r2 = FILTER r1 BY rexp IS NOT NULL;
result = FOREACH r2 GENERATE user;
