/* https://jira.codenvycorp.com/browse/CODENVY-343 */

// organization database updates
db = db.getSiblingDB("organization");

print("Updating workspaces2");
var workspaces2 = db.getCollection("workspaces2");
workspaces2.dropIndexes();
var wrWorkspaces = workspaces2.update({}, {$rename : {"owner" : "namespace", "config.attributes" : "attributes"}}, {multi : true});
print("Result: " + wrWorkspaces);

print("Updating snapshots");
var snapshots = db.getCollection("snapshots");
var wsSnapshots = snapshots.update({}, {$rename : {"owner" : "namespace"}}, {multi : true});
print("Result: " + wsSnapshots);

// factory database updates
db = db.getSiblingDB("factory");
print("Updating factories");
var factories = db.getCollection("factory");
var wrFactories = factories.update({}, {$unset : {"factory.workspace.attributes" : ""}}, {multi : true});
print("Result: " + wrFactories)
