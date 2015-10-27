/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.swarm.client;

import com.codenvy.swarm.client.json.DockerNode;
import com.codenvy.swarm.client.json.Node;
import com.codenvy.swarm.client.json.SwarmContainerInfo;
import com.google.common.base.Strings;
import com.google.common.io.CharStreams;

import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.plugin.docker.client.DockerCertificates;
import org.eclipse.che.plugin.docker.client.DockerConnector;
import org.eclipse.che.plugin.docker.client.DockerConnectorConfiguration;
import org.eclipse.che.plugin.docker.client.DockerException;
import org.eclipse.che.plugin.docker.client.InitialAuthConfig;
import org.eclipse.che.plugin.docker.client.ProgressMonitor;
import org.eclipse.che.plugin.docker.client.connection.DockerConnection;
import org.eclipse.che.plugin.docker.client.connection.DockerResponse;
import org.eclipse.che.plugin.docker.client.dto.AuthConfigs;
import org.eclipse.che.plugin.docker.client.json.ContainerConfig;
import org.eclipse.che.plugin.docker.client.json.ContainerCreated;
import org.eclipse.che.plugin.docker.client.json.HostConfig;
import org.eclipse.che.plugin.docker.client.json.SystemInfo;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.primitives.Ints.tryParse;

/**
 * Swarm implementation of {@link DockerConnector} that can be used on distributed system
 *
 * @author Eugene Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class SwarmDockerConnector extends DockerConnector {
    /**
     * We perform some operations manually on the docker node instead of swarm because of not implemented methods.
     * That's why swarm doesn't see container immediately after its start and can't make inspect or run exec.
     * So we inspect container with swarm to find when it is reachable by swarm.
     * This variable limits time to wait until container is reachable by swarm.
     */
    private static final long WAIT_CONTAINER_AVAILABILITY_TIMEOUT_MILLISECONDS = 40000;

    private final URI                   swarmManagerUri;
    private final NodeSelectionStrategy strategy;
    //TODO should it be done in other way?
    private final String                nodeDaemonScheme;

    public SwarmDockerConnector(URI swarmManagerUri,
                                DockerCertificates dockerCertificates,
                                InitialAuthConfig initialAuthConfig,
                                String dockerHostIp,
                                NodeSelectionStrategy strategy,
                                String nodeDaemonUrisScheme) {
        super(swarmManagerUri, dockerCertificates, initialAuthConfig, dockerHostIp);
        this.swarmManagerUri = swarmManagerUri;
        this.strategy = strategy;
        this.nodeDaemonScheme = nodeDaemonUrisScheme;
    }

    @Inject
    private SwarmDockerConnector(DockerConnectorConfiguration connectorConfiguration) {
        this(connectorConfiguration.getDockerDaemonUri(),
             connectorConfiguration.getDockerCertificates(),
             connectorConfiguration.getAuthConfigs(),
             connectorConfiguration.getDockerHostIp(),
             new RandomNodeSelectionStrategy(),
             "http");
    }

    @Override
    public void removeImage(String image, boolean force) throws IOException {
        final URI nodeUri = getNodeUriByImage(image);
        doRemoveImage(image, force, nodeUri);
    }

    @Override
    public void tag(String image, String repository, String tag) throws IOException {
        final URI nodeUri = getNodeUriByImage(image);
        doTag(image, repository, tag, nodeUri);
    }

    @Override
    public void push(String repository,
                     String tag,
                     String registry,
                     ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        String target = repository;
        if (tag != null) {
            target += ':' + tag;
        }
        final URI nodeUri = getNodeUriByImage(target);
        doPush(repository, tag, registry, progressMonitor, nodeUri);
    }

    @Override
    protected String buildImage(String repository,
                                File tar,
                                ProgressMonitor progressMonitor,
                                AuthConfigs authConfigs) throws IOException, InterruptedException {
        final DockerNode node = strategy.select(getAvailableNodes());
        return doBuildImage(repository, tar, progressMonitor, addrToUri(node.getAddr()), authConfigs);
    }

    @Override
    public String commit(String container, String repository, String tag, String comment, String author) throws IOException {
        final String addr = inspectContainer(container).getNode().getAddr();
        return doCommit(container, repository, tag, comment, author, addrToUri(addr));
    }

    @Override
    public ContainerCreated createContainer(ContainerConfig containerConfig, String containerName) throws IOException {
        final URI nodeUri = getNodeUriByImage(containerConfig.getImage());
        if (nodeUri == null) {
            return super.createContainer(containerConfig, containerName);
        }
        return doCreateContainer(containerConfig, containerName, nodeUri);
    }

    @Override
    public void pull(String image,
                     String tag,
                     String registry,
                     ProgressMonitor progressMonitor) throws IOException, InterruptedException {
        final DockerNode node = strategy.select(getAvailableNodes());
        doPull(image, tag, registry, progressMonitor, addrToUri(node.getAddr()));
    }

    @Override
    public void startContainer(final String container, HostConfig hostConfig)
            throws IOException {
        final Node node = inspectContainerDirectly(container).getNode();
        doStartContainer(container, hostConfig, addrToUri(node.getAddr()));

        final long containerStartTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - containerStartTime < WAIT_CONTAINER_AVAILABILITY_TIMEOUT_MILLISECONDS) {
            try {
                // try to inspect container with swarm, if not it will throw an exception
                inspectContainer(container);

                // container is reachable for swarm, return
                return;
            } catch (IOException e) {
                // container is not reachable yet
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    @Override
    public SwarmContainerInfo inspectContainer(String container) throws IOException {
        return doInspectContainer(container, swarmManagerUri);
    }

    @Override
    protected SwarmContainerInfo doInspectContainer(String container, URI dockerDaemonUri) throws IOException {
        final DockerConnection connection = openConnection(dockerDaemonUri);
        try {
            final DockerResponse response = connection.method("GET").path(String.format("/containers/%s/json", container)).request();
            final int status = response.getStatus();
            if (200 != status) {
                final String msg = CharStreams.toString(new InputStreamReader(response.getInputStream()));
                throw new DockerException(String.format("Error response from docker API, status: %d, message: %s", status, msg), status);
            }
            return JsonHelper.fromJson(response.getInputStream(), SwarmContainerInfo.class, null, FIRST_LETTER_LOWERCASE);
        } catch (JsonParseException e) {
            throw new IOException(e.getMessage(), e);
        } finally {
            connection.close();
        }
    }

    public SwarmContainerInfo inspectContainerDirectly(String container) throws IOException {
        for (DockerNode node : getAvailableNodes()) {
            final URI uri = addrToUri(node.getAddr());
            try {
                final SwarmContainerInfo info = doInspectContainer(container, uri);
                //direct inspection does not provide any information about container node
                final Node newNode = new Node();
                newNode.setAddr(node.getAddr());
                newNode.setName(node.getHostname());
                newNode.setIp(node.getAddr().substring(0, node.getAddr().indexOf(':')));
                info.setNode(newNode);
                return info;
            } catch (DockerException ex) {
                //ignore exception when image was not found
                if (ex.getStatus() != 404) {
                    throw ex;
                }
            }
        }
        return null;
    }

    /**
     * Fetches nodes from {@link SystemInfo#getDriverStatus()} which contains
     * information about all available nodes(addresses available RAM etc).
     * <pre>
     * Scheme of driver status content:
     *
     * [0] -> ["Nodes", "number of nodes"]
     * [1] -> ["hostname", "ip:port"]
     * [2] -> ["Containers", "number of containers"]
     * [3] -> ["Reserved CPUs", "number of free/reserved CPUs"]
     * [4] -> ["Reserved Memory", "number of free/reserved Memory"]
     * [5] -> ["Labels", "executiondriver=native-0.2, kernel..."]
     *
     * Example:
     *
     * [0] -> ["Nodes", "2"]
     * [1] -> ["swarm1.codenvy.com", "192.168.1.1:2375"]
     * [2] -> ["Containers", "14"]
     * [3] -> ["Reserved CPUs", "0/2"]
     * [4] -> ["Reserved Memory", "0 b / 3.79GiB"]
     * [5] -> ["Labels", "executiondriver=native-0.2, kernel..."]
     * [6] -> ["swarm2.codenvy.com", "192.168.1.2:2375"]
     * [7] -> ["Containers", "9"]
     * [8] -> ["Reserved CPUs", "0/2"]
     * [9] -> ["Reserved Memory", "0 b / 3.79GiB"]
     * [10] -> ["Labels", "executiondriver=native-0.2, kernel..."]
     * </pre>
     */
    private List<DockerNode> getAvailableNodes() throws IOException {
        final String[][] driverStatus = getSystemInfo().getDriverStatus();
        int count = 0;
        int startsFrom = 0;
        for (int i = 0; i < driverStatus.length; ++i) {
            if ("Nodes".equals(Strings.nullToEmpty(driverStatus[i][0]).trim())) {
                count = firstNonNull(tryParse(driverStatus[i][1]), 0);
                startsFrom = i + 1;
                break;
            }
        }
        final ArrayList<DockerNode> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            final String[] node = driverStatus[i * 5 + startsFrom];
            nodes.add(new DockerNode(node[0], node[1]));
        }
        return nodes;
    }

    /**
     * Foreach all nodes and search for node which contains required image
     */
    //TODO find better solution
    private URI getNodeUriByImage(String image) throws IOException {
        for (DockerNode node : getAvailableNodes()) {
            final URI uri = addrToUri(node.getAddr());
            try {
                doInspectImage(image, uri);
                return uri;
            } catch (DockerException ex) {
                //ignore exception when image was not found
                if (ex.getStatus() != 404) {
                    throw ex;
                }
            }
        }
        return null;
    }

    //TODO find better solution
    private URI addrToUri(String addr) {
        return URI.create(nodeDaemonScheme + "://" + addr);
    }
}
