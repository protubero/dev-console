package de.gebit.rp.tool.workbench.viewer;


import java.util.function.Consumer;

import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ConsoleSession;

public interface LogItemDatabaseListener {

    void onItemChanged(ConsoleItem item);

    void onNewItem(ConsoleItem item);

    void onNewSessionAdded(ConsoleSession aConsoleSession);
}
