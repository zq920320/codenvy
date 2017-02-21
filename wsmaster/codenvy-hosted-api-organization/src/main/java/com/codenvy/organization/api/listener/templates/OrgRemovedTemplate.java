package com.codenvy.organization.api.listener.templates;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Defines thymeleaf template for organization removed notifications.
 *
 * @author Anton Korneta
 */
public class OrgRemovedTemplate extends ThymeleafTemplate {

    public OrgRemovedTemplate(String teamName) {
        context.setVariable("teamName", teamName);
    }

    @Override
    public String getPath() {
        return "/email-templates/team_deleted";
    }

}
