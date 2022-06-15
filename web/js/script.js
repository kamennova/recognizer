const API_URL_BASE = "http://localhost:8001/api";

const MODES = {LEARN: "Learn", RECOGNIZE: "Recognize"};

const SESSION_STATE = {ON: "On", OVER: "Over", PAUSED: "Paused"};

const LEARN_LEVEL_MIN = 5;

const STATE = {
    currMode: MODES.LEARN,
    learn: {
        pieceName: null,
        lastResult: null
    },
    session: SESSION_STATE.OVER,
};

const getState = () => STATE;

// --- REQUESTS ---

const sendLearnRecording = (blob, pieceName) => {
    console.log("sending");
    console.log(new Date())
    return fetch(`${API_URL_BASE}/learn?pieceName=${pieceName}`, {
        method: "POST",
        body: blob,
    })
        .then(res => res.json())
        .then(res => {
            console.log(new Date());
            processLearnRes(res);
        });
};

const sendRecognizeRecording = (blob) => {
    console.log(new Date())
    return fetch(`${API_URL_BASE}/recognize`, {
        method: "POST",
        body: blob,
    }).then(res => res.json())
        .then(res => {
            console.log(new Date())
            processRecognizeRes(res);
        });
};

const sendCorrection = (pieceName) => {
    return fetch(`${API_URL_BASE}/recognize/correct?pieceName=${pieceName}`, {
        method: "POST",
    });
};

const getDbInfo = () => fetch(`${API_URL_BASE}/info`, {
    method: "GET"
}).then(res => res.json());

// --- UI ELEMENTS ---

const learnPopup = document.getElementsByClassName("popup")[ 0 ];
const startBtn = document.getElementById("btn-start");
const pauseBtn = document.getElementById("btn-pause");
const stopBtn = document.getElementById("btn-stop");
const learnResultPopup = document.getElementById("learn-result");
const modeSwitch = document.getElementById("mode-switch");
const resultList = document.getElementById("results-list");
const resultsContainer = document.getElementById("recognize-results");
const noResultsMsg = document.getElementById("no-results-msg");
const correctionBtn = document.getElementById("btn-correct");
const pieceNameInput = document.getElementById("piece-name-input");
const correctionPopup = document.getElementById("popup-correct");

const disable = (elem) => elem.classList.add("disabled");
const enable = (elem) => elem.classList.remove("disabled");

const hide = (elem) => elem.classList.add("hidden");
const show = (elem) => elem.classList.remove("hidden");

// --- PROCESS RESULTS ---

const clearResults = () => {
    hide(resultList);
    resultList.innerHtml = '';
};

const processRecognizeRes = (res) => {
    console.log(res);
    clearResults();
    if (res.isSuccess && res.results.length > 0) {
        const baseValue = res.results[ 0 ].precision;

        res.results.forEach(result => {
            const elem = document.createElement("li");
            const nameElem = document.createElement("span");
            nameElem.innerText = result.name;
            const valueElem = document.createElement("span");
            valueElem.innerText = Number(result.precision / baseValue).toFixed(1);
            elem.appendChild(nameElem);
            elem.appendChild(valueElem);
            resultList.appendChild(elem);
        });
        show(resultList);
        show(resultsContainer);
    } else {
        if (res.message !== undefined) {
            noResultsMsg.innerText = res.message;
        } else {
            noResultsMsg.innerText = "An unexpected error occured";
        }

        show(noResultsMsg);
    }
};

const processLearnRes = (res) => {
    console.log(res);
    if (STATE.learn.lastResult === null || STATE.learn.lastResult.level < LEARN_LEVEL_MIN) {
        show()
    }
};


// --- RECOGNIZE ---

const startRecognize = () => {
    updateSessionState(SESSION_STATE.ON);
    startIntervalRecording(15000, (blob) => sendRecognizeRecording(blob));
};

document.getElementById("correct-cancel").addEventListener("click", () => hide(correctionPopup));
document.getElementById("correct-proceed").addEventListener("click", () => {
    const pieceName = document.getElementById("correction-name").value;
    if (validatePieceName(pieceName)) {
        sendCorrection(pieceName);
        hide(correctionPopup);
    }
});

correctionBtn.addEventListener("click", () => {
    show(correctionPopup);
});

// --- LEARN ---


const startLearn = (pieceName) => {
    console.log("start learn");
    updateSessionState(SESSION_STATE.ON);
    updateLearnPieceName(pieceName);
    startIntervalRecording(15000, (blob) => sendLearnRecording(blob, pieceName));
};

const validatePieceName = (pieceName) => {
    return pieceName !== null && pieceName !== undefined && pieceName.length > 2;
};

document.getElementById("learn-proceed").addEventListener("click", () => {
    let pieceName = document.getElementById("piece-name").value;
    if (validatePieceName(pieceName)) {
        hide(learnPopup);
        startLearn(pieceName);
    } else {

    }
});

document.getElementById("learn-cancel").addEventListener("click", () => hide(learnPopup));

// ---

const startSession = () => {
    console.log("try start session");

    if (STATE.currMode === MODES.LEARN) {
        show(pieceNameInput)
    } else {
        startRecognize();
    }
};

const stopSession = () => {
    hide(learnResultPopup);
    updateSessionState(SESSION_STATE.OVER);
    stopIntervalRecording();
};


const pauseSession = () => {
    updateSessionState(SESSION_STATE.PAUSED);
    stopIntervalRecording();
};

// -----------

let INTERVAL_ID;

const startIntervalRecording = (interval, onComplete) => {
    setTimeout(() => repeatRecording(interval, onComplete), 1000);
};

const stopIntervalRecording = () => {
    if (INTERVAL_ID !== null) {
        clearInterval(INTERVAL_ID);
        INTERVAL_ID = null;
        stopRecording();
    }
};

const repeatRecording = (interval, onComplete) => {
    if (STATE.session === SESSION_STATE.ON) {
        startRecording(onComplete);// todo
        INTERVAL_ID = window.setTimeout(() => {
            stopRecording();
            repeatRecording(interval, onComplete);
        }, interval);
    } else {
        stopIntervalRecording();
    }
};

// ---

startBtn.addEventListener("click", () => {
    if (STATE.session !== SESSION_STATE.ON) { // can be started
        startSession();
    }
});

stopBtn.addEventListener("click", () => {
    if (STATE.session !== SESSION_STATE.OVER) {
        if (STATE.currMode === MODES.LEARN) {
            show(learnResultPopup);
        } else {
            stopSession();
        }
    }
});

document.getElementById("result-proceed").addEventListener("click", stopSession);

pauseBtn.addEventListener("click", () => {
    if (STATE.session === SESSION_STATE.ON) {
        pauseSession();
    }
});

const updateSessionState = (state) => {
    switch (state) {
        case SESSION_STATE.ON:
            disable(startBtn);
            enable(pauseBtn);
            enable(stopBtn);
            break;
        case SESSION_STATE.OVER:
            disable(pauseBtn);
            disable(stopBtn);
            enable(startBtn);
            break;
        case SESSION_STATE.PAUSED:
            enable(stopBtn);
            enable(startBtn);
            disable(pauseBtn);
            break;
    }

    STATE.session = state;
    console.log(state)
};

const updateLearnPieceName = (pieceName) => {
    STATE.learn.pieceName = pieceName;
    document.getElementById("learn-piece-name").innerText = pieceName;
};

const updateMode = (mode) => {
    if (mode === MODES.RECOGNIZE) {
        modeSwitch.classList.add("recognize");
    } else {
        modeSwitch.classList.remove("recognize");
    }
    STATE.currMode = mode;
};

modeSwitch.addEventListener("click", () => {
    if (STATE.session === SESSION_STATE.ON) {
        pauseSession();
    }
    updateMode(STATE.currMode === MODES.LEARN ? MODES.RECOGNIZE : MODES.LEARN);

    if (STATE.currMode === MODES.LEARN) {

    } else {

    }
});

getDbInfo().then(res => document.getElementById("learned-num").innerText = res.learnedNum);
