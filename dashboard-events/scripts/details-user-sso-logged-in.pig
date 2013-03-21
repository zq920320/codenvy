---------------------------------------------------------------------------
-- Reveals detail information what ways were used by users to log in.
--
-- Incoming parameters:
-- log        - the list of resources to load
-- date       - beginning of the time frame
-- toDate     - ending of the time frame
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countSecondParamInDist2ParamsEvent('$log', '$date', '$toDate', 'user-sso-logged-in', 'USER', 'USING');
result = FOREACH r1 GENERATE '$date', '$toDate', *;

DUMP result;
