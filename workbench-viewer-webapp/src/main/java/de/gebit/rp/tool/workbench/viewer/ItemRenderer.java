package de.gebit.rp.tool.workbench.viewer;

import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ItemBadge;
import de.gebit.rp.tool.workbench.viewercommon.ItemType;

public class ItemRenderer extends ComponentRenderer<Component, ConsoleItem> {

    private static DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    public ItemRenderer(Consumer<ConsoleItem> aClickListener) {
        super(item -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.addClassName("itemlistentry");
            layout.addClickListener(evt -> {
                aClickListener.accept(item);
            });

            /*
            Button btn = new Button(item.getName(), new Icon(itemTypeIcon(item.getType())));
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_CONTRAST);
            btn.addClassName("itemlistentrybtn");
            btn.addClickListener(evt -> {
                aClickListener.accept(item);
            });
             */
            String idAndTime = String.format("%-4s %s ", item.getId(), TIME_FORMATTER.format(item.getTimestamp()));
            Icon icon = new Icon(itemTypeIcon(item.getType()));
            icon.setSize("16px");
            icon.addClassName("itemlistentryicon");
            layout.add(new Span(new Text(idAndTime)), icon, new Span(new Text(item.getName())));



            if (item.getBadges() != null) {
                for (var badge : item.getBadges()) {
                    layout.add(createBadgeOf(badge));
                }
            }


            return layout;
        });
    }

    public static Span createBadgeOf(ItemBadge badge) {
        Span badgeSpan = new Span(badge.getLabel());
        String theme = "badge small pill";
        switch (badge.getType()) {
            case error -> theme = theme + " error";
        }
        badgeSpan.getElement().getThemeList().add(theme);
        badgeSpan.addClassName("itembadge");
        if (badge.getText() != null) {
            badgeSpan.setTitle(badge.getText());
        }

        return badgeSpan;
    }

    public static VaadinIcon itemTypeIcon(ItemType itemType) {
        switch (itemType) {
            case command -> {
                return VaadinIcon.OPTION_A;
            }
            case info -> {
                return VaadinIcon.INFO_CIRCLE_O;
            }
            case event -> {
                return VaadinIcon.ENVELOPE_O;
            }
            case error -> {
                return VaadinIcon.BOMB;
            }
            default -> {
                throw new AssertionError();
            }
        }
    }
}
