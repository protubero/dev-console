<script setup>
import {nextTick, useTemplateRef, onUpdated, watchEffect, reactive, onMounted } from 'vue'
import IconCommand from './components/icons/IconCommand.vue'


const items = reactive([])
const sessions = reactive([])
const selectedItem = reactive({ item: null })

const selectedRawItem = reactive({ rawItem: null, consoleItem: null })

let scroll = false;

onUpdated(() => {
    // Scroll to the bottom of the page
    if (scroll) {
        scroll = false;
        window.scrollTo({
          top: document.body.scrollHeight,
          behavior: 'smooth' // Optional: Add smooth scrolling effect
        });
    }
});

onMounted(() => {
    connect();
})

function clearArray(array) {
  while (array.length > 0) {
    array.pop();
  }
}

function connect() {
     let ws = new WebSocket('ws://' + window.location.host + '/clientCommands');

     ws.onopen = function(evt) {
        console.log(evt)
     };

     ws.onclose = function(evt) {
        console.log('Socket is closed. Reconnect will be attempted in 1 second.', evt.reason);
        setTimeout(function() {
              connect();
        }, 1000);
     };

     ws.onerror = function(err) {
        console.error('Socket encountered error: ', err.message, 'Closing socket');
        ws.close();
     };

    ws.onmessage = function(data){
        const msg = JSON.parse(data.data);

        switch (msg.command) {
          case 'reset':
            clearArray(items);
            break;
          case 'items':
            scroll = true;
            items.push(... msg.payload)
            break;
          case 'update':
            const foundItemIdx = items.findIndex(({ id }) => id === msg.payload.id);
            console.log(foundItemIdx)
            if (foundItemIdx > -1) {
                let foundItem = items[foundItemIdx]
                console.log(foundItem)
                foundItem.raw = msg.payload.raw
                foundItem.htmlText = msg.payload.htmlText
                foundItem.name = msg.payload.name
                foundItem.type = msg.payload.type
                foundItem.version = msg.payload.version
                foundItem.timestamp = msg.payload.timestamp

                // trigger reactivity
                items[foundItemIdx] = foundItem

                if (selectedRawItem.consoleItem.id === foundItem.id) {
                    if (foundItem.raw == null || foundItem.raw.length == 0) {
                        selectedRawItem.consoleItem = null;
                        selectedRawItem.rawItem = null;
                    } else {
                        const foundRawIdx = foundItem.raw.findIndex(({ label }) => label === selectedRawItem.rawItem.label);
                        if (foundRawIdx == -1) {
                            selectedRawItem.consoleItem = null;
                            selectedRawItem.rawItem = null;
                        } else {
                            console.log("update log")

                            selectedRawItem.rawItem = foundItem.raw[foundRawIdx];
                        }
                    }
                }
            } else {
                console.log("Console item not found: " + msg.payload.id)
            }
            break;
          default:
            console.log(`Unknown command ${msg.command}.`);
        }

    }
}

function selectItem(item) {
    selectedItem.item = item;

     if (selectedItem.item.raw && selectedItem.item.raw.length > 0) {
       selectedRawItem.rawItem = item.raw[0];
       selectedRawItem.consoleItem = item;
     } else {
       selectedRawItem.rawItem = null;
       selectedRawItem.consoleItem = null;
     }
}

function clearItems() {
  clearArray(items);
}

</script>

<template>
    <div class="flex flex-row bg-slate-200 sticky top-0">
        <div class="">SIZE: {{items.length}}</div>
        <div class="flex-grow"></div>
        <div class="">
            <a @click="clearItems()">Clear</a>
        </div>
    </div>
    <div ref="logPaneDiv" class="flex flex-row m-5 gap-x-4 min-w-120">
        <!-- Item list -->
        <div class="cursor-pointer flex-none w-80 h-full overflow-y-auto">
            <div v-for="item in items" :key="item.id" class="overflow-hidden text-nowrap hover:bg-indigo-500 text-ellipsis w-80"
                :class="{ 'font-bold': selectedItem.item == item }" @click="selectItem(item)">
                 {{ item.timestamp.split(' ')[1] }} <component :is="IconCommand" /> {{ item.name }}
            </div>
        </div>
        <!-- Details pane -->
        <div v-if="selectedItem.item" class="top-4 min-w-80 flex-grow ">
            <div class="sticky top-4">

                <!-- Head infos -->
                <div class="flex flex-row gap-x-1.5 mb-5">
                    <div class="text-lg">{{ selectedItem.item.timestamp.split(' ')[1] }}</div>
                    <div class="text-lg">{{ selectedItem.item.type }}</div>
                    <div class="text-lg">{{ selectedItem.item.name }}</div>
                </div>

                <!-- HTML Text -->
                <div v-if="selectedItem.item.htmlText" v-html="selectedItem.item.htmlText"></div>

                <!-- Raw data -->
                <div v-if="selectedItem.item.raw" role="tablist" class="tabs">
                  <a v-for="raw in selectedItem.item.raw"
                        role="tab"
                        @click="selectedRawItem.rawItem = raw"
                        class="tab { tab-active: selectedRawItem.rawItem == raw }" >{{raw.label}}</a>
                </div>

                <div v-if="selectedRawItem.rawItem">
                    <pre class="rounded-box bg-slate-500 pl-4">{{selectedRawItem.rawItem.value}}</pre>
                </div>
            </div>
        </div>
    </div>
    <div class="bg-slate-200 sticky bottom-0">SIZE: {{items.length}}</div>
</template>


