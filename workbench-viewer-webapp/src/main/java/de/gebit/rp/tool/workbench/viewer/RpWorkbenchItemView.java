package de.gebit.rp.tool.workbench.viewer;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleSession;
import jakarta.annotation.PostConstruct;

@Route("")
@Uses(Icon.class)
public class RpWorkbenchItemView extends VerticalLayout implements LogItemDatabaseListener {

    private ConsoleSession allSession = ConsoleSession.builder().id("_all").name("All").build();;

    private LogItemDatabase itemDb;
    private Registration dbRegistration;
    private ConfigurableFilterDataProvider<ConsoleItem, Void, SerializablePredicate<ConsoleItem>> dataProvider;

    private Select<ConsoleSession> sessionSelect;
    private ContentPaneComponent contentPane;
    private Span countSpan;
    private VirtualList<ConsoleItem> virtualItemList;

    /// //
    /// STATE
    /// //
    private FlagMenuItem idAndTimeVisibleFlag;
    private FlagMenuItem durationVisibleFlag;
    private FlagMenuItem contextVisibleFlag;
    private FlagMenuItem scrollLockFlag;

    private ConsoleSession selectedSession = allSession;
    private ConsoleItem selectedItem;

    public RpWorkbenchItemView(@Autowired LogItemDatabase itemDb) {
        this.itemDb = itemDb;
        ListDataProvider<ConsoleItem> listDataProvider = new ListDataProvider<>(itemDb.getConsoleItemList());
        dataProvider = listDataProvider.withConfigurableFilter();
        dataProvider.setFilter(ci -> {
            if (selectedSession == null) {
                return false;
            }
            return selectedSession == allSession || ci.getSessionId().equals(selectedSession.getId());
        });

    }

    @PostConstruct
    public void initUi() {

        /// //
        /// ITEM LIST
        /// //
        virtualItemList = new VirtualList<>();
        virtualItemList.setHeightFull();
        virtualItemList.setWidth("500px");
        virtualItemList.setDataProvider(dataProvider);
        virtualItemList.setRenderer(new ItemRenderer(this::onItemSelected));


        /// //
        /// Top Panel
        /// //
        HorizontalLayout topPanel = new HorizontalLayout();
        topPanel.setHeight("50px");
        topPanel.setWidthFull();
        topPanel.setClassName("toppanel");
        topPanel.setAlignItems(Alignment.CENTER);
        H4 appTitle = new H4("RP Workbench Viewer");
        appTitle.setClassName("apptitle");
        topPanel.add(appTitle);
        add(topPanel);

        // MENU BAR
        MenuBar menubar = new MenuBar();
        menubar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);

        scrollLockFlag = new FlagMenuItem(VaadinIcon.LOCK, false)
                .appendTo(menubar);

        idAndTimeVisibleFlag = new FlagMenuItem(VaadinIcon.CLOCK, false)
                .register(virtualItemList, "hideIdAndTime")
                .appendTo(menubar);
        durationVisibleFlag = new FlagMenuItem(VaadinIcon.STOPWATCH, true)
                .register(virtualItemList, "hideDuration")
                .appendTo(menubar);
        contextVisibleFlag = new FlagMenuItem(VaadinIcon.STAR, true)
                .register(virtualItemList, "hideContext")
                .appendTo(menubar);

        topPanel.add(menubar);


        // SESSION SWITCH
        sessionSelect = new Select<>();
        sessionSelect.setItems(new SessionListProxy(allSession, itemDb.getConsoleSessionList()));

        sessionSelect.setItemLabelGenerator(session -> {
            return session.getName();
        });
        sessionSelect.setValue(allSession);
        sessionSelect.setEmptySelectionAllowed(false);
        sessionSelect.addValueChangeListener(this::onSessionSelected);

        topPanel.add(sessionSelect);

        // COUNT DISPLAY
        countSpan = new Span("Count: " + String.valueOf(itemDb.getConsoleItemList().size()));
        countSpan.setClassName("headerInfoCount");
        Div countDiv = new Div(countSpan);
        countDiv.setClassName("headerInfo");
        topPanel.add(countDiv);



        /// //
        /// CONTENT PANE
        /// //
        contentPane = new ContentPaneComponent();


        SplitLayout splitLayout = new SplitLayout(virtualItemList, contentPane);
        splitLayout.setSizeFull();

        add(splitLayout);

        setSizeFull();

        refreshView();
    }



    private void onSessionSelected(AbstractField.ComponentValueChangeEvent<Select<ConsoleSession>, ConsoleSession> evt) {
        if (evt.getValue() != null) {
            selectedSession = evt.getValue();
            refreshView();
        }
    }

    private void refreshView() {
        contentPane.clear();
        virtualItemList.getDataProvider().refreshAll();
        updateCount();
    }

    private void onItemSelected(ConsoleItem consoleItem) {
        Objects.requireNonNull(consoleItem);

        if (selectedItem == null || selectedItem != consoleItem) {
            selectedItem = consoleItem;
            contentPane.displayItem(consoleItem);
        }
    }


    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dbRegistration != null) {
            dbRegistration.remove();
        }
        super.onDetach(detachEvent);
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        dbRegistration = itemDb.addListener(this);
    }

    @Override
    public void onItemChanged(ConsoleItem item) {
        Optional<UI> ui = virtualItemList.getUI();
        if (ui.isPresent()) {
            ui.get().access(() -> {
                dataProvider.refreshItem(item);

                // If currently selected item has been updated: update display
                if (selectedItem != null && selectedItem == item) {
                    contentPane.displayItem(item);
                }
            });
        }
    }

    @Override
    public void onNewItem(ConsoleItem item) {
        Optional<UI> ui = virtualItemList.getUI();
        if (ui.isPresent()) {
            ui.get().access(() -> {
                ConsoleSession session = itemDb.sessionById(item.getSessionId()).get();
                ConsoleSession tCurrentlySelectedSession = Objects.requireNonNull(selectedSession);

                if (scrollLockFlag.isEnabled()) {
                    sessionSelect.setValue(tCurrentlySelectedSession);
                } else {
                    if (item.getSessionId().equals(tCurrentlySelectedSession.getId())) {
                        sessionSelect.setValue(tCurrentlySelectedSession);
                    } else {
                        sessionSelect.setValue(session);
                    }
                }

                Objects.requireNonNull(selectedSession);

                if (selectedSession == allSession || selectedSession.getId().equals(item.getSessionId())) {
                    dataProvider.refreshAll();
                    updateCount();
                    if (scrollLockFlag.isEnabled()) {
                        virtualItemList.scrollToEnd();
                    }
                }
            });
        }
    }

    @Override
    public void onNewSessionAdded(ConsoleSession aConsoleSession) {
        Optional<UI> ui = sessionSelect.getUI();
        if (ui.isPresent()) {
            ui.get().access(() -> {
                sessionSelect.getDataProvider().refreshAll();
                sessionSelect.setValue(selectedSession);
            });
        }
    }

    private void updateCount() {
        countSpan.setText("Count: " + dataProvider.size(new Query<ConsoleItem, Void>()));
    }

}
