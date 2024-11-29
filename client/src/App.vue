<script setup>
import { reactive, onMounted } from 'vue'
import {testData} from './testdata.mjs'

const items = reactive([])

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
        items.push(JSON.parse(data.data))
        console.log(items)
    }
}


onMounted(() => {
    connect();
})

</script>

<template>
    <div class="columns-2 gap-3">

        <div class="w-full">
            <ul class="menu menu-xs bg-base-200 rounded-box w-56">
              <li><a>Script run 1 dddaf dadsad dsadsa dsdas</a></li>
              <li><a>Script run 2</a></li>
            </ul>
        </div>

        <div class="join join-vertical w-full">
          <div v-for="item in items" :key="item.id"  class="collapse collapse-arrow bg-base-200">
                <input type="radio" name="my-accordion-1"/>
            <div class="collapse-title text-xl font-medium"><h1 class="text-lg text-gray-700">ss {{ item.type }}</h1></div>
            <div class="collapse-content">Details </div>
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
