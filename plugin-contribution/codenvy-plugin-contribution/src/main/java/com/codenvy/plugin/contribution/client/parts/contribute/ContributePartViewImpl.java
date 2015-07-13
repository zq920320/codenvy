/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.plugin.contribution.client.parts.contribute;

import com.codenvy.plugin.contribution.client.ContributeMessages;
import com.codenvy.plugin.contribution.client.ContributeResources;
import com.codenvy.plugin.contribution.client.dialogs.paste.PasteEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;
import org.eclipse.che.ide.ui.buttonLoader.ButtonLoaderResources;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGPushButton;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.google.gwt.dom.client.Style.Cursor.POINTER;
import static com.google.gwt.dom.client.Style.TextAlign.CENTER;
import static com.google.gwt.dom.client.Style.Unit.EM;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Implementation of {@link com.codenvy.plugin.contribution.client.parts.contribute.ContributePartView}.
 */
public class ContributePartViewImpl extends BaseView<ContributePartView.ActionDelegate> implements ContributePartView {

    /** The status component. */
    private final StatusSteps statusSteps;

    /** The contribute button. */
    @UiField
    Button contributeButton;

    /** The resources for the view. */
    @UiField(provided = true)
    ContributeResources resources;

    /** The component for the URL of factory repository. */
    @UiField
    Anchor repositoryUrl;

    /** The component for the name of cloned branch. */
    @UiField
    Label clonedBranch;

    /** The input component for the contribution branch name. */
    @UiField
    ListBox contributionBranchName;

    /** Button used to refresh the contribution branch name list. */
    @UiField
    SVGPushButton refreshContributionBranchNameListButton;

    /** The input component for the contribution title. */
    @UiField
    TextBox contributionTitle;

    /** The input zone for the contribution comment. */
    @UiField
    TextArea contributionComment;

    /** The i18n messages. */
    @UiField(provided = true)
    ContributeMessages messages;

    /** The contribution status section. */
    @UiField
    FlowPanel statusSection;

    /** The status section message. */
    @UiField
    Label statusSectionMessage;

    /** Open on repository host button. */
    @UiField
    Button openPullRequestOnVcsHostButton;

    /** The start new contribution section. */
    @UiField
    HTMLPanel newContributionSection;

    /** The new contribution button. */
    @UiField
    Button newContributionButton;

    /** The contribute button text. */
    private String contributeButtonText;

    @Inject
    public ContributePartViewImpl(@Nonnull final PartStackUIResources partStackUIResources,
                                  @Nonnull final ContributeMessages messages,
                                  @Nonnull final ContributeResources resources,
                                  @Nonnull final ButtonLoaderResources buttonLoaderResources,
                                  @Nonnull final ContributePartViewUiBinder uiBinder) {
        super(partStackUIResources);

        this.messages = messages;
        this.resources = resources;
        this.statusSteps = new StatusSteps();

        setContentWidget(uiBinder.createAndBindUi(this));

        setTitle(messages.contributePartTitle());

        this.contributeButtonText = contributeButton.getText();
        this.contributeButton.addStyleName(buttonLoaderResources.Css().buttonLoader());

        this.refreshContributionBranchNameListButton.getElement().getStyle().setWidth(23, PX);
        this.refreshContributionBranchNameListButton.getElement().getStyle().setHeight(20, PX);
        this.refreshContributionBranchNameListButton.getElement().getStyle().setCursor(POINTER);
        this.refreshContributionBranchNameListButton.getElement().getStyle().setProperty("fill", "#dbdbdb");

        this.statusSection.setVisible(false);
        this.newContributionSection.setVisible(false);
        this.contributionTitle.getElement().setPropertyString("placeholder",
                                                              messages.contributePartConfigureContributionSectionContributionTitlePlaceholder());
        this.contributionComment.getElement().setPropertyString("placeholder",
                                                                messages.contributePartConfigureContributionSectionContributionCommentPlaceholder());

        this.statusSection.insert(statusSteps, 1);
    }

    @Override
    public void setRepositoryUrl(final String url) {
        repositoryUrl.setHref(url);
        repositoryUrl.setText(url);
    }

    @Override
    public void setClonedBranch(final String branch) {
        clonedBranch.setText(branch);
    }

