var VERSION = "1.0";
var DEBUG = false;




var lastData = [];
var numItems = 0;

/********************************** Helpers ***********************************/

function info(content) {
  console.log(content);
}

function debug(content) {
  if(DEBUG) info(content);
}


/******************************** PebbleKit JS ********************************/

Pebble.addEventListener('ready', function() {
  info('PebbleKit JS ready! Version ' + VERSION);

  Pebble.sendAppMessage({'AppKeyJSReady': 0});
});


  
});
