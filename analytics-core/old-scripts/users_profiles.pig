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

l = LOAD '$LOAD_DIR' USING PigStorage() AS (user : chararray, firstName: chararray, lastName: chararray, company: chararray, phone : chararray, job : chararray);

r1 = GROUP l BY user;
r2 = FOREACH r1 GENERATE group, FLATTEN(l);
result = FOREACH r2 GENERATE group, TOBAG(user, (firstName IS NULL ? '' : firstName),
                                                (lastName IS NULL ? '' : lastName),
                                                (company IS NULL ? '' : company),
                                                (phone IS NULL ? '' : phone),
                                                (job IS NULL ? '' : job));
