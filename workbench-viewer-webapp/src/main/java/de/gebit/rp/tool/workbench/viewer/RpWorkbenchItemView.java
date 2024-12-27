package de.gebit.rp.tool.workbench.viewer;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;

@Route("")
@Uses(Icon.class)
public class RpWorkbenchItemView extends VerticalLayout {

    private LogItemDatabase itemDb;
    private Registration dbRegistration;
    private VirtualList<ConsoleItem> list;
    private boolean scrollToEnd = true;
    private Span countSpan;

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
        list.setSizeFull();
        ListDataProvider<ConsoleItem> dataProvider = new ListDataProvider<>(itemDb.getConsoleItemList());
        list.setDataProvider(dataProvider);
        list.setRenderer(new ItemRenderer());

        add(list);


        dbRegistration = itemDb.addListener(new LogItemDatabaseListener() {
            @Override
            public void onItemChanged(ConsoleItem item) {
                Optional<UI> ui = list.getUI();
                if (ui.isPresent()) {
                    ui.get().access(() -> {
                        dataProvider.refreshItem(item);
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
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (dbRegistration != null) {
            dbRegistration.remove();
        }
        super.onDetach(detachEvent);
    }

}
