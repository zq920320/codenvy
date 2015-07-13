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

import javax.annotation.Nonnull;
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
    public ContributorWorkflow(@Nonnull final Provider<Context> contextProvider,
                               @Nonnull final EventBus eventBus,
                               @Nonnull final AuthorizeCodenvyOnVCSHostStep authorizeCodenvyOnVCSHostStep,
                               @Nonnull final DtoFactory dtoFactory) {
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
    public void setStep(@Nonnull final Step currentStep) {
        this.step = currentStep;
    }

    /**
     * Returns the contributor workflow context object.
     *
     * @return the contributor workflow context object.
     */
    @Nonnull
    public Context getContext() {
        return context;
    }

    /**
     * Returns the contributor workflow configuration object.
     *
     * @return the contributor workflow configuration object.
     */
    @Nonnull
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is successfully done.
     *
     * @param step
     *         the successfully done step.
     */
    void fireStepDoneEvent(@Nonnull final StepEvent.Step step) {
        eventBus.fireEvent(new StepEvent(step, true));
    }

    /**
     * Fires a {@link com.codenvy.plugin.contribution.client.steps.events.StepEvent} indicating that the given step is in error.
     *
     * @param step
     *         the step in error.
     */
    void fireStepErrorEvent(@Nonnull final StepEvent.Step step) {
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
    void fireStepErrorEvent(@Nonnull final StepEvent.Step step, final String errorMessage) {
        eventBus.fireEvent(new StepEvent(step, false, errorMessage));
    }
}
