package com.example.application;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.application.server.LogItemDatabase;
import com.example.application.server.LogItemDatabaseListener;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import de.protubero.devconsole.common.ConsoleItem;

@Route("")
@Uses(Icon.class)
public class RpWorkbenchItemView extends VerticalLayout {

    private LogItemDatabase itemDb;
    private Registration dbRegistration;

    public RpWorkbenchItemView(@Autowired LogItemDatabase itemDb) {
        this.itemDb = itemDb;

        setSizeFull();

        VirtualList<ConsoleItem> list = new VirtualList<>();
        list.setSizeFull();

        ListDataProvider<ConsoleItem> dataProvider = new ListDataProvider<>(itemDb.getConsoleItemList());
        list.setDataProvider(dataProvider);
        list.setRenderer(personCardRenderer);

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
                        dataProvider.refreshAll();
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



    private ComponentRenderer<Component, ConsoleItem> personCardRenderer = new ComponentRenderer<>(
            item -> {
                Button btn = new Button(item.getName() + item.getRaw().get(0).getValue(), new Icon(VaadinIcon.ARROW_LEFT));
                btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_CONTRAST);

                Span confirmed = new Span("Confirmed");
                confirmed.getElement().getThemeList().add("badge success");

                HorizontalLayout layout = new HorizontalLayout();
                layout.add(btn, confirmed);

                return layout;
            });

}
