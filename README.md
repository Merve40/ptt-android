# Push-To-Talk (PTT) App for Android

This is a sample PTT app, following the browser based [ptt](github.com/merve40/ptt) application.
It connects to the same server within a [WebView](https://developer.android.com/reference/android/webkit/WebView).
Sound is recorded using [AudioRecord](https://developer.android.com/reference/android/media/AudioRecord.html?hl=en) and is streamed over a websocket (in WebView).      
Audio is played back from the WebView using [AudioContext](https://developer.mozilla.org/en-US/docs/Web/API/AudioContext).


## Support
All android devices starting from API Level 17 **and** WebView/Chrome version starting from 55 should be supported.


## Demo 
A demo can be performed between browser and android app at [https://ptt-demo.herokuapp.com](https://ptt-demo.herokuapp.com)
