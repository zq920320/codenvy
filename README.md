# Codenvy

https://www.codenvy.com/.  
Cloud workspaces for development teams featuring one-click Docker environments, team onboarding and collaboration, plus a workspace platform for DevOps. Codenvy can be run anywhere that CentOS or RHEL run (or try our [beta of Codenvy in Docker](https://github.com/codenvy/codenvy/tree/cli-in-container/docs)...it runs anywhere Docker runs!)

![Eclipse Che](https://www.eclipse.org/che/images/hero-home.png "Eclipse Che")

### One-click Docker Environments
Create workspaces with production runtimes containing your source code and dev tools. Choose any architecture - microservices, multi-tier, multi-container, or shared server. We excel at complex topologies ... [Read More](https://codenvy.com/solutions/bootstrapping)

### Team Onboarding and Collaboration
Onboard teams with powerful collaboration, workspace automation, and permissions. Devs can use their local IDE or the gorgeous Eclipse Che cloud IDE ... [Read More](https://codenvy.com/product/next-generation)

### Workspace Platform for DevOps
Manage workspaces at scale with programmable and customizable infrastructure that lets you control system performance, availability, and functionality ... [Read More](https://codenvy.com/product/technology)

### Deployment Types
You can install Codenvy behind your firewall or use it in our hosted public cloud. This is preferred by enterprises as it allows them complete control over the permissions, LDAP, JIRA and Jenkins integrations and connects seamlessly into their private toolchain. Individuals and small teams often prefer to use our hosted system at codenvy.io because it's simple and low-maintenance - although it gives up many administrative controls you can still connect to private repositories and image registries.
[Get Started with production Codenvy](https://codenvy.com/getting-started/).

We also have a beta of Codenvy running in Docker that can be run anywhere that Docker runs and supports offline installations. You can [try Dockerized Codenvy](https://github.com/codenvy/codenvy/tree/cli-in-container/docs) and [give us your feedback](https://github.com/codenvy/codenvy/issues).

### License
Codenvy is free for 3 users and licensed under Codenvy Fair Source. For more users, you can purchase Codenvy which brings with it enterprise support and optional professional services - it's licensed under the [commercial Codenvy license](https://codenvy.com/docs/terms-of-service.pdf). Contact us to [trial Codenvy for larger teams](https://codenvy.com/contact/download/).

### Dependencies
Codenvy requires CentOS / RHEL 7.1+ as the only initial dependency. Our installation scripts will install an installation manager based upon Java, which then coordinates the installation of Puppet. Puppet then configures the rest of the system with dependencies for Docker, Swarm, and a variety of other packages.

Our Docker-based Codenvy (in beta) only requires Docker on the machine which makes it runnable on Windows, Mac and Linux. [Try Dockerized Codenvy](https://github.com/codenvy/codenvy/tree/cli-in-container/docs) and [give us your feedback](https://github.com/codenvy/codenvy/issues).

### Clone

```sh
git clone https://github.com/codenvy/codenvy.git
```
If master is unstable, checkout the latest tagged version.

### Build and Run
```sh
cd codenvy
mvn clean install

# A new assembly is placed in:
cd onpremises-ide-packaging-tomcat-codenvy-allinone\target\

# Assembly:
onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip

# Update Codenvy's puppet with the new assembly:
cd /etc/puppet/files/servers/prod/aio/
rm onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip
cp ~/onpremises-ide-packaging-tomcat-codenvy-allinone-${version}.zip .
puppet agent -t
```
Codenvy will perform a short maintenance window and will be available at the hostname configured by the admin.

### Engage
* **Customize:** [Runtimes, stacks, commands, assemblies, extensions, plug-ins](https://github.com/eclipse/che/blob/master/CUSTOMIZING.md).
* **Support:** [We love to help you](https://codenvy.com/support/).
* **Developers:** Plug-in developers can get API help at [che-dev@eclipse.org](email:che-dev@eclipse.org). 
* **Website:** [codenvy.com](https://codenvy.com).
