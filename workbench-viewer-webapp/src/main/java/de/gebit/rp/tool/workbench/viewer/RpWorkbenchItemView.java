package de.gebit.rp.tool.workbench.viewer;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ItemBadge;
import de.gebit.rp.tool.workbench.viewercommon.RawContent;

@Route("")
@Uses(Icon.class)
public class RpWorkbenchItemView extends VerticalLayout {

    private LogItemDatabase itemDb;
    private Registration dbRegistration;
    private VirtualList<ConsoleItem> list;
    private boolean scrollToEnd = true;
    private Span countSpan;
    private HorizontalLayout contentPaneHeader;
    private Div contentPaneHtmlWrapper;
    private TabSheet contentPaneRawTabs;
    private ConsoleItem selectedItem;

    public RpWorkbenchItemView(@Autowired LogItemDatabase itemDb) {
        this.itemDb = itemDb;

        HorizontalLayout topPanel = new HorizontalLayout();
        topPanel.add(new Span("RP Workbench Viewer"));

        Button scrollLockBtn = new Button("Auto Scroll ON");
        scrollLockBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        scrollLockBtn.addClickListener(evt -> {
           scrollToEnd = !scrollToEnd;
           if (scrollToEnd) {
               scrollLockBtn.setText("Auto Scroll ON");
           } else {
               scrollLockBtn.setText("Auto Scroll OFF");
           }
        });
        topPanel.add(scrollLockBtn);

        Button clearBtn = new Button("Clear");
        clearBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        clearBtn.addClickListener(evt -> {
            itemDb.clear();
            list.getDataProvider().refreshAll();
        });
        topPanel.add(clearBtn);

        Span countSpan = new Span(String.valueOf(itemDb.getConsoleItemList().size()));
        topPanel.add(countSpan);

        add(topPanel);


        setSizeFull();

        list = new VirtualList<>();

        list.setHeightFull();
        list.setWidth("500px");
        ListDataProvider<ConsoleItem> dataProvider = new ListDataProvider<>(itemDb.getConsoleItemList());
        list.setDataProvider(dataProvider);
        list.setRenderer(new ItemRenderer(this::onItemSelected));

        dbRegistration = itemDb.addListener(new LogItemDatabaseListener() {
            @Override
            public void onItemChanged(ConsoleItem item) {
                Optional<UI> ui = list.getUI();
                if (ui.isPresent()) {
                    ui.get().access(() -> {
                        dataProvider.refreshItem(item);

                        if (selectedItem != null && selectedItem == item) {
                            displayItemInContentPane(item);
                        }
                    });
                }
            }

            @Override
            public void onNewItem(ConsoleItem item) {
                Optional<UI> ui = list.getUI();
                if (ui.isPresent()) {
                    ui.get().access(() -> {
                        countSpan.setText(String.valueOf(itemDb.getConsoleItemList().size()));
                        dataProvider.refreshAll();
                        if (scrollToEnd) {
                            list.scrollToEnd();
                        }
                    });
                }
            }
        });


        VerticalLayout contentPane = new VerticalLayout();
        contentPane.setSizeFull();

        contentPaneHeader = new HorizontalLayout();
        contentPane.add(contentPaneHeader);

        contentPaneHtmlWrapper = new Div();
        contentPane.add(contentPaneHtmlWrapper);

        contentPaneRawTabs = new TabSheet();
        contentPane.add(contentPaneRawTabs);

        SplitLayout splitLayout = new SplitLayout(list, contentPane);
        splitLayout.setSizeFull();

        add(splitLayout);
    }


    private void onItemSelected(ConsoleItem consoleItem) {
        Objects.requireNonNull(consoleItem);

        if (selectedItem != null && consoleItem == selectedItem) {
            return;
        }

        this.selectedItem = consoleItem;

        displayItemInContentPane(consoleItem);
    }

    private void displayItemInContentPane(ConsoleItem consoleItem) {
        contentPaneHeader.removeAll();
        contentPaneHeader.add(new Icon(ItemRenderer.itemTypeIcon(consoleItem.getType())));
        contentPaneHeader.add(new Span(consoleItem.getName()));

        // badges
        if (consoleItem.getBadges() != null) {
            for (ItemBadge badge : consoleItem.getBadges()) {
                contentPaneHeader.add(ItemRenderer.createBadgeOf(badge));
            }
        }

        // HTML
        contentPaneHtmlWrapper.removeAll();
        if (consoleItem.getHtmlText() != null) {
            contentPaneHtmlWrapper.add(new Html("<div>" + consoleItem.getHtmlText() + "</div>"));
        }

        while (contentPaneRawTabs.getTabCount() > 0) {
            contentPaneRawTabs.remove(0);
        }
        if (consoleItem.getRaw() != null) {
            for (RawContent rawContent : consoleItem.getRaw()) {
                contentPaneRawTabs.add(rawContent.getLabel(),  new Div(new Text(rawContent.getValue())));
            }
        }
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dbRegistration != null) {
            dbRegistration.remove();
        }
        super.onDetach(detachEvent);
    }

}
