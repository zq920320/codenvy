package com.codenvy.organization.api.listener.templates;

import com.codenvy.template.processor.html.thymeleaf.ThymeleafTemplate;

/**
 * Defines thymeleaf template for organization member removed notifications.
 *
 * @author Anton Korneta
 */
public class OrgMemberRemovedTemplate extends ThymeleafTemplate {

    public OrgMemberRemovedTemplate(String teamName, String managerName) {
        context.setVariable("teamName", teamName);
        context.setVariable("managerName", managerName);
    }

    @Override
    public String getPath() {
        return "/email-templates/user_removed_from_team";
    }

}
