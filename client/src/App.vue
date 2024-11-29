<script setup>
import {nextTick, useTemplateRef, onUpdated, reactive, onMounted } from 'vue'
import {testData} from './testdata.mjs'

const items = reactive([])
const selectedItem = reactive({ item: null })

let scroll = false;

onUpdated(() => {
console.log('updated')
// Scroll to the bottom of the page
if (scroll) {
    scroll = false;
    window.scrollTo({
      top: document.body.scrollHeight,
      behavior: 'smooth' // Optional: Add smooth scrolling effect
    });
}
/*
  const div = logPaneDiv.value;
  console.log(div.offsetHeight)
  div.scrollTo({top: div.offsetHeight});
  */
});

onMounted(() => {
    connect();
})

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
            break;
          case 'items':
            scroll = true;
            items.push(... msg.payload)
            break;
          case 'sessions':
            break;
          default:
            console.log(`Unknown command ${msg.command}.`);
        }

    }
}

function selectItem(item) {
    selectedItem.item = item;
}

</script>

<template>
    <div ref="logPaneDiv" class="flex flex-row m-5 gap-x-4 min-w-120">
        <!-- Item list -->
        <div class="cursor-pointer flex-none w-80">
            <div v-for="item in items" :key="item.id" class="overflow-hidden text-nowrap hover:bg-indigo-500 text-ellipsis w-80" @click="selectItem(item)">
                 {{ item.timestamp }} {{ item.type }} {{ item.name }}
            </div>
        </div>
        <!-- Details pane -->
        <div v-if="selectedItem.item" class="top-4 min-w-80 flex-grow ">
            <div class="sticky top-4">

                <!-- Head infos -->
                <div class="flex flex-row gap-x-1.5 mb-5">
                    <div class="text-lg">{{ selectedItem.item.timestamp }}</div>
                    <div class="text-lg">{{ selectedItem.item.type }}</div>
                    <div class="text-lg">{{ selectedItem.item.name }}</div>
                </div>

                <!-- Item Properties -->
                <div v-if="selectedItem.item.itemProperties">
                  <table class="table-auto border-spacing-4  mb-5">
                    <thead>
                      <tr>
                        <th>Key</th>
                        <th>Value</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="props in selectedItem.item.itemProperties">
                        <th>{{ props.label }}</th>
                        <td>{{ props.value }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                <!-- Raw data -->
                <div v-if="selectedItem.item.raw" class="rounded-box bg-slate-500 pl-4">
                    {{selectedItem.item.raw}}
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
