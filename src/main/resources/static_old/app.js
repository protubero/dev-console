
function connect() {
     let ws = new WebSocket('ws://' + window.location.host + '/name');

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
        $("#greetings").append("<tr><td>" + data.data + "</td></tr>");
    }
}

document.addEventListener("DOMContentLoaded", function(e) {
   console.log("Example page has loaded!");

   connect();
});

