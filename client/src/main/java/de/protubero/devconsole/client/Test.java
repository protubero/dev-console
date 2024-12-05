package de.protubero.devconsole.client;

import java.util.Date;
import java.util.List;

import de.protubero.devconsole.common.ConsoleItem;
import de.protubero.devconsole.common.RawContent;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        var client = DevConsoleClient.of("localhost", 8080);

        var logConsoleItem = ConsoleItem.builder()
                .name("Log")
                .type("Command")
                .sessionId("xyz")
                .build();
        client.send(logConsoleItem);



        System.out.println("start");
        for (int i = 0; i < 100; i++) {
            var item = ConsoleItem.builder()
                    .name("CreatePurchaseTransaction")
                    .type("Command")
                    .htmlText("a <b>simple</b> text")
                    .raw(List.of(RawContent.of("my key", "my value"),
                            RawContent.of("my key2", "my value2")))
                    .sessionId("xyz")
                    .build();
            client.send(item);
            client.append(logConsoleItem, "text", String.valueOf(new Date()) + System.lineSeparator());

            Thread.sleep(1000l);

            item.setHtmlText(String.valueOf(new Date()));
            item.setName("CPT");
            client.send(item);

            Thread.sleep(1000l);
        }
        System.out.println("stop");
        Thread.sleep(5000l);
    }
}
