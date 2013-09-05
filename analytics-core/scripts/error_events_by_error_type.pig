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

-- e1 = LOAD '$LOG' using PigStorage() as (message : chararray);
-- e2 = FOREACH e1 GENERATE REGEX_EXTRACT_ALL($0, '([0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}) ([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3}).* [\\[]ERROR[]] [\\[](.*) [0-9]+[]] .*') 
--                         AS patten, messag;
--    dump e2;

a1 = loadErrorEvents('$LOG', '$FROM_DATE', '$TO_DATE');
             
result = countByField(a1, errortype);
