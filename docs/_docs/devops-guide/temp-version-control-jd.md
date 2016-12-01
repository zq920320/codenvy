---
title: "Temp Version Control - JD"
excerpt: ""
---
# Adding OAuth
Register an application in your GitHub account. Refer to [GitHub OAuth](https://eclipse-che.readme.io/docs/git#section-github-oauth) for additional information.

Update the `/etc/puppet/manifests/nodes/codenvy/codenvy.pp` with secret, id and callback:
```text  
oauth.github.clientid=yourClientID
oauth.github.clientsecret=yourClientSecret
oauth.github.authuri=https://github.com/login/oauth/authorize
oauth.github.tokenuri=https://github.com/login/oauth/access_token
oauth.github.redirecturis=http://$hostname/wsmaster/api/oauth/callback\
```
Execute `puppet agent -t`.

# Cloning Private Repos with Keys
You need to generate SSH key to be able to clone private repositories. 

For GitHub SSH keys are generated and uploaded automatically at Profile > Preferences > SSH > VCS. 
![github-button.png](images/github-button.png)
For other Git hosting providers SSH key should be generated and manually saved to profile settings of git hosting settings. Find more details on how to do that at https://eclipse-che.readme.io/docs/git#section-other-git-hosting-providers

# Git and Subversion Clients
After importing repository, you can perform the most common Git and SVN operations using interactive menus or as console commands.
![git-menu.png](images/git-menu.png)

![svn-menu.png](images/svn-menu.png)
# In-IDE Pull Requests for GitHub, BitBucket and Microsoft VSTS (git)
 
# Customizing How Factories Import Source Code
