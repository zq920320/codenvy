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
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.CustomComponentAction;
import org.eclipse.che.ide.api.action.Presentation;
import org.vectomatic.dom.svg.ui.SVGImage;

import static com.codenvy.ide.onpremises.QueueType.SHARED;
import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Show the queue type indicator tooltip.
 *
 * @author Oleksii Orel
 */
public class QueueTypeIndicatorAction extends Action implements CustomComponentAction {
    private final PanelResources                 resources;
    private final IndicatorsLocalizationConstant locale;
    private final AnalyticsEventLogger           eventLogger;
    private final Element                        tooltipHeader;
    private final Element                        tooltipBodyMessageElement;
    private final Element                        iconBlockElement;

    private QueueType queueType;

    @Inject
    public QueueTypeIndicatorAction(PanelResources resources, IndicatorsLocalizationConstant locale,
                                    AnalyticsEventLogger eventLogger) {
        this.resources = resources;
        this.locale = locale;

        tooltipHeader = DOM.createDiv();
        tooltipHeader.addClassName(resources.subscriptionsCSS().bottomMenuTooltipHeader());

        tooltipBodyMessageElement = DOM.createSpan();
        iconBlockElement = DOM.createDiv();

        this.eventLogger = eventLogger;
    }

    public void setQueueType(QueueType queueType) {
        this.queueType = queueType;
        updateCustomComponent();
    }

    private void updateCustomComponent() {
        if (queueType == null) {
            return;
        }
        tooltipHeader.removeAllChildren();
        iconBlockElement.removeAllChildren();
        final Element tooltipHeaderMessageElement = DOM.createSpan();

        if (SHARED.equals(queueType)) {
            tooltipHeaderMessageElement.setInnerHTML(locale.queueTypeTooltipSharedTitle());
            tooltipBodyMessageElement.setInnerHTML(locale.queueTypeTooltipSharedMessage());
            tooltipHeader.appendChild(new SVGImage(resources.sharedQueue()).getElement());
            iconBlockElement.appendChild(new SVGImage(resources.sharedQueue()).getElement());
        } else {
            tooltipHeaderMessageElement.setInnerHTML(locale.queueTypeTooltipDedicatedTitle());
            tooltipBodyMessageElement.setInnerHTML(locale.queueTypeTooltipDedicatedMessage(1));
            tooltipHeader.appendChild(new SVGImage(resources.dedicatedQueue()).getElement());
            iconBlockElement.appendChild(new SVGImage(resources.dedicatedQueue()).getElement());
        }
        tooltipHeader.appendChild(tooltipHeaderMessageElement);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);
    }

    @Override
    public Widget createCustomComponent(Presentation presentation) {
        final FlowPanel wrapper = new FlowPanel();
        final Element tooltipElement = DOM.createDiv();
        final Element tooltipBody = DOM.createDiv();
        final Element tooltipArrow = DOM.createDiv();

        wrapper.addStyleName(resources.subscriptionsCSS().panel());
        wrapper.addStyleName(resources.subscriptionsCSS().queueTypeIndicator());

        // add handlers
        wrapper.addDomHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {

                tooltipElement.getStyle().setRight(0, PX);
                tooltipElement.getStyle().setBottom(0, PX);
                tooltipElement.getStyle().setDisplay(BLOCK);

                final Element parent = wrapper.getElement();
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

        wrapper.addDomHandler(new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                tooltipElement.getStyle().setDisplay(NONE);
            }
        }, MouseOutEvent.getType());

        tooltipBody.addClassName(resources.subscriptionsCSS().bottomMenuTooltipBody());
        tooltipBody.appendChild(tooltipBodyMessageElement);

        tooltipElement.addClassName(resources.subscriptionsCSS().bottomMenuTooltip());
        tooltipElement.appendChild(tooltipHeader);
        tooltipElement.appendChild(tooltipBody);
        tooltipElement.appendChild(tooltipArrow);

        wrapper.getElement().appendChild(tooltipElement);
        wrapper.getElement().appendChild(iconBlockElement);

        return wrapper;
    }

    @Override
    public void update(ActionEvent e) {
        e.getPresentation().setVisible(queueType != null);
    }

}
