const video_ratio = document.getElementById("video_ratio");
const user_input_video = document.getElementById("user_input_video");
const user_video_btn = document.getElementById("user_video_btn");
const user_live_button = document.getElementById("user_live_button");
const compare_video_btn = document.getElementById("compare_video_btn");
const compare_input_video = document.getElementById("compare_input_video");
const analyze_btn = document.getElementById("analyze_btn");
const analyzeAll_btn = document.getElementById("analyzeAll_btn");
const isCanvas = document.getElementById("isCanvas");
const is3DGrid = document.getElementById("is3DGrid");

// keyPoint 구분
const leftIndices = [1, 2, 3, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31];
const rightIndices = [4, 5, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32];
const leftConnections = [
    [11, 13], [13, 15], [15, 21], [15, 17], [15, 19], [17, 19],
    [11, 23], [23, 25], [25, 27], [27, 29], [27, 31], [29, 31]
];
const rightConnections = [
    [12, 14], [14, 16], [16, 22], [16, 18], [16, 20], [18, 20],
    [12, 24], [24, 26], [26, 28], [28, 30], [28, 32], [30, 32]
];
const centerConnections = [
    [11, 12], [23, 24]
];

let camera;

let compareResult = [];
let userResult = [];

is3DGrid.addEventListener("click",function (){
    if(this.checked){
        document.getElementsByClassName("video_analyze_canvas")[0].style.display = "block";
    }else {
        document.getElementsByClassName("video_analyze_canvas")[0].style.display = "none";
    }
});

isCanvas.addEventListener("click",function (){
    if(this.checked){
        document.getElementsByClassName("compare_canvas")[0].style.display = "block";
        document.getElementsByClassName("user_canvas")[0].style.display = "block";
    }else {
        document.getElementsByClassName("compare_canvas")[0].style.display = "none";
        document.getElementsByClassName("user_canvas")[0].style.display = "none";
    }
});


// 비디오 크기를 조절하는 함수
video_ratio.addEventListener("change", function () {
    const video = document.getElementsByClassName("video");

    for (let i = 0; i < video.length; i++) {
        video[i].style.height = video[0].videoWidth * video_ratio.value + "px";
    }
});

// 영상 선택 버튼 이벤트
compare_video_btn.addEventListener("click", () => {
    comparePose.send({images:null});
    compare_input_video.click();
});
user_video_btn.addEventListener("click", () => {
    userPose.send({images:null});
    user_input_video.click();
});

// 파일 입력 이벤트
user_input_video.addEventListener("change", () =>  Analyze("user",userPose));
compare_input_video.addEventListener("change", () => Analyze("compare",comparePose));

/**
 * 비디오 분석 함수
 * @param {string} part - user or compare
 */
async function Analyze(part, poseModel) {
    const input_video = document.getElementById(part + "_input_video");
    const video_box = document.getElementsByClassName(part + '_video_box')[0];
    const button_box = document.getElementsByClassName(part + '_button_box')[0];
    const canvasElement = document.getElementsByClassName(part + '_canvas')[0];
    const canvasCtx = canvasElement.getContext('2d');
    const show_video = createVideoElement(video_box, URL.createObjectURL(input_video.files[0]));
    const video_back = document.getElementsByClassName(part + '_video_back')[0];
    const landmarkContainer =
        document.getElementsByClassName(part + '_landmark_grid_container')[0];
    const grid = new LandmarkGrid(landmarkContainer, gridOption);
    const loading = document.getElementsByClassName(part +'_loading')[0];
    const loading_background = document.getElementsByClassName(part +'_loading_background')[0];
    const analyze_data = [];

    loading.classList.add('show-modal');
    loading_background.classList.add('show-modal');
    show_video.pause();
    const analyze_video = createVideoElement(video_box, await setPlaybackRate(input_video));
    analyze_video.style.display = "none";
    video_box.style.display = "block";
    button_box.style.display = "none";

    analyze_video.onloadeddata = () => {
        canvasCtx.canvas.style.aspectRatio = analyze_video.videoWidth / analyze_video.videoHeight +"";
        canvasCtx.canvas.width = analyze_video.videoWidth;
        canvasCtx.canvas.height = analyze_video.videoHeight;
        canvasCtx.canvas.style.width = "100%";
        poseModel.initialize().then(() => {
            analyze_video.pause();
            requestAnalyze(analyze_video, canvasCtx, poseModel, loading,loading_background);
        });
    };
    show_video.addEventListener('play', () =>{
            const intervalID = setInterval(() => {
                drawSkeleton(getTimeStampAnalyze(show_video.currentTime, part), canvasCtx, grid);
            }, 100);

            show_video.addEventListener('pause' ,function () {
                clearInterval(intervalID);
            },{once : true});
      }
    );

    show_video.addEventListener('seeking', () =>
         drawSkeleton(getTimeStampAnalyze(show_video.currentTime, part), canvasCtx, grid)
    );

    poseModel.onResults((results) => {

        const jsonData = {
            poseLandmarks : results.poseLandmarks,
            poseWorldLandmarks : results.poseWorldLandmarks,
            timestamp: analyze_video.currentTime - 0.08,
        };
        analyze_data.push(jsonData);
        document.querySelector("." + part + "_loading").querySelector(".progress-bar__bar").style.transform = `translateX(${analyze_video.currentTime/(analyze_video.duration) *100}%)`;
        document.querySelector("." + part + "_loading").querySelector(".progress-bar__bar").style.webkitTransform = `translateX(${analyze_video.currentTime/(analyze_video.duration) *100}%)`;
    });


    video_back.addEventListener("click", () => {
        deleteVideo(show_video, analyze_video)

        video_box.style.display = "none";
        button_box.style.display = "flex";
        document.getElementById(part + "_input_video").value = "";
        landmarkContainer.querySelector('div').remove();
    });


    /**
     * 분석을 반복적으로 요청하는 함수
     * @param videoElement - 분석 영상
     * @param canvasCtx - 결과가 그려질 canvas
     * @param poseModel - 포즈 객체
     * @param loading - loading Element
     * @param loading_background -  loading_background Element
     */
    function requestAnalyze(videoElement, canvasCtx, poseModel, loading, loading_background) {
        let videoPre = videoElement.currentTime;
        videoElement.currentTime += 50 / 1000;

        if (videoPre === videoElement.currentTime) {
            videoElement.removeEventListener('timeupdate', onTimeUpdate);
            videoElement.remove();
            loading.classList.remove('show-modal');
            loading_background.classList.remove('show-modal');

            saveAnalyzeData(part, analyze_video);

            fetch('removeVideo', {
                method: 'POST',
                body: videoElement.src,
            });
            return;
        }

        function onTimeUpdate() {
            poseModel.send({ image: videoElement });
            requestAnimationFrame(() =>
                requestAnalyze(videoElement, canvasCtx, poseModel, loading, loading_background)
            );
        }

        videoElement.addEventListener('timeupdate', onTimeUpdate, { once: true });
    }

    /**
     * 분석 결과를 저장하는 함수
     * @param analyze_data
     * @param part
     * @param video
     */
    function saveAnalyzeData(part, video) {

        const jsonData = JSON.stringify({
            data : analyze_data,
        });
        $.ajax({
            type: 'POST',
            url: 'setAnalyzePose',
            contentType: 'application/json',
            processData: false,
            dataType: 'json',
            data: jsonData,
            success: function (data) {
                if(part == "user"){
                    userResult = data;
                }else{
                    compareResult = data;
                }
                console.log(data);
            }
        })
    }
}

/**
 * 주어진 비디오의 2배속 비디오를 저장하고, 경로를 반환하는 함수
 * @param input_video - inputFile 태그
 * @returns {Promise<string>} - 2배속 비디오 경로
 */
