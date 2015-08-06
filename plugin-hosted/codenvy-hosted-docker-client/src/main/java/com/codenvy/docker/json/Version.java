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
package com.codenvy.docker.json;

/**
 * @author Anton Korneta
 */
public class Version {
    private String version;
    private String aPIVersion;
    private String goVersion;
    private String gitCommit;
    private String os;
    private String arch;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getaPIVersion() {
        return aPIVersion;
    }

    public void setaPIVersion(String aPIVersion) {
        this.aPIVersion = aPIVersion;
    }

    public String getGoVersion() {
        return goVersion;
    }

    public void setGoVersion(String goVersion) {
        this.goVersion = goVersion;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public void setGitCommit(String gitCommit) {
        this.gitCommit = gitCommit;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    @Override
    public String toString() {
        return "Version{" +
               "Version='" + version + '\'' +
               ", APIVersion='" + aPIVersion + '\'' +
               ", GoVersion='" + goVersion + '\'' +
               ", GitCommit='" + gitCommit + '\'' +
               ", Os='" + os + '\'' +
               ", Arch='" + arch + '\'' +
               '}';
    }
}
