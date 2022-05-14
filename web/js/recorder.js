URL = window.URL || window.webkitURL;

var gumStream; 						//stream from getUserMedia()
var recorder; 						//WebAudioRecorder object
var input; 							//MediaStreamAudioSourceNode  we'll be recording
const encodingType = "mp3"; 					//holds selected encoding for resulting audio (file)
var encodeAfterRecord = true;       // when to encode

var AudioContext = window.AudioContext || window.webkitAudioContext;
var audioContext; //new audio context to help us record


var recordButton = document.getElementById("btn-start");
var stopButton = document.getElementById("btn-stop");

//add events to those 2 buttons
recordButton.addEventListener("click", startRecording);
stopButton.addEventListener("click", stopRecording);

function startRecording() {
	console.log("startRecording() called");
    var constraints = { audio: true, video:false }
    navigator.mediaDevices.getUserMedia(constraints).then(function(stream) {
		__log("getUserMedia() success, stream created, initializing WebAudioRecorder...");

		audioContext = new AudioContext();
		console.log("Format: 2 channel @ "+audioContext.sampleRate/1000+"kHz")
		gumStream = stream;
		input = audioContext.createMediaStreamSource(stream);

		//stop the input from playing back through the speakers
		//input.connect(audioContext.destination)

		recorder = new WebAudioRecorder(input, {
		  workerDir: "../recorder-lib/web-audio-recorder-js/lib-minified/", // must end with slash
		  encoding: "mp3",
		  numChannels:2, //2 is the default, mp3 encoding supports only 2
		  onEncoderLoading: function(recorder, encoding) {
		    __log("Loading "+encoding+" encoder...");
		  },
		  onEncoderLoaded: function(recorder, encoding) {
		    // hide "loading encoder..." display
		    __log(encoding+" encoder loaded");
		  }
		});

		recorder.onComplete = function(recorder, blob) {
			__log("Encoding complete");
            sendRecording(blob);
			createDownloadLink(blob,recorder.encoding);
		}

		recorder.setOptions({
		  timeLimit:120,
		  encodeAfterRecord:encodeAfterRecord,
	      ogg: {quality: 0.5},
	      mp3: {bitRate: 160}
	    });

		recorder.startRecording();

		 __log("Recording started");

	}).catch(function(err) {
	console.log(err);
	  	//enable the record button if getUSerMedia() fails
    	recordButton.disabled = false;
    	stopButton.disabled = true;

	});

	//disable the record button
    recordButton.disabled = true;
    stopButton.disabled = false;
}

function stopRecording() {
	console.log("stopRecording() called");
	gumStream.getAudioTracks()[0].stop();
	stopButton.disabled = true;
	recordButton.disabled = false;
	recorder.finishRecording();

	__log('Recording stopped');
}

function createDownloadLink(blob,encoding) {
	var url = URL.createObjectURL(blob);
	console.log(url);
}

//helper function
function __log(e, data) {
	console.log(e, data)
}