async function setPlaybackRate(input_video) {
    const files = input_video.files;

    if (!files || files.length === 0) {
        console.error("No file selected");
        return null;
    }

    const formData = new FormData();
    formData.append('file', input_video.files[0]);

    const options = {
        method: "POST",
        body: formData
    };

    try {
        const response = await fetch("changePlaybackRate", options);
        const data = await response.text();
        return data;
    } catch (error) {
        console.error(error);
    }
}

/**
 * VideoElement 생성 함수
 * @param video_box - 생성될 비디오 부모 박스
 * @param srcURL - 비디오 경로
 * @returns {HTMLVideoElement}
 */
function createVideoElement(video_box, srcURL) {
    const video = document.createElement("video");
    video.className = "video";
    video.setAttribute("controls", "controls");
    video.setAttribute("src", srcURL);

    video_box.appendChild(video);
    return video;
}

/**
 * 스켈레톤을 그려주는 함수
 * @param results - 분석 결과
 * @param canvasCtx - 2D 캔버스
 * @param grid - 3D 캔버스
 */
function drawSkeleton(results, canvasCtx, grid) {
    // console.log(results);
    if (results.poseLandmarks && isCanvas.checked) {
        let leftKeyPoint = [];
        let rightKeyPoint = [];

        for (let i = 0; i < results.poseLandmarks.length; i++) {
            if (leftIndices.includes(i)) {
                leftKeyPoint.push(results.poseLandmarks[i]);
            } else {
                rightKeyPoint.push(results.poseLandmarks[i]);
            }
        }
        canvasCtx.save();
        canvasCtx.clearRect(0, 0, canvasCtx.canvas.width, canvasCtx.canvas.height);
        // canvasCtx.drawImage(results.image, 0, 0, canvasCtx.canvas.width, canvasCtx.canvas.height);

        drawLandmarks(canvasCtx, leftKeyPoint, {
            color: '#FF0000', lineWidth: 2
        });
        drawLandmarks(canvasCtx, rightKeyPoint, {
            color: '#0000FF', lineWidth: 2
        });
        drawConnectors(canvasCtx, results.poseLandmarks, leftConnections, {
            color: '#00FFFF', lineWidth: 3
        });
        drawConnectors(canvasCtx, results.poseLandmarks, rightConnections, {
            color: '#00FF00', lineWidth: 3
        });
        drawConnectors(canvasCtx, results.poseLandmarks, centerConnections, {
            color: '#EEEEEE', lineWidth: 3
        });
        canvasCtx.restore();

    }
    if (results.poseWorldLandmarks && is3DGrid.checked) {
        grid.updateLandmarks(results.poseWorldLandmarks, [
                {list: leftConnections, color: 'LEFTCONNECTIONS'},
                {list: rightConnections, color: 'RIGHTCONNECTIONS'},
                {list: centerConnections, color: '0xEEEEEE'}]
            , [
                {list: leftIndices, color: 'LEFT'},
                {list: rightIndices, color: 'RIGHT'}
            ]);

    } else {
        grid.updateLandmarks([]);
    }
}


/**
 * 뒤로가기를 눌렀을때 비디오를 삭제하는 함수
 * @param show_video
 * @param analyze_video
 */
function deleteVideo(show_video, analyze_video) {
    show_video.remove();
    analyze_video.remove();

    if (camera != null) {
        camera.stop();
    }
}

analyze_btn.addEventListener("click", function (){
    console.log("분석 버튼 클릭");
    const compareVideo = document.getElementsByClassName('compare_video_box')[0].querySelector('video');
    const userVideo = document.getElementsByClassName('user_video_box')[0].querySelector('video');

    const poseVOs = [];
    poseVOs.push( getTimeStampAnalyze(compareVideo.currentTime , "compare"));
    poseVOs.push( getTimeStampAnalyze(userVideo.currentTime, "user"));

    console.log(poseVOs);

    fetch("matchCurrentPose", {
        method : "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body : JSON.stringify(poseVOs)
    })
})


