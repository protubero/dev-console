package de.gebit.rp.tool.workbench.viewer;

import java.util.Objects;

import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;

public class FlagMenuItem {

    private VaadinIcon icon;
    private boolean enabled;

    private String hidingCssClass;
    private HasStyle container;

    private Runnable listener;

    public FlagMenuItem(VaadinIcon icon, boolean enabled) {
        this.icon = Objects.requireNonNull(icon);
        this.enabled = Objects.requireNonNull(enabled);
    }

    public FlagMenuItem register(HasStyle container, String hidingCssClass) {
        this.container = Objects.requireNonNull(container);
        this.hidingCssClass = Objects.requireNonNull(hidingCssClass);

        if (!enabled) {
            container.addClassName(hidingCssClass);
        }

        return this;
    }

    public FlagMenuItem register(Runnable listener) {
        this.listener = Objects.requireNonNull(listener);
        return this;
    }

    public FlagMenuItem appendTo(MenuBar menuBar) {
        MenuItem menuItem = menuBar.addItem(new Icon(icon));
        menuItem.addClassNames("toggle-icon", enabled ? "selected-icon" : "deselected-icon");
        menuItem.addClickListener(evt -> {
            if (enabled) {
                menuItem.removeClassName("selected-icon");
                menuItem.addClassName("deselected-icon");
                if (hidingCssClass != null) {
                    container.addClassName(hidingCssClass);
                }
            } else {
                menuItem.removeClassName("deselected-icon");
                menuItem.addClassName("selected-icon");
                if (hidingCssClass != null) {
                    container.removeClassName(hidingCssClass);
                }
            }
            enabled = !enabled;

            if (listener != null) {
                listener.run();
            }
        });


        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
