package de.gebit.rp.tool.workbench.viewer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;

public class ItemRenderer extends ComponentRenderer<Component, ConsoleItem> {

    public ItemRenderer() {
        super(item -> {
            HorizontalLayout layout = new HorizontalLayout();

            VaadinIcon typeIcon = null;
            switch (item.getType()) {
                case command -> {
                    typeIcon = VaadinIcon.OPTION_A;
                }
                case info -> {
                    typeIcon = VaadinIcon.INFO_CIRCLE_O;
                }
                case event -> {
                    typeIcon = VaadinIcon.ENVELOPE_O;
                }
            }

            Button btn = new Button(item.getName(), new Icon(typeIcon));
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_CONTRAST);
            layout.add(btn);


            if (item.getBadges() != null) {
                for (var badge : item.getBadges()) {
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
                    layout.add(badgeSpan);
                }
            }


            return layout;
        });
    }
}
