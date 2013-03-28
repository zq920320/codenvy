-----------------------------------------------------------------------------
-- Finds users who did not sent invitation.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT fromDate '00010101';
%DEFAULT toDate   '55551231';

f1 = loadResources('$log');
fR = filterByDate(f1, '$fromDate', '$toDate');

--
-- prepare list of created users
-- extract user emails from ALIASES#...# or ALIASES#[...]#
--
a1 = filterByEvent(fR, 'user-created');
a2 = FOREACH a1 GENERATE FLATTEN(REGEX_EXTRACT_ALL(message, '.*ALIASES\\#[\\[]?([^\\#^\\[^\\]]*)[\\]]?\\#.*')) AS user;
a3 = FOREACH a2 GENERATE FLATTEN(TOKENIZE(user, ',')) AS user;
aR = prepareSet(a3, 'user');

--
-- prepare list of users who make invitation
--
b1 = filterByEvent(fR, 'user-invite');
b2 = extractUser(b1);
bR = prepareSet(b2, 'user');

result = differSets(aR, bR);
DUMP result;
