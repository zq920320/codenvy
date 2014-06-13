package com.codenvy.migration;

import com.codenvy.commons.json.JsonHelper;
import com.codenvy.migration.model.ProjectDescription;
import com.codenvy.migration.model.Property;
import com.codenvy.migration.model.ProjectTypes;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

        File[] projectsList = projectName == null ? wsFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory() && !pathname.getName().equals(".vfs");
            }
        })
                                                  : new File[]{new File(wsFolder, projectName)};

        StringBuilder out = new StringBuilder();

        for (File projectFolder : projectsList) {
            // In case manually set project
            if (!projectFolder.exists() || !projectFolder.isDirectory()) {
                return Response.status(400).entity(String.format("Cannot find FS for project %s", projectFolder.getName())).build();
            }


            String projectType = ProjectTypes.UNKNOWN.toString();
            File[] list = projectFolder.listFiles();
            if (list == null) {
                out.append(String.format("Project %s FS is found, but it is empty. <br/> ", projectFolder.getName()));
                continue;
            }

            List<Property> props = new ArrayList<>();

            for (File one : list) {
                if (one.getName().equals("pom.xml")) {
                    projectType = ProjectTypes.MAVEN.toString();
                    props.add(new Property("builder.name", new String[]{"maven"}));

                    // Trying to detect runner
                    String packaging = null;
                    try {
                        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                                .newInstance();
                        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                        Document document = documentBuilder.parse(one);
                        packaging = document.getElementsByTagName("packaging").item(0).getTextContent();
                    } catch (ParserConfigurationException  | SAXException  | IOException e) {
                        out.append(String.format("Cannot find packaging for project %s <br/>", projectFolder.getName()));
                    }
                    if (packaging == null)
                        break;
                    if (packaging.equals("war"))
                        props.add(new Property("runner.name", new String[] {"JavaWeb"}));
                    else if (packaging.equals("jar"))
                        props.add(new Property("runner.name", new String[] {"JavaStandalone"}));
                    break;
                }

            }


            ProjectDescription projectDescription = new ProjectDescription();
            projectDescription.setType(projectType);
            projectDescription.setProperties(props.toArray(new Property[props.size()]));


            // Writing collected data to a file
            File codenvyFolder = new File(projectFolder, ".codenvy");
            if (!codenvyFolder.exists())
                codenvyFolder.mkdir();


            File descriptionFile = new File(codenvyFolder, "project");
            if (!descriptionFile.exists()) {
                try (Writer writer = new BufferedWriter(new FileWriter(descriptionFile))) {
                    out.append(String.format("Converting project: %s, type: %s <br/> ", projectFolder.getName(),
                                             projectDescription.getType()));
                    writer.write(JsonHelper.toJson(projectDescription));
                    File metadataDir = new File(codenvyFolder, PROPS_DIR );
                    File metadataFile= new File(metadataDir, descriptionFile.getName() + PROPERTIES_FILE_SUFFIX);
                    metadataFile.getParentFile().mkdirs();
                    DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metadataFile)));
                    Map<String, String[]> properties = new HashMap<>();
                    properties.put("vfs:mimeType",  new String[]{"application/json"});
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
