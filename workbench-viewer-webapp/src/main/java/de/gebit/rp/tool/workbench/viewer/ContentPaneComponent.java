package de.gebit.rp.tool.workbench.viewer;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ItemBadge;
import de.gebit.rp.tool.workbench.viewercommon.RawContent;

public class ContentPaneComponent extends VerticalLayout  {

    private final HorizontalLayout contentPaneHeader;
    private final Div contentPaneHtmlWrapper;
    private final TabSheet contentPaneRawTabs;

    public ContentPaneComponent() {
        setSizeFull();

        contentPaneHeader = new HorizontalLayout();
        add(contentPaneHeader);

        contentPaneHtmlWrapper = new Div();
        add(contentPaneHtmlWrapper);

        contentPaneRawTabs = new TabSheet();
        contentPaneRawTabs.setVisible(false);
        add(contentPaneRawTabs);
    }

    void displayItem(ConsoleItem consoleItem) {
        clear();

        contentPaneHeader.add(new Icon(ItemRenderer.itemTypeIcon(consoleItem.getType())));
        contentPaneHeader.add(new Span(consoleItem.getName()));

        // badges
        if (consoleItem.getBadges() != null) {
            for (ItemBadge badge : consoleItem.getBadges()) {
                contentPaneHeader.add(ItemRenderer.createBadgeOf(badge));
            }
        }

        // HTML
        if (consoleItem.getHtmlText() != null) {
            contentPaneHtmlWrapper.add(new Html("<div>" + consoleItem.getHtmlText() + "</div>"));
        }

        if (consoleItem.getRaw() != null) {
            contentPaneRawTabs.setVisible(true);
            for (RawContent rawContent : consoleItem.getRaw()) {
                Pre raw = new Pre(rawContent.getValue());
                raw.setClassName("rawContent");
                contentPaneRawTabs.add(rawContent.getLabel(), raw);
            }
        }
    }

    void clear() {
        contentPaneHeader.removeAll();
        contentPaneHtmlWrapper.removeAll();
        while (contentPaneRawTabs.getTabCount() > 0) {
            contentPaneRawTabs.remove(0);
        }
        contentPaneRawTabs.setVisible(false);
    }
}
