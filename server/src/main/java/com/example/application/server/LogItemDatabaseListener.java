package com.example.application.server;

import de.protubero.devconsole.common.ConsoleItem;

public interface LogItemDatabaseListener {

    void onItemChanged(ConsoleItem item);

    void onNewItem(ConsoleItem item);

}