analyzeAll_btn.addEventListener("click", function (){
    console.log("전체 분석 버튼 클릭");
    const poseVOs = [];
    poseVOs.push(compareResult);
    poseVOs.push(userResult);

    fetch("matchAllPose", {
        method : "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body : JSON.stringify(poseVOs)
    }).then(response => response.json())
        .then(data => {
            console.log(data);
            console.log(data[0].timeStamp);
            console.log(data[1].timeStamp);
            if (!isNaN(data[0].timeStamp) && isFinite(data[0].timeStamp) && !isNaN(data[1].timeStamp) && isFinite(data[1].timeStamp)) {
                document.querySelector(".user_video_box video").currentTime = data[0].timeStamp;
                document.querySelector(".compare_video_box video").currentTime = data[1].timeStamp;
            }


        }).catch(error => {

            console.error(error);
        });


})


/**
 * 주어진 timeStamp와 가장 가까운 Pose 결과를 return 하는 함수
 * @param timeStamp
 * @param part
 */
function getTimeStampAnalyze(timeStamp, part){
    let result = [];

    if(part === "user"){
        result = userResult;
    }else{
        result = compareResult;
    }

    let low = 0;
    let high = result.length-1;

    let closest = result[0].timeStamp;

    while (low < high) {
        let mid = Math.floor((low + high) / 2);

        if (timeStamp == result[mid].timeStamp) {
            closest = result[mid].timeStamp;
            break;
        }

        if (timeStamp < result[mid].timeStamp) {
            high = mid - 1;
        } else {
            low = mid + 1;
        }

        if (Math.abs(result[mid].timeStamp - timeStamp) < Math.abs(closest - timeStamp)) {
            closest = result[mid].timeStamp;
        }
    }

    return result[low];
}








// User live 버튼 클릭 이벤트
// user_live_button.addEventListener("click", function() {
// 	pose.reset();
// 	user_video = createVideoElement(user_video_box);
//
// 	camera = new Camera(user_video, {
// 		onFrame: async () => {
// 			await pose.send({ image: user_video });
// 		},
// 		width: 1280,
// 		height: 720
// 	});
//
// 	canvasElement.width = 1280 / 2;
// 	canvasElement.height = 720 / 2;
// 	canvasCtx = canvasElement.getContext('2d');
// 	pose.onResults((results) => responseAnalyze(results, canvasCtx));
//
// 	camera.start();
// 	user_video_box.style.display = "block";
// 	user_button_box.style.display = "none";
// });


// 포즈 세팅

const userPose = new Pose({
    locateFile: (file) => {
        return `https://cdn.jsdelivr.net/npm/@mediapipe/pose/${file}`;
    }
});

const comparePose = new Pose({
    locateFile: (file) => {
        return `https://cdn.jsdelivr.net/npm/@mediapipe/pose/${file}`;
    }
});

const poseOptions = {
    upperBodyOnly: true,
    modelComplexity: 1,
    smoothLandmarks: true,
    enableSegmentation: false,
    minDetectionConfidence: 0.5,
    minTrackingConfidence: 0.5,
    maxNumDetection: 3
};

userPose.setOptions(poseOptions);
comparePose.setOptions(poseOptions);

const gridOption = {
    connectionColor: 0xCCCCCC,
    definedColors: [
        {name: 'LEFT', value: 0xFF0000},
        {name: 'RIGHT', value: 0x0000FF},
        {name: 'LEFTCONNECTIONS', value: 0x75fbfd},
        {name: 'RIGHTCONNECTIONS', value: 0x00FFAA}],

    range: 1,
    fitToGrid: true,
    labelSuffix: 'm',
    landmarkSize: 2,
    numCellsPerAxis: 2,
    showHidden: false,
    centered: true,
    isRotating : false,
    rotationSpeed : 0.5,
}

