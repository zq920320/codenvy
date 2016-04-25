/* https://jira.codenvycorp.com/browse/CODENVY-298 */

// update workspace configs
db = db.getSiblingDB('organization');

var keepDirEntries = 0;
var fetchEntries = 0;
var cotributionEntries = 0;

db.workspaces2.find({}).snapshot().forEach(
   function(doc) {
       print("Doc ID:" + doc._id + ". Doc name:" + doc.config.name);
       doc.config.projects.forEach(
           function(project) {
               project.source.parameters.forEach(
                   function(param) {
                       if (param.name == "keepDirectory") {
                           param.name = "keepDir";
                           keepDirEntries++;
                       }
                       if (param.name == "remoteOriginFetch") {
                           param.name = "fetch";
                           fetchEntries++;
                       }
                   }
               );
	       for(i = 0; i<project.mixins.length; i++) {
                   if (project.mixins[i] == "contribution") {
                       project.mixins[i] = "pullrequest";
                       cotributionEntries++;
                   }
               }
           }
       );
       db.workspaces2.save(doc);
   }
);
print("keepDir parameter updated: " + keepDirEntries);
print("fetch parameter updated:   " + fetchEntries);
print("cotribution mixin updated: " + cotributionEntries);

// update factories
db = db.getSiblingDB('factory');

keepDirEntries = 0;
fetchEntries = 0;
cotributionEntries = 0;

db.factory.find({}).snapshot().forEach(
   function(doc) {
       print("Doc ID:" + doc._id);
       doc.factory.workspace.projects.forEach(
           function(project) {
               if (project.source.parameters.keepDirectory) {
                   project.source.parameters.keepDir = project.source.parameters.keepDirectory;
                   delete(project.source.parameters.keepDirectory);
                   keepDirEntries++;
               }
               if (project.source.parameters.remoteOriginFetch) {
                   project.source.parameters.fetch = project.source.parameters.remoteOriginFetch;
                   delete(project.source.parameters.remoteOriginFetch);
                   fetchEntries++;
               }
	       for(i = 0; i < project.mixins.length; i++) {
                   if (project.mixins[i] == "contribution") {
                       project.mixins[i] = "pullrequest";
                       cotributionEntries++;
                   }
               }
           }
       );
       db.factory.save(doc);
   }
);
print("keepDir parameter updated: " + keepDirEntries);
print("fetch parameter updated:   " + fetchEntries);
print("cotribution mixin updated: " + cotributionEntries);
