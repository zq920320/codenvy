/*
 *
 * CODENVY CONFIDENTIAL
 * ________________
 *
 * [2012] - [2014] Codenvy, S.A.
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

a1 = FOREACH l GENERATE $PARAM;
a2 = removeEmptyField(a1, '$PARAM');
a = DISTINCT a2;

result = FOREACH a GENERATE UUID(),
                            TOTUPLE('date', ToMilliSeconds(ToDate('$TO_DATE', 'yyyyMMdd'))),
                            TOTUPLE('$PARAM', $PARAM);

STORE result INTO '$STORAGE_URL.$STORAGE_TABLE' USING MongoStorage;
