---------------------------------------------------------------------------
-- Finds amount of active projects.
---------------------------------------------------------------------------
IMPORT 'macros.pig';

l1 = loadResources('$log');
lR = filterByDate(l1, '$FROM_DATE', '$TO_DATE');

SPLIT lR INTO a1 IF INDEXOF(message, '#null#', 0) >= 0, b1 IF INDEXOF(message, '##', 0) >= 0, c1 IF INDEXOF(message, '[][][]', 0) >= 0;

c2 = removeEvent(c1, 'tenant-created,tenant-destroyed,tenant-started,tenant-stopped,user-sso-logged-in,user-sso-logged-out,user-created,user-removed,user-added-to-ws');

aR = FOREACH a1 GENERATE event, message;
bR = FOREACH b1 GENERATE event, message;
cR = FOREACH c2 GENERATE event, message;

STORE aR INTO 'aR' USING PigStorage();
STORE bR INTO 'bR' USING PigStorage();
STORE cR INTO 'cR' USING PigStorage();
