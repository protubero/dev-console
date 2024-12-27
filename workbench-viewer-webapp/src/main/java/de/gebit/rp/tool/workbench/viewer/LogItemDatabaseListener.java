package de.gebit.rp.tool.workbench.viewer;

import de.protubero.devconsole.common.ConsoleItem;

public interface LogItemDatabaseListener {

    void onItemChanged(ConsoleItem item);

    void onNewItem(ConsoleItem item);

}
