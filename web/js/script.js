const API_URL_BASE = "http://localhost:8001"; // todo api??

const MODES = {LEARN: "Learn", RECOGNIZE: "Recognize"};

const STATE = {
    currMode: null,
    learn: {pieceName: null}
};

const sendStartLearning = () => {
    let formData = new FormData();
    const pieceName = "alala";
    formData.append("pieceName", pieceName);

    return fetch(`${API_URL_BASE}/learn/start`, {
        method: "POST",
        body: formData,
            })
//                      .then(res => res.json());

}

const sendLearnRecording = (blob) => {
console.log(blob)
    return fetch(`${API_URL_BASE}/recognize`, {
        method: "POST",
        body: blob,
    })
//                      .then(res => res.json());
}

const sendRecognizeRecording = (blob) => {
    return fetch(`${API_URL_BASE}/recognize`, {
        method: "POST",
        body: blob,
    })
//                      .then(res => res.json());
}

const learnPopup = document.getElementsByClassName("popup")[0];
const audioInput = document.getElementsByClassName("audio-input")[0];

const showLearnPopup = () => learnPopup.classList.remove("hidden");
const hideLearnPopup = () => learnPopup.classList.add("hidden");

const showAudioInput = () => audioInput.classList.remove("hidden");
const hideAudioInput = () => audioInput.classList.add("hidden");

const onLearnStart = () => {
let text = "Please enter piece name";
    if (STATE.learn.pieceName == null) {

    } else {

    }

    document.getElementById("learn-popup-text").innerText = text;
    showLearnPopup();
}

document.getElementById("btn-learn").addEventListener("click", () => {
onLearnStart();
});

const startLearn = () => {
    STATE.currMode = MODES.LEARN;
    showAudioInput();
}

const startRecognize = () => {
    STATE.currMode = MODES.RECOGNIZE;
    showAudioInput();
}

document.getElementById("learn-proceed").addEventListener("click", () => {
    hideLearnPopup();
    startLearn();
});

const sendRecording = (blob) => {
console.log("here111")
if (STATE.currMode === MODES.LEARN) {
        sendLearnRecording(blob);
    } else {
    console.log("here")
        sendRecognizeRecording(blob);
    }
}

document.getElementById("recording-send").addEventListener("click", () => {
    const blob = document.getElementById("image-file").files[ 0 ];
    sendRecording(blob);
})

document.getElementById("learn-cancel").addEventListener("click", hideLearnPopup);

document.getElementById("btn-recognize").addEventListener("click", startRecognize);