package de.gebit.rp.tool.workbench.viewerclient;

import java.util.Date;
import java.util.List;

import de.gebit.rp.tool.workbench.viewercommon.ConsoleItem;
import de.gebit.rp.tool.workbench.viewercommon.ItemBadge;
import de.gebit.rp.tool.workbench.viewercommon.ItemBadgeType;
import de.gebit.rp.tool.workbench.viewercommon.ItemType;
import de.gebit.rp.tool.workbench.viewercommon.RawContent;


public class Test {

    public static void main(String[] args) throws InterruptedException {
        var client = WorkbenchViewerClient.of("localhost", 8080);

        var logConsoleItem = ConsoleItem.builder()
                .name("Log")
                .type(ItemType.info)
                .sessionId("xyz")
                .build();
        client.send(logConsoleItem);

        logConsoleItem = ConsoleItem.builder()
                .name("Eventg")
                .type(ItemType.event)
                .sessionId("xyz")
                .build();
        client.send(logConsoleItem);

        System.out.println("start");
        for (int i = 0; i < 100; i++) {
            var item = ConsoleItem.builder()
                    .name("CreatePurchaseTransaction")
                    .type(ItemType.command)
                    .htmlText("a <b>simple</b> text")
                    .raw(List.of(RawContent.of("my key", "my value"),
                            RawContent.of("my key2", "my value2")))
                    .sessionId("xyz")
                    .build();
            client.send(item);
            client.appendRawContent(logConsoleItem, "text", String.valueOf(new Date()) + System.lineSeparator());
            if (i == 1) {
                client.appendBadge(logConsoleItem, ItemBadge.of(ItemBadgeType.error, "Exc", "hint"));
            }

            Thread.sleep(300l);

            item.setHtmlText(String.valueOf(new Date()));
            item.setName("CreatePurchaseTransaction 2");
            client.send(item);

            Thread.sleep(1000l);
        }
        System.out.println("stop");
        Thread.sleep(5000l);
    }
}
