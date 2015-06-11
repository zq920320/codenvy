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
package com.codenvy.ide.onpremises;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.api.runner.dto.ResourcesDescriptor;
import org.eclipse.che.api.runner.gwt.client.RunnerServiceClient;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.Bytes;
import org.eclipse.che.ide.util.loging.Log;
import org.vectomatic.dom.svg.ui.SVGImage;

import javax.annotation.Nullable;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Show memory indicator.
 *
 * @author Oleksii Orel
 * @author Valeriy Svydenko
 */
public class MemoryIndicatorAction extends Action implements CustomComponentAction {
    private static final int FULL_WIDTH = 100;

    private final PanelResources         resources;
    private final RunnerServiceClient    runnerServiceClient;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final LocalizationConstants  locale;
    private final Element                usedMemoryElement;
    private final Element                valueUsedMemoryElement;
    private final Element                totalMemoryElement;
    private final Element                valueTotalMemoryElement;
    private final AnalyticsEventLogger   eventLogger;
    private final Element                tooltipBodyMessageElement;

    private String totalMemory;
    private String usedMemory;

    @Inject
    public MemoryIndicatorAction(PanelResources resources,
                                 LocalizationConstants locale,
                                 RunnerServiceClient runnerServiceClient,
                                 AnalyticsEventLogger eventLogger,
                                 DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.resources = resources;
        this.locale = locale;
        this.eventLogger = eventLogger;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.runnerServiceClient = runnerServiceClient;

        valueUsedMemoryElement = DOM.createSpan();
        valueUsedMemoryElement.setClassName(resources.subscriptionsCSS().usedMemory());
        valueTotalMemoryElement = DOM.createSpan();
        valueTotalMemoryElement.setClassName(resources.subscriptionsCSS().totalMemory());
        usedMemoryElement = DOM.createDiv();
        usedMemoryElement.setClassName(resources.subscriptionsCSS().indicatorBackground());
        usedMemoryElement.setId("memory-widget-usedram");
        totalMemoryElement = DOM.createDiv();
        totalMemoryElement.setClassName(resources.subscriptionsCSS().indicatorBackground());

        tooltipBodyMessageElement = DOM.createSpan();
    }

    public void setTotalMemorySize(String totalMemorySize) {
        this.totalMemory = totalMemorySize;
        updateMemoryIndication(totalMemory, usedMemory);
    }

