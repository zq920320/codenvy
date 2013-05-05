-----------------------------------------------------------------------------
-- Finds users who did not create any projects.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
fR = filterByDate(f1, '$fromDate', '$toDate');

--
-- prepare list of created users
-- extract user emails from ALIASES#...# or ALIASES#[...]#
--
a1 = filterByEvent(fR, 'user-created');
a2 = extractUserFromAliases(a1);
aR = prepareSet(a2, 'user');

--
-- prepare list of users who created projects in time frame
--
b1 = filterByEvent(fR, 'project-created');
b2 = extractUser(b1);
bR = prepareSet(b2, 'user');

result = differSets(aR, bR);

