IMPORT 'macros.pig';

DEFINE keepOneInstance(X) RETURNS Y {
    x1 = GROUP $X BY event;
    $Y = FOREACH x1 {
	result = LIMIT $X 1;
	GENERATE group AS event, result AS message;
    }
};

l1 = loadResources('$log');
lR = filterByDate(l1, '$FROM_DATE', '$TO_DATE');

a1 = FILTER lR BY INDEXOF(message, '#null#', 0) >= 0;
a = keepOneInstance(a1);

b1 = FILTER lR BY INDEXOF(message, '##', 0) >= 0;
b2 = removeEvent(b1, 'user-update-profile');
b = keepOneInstance(b2);

c1 = FILTER lR BY INDEXOF(message, '[][][]', 0) >= 0;
c2 = removeEvent(c1, 'tenant-created,tenant-destroyed,tenant-started,tenant-stopped,user-sso-logged-in,user-sso-logged-out,user-created,user-removed,user-added-to-ws,user-invite');
c3 = FILTER c2 BY INDEXOF(message, 'WS#', 0) < 0 OR INDEXOF(message, 'USER#', 0) < 0;
c = keepOneInstance(c3);

r1 = UNION a, b, c;
result = FOREACH r1 GENERATE TOTUPLE(TOTUPLE(event), TOTUPLE(message));


