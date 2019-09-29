# Push-To-Talk (PTT) App for Android

This is a sample PTT app, following the [**browser-based PTT**](https://github.com/merve40/ptt) application. The app is divided into 2 parts: native implementation and web-view based implementation.    
[WebView](https://developer.android.com/reference/android/webkit/WebView)-based implementation connects to the same web site as the browser version.    
The sound is recorded using [AudioRecord](https://developer.android.com/reference/android/media/AudioRecord.html?hl=en) for both native and webview implementation.    
WebView uses [AudioContext](https://developer.mozilla.org/en-US/docs/Web/API/AudioContext) to play back audio and native implementation uses [AudioTrack](https://developer.android.com/reference/android/media/AudioTrack).


## Support

### WebView based streaming (webstream) 
* cross-platform (desktop browsers & android devices)
* support starting from API level 17 **and** WebView/Chrome version starting from 55    


### Native streaming (nativestream)    
* better sound quality
* works only from android to android for now
* support starting from API level 23

## Demo 
A demo can be performed between browser and android app at [https://ptt-demo.herokuapp.com](https://ptt-demo.herokuapp.com)
