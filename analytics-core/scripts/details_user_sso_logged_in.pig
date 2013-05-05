---------------------------------------------------------------------------
-- Reveals detail information what ways were used by users to log in.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

result = countSecondParamInDist2ParamsEvent('$log', '$fromDate', '$toDate', 'user-sso-logged-in', 'USER', 'USING');

