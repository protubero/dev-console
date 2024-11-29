package com.example.messagingstompwebsocket.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.messagingstompwebsocket.wsmodel.SocketHandler;
import com.example.messagingstompwebsocket.common.ItemProperty;
import com.example.messagingstompwebsocket.wsmodel.ClientConsoleItem;
import com.example.messagingstompwebsocket.wsmodel.ClientItemProperty;


@RestController
@RequestMapping("api")
public class ConsoleController {

	private static AtomicLong idGenerator = new AtomicLong();
    private final SocketHandler socketHandler;

    private ConsoleController(@Autowired SocketHandler aSocketHandler) {
		this.socketHandler = aSocketHandler;
    }


	@PostMapping("/describe")
	public void describe(@RequestBody SessionInfo sessionInfo)  {
		socketHandler.sessionInfo(sessionInfo);
	}

    @PostMapping("/append")
    public void append(@RequestBody ConsoleItem item) throws Exception {

		ClientConsoleItem clientItem = new ClientConsoleItem();
		clientItem.setId(idGenerator.incrementAndGet());
		clientItem.setName(item.getName());
		clientItem.setType(item.getType());
		clientItem.setRaw(item.getRaw());

		if (item.getItemProperties() != null && item.getItemProperties().length > 0) {
			ClientItemProperty[] clientItemProperties = new ClientItemProperty[item.getItemProperties().length];
			for (int i = 0; i < item.getItemProperties().length; i++) {
				ItemProperty sourceProp = item.getItemProperties()[i];
				clientItemProperties[i] = new ClientItemProperty();
				clientItemProperties[i].setLabel(sourceProp.getLabel());
				clientItemProperties[i].setValue(sourceProp.getValue());
			}
		}

        socketHandler.append(clientItem);
    }

}
