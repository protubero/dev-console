package de.protubero.devconsole.client;

import java.util.Collections;
import java.util.List;

import de.protubero.devconsole.common.ConsoleItem;
import de.protubero.devconsole.common.ItemProperty;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        var client = DevConsoleClient.of("localhost", 8080);

        var item = ConsoleItem.builder()
                .raw("raw")
                .name("CreatePurchaseTransaction")
                .type("Command")
                .itemProperties(List.of(ItemProperty.of("my key", "my value")))
                .sessionId("xyz")
                .build();

        System.out.println("start");
        for (int i = 0; i < 100; i++) {
            client.send(item);
        }
        System.out.println("stop");
        Thread.sleep(5000l);
    }
}
