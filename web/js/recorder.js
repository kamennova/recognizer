URL = window.URL || window.webkitURL;

let gumStream;
let recorder;
let input;
const encodeAfterRecord = true;

const AudioContext = window.AudioContext || window.webkitAudioContext;
let audioContext;

const startRecording = (onComplete) => {
    console.log("startRecording() called");

    navigator.mediaDevices.getUserMedia({audio: true, video: false}).then((stream) => {
        __log("getUserMedia() success, stream created, initializing WebAudioRecorder...");

        audioContext = new AudioContext();
        gumStream = stream;
        input = audioContext.createMediaStreamSource(stream);

        //stop the input from playing back through the speakers
        //input.connect(audioContext.destination)

        recorder = new WebAudioRecorder(input, {
            workerDir: "../recorder-lib/web-audio-recorder-js/lib-minified/",
            encoding: "mp3",
            numChannels: 2,
            onEncoderLoading: function (recorder, encoding) {
                __log("Loading " + encoding + " encoder...");
            },
            onEncoderLoaded: function (recorder, encoding) {
                // hide "loading encoder..." display
                __log(encoding + " encoder loaded");
            }
        });

        recorder.onComplete = (recorder, blob) => onComplete(blob);

        recorder.setOptions({
            timeLimit: 120,
            encodeAfterRecord: encodeAfterRecord,
            ogg: {quality: 0.5},
            mp3: {bitRate: 160}
        });

        recorder.startRecording();

        __log("Recording started");

    }).catch(function (err) {
        console.log(err);
    });
};

const stopRecording = () => {
    console.log("stopRecording() called");
    gumStream.getAudioTracks()[ 0 ].stop();
    recorder.finishRecording();

    __log('Recording stopped');
};

function createDownloadLink(blob, encoding) {
    const url = URL.createObjectURL(blob);
    console.log(url);
}

function __log(e, data) {
    // console.log(e, data)
}

// createDownloadLink(blob, recorder.encoding);
