package de.gebit.rp.tool.workbench.viewer;


import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;

public interface LogItemDatabaseListener {

    void onItemChanged(ConsoleItem item);

    void onNewItem(ConsoleItem item);

}