    @Override
    public void setContributeButtonText(final String text) {
        contributeButton.setText(text);
        contributeButtonText = contributeButton.getText();
    }

    @Override
    public String getContributionBranchName() {
        final int selectedIndex = contributionBranchName.getSelectedIndex();
        return selectedIndex == -1 ? null : contributionBranchName.getValue(selectedIndex);
    }

    @Override
    public void setContributionBranchName(final String branchName) {
        for (int i = 0; i < contributionBranchName.getItemCount(); i++) {
            if (contributionBranchName.getValue(i).equals(branchName)) {
                contributionBranchName.setSelectedIndex(i);
                return;
            }
        }

        if (contributionBranchName.getItemCount() > 1) {
            contributionBranchName.setSelectedIndex(1);
        }
    }

    @Override
    public void setContributionBranchNameList(final List<String> branchNames) {
        final String selectedBranchName = getContributionBranchName();

        contributionBranchName.clear();
        contributionBranchName.addItem(messages.contributePartConfigureContributionSectionContributionBranchNameCreateNewItemText());
        for (final String oneBranchName : branchNames) {
            contributionBranchName.addItem(oneBranchName);
        }

        setContributionBranchName(selectedBranchName);
    }

    @Override
    public String getContributionComment() {
        return contributionComment.getValue();
    }

    @Override
    public void setContributionComment(final String comment) {
        contributionComment.setText(comment);
    }

    @Override
    public String getContributionTitle() {
        return contributionTitle.getValue();
    }

    @Override
    public void setContributionTitle(final String title) {
        contributionTitle.setText(title);
    }

    @Override
    public void setContributionBranchNameEnabled(final boolean enabled) {
        contributionBranchName.setEnabled(enabled);
    }

    @Override
    public void setContributionCommentEnabled(final boolean enabled) {
        contributionComment.setEnabled(enabled);
        if (!enabled) {
            contributionComment.getElement().getStyle().setBackgroundColor("#5a5c5c");
        } else {
            contributionComment.getElement().getStyle().clearBackgroundColor();
        }
    }

    @Override
    public void setContributionTitleEnabled(final boolean enabled) {
        contributionTitle.setEnabled(enabled);
    }

    @Override
    public void setContributeButtonEnabled(final boolean enabled) {
        contributeButton.setEnabled(enabled);
    }

    @Override
    public void showContributionTitleError(final boolean showError) {
        if (showError) {
            contributionTitle.addStyleName(resources.contributeCss().inputError());
        } else {
            contributionTitle.removeStyleName(resources.contributeCss().inputError());
        }
    }

    @Override
    public void showStatusSection(final String... statusSteps) {
        this.statusSteps.removeAll();
        for (final String oneStatusStep : statusSteps) {
            this.statusSteps.addStep(oneStatusStep);
        }
        statusSection.setVisible(true);
    }

    @Override
    public void setCurrentStatusStepStatus(boolean success) {
        statusSteps.setCurrentStepStatus(success);
    }

    @Override
    public void showStatusSectionMessage(final String message, final boolean error) {
        if (error) {
            statusSectionMessage.addStyleName(resources.contributeCss().errorMessage());
        } else {
            statusSectionMessage.removeStyleName(resources.contributeCss().errorMessage());
        }

        statusSectionMessage.setText(message);
        statusSectionMessage.setVisible(true);
    }

    @Override
    public void hideStatusSectionMessage() {
        statusSectionMessage.setVisible(false);
    }

    @Override
    public void hideStatusSection() {
        statusSection.setVisible(false);
    }

    @Override
    public void setContributionProgressState(final boolean progress) {
        if (progress) {
            contributeButton.setHTML("<i></i>");
        } else {
            contributeButton.setText(contributeButtonText);
        }
    }

    @Override
    public void showNewContributionSection(final String vcsHostName) {
        openPullRequestOnVcsHostButton
                .setText(messages.contributePartNewContributionSectionButtonOpenPullRequestOnVcsHostText(vcsHostName));
        newContributionSection.setVisible(true);
    }

