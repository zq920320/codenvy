-----------------------------------------------------------------------------
-- Finds users who сreated a project, but never deploy project
---------------------------------------------------------------------------
IMPORT 'macros.pig';

%DEFAULT fromDate '00010101';
%DEFAULT toDate   '55551231';

f1 = loadResources('$log');
fR = filterByDate(f1, '$fromDate', '$toDate');

--
-- prepare list of users who created projects
--
a1 = filterByEvent(fR, 'project-created');
a2 = extractUser(a1);
aR = prepareSet(a2, 'user');

--
-- prepare list of uses who deploy projects
--
b1 = filterByEvent(fR, 'application-created,project-deployed');
b2 = extractUser(b1);
bR = prepareSet(b2, 'user');

result = differSets(aR, bR);
DUMP result;