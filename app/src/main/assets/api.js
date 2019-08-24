var Writable = require('web-audio-stream/writable');

const config = {
    url_login : 'https://ptt-demo.herokuapp.com/login',
    url_subscribe: 'https://ptt-demo.herokuapp.com/subscribe?channel=',
    url_socket: 'wss://ptt-demo.herokuapp.com/wss?id='
};

const ptt = (function() {

    var writable;
    var context;
    var ws;
    var button;
    var id;

    const recorder = {
        start: ()=>{
            ws.send('started');
            Android.start('ptt.onDataReceived');
        },
        stop: ()=>{
            Android.stop();
            setTimeout(()=>{
                ws.send('stopped');
            }, 500);
        }
    };

    function onDataReceived(stringArray){
        var byteArray = JSON.parse(stringArray);
        var arraybuffer = new Int8Array(byteArray).buffer;
        ws.send(arraybuffer);
    }

    return{

        onDataReceived,

        connect : function(){

            const subscribe = (channel)=>{
                return fetch(config.url_subscribe+channel+"&id="+id, {method: 'GET'});
            };

            const bind = (btn)=>{
                button = btn;

                button.onpointerdown = ()=>{
                    recorder.start();
                };

                button.onpointerup = ()=>{
                    recorder.stop();
                };
            };

            return new Promise((resolve, reject) =>{
                fetch(config.url_login, { method: 'GET' })
                .then(r=>r.json())
                .then(data =>{
                    id = data.id;

                    var reconnect = ()=>{
                        var socket = new WebSocket(config.url_socket+id);
                        socket.binaryType = ws.binaryType;
                        socket.onopen = ws.onopen;
                        socket.onerror = ws.onerror;
                        socket.onmessage = ws.onmessage;
                        socket.onclose = ws.onclose;
                        ws = socket;
                    }

                    ws = new WebSocket(config.url_socket+id);
                    ws.binaryType = 'arraybuffer';

                    ws.onopen = function(){
                        resolve({subscribe, bind});
                    }

                    ws.onerror = function(e) {
                        reject(e);
                    };

                    ws.onclose = function(e){
                        if(e.code == 1011 || e.code == 1006){
                            var msg = `Could not connect to websocket. reason=${e.reason}`;
                            console.log(msg);
                            reject({error: msg});
                        }else{
                            console.log(e);
                            reconnect();
                        }
                    }

                    ws.onmessage = (e)=>{
                        if(e.data == 'ping'){
                            ws.send('pong');
                        }else if(e.data == 'started'){

                            if(button){
                                button.disabled = true;
                            }

                            if (context){
                                if(context.state == 'running'){
                                    context.close();
                                }
                            }

                            context = new (window.AudioContext || window.webkitAudioContext)();

                            writable = Writable(context.destination, {
                                context: context,
                                //channels: 2,
                                //sampleRate: context.sampleRate,
                                autoend: true
                            });

                        }else if(e.data == 'stopped'){
                            context.close();

                            if(button){
                                button.disabled = false;
                            }

                        }else{
                            if (context.state == 'running'){
                                context.decodeAudioData(e.data, (buffer)=>{
                                    writable.write(buffer);
                                });
                            }
                        }
                    }

                    /*
                    setInterval(()=>{
                        if(ws.readyState == 3 || ws.readyState == 2){
                            reconnect();
                        }
                    }, 5000);
                    */
                })
                .catch(err => {
                    reject(err);
                });
            });
        }
    }

})();