    @Override
    public void hideNewContributionSection() {
        newContributionSection.setVisible(false);
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionBranchName")
    protected void contributionBranchNameChange(final ChangeEvent event) {
        final int selectedIndex = contributionBranchName.getSelectedIndex();
        if (selectedIndex == 0) {
            delegate.onCreateNewBranch();
        }
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("refreshContributionBranchNameListButton")
    protected void refreshContributionBranchNameList(final ClickEvent event) {
        delegate.onRefreshContributionBranchNameList();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionComment")
    protected void contributionCommentChanged(final ValueChangeEvent<String> event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionTitle")
    protected void contributionTitleChanged(final ValueChangeEvent<String> event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("openPullRequestOnVcsHostButton")
    protected void openPullRequestOnVcsHostClick(final ClickEvent event) {
        delegate.onOpenPullRequestOnVcsHost();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("newContributionButton")
    protected void newContributionClick(final ClickEvent event) {
        delegate.onNewContribution();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributeButton")
    protected void contributeClick(final ClickEvent event) {
        delegate.onContribute();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionTitle")
    protected void contributionTitleKeyUp(final KeyUpEvent event) {
        delegate.updateControls();
    }

    @SuppressWarnings("UnusedParameters")
    @UiHandler("contributionTitle")
    protected void contributionTitlePaste(final PasteEvent event) {
        delegate.updateControls();
    }

    private class StatusSteps extends FlowPanel {
        private final List<StatusStep> steps;
        private       int              currentStep;

        private StatusSteps() {
            this.currentStep = 0;
            this.steps = new ArrayList<>();

            getElement().getStyle().setProperty("display", "flex");
            getElement().getStyle().setProperty("flexDirection", "column");
        }

        public void addStep(final String label) {
            final StatusStep statusStep = new StatusStep(steps.size() + 1, label);

            steps.add(statusStep);
            add(statusStep);
        }

        public void removeAll() {
            clear();
            currentStep = 0;
            steps.clear();
        }

        public void setCurrentStepStatus(final boolean status) {
            steps.get(currentStep).setStatus(status);
            currentStep++;
        }
    }

    private class StatusStep extends FlowPanel {
        private final SimplePanel status;

        private StatusStep(final int index, final String label) {
            final Label indexLabel = new Label(String.valueOf(index));
            final Label titleLabel = new Label(label);
            this.status = new SimplePanel();

            add(indexLabel);
            add(titleLabel);
            add(this.status);

            // initialize panel style
            this.getElement().getStyle().setProperty("display", "flex");
            this.getElement().getStyle().setProperty("flexDirection", "row");
            this.getElement().getStyle().setMarginBottom(1, EM);
            this.getElement().getStyle().setHeight(24, PX);

            // initialize index style
            indexLabel.getElement().getStyle().setColor("#47AFDD");
            indexLabel.getElement().getStyle().setProperty("border", "1px solid #a1a1a1");
            indexLabel.getElement().getStyle().setProperty("borderRadius", 14, PX);
            indexLabel.getElement().getStyle().setTextAlign(CENTER);
            indexLabel.getElement().getStyle().setWidth(22, PX);
            indexLabel.getElement().getStyle().setHeight(22, PX);
            indexLabel.getElement().getStyle().setBackgroundColor("#353535");
            indexLabel.getElement().getStyle().setProperty("alignSelf", "center");
            indexLabel.getElement().getStyle().setMarginRight(15, PX);
            indexLabel.getElement().getStyle().setLineHeight(22, PX);
            indexLabel.getElement().getStyle().setProperty("flexShrink", "0");

            // initialize label style
            titleLabel.getElement().getStyle().setProperty("alignSelf", "center");
            titleLabel.getElement().getStyle().setProperty("flexShrink", "0");

            // initialize status style
            this.status.getElement().getStyle().setProperty("display", "flex");
            this.status.getElement().getStyle().setProperty("justifyContent", "flex-end");
            this.status.getElement().getStyle().setProperty("alignSelf", "center");
            this.status.getElement().getStyle().setProperty("flexGrow", "2");
            this.status.getElement().getStyle().setProperty("flexShrink", "0");
        }

        public void setStatus(final boolean success) {
            status.clear();
            status.add(getStatusImage(success));
        }

        private SVGImage getStatusImage(final boolean success) {
            final SVGImage image = new SVGImage(success ? resources.statusOkIcon() : resources.statusErrorIcon());
            image.getElement().getStyle().setWidth(15, PX);
            image.getElement().getStyle().setProperty("fill", success ? "#FFFFFF" : "#CF3C3E");

            return image;
        }
    }
}