    public void setUsedMemorySize(String usedMemorySize) {
        this.usedMemory = usedMemorySize;
        updateMemoryIndication(totalMemory, usedMemory);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        final FlowPanel panel = new FlowPanel();
        final Element memoryElement = DOM.createDiv();
        final Element tooltipElement = DOM.createDiv();
        final Element tooltipHeader = DOM.createDiv();
        final Element tooltipHeaderMessageElement = DOM.createSpan();
        final Element tooltipBody = DOM.createDiv();
        final Element tooltipArrow = DOM.createDiv();

        panel.addStyleName(resources.subscriptionsCSS().panel());
        memoryElement.setId("memory-widget-panel");
        memoryElement.setClassName(resources.subscriptionsCSS().memoryIndicator());

        // add the 5 children
        memoryElement.appendChild(valueUsedMemoryElement);
        memoryElement.appendChild(valueTotalMemoryElement);
        memoryElement.appendChild(usedMemoryElement);
        memoryElement.appendChild(totalMemoryElement);
        memoryElement.appendChild(tooltipElement);

        tooltipBodyMessageElement.setInnerHTML(locale.memoryIndicatorTooltipMessage());
        tooltipHeaderMessageElement.setInnerHTML(locale.memoryIndicatorTooltipTitle());
        // add handlers
        panel.addDomHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                tooltipElement.getStyle().setRight(0, PX);
                tooltipElement.getStyle().setBottom(0, PX);
                tooltipElement.getStyle().setDisplay(BLOCK);

                final Element parent = panel.getElement();
                final int screenWidth = Document.get().getClientWidth();
                final int screenHeight = Document.get().getClientHeight();
                final int parentRight = screenWidth - parent.getAbsoluteRight();
                final double parentMiddleRight = parentRight + (parent.getClientWidth() / 2.0);

                double right = parentRight - (tooltipElement.getOffsetWidth() / 2.0);
                right += parent.getClientWidth() / 2.0;
                if (right < 0) {
                    right = 0;
                }
                if (screenWidth < (right + tooltipElement.getOffsetWidth())) {
                    right = screenWidth - tooltipElement.getOffsetWidth();
                }
                tooltipElement.getStyle().setRight(right, PX);
                tooltipArrow.getStyle().setRight(parentMiddleRight - (tooltipArrow.getOffsetWidth() / 2.0), PX);
                tooltipElement.getStyle()
                              .setBottom(screenHeight - parent.getAbsoluteTop() + (tooltipArrow.getOffsetHeight() / 2.0), PX);
                tooltipArrow.getStyle().setBottom(screenHeight - parent.getAbsoluteTop() - (tooltipArrow.getOffsetHeight() / 2.0), PX);
            }
        }, MouseOverEvent.getType());

        panel.addDomHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                tooltipElement.getStyle().setDisplay(NONE);
            }
        }, MouseOutEvent.getType());

        tooltipHeader.addClassName(resources.subscriptionsCSS().bottomMenuTooltipHeader());
        tooltipHeader.appendChild(new SVGImage(resources.memory()).getElement());
        tooltipHeader.appendChild(tooltipHeaderMessageElement);

        tooltipBody.addClassName(resources.subscriptionsCSS().bottomMenuTooltipBody());
        tooltipBody.appendChild(tooltipBodyMessageElement);

        tooltipElement.addClassName(resources.subscriptionsCSS().bottomMenuTooltip());
        tooltipElement.appendChild(tooltipHeader);
        tooltipElement.appendChild(tooltipBody);
        tooltipElement.appendChild(tooltipArrow);

        panel.getElement().appendChild(memoryElement);

        return panel;
    }

    @Override
    public void update(ActionEvent e) {
        tooltipBodyMessageElement.setInnerHTML(locale.memoryIndicatorTooltipMessage());
        valueUsedMemoryElement.getStyle().setDisplay(Style.Display.INLINE);
        usedMemoryElement.getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        refreshMemory();
    }

    private void refreshMemory() {
        runnerServiceClient.getResources(
                new AsyncRequestCallback<ResourcesDescriptor>(dtoUnmarshallerFactory.newUnmarshaller(ResourcesDescriptor.class)) {
                    @Override
                    protected void onSuccess(ResourcesDescriptor result) {
                        updateMemoryIndication(result.getTotalMemory(), result.getUsedMemory());
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        Log.error(getClass(), exception.getMessage());
                    }
                });
    }

    private void updateMemoryIndication(@Nullable String totalMemory, @Nullable String usedMemory) {
        // setting new memory values
        valueTotalMemoryElement.setInnerHTML("");
        valueUsedMemoryElement.setInnerHTML("");

        if (totalMemory == null || usedMemory == null) {
            return;
        }

        int usedMemoryInteger = Integer.parseInt(usedMemory);
        int percentOfUsedMemory = (usedMemoryInteger * 100) / Integer.parseInt(totalMemory);

        // setting backgrounds color
        String colorUsed;
        String colorTotal;
        if (percentOfUsedMemory == 0) {
            colorUsed = "#818181";
            colorTotal = "#818181";
        } else if (percentOfUsedMemory < 30) {
            colorUsed = "#5c5c5c";
            colorTotal = "#818181";
        } else if (percentOfUsedMemory >= 30 && percentOfUsedMemory < 70) {
            colorUsed = "#2d638e";
            colorTotal = "#a0b9c8";
        } else if (percentOfUsedMemory >= 70 && percentOfUsedMemory <= 99) {
            colorUsed = "#be7e09";
            colorTotal = "#c8a35e";
        } else {
            colorUsed = "#ef2415";
            colorTotal = "#ef2415";
        }
        usedMemoryElement.getStyle().setProperty("backgroundColor", colorUsed);
        totalMemoryElement.getStyle().setProperty("backgroundColor", colorTotal);

        // setting visible for value of used memory
        if (percentOfUsedMemory > 99) {
            if (percentOfUsedMemory > 100) {
                percentOfUsedMemory = 100;
            }
            valueUsedMemoryElement.getStyle().setDisplay(Style.Display.NONE);
        } else {
            valueUsedMemoryElement.getStyle().setDisplay(Style.Display.INLINE);
        }

        // updating width of html elements
        usedMemoryElement.getStyle().setProperty("width", (percentOfUsedMemory / 100d) * FULL_WIDTH + "px");
        int remain = 100 - percentOfUsedMemory;
        totalMemoryElement.getStyle().setProperty("width", (remain / 100d) * FULL_WIDTH + "px");

        String usedMemoryDisplay = Bytes.toHumanSize(usedMemory + "MB");
        String totalMemoryDisplay = Bytes.toHumanSize(totalMemory + "MB");
        valueUsedMemoryElement.setInnerHTML(usedMemoryDisplay);
        valueTotalMemoryElement.setInnerHTML(totalMemoryDisplay);
    }
}
