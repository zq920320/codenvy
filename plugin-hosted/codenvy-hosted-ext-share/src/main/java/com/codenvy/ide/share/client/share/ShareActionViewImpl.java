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
package com.codenvy.ide.share.client.share;

import com.codenvy.ide.share.client.ShareLocalizationConstant;
import com.codenvy.ide.share.client.ShareResources;
import com.codenvy.ide.share.client.share.social.Item;
import com.codenvy.ide.share.client.share.social.SharingChannel;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;
import javax.inject.Singleton;
import java.util.List;

import static com.google.gwt.dom.client.Style.Display.BLOCK;
import static com.google.gwt.dom.client.Style.Display.NONE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * UI for {@link ShareActionView}.
 *
 * @author Kevin Pollet
 */
@Singleton
public class ShareActionViewImpl implements ShareActionView {

    @UiField
    FlowPanel button;

    @UiField
    InlineHTML tooltip;

    PopupPanel popup;

    FlowPanel dropDown;

    FlowPanel dropDownHeader;

    SVGImage dropDownHeaderBack;

    InlineLabel dropDownHeaderTitle;

    FlowPanel dropDownContent;

    @UiField(provided = true)
    ShareResources resources;

    @UiField(provided = true)
    ShareLocalizationConstant locale;

    private ActionDelegate delegate;

    @Inject
    public ShareActionViewImpl(ShareActionViewImplUiBinder uiBinder, ShareResources resources, ShareLocalizationConstant locale) {
        this.resources = resources;
        this.locale = locale;

        uiBinder.createAndBindUi(this);

        this.initializePopup();
        this.tooltip.getElement().getStyle().setDisplay(NONE);
        this.button.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onClick();
            }
        }, ClickEvent.getType());
        this.button.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                delegate.onMouseOver();
            }
        }, MouseOverEvent.getType());
        this.button.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                delegate.onMouseOut();
            }
        }, MouseOutEvent.getType());
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return button.asWidget();
    }

    @Override
    public void showTooltip() {
        tooltip.getElement().getStyle().setDisplay(BLOCK);
        tooltip.getElement().getStyle().setTop(button.getOffsetHeight() + 10, PX);
        tooltip.getElement().getStyle().setRight((Document.get().getClientWidth() - button.getElement().getAbsoluteRight()) - 20, PX);
    }

    @Override
    public void hideTooltip() {
        tooltip.getElement().getStyle().setDisplay(NONE);
    }

    @Override
    public boolean isDropDownVisible() {
        return popup.isAttached();
    }

    @Override
    public void showItemsToShareDropDown(@NotNull List<Item> itemsToShare) {
        initializePopup();

        dropDown.removeStyleName(resources.shareCSS().shareDropDownWithHeader());
        dropDownHeader.getElement().getStyle().setDisplay(NONE);
        dropDownContent.clear();

        for (final Item item : itemsToShare) {
            final FlowPanel dropDownItem = new FlowPanel();
            dropDownItem.add(new SVGImage(item.getIcon()));
            dropDownItem.add(new InlineLabel(item.getLabel()));
            dropDownItem.addDomHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    delegate.onItemToShareClick(item);
                }
            }, ClickEvent.getType());

            dropDownContent.add(dropDownItem);
        }

        showDropDown();
    }

    @Override
    public void showSharingChannelsDropDown(@NotNull Item item, @NotNull String... params) {
        initializePopup();

        dropDown.addStyleName(resources.shareCSS().shareDropDownWithHeader());
        dropDownHeaderTitle.setText(locale.shareButtonDropDownHeaderTitle(item.getName()));
        dropDownHeader.getElement().getStyle().setDisplay(BLOCK);
        dropDownContent.clear();

        for (SharingChannel sharingChannel : item.getSharingChannels()) {
            final FlowPanel dropDownItem = new FlowPanel();
            dropDownItem.add(new SVGImage(sharingChannel.getIcon()));
            dropDownItem.add(new InlineLabel(sharingChannel.getLabel()));

            sharingChannel.decorate(dropDownItem, params);

            dropDownContent.add(dropDownItem);
        }

        showDropDown();
    }

    @Override
    public void hideDropDown() {
        button.removeStyleName(resources.shareCSS().mainMenuBarButtonClicked());
        popup.hide(false);
    }

    private void showDropDown() {
        button.addStyleName(resources.shareCSS().mainMenuBarButtonClicked());
        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                popup.setPopupPosition(button.getElement().getAbsoluteRight() - dropDownContent.getOffsetWidth() + 20,
                                       button.getOffsetHeight() + 10);
            }
        });
    }

    private void initializePopup() {
        if (popup != null) {
            popup.hide(false);
        }

        dropDownHeaderBack = new SVGImage(resources.backButton());
        dropDownHeaderBack.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onSharingChannelsBackClick();
            }
        });

        dropDownHeaderTitle = new InlineLabel();

        dropDownHeader = new FlowPanel();
        dropDownHeader.addStyleName(resources.shareCSS().shareDropDownHeader());
        dropDownHeader.add(dropDownHeaderBack);
        dropDownHeader.add(dropDownHeaderTitle);

        dropDownContent = new FlowPanel();
        dropDownContent.addStyleName(resources.shareCSS().shareDropDownContent());

        dropDown = new FlowPanel();
        dropDown.addStyleName(resources.shareCSS().shareDropDown());
        dropDown.add(new InlineLabel());
        dropDown.add(dropDownHeader);
        dropDown.add(dropDownContent);

        popup = new PopupPanel(true);
        popup.addAutoHidePartner(button.getElement());
        popup.getElement().getStyle().setPadding(0, PX);
        popup.getElement().getStyle().setBorderWidth(0, PX);
        popup.add(dropDown);
        popup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (event.isAutoClosed()) {
                    hideDropDown();
                }
            }
        });
    }

    interface ShareActionViewImplUiBinder extends UiBinder<Widget, ShareActionViewImpl> {
    }
}
