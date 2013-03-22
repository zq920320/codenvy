-----------------------------------------------------------------------------
-- Finds users who did not create any projects.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT fromDate '00000000';
%DEFAULT toDate   '99999999';
%DEFAULT storeLocation 'users-without-projects';

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
-- prepare list of users who created projects in time frame
--
b1 = filterByEvent(fR, 'project-created');
b2 = extractUser(b1);
bR = prepareSet(b2, 'user');

result = differSets(aR, bR);

sR = FOREACH result GENERATE FLATTEN($0);
STORE sR INTO '$storeLocation' USING PigStorage(',');
