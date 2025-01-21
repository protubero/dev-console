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
            layout.addClassName("list-item");
            layout.setPadding(false);
            layout.addClickListener(evt -> {
                aClickListener.accept(item);
            });

            // id
            Span idSpan = new Span(new Text(String.valueOf(item.getId())));
            idSpan.setClassName("list-id");

            // timestamp
            Span timestampSpan = new Span(new Text(TIME_FORMATTER.format(item.getTimestamp())));
            timestampSpan.setClassName("list-timestamp");

            // duration
            Span durationSpan = new Span(new Text(item.getDuration() == null ? "-" : item.getDuration().toString()));
            durationSpan.setClassName("list-duration");

            // context
            String context = String.format(item.getContextShort() == null ? "" : item.getContextShort());
            Span contextSpan = new Span(new Text(context));
            contextSpan.setClassName("list-context");

            // icon
            Icon icon = new Icon(itemTypeIcon(item.getType()));
            icon.setClassName("list-typeicon");
            Span iconSpan = new Span(icon);
            iconSpan.addClassName("list-typeicon-span");

            // name
            Span nameSpan = new Span(new Text(item.getName()));

            layout.add(idSpan, timestampSpan, durationSpan, iconSpan, contextSpan, nameSpan);

            // badges
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
        badgeSpan.addClassName("list-badge-span");
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
            case receipt -> {
                return VaadinIcon.NEWSPAPER;
            }
            case diff -> {
                return VaadinIcon.ARROWS_LONG_H;
            }
            default -> {
                throw new AssertionError();
            }
        }
    }
}
