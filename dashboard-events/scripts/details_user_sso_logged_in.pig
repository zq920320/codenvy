---------------------------------------------------------------------------
-- Reveals detail information what ways were used by users to log in.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

r1 = countSecondParamInDist2ParamsEvent('$log', '$fromDate', '$toDate', 'user-sso-logged-in', 'USER', 'USING');
result = FOREACH r1 GENERATE '$fromDate', '$toDate', *;

DUMP result;
