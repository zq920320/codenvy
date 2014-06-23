package com.codenvy.migration;

import com.codenvy.commons.json.JsonHelper;
import com.codenvy.migration.model.ProjectDescription;
import com.codenvy.migration.model.Property;
import com.codenvy.migration.model.ProjectTypes;


import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

@Path("/internal/convert/{ws-id}")
public class MigrationService {

    static final         String PROPS_DIR              = ".vfs" + java.io.File.separatorChar + "props";
    static final         String PROPERTIES_FILE_SUFFIX = "_props";

    @Inject
    @Named("vfs.local.fs_root_dir")
    private String fsRoot;


    @GET
//    @RolesAllowed("system/admin")
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
        Map<String, String[]> inputProps = new HashMap<>();

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

            String projectType;
            String currentName = projectFile.getName().substring(0, projectName.lastIndexOf("_props"));
            List<Property> outputProps = new ArrayList<>();


            switch (inputProps.get("vfs:projectType")[0]) {

                case "Jar": {
                    projectType = ProjectTypes.MAVEN.toString();
                    outputProps.add(new Property("builder.name", new String[]{"maven"}));
                    outputProps.add(new Property("runner.name", new String[]{"JavaStandalone"}));
                    break;
                }
                case "Servlet/JSP": {
                    projectType = ProjectTypes.MAVEN.toString();
                    outputProps.add(new Property("builder.name", new String[]{"maven"}));
                    outputProps.add(new Property("runner.name", new String[]{"JavaWeb"}));
                    break;
                }
                default: {
                    projectType = ProjectTypes.UNKNOWN.toString();
                }
            }


            ProjectDescription projectDescription = new ProjectDescription();
            projectDescription.setType(projectType);
            projectDescription.setProperties(outputProps.toArray(new Property[outputProps.size()]));


            // Writing collected data to a file
            File codenvyFolder = new File(wsFolder, currentName + "/.codenvy");
            if (!codenvyFolder.exists())
                codenvyFolder.mkdir();


            File descriptionFile = new File(codenvyFolder, "project");
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
        }
        return Response.ok().entity(out.toString()).build();
    }
}
