package com.codenvy.organization.api.listener.templates;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Defines thymeleaf template for organization renamed notifications.
 *
 * @author Anton Korneta
 */
public class OrgRenamedTemplate extends ThymeleafTemplate {

    public OrgRenamedTemplate(String oldName, String newName) {
        context.setVariable("teamOldName", oldName);
        context.setVariable("teamNewName", newName);
    }

    @Override
    public String getPath() {
        return "/email-templates/team_renamed";
    }

}
