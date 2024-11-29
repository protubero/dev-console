<script setup>
import {nextTick, useTemplateRef, reactive, onMounted } from 'vue'
import {testData} from './testdata.mjs'

const myDiv = useTemplateRef("myDiv");
const items = reactive([])

onMounted(async () => {
  await nextTick();
  const div = myDiv.value;
  div.scrollTo({top: div.offsetHeight});
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
        console.log(data)

        const msg = JSON.parse(data.data);

        switch (msg.command) {
          case 'reset':
            break;
          case 'items':
            console.log("items")
            items.push(... msg.payload)
            // Scroll to the bottom of the page
            window.scrollTo({
              top: document.body.scrollHeight,
              behavior: 'smooth' // Optional: Add smooth scrolling effect
            });
            break;
          case 'sessions':
            break;
          default:
            console.log(`Unknown command ${msg.command}.`);
        }

    }
}

</script>

<template>
    <div ref="myDiv">
        <div v-for="item in items" :key="item.id">

             {{ item.timestamp }} {{ item.type }} {{ item.name }}
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
