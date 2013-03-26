-----------------------------------------------------------------------------
-- Find top workspaces by amount of created projects.
-- If project was removed, then it will not be calculated.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

f1 = loadResources('$log');
fR = filterByDate(f1, '$fromDate', '$toDate');

aR = countEventsInWsFlatten(fR, 'project-created');
bR = countEventsInWsFlatten(fR, 'project-destroyed');

-------------------------------------------------------
-- Subtract the amount of destroyed projects
-------------------------------------------------------
j1 = JOIN aR BY ws LEFT, bR BY ws;
jR = FOREACH j1 GENERATE aR::ws, (bR::count IS NULL ? aR::count : aR::count - bR::count);

-------------------------------------------------------
-- Keep in result '$top' workspaces only
-------------------------------------------------------
r1 = GROUP jR ALL;
result = FOREACH r1 {
    GENERATE '$fromDate', '$toDate', TOP($top, 1, jR);
}

DUMP result;
