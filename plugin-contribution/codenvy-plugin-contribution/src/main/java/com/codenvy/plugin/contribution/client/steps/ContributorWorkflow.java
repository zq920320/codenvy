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
package com.codenvy.plugin.contribution.client.steps;

import com.codenvy.plugin.contribution.client.steps.events.StepEvent;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.dto.DtoFactory;

import javax.validation.constraints.NotNull;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * This class is responsible to maintain the context between the different steps and to maintain the state of the contribution workflow.
 *
 * @author Kevin Pollet
 */
public class ContributorWorkflow {
    private final EventBus          eventBus;
    private final Step              initialStep;
    private final DtoFactory        dtoFactory;
    private       Provider<Context> contextProvider;
    private       Context           context;
    private       Configuration     configuration;
    private       Step              step;

    @Inject
    public ContributorWorkflow(@NotNull final Provider<Context> contextProvider,
                               @NotNull final EventBus eventBus,
                               @NotNull final AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep,
                               @NotNull final DtoFactory dtoFactory) {
        this.contextProvider = contextProvider;
        this.eventBus = eventBus;
        this.dtoFactory = dtoFactory;
        this.initialStep = authorizeCodenvyOnVCSHostStep;
    }

    /**
     * Initialize the contributor workflow to it's initial state.
     */
    public void init() {
        setStep(initialStep);
        context = contextProvider.get();
        configuration = dtoFactory.createDto(Configuration.class);
    }

    /**
     * Executes the current step.
     */
    public void executeStep() {
        step.execute(this);
    }

    /**
     * Sets the new current step.
     *
     * @param currentStep
     *         the current step.
     */
    public void setStep(@NotNull final Step currentStep) {
        this.step = currentStep;
    }

    /**
     * Returns the contributor workflow context object.
     *
     * @return the contributor workflow context object.
     */
    @NotNull
    public Context getContext() {
        return context;
    }

    /**
     * Returns the contributor workflow configuration object.
     *
     * @return the contributor workflow configuration object.
     */
    @NotNull
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is successfully done.
     *
     * @param step
     *         the successfully done step.
     */
    void fireStepDoneEvent(@NotNull final StepEvent.Step step) {
        eventBus.fireEvent(new StepEvent(step, true));
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is in error.
     *
     * @param step
     *         the step in error.
     */
    void fireStepErrorEvent(@NotNull final StepEvent.Step step) {
        fireStepErrorEvent(step, null);
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is in error.
     *
     * @param step
     *         the step in error.
     * @param errorMessage
     *         the error message.
     */
    void fireStepErrorEvent(@NotNull final StepEvent.Step step, final String errorMessage) {
        eventBus.fireEvent(new StepEvent(step, false, errorMessage));
    }
}
