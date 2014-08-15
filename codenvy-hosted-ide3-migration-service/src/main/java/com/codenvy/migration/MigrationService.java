/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.migration;

import com.codenvy.api.project.server.ProjectJson;
import com.codenvy.commons.json.JsonHelper;
import com.codenvy.ide.factory.server.migration.ProjectTypeHelper;


import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Path("/internal/convert/{ws-id}")
public class MigrationService {

    static final         String PROPS_DIR              = ".vfs" + java.io.File.separatorChar + "props";
    static final         String PROPERTIES_FILE_SUFFIX = "_props";

    @Inject
    @Named("vfs.local.fs_root_dir")
    private String fsRoot;

    @GET
    @RolesAllowed("system/admin")
    @Produces("text/html")
    public Response convert(@PathParam("ws-id") String workspaceId, @QueryParam("project") String projectName) {

        FileMetadataSerializer metadataSerializer = new FileMetadataSerializer();

        File wsFolder = PathCalculator.calculateDirPath(new File(fsRoot), workspaceId);
        File propsFolder = new File(wsFolder, ".vfs/props");

        File[] projectsList = projectName == null ? propsFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith("_props");
            }
        })
                                                  : new File[]{new File(propsFolder, projectName + "_props")};

        StringBuilder out = new StringBuilder();
        Map<String, String[]> inputProps;

        for (File projectFile : projectsList) {
            // In case manually set project
            try  {
                 inputProps = metadataSerializer.read(new DataInputStream(new FileInputStream(projectFile)));
            } catch (FileNotFoundException e) {
                return Response.status(400)
                               .entity(String.format("Cannot find FS for project %s", projectFile.getName())).build();
            }
            catch (IOException e) {
                return Response.status(500)
                               .entity(String.format("IO Exception for project %s", projectFile.getName())).build();
            }

            if (inputProps == null || inputProps.isEmpty())
                return Response.status(400)
                        .entity(String.format("Cannot find properties for project %s", projectFile.getName())).build();

            String currentName = projectFile.getName().substring(0, projectFile.getName().lastIndexOf("_props"));
            String runnerTemplate = ProjectTypeHelper.getRunnerTemplate(inputProps.get("vfs:projectType")[0]);
            ProjectJson projectDescription = ProjectTypeHelper.projectTypeToDescription(inputProps.get("vfs:projectType")[0]);



            // Writing collected data to a file
            File codenvyFolder = new File(wsFolder, currentName + "/.codenvy");
            if (!codenvyFolder.exists()) {
                if (!codenvyFolder.mkdir())
                    return Response.status(400)
                                   .entity(String.format("Cannot create .codenvy folder for project %s",
                                                         projectFile.getName())).build();
            }


            File descriptionFile = new File(codenvyFolder, "project.json");
            if (!descriptionFile.exists()) {
                try (Writer writer = new BufferedWriter(new FileWriter(descriptionFile))) {
                    out.append(String.format("Converting project: %s, type: %s <br/> ", projectFile.getName(),
                                             projectDescription.getType()));
                    writer.write(JsonHelper.toJson(projectDescription));
                    File metadataDir = new File(codenvyFolder, PROPS_DIR);
                    File metadataFile = new File(metadataDir, descriptionFile.getName() + PROPERTIES_FILE_SUFFIX);
                    metadataFile.getParentFile().mkdirs();
                    DataOutputStream dos =
                            new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metadataFile)));
                    Map<String, String[]> properties = new HashMap<>();
                    properties.put("vfs:mimeType", new String[]{"application/json"});
                    metadataSerializer.write(dos, properties);
                    if (dos != null)
                        dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // writing run.dc5y if need
            if (runnerTemplate != null) {
                try (Writer writer = new BufferedWriter(new FileWriter(new File(wsFolder, currentName+"/run.dc5y")))) {
                    writer.write(runnerTemplate);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Response.ok().entity(out.toString()).build();
    }
}
