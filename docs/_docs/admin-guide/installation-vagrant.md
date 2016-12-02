---
title: "Installation - Vagrant"
excerpt: "Other ways to install Codenvy."
---
# Vagrant  
You can use Vagrant to spin up a CentOS 7.2 node that can be used to perform an installation of Codenvy.  You need to have Vagrant and VirtualBox installed. 

Create a `Vagrantfile` in an empty directory and fill it with the contents of our [Vagrantfile hosted on our GitHub repository](https://github.com/codenvy/codenvy/blob/master/Vagrantfile). After you have created the Vagrantfile, run `vagrant up`. This will create a VM and run our installer inside of it. After a few minutes, you will have Codenvy installed.

Once you are logged in you can get started by creating [projects and workspaces](http://codenvy.readme.io/v4.0/docs/installation-getting-started#3-create-workspaces-and-projects).
# Multi-Node  
The multi-node installation of Codenvy lets you distribute the internal services that Codenvy depends upon onto separate physical nodes. You do not need this configuration if you want to scale Codenvy. You can perform a simple single server installation and then add up to 1000 machine nodes - see [Scaling](doc:scaling).

This installation is activated with the `--multi` flag for the installer. The nodes that Codenvy will be installed onto must be pre-configured with hostnames, SSH keys, user names, and ports. After these nodes are configured, the Codenvy installer will SSH into each node and install the appropriate software onto each server.
#### Properties File
If you want to customize the configuration before the installation begins, start with the `codenvy.properties` file that is tailored for a multi-node installation.  See: [installation pre-reqs.](https://codenvy.readme.io/v4.0/docs/installation?#installation)  

### DNS Names
Each node must be accessible by a pre-configured DNS name. You cannot use IP addresses, as the internal Codenvy services depend upon hostnames. 

The `site` and `machine` nodes must have public IP addresses.  All other nodes can communicate through port openings.  Each node must be reachable through SSH.  It is allowed to use fake DNS names, provided that all nodes can resolve them. 

| DNS>>>>>>>>>>>>>>>>>>>>>>>>>   | Description   
| --- | --- 
| `[your_hostname]`   | This is the primary hostname that users will access Codenvy on.   
| `puppet-master.[your_hostname]`   | Admin node that will host puppet master and the Codenvy installer.   
| `data.[your_hostname]`   | MongoDB and LDAP used for user account and profile storage.   
| `api.[your_hostname]`   | Core Codenvy REST services that manage workspaces and environments.   
| `machine.[your_dns]`   | Machines power the workspaces used by developers to edit, build and run. This node runs a Docker swarm cluster which can add additional servers to the cluster.   

### SSH Keys
All nodes should be reachable by SSH. Generate an SSH key pair and store it in `./ssh` of the puppet master. Take the public key part and add to all other nodes `~/.ssh/authorized_keys` file.  Once this is configured you should verify that each node is reachable from the puppet master node.

### Linux User
The default installation assumes that Codenvy is installed as root user. You can create a different Linux user that is not named `codenvy-im` or `codenvy` as these users are created created and used by Codenvy during installation.

Your users account must be able to execute sudo commands without a password prompt.  
```shell  
# Enable <user-name> to use sudo without a password prompt
echo "<user-name> ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers
```

# Microsoft Azure Marketplace  
These install docs are specific to the Codenvy Team and Codenvy Enterprise VMs available on the [Microsoft Azure Portal](https://azure.microsoft.com/en-us/marketplace/partners/codenvy/codenvy-on-prem/).

### Installing Codenvy on an Azure VM
In order to ensure you have the most up-to-date version available you must first install Codenvy onto your VM.
1. Add the [Codenvy Team or Enterprise VM](https://azure.microsoft.com/en-us/marketplace/partners/codenvy/codenvy-on-prem/) to your Azure account using Azure's Resource Manager deployment model.
2. We suggest using SSH public key authentication and deploying into a new resource group.

### Configuring Codenvy Azure VM
1. From your [Azure homepage](https://portal.azure.com/) select "Resource groups" in the left-hand menu.
![ScreenShot2016-03-01at10.16.46AM.png](images/ScreenShot2016-03-01at10.16.46AM.png)
2. Choose the resource group into which you deployed the Codenvy VM.
3. In the Resources list click on the network security group.
![ScreenShot2016-02-29at2.31.25PM.png](images/ScreenShot2016-02-29at2.31.25PM.png)
4. Select "Inbound security rules" from the list.
5. Click on the "Docker" entry.
6. Change the "Destination port range" from `32768 TCP` to `	
32768-65535 TCP`.
7. Save your changes.
8. From the left-hand pane select "Virtual Machines" and the Codenvy VM.
9. Click the DNS name label in the VM details pane.
10. Click "Configuration" on the right-hand pane.
11. Create a DNS name label and hit "Save".

### Installing the Latest Codenvy Release
1. SSH into the Codenvy Azure VM using the user you created with the Codenvy VM and the DNS you set.
2. If you used password authentication with your VM you must execute [additional steps](http://codenvy.readme.io/v4.0/docs/installation-other#section-pre-install-steps-for-password-authenticated-vms) before installing.
3. At the prompt:
```shell  
bash <(curl -L -s https://start.codenvy.com/install-codenvy)\
```
The installation takes ~15 minutes on a fast Internet connection. You can then access Codenvy at:
```http  
# Loads the login page
http://codenvy/\
```
The default admin user name is `admin` and password is `password`.  Please do not forget to change the password.
```http  
# Loads the embedded monitor to check service health
http://codenvy/monit/\
```
The default admin user name is `admin` and password is `zabbix`. You change this password in the Codenvy puppet configuration files.
```http  
# Loads the Swagger UI for browsing the built-in REST API
http://codenvy/api-docs-ui/\
```

```shell  
# Refresh your shell to add the CLI to your path
source ${HOME}/.bashrc

# Verify that the admin CLI is installed and working
codenvy im-version\
```
### Other Configuration Options
The main [Installation section](http://codenvy.readme.io/docs/installation#installation) includes further information including networking, LDAP, oAuth and SMTP setup.

### Pre-Install Steps for Password Authenticated VMs
```shell  
# Add the following to /etc/sudoers.d/waagen immediately after the existing entry
ALL=(ALL) NOPASSWD:ALL

# Change permissions
chown -R user:user /home/user

# Close SSH session and re-login\
```
