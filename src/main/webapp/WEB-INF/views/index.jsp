<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>OX-POSE</title>
<!-- <link href="/css/index.css" rel="stylesheet"> -->

<style type="text/css">
html, body {
	height: 100%;
	background-color: #528aed;
}
.body_section {
	display: flex;
	width: 80%;
	min-width: 1000px;
	aspect-ratio: 15/9;
	margin: 100px auto auto;
	border: 8px solid #2b487b;
	border-radius: 40px;
	background-color: white;
	overflow: hidden;
}
.video_option{
	border-left: 8px solid #2b487b;
	width: 20%;
}

.video_action{
	display: block;
	padding: 5%;
	height: 50%;
}
.video_analyze_result{
	padding: 5%;
	border-top: 6px solid #2b487b;
}

.container {
	justify-content: start;

	width: 80%;
	height: 100%;
	overflow: scroll;
	overflow-x: hidden;
	scroll-padding: 10px;
}

.video_analyze_box {
	display: inline-flex;
	justify-content: start;
	width: 100%;
	height: 100%;
	border-bottom: 6px solid #2b487b;
	padding-bottom: 5%;
	overflow: hidden;
}


.video_analyze_canvas{
	width: 100%;
}

.canvas_box{
	display: inline-flex;
	width: 100%;


}
.canvas_box div{
	width: 100%;
}

.video_box {
	padding: 0 3%;
	width: 100%;
	position: relative;
}

.video {
	margin: auto;
	position: absolute;
	min-height: 100%;
	max-width: 100%;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	object-fit: contain;
}

.user_button_box, .compare_button_box {
	display: flex;
	justify-content: center;
	align-items: center;
	width: 100%;
	aspect-ratio: 9/16;
	/*height: 100%;*/
}

.user_video_box, .compare_video_box {
	display: none;
	position: relative;
	height: 100%;
	align-items: center;
	justify-content: center;
}

.compare_canvas ,.user_canvas{
	position: absolute;
	width: 100%;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	z-index: 1;
	pointer-events: none;
}

#user_input_video, #compare_input_video {
	display: none;
}

.compare_video_back, .user_video_back{
	position: absolute;
	top : 5px;
	left: 5px;
	z-index: 1;

}




/* 스크롤 */
::-webkit-scrollbar {
	width: 20px;
	background-color: #5f89e6;
}

::-webkit-scrollbar-track {
	background-color: #C2DEF5;

}

::-webkit-scrollbar-thumb {
	background-color: #5f89e6;
	background-clip: padding-box;
	border: 2px solid transparent;
	border-top: 4px solid transparent;
	border-bottom: 4px solid transparent;
	border-radius: 150px;
	/*border-radius: 30px;*/
	width: 10px;
}

::-webkit-scrollbar-thumb:hover {
	background-color: #999;
}




/* loading */

.compare_loading, .user_loading {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	display: none;
	background-color: rgba(0, 0, 0, 0.4);
}
.compare_loading_background, .user_loading_background{
	position: absolute;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	top: 0;
	left: 0;
	z-index: 1;
	display: none;
}

.compare_loading.show-modal , .user_loading.show-modal {
	display: block;
}

.compare_loading_background.show-modal, .user_loading_background.show-modal {
	display: block;
}
.loading_body {
	position: absolute;
	top: 50%;
	left: 50%;
	width: 100px;
	height: 40px;
	padding: 20px;
	z-index: 2;
	text-align: center;
	background-color: rgb(255, 255, 255);
	border-radius: 10px;
	box-shadow: 0 2px 3px 0 rgba(34, 36, 38, 0.15);
	transform: translateX(-50%) translateY(-50%);
	overflow: hidden;
}

.progress-bar {
	background-color: #5f89e6;
	border-radius: 4px;
	box-shadow: inset 0 0.5em 0.5em rgba(0,0,0,0.05);
	height: 5px;
	margin: 2rem 0 2rem 0;
	overflow: hidden;
	position: relative;
	transform: translateZ(0);
	width: 100%;
}

.progress-bar__bar {
	background-color: #ececec;
	box-shadow: inset 0 0.5em 0.5em rgba(94, 49, 49, 0.05);
	bottom: 0;
	left: 0;
	position: absolute;
	right: 0;
	top: 0;
}

.progress-bar__bar.active {
	transition: all 10000ms ease-out;
	transform: translateX(100%);
	-webkit-transform: translateX(100%)
}


/* modal */

.modal {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	display: none;
	background-color: rgba(0, 0, 0, 0.4);
}


.modal.show-modal {
	display: block;
}

.modal_background.show-modal {
	display: block;
}

.modal_body {
	position: absolute;
	top: 50%;
	left: 50%;
	width: 400px;
	height: 600px;
	padding: 40px;
	z-index: 2;
	text-align: center;
	background-color: rgb(255, 255, 255);
	border-radius: 10px;
	box-shadow: 0 2px 3px 0 rgba(34, 36, 38, 0.15);
	transform: translateX(-50%) translateY(-50%);
	overflow: scroll;
}

.modal_background {
	position: absolute;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	top: 0;
	left: 0;
	z-index: 1;
	display: none;
}

.modal_close {
	width: 26px;
	height: 26px;
	position: absolute;
	top: 10px;
	right: 10px;
}

.modal_video_box {
	height: 20%;
	background-color: aqua;
	display: flex;
	margin: 2%;
}

.modal_video_thum {
	background-color: black;
	width: 40%;
	margin: 2%;
}

.modal_video_info {
	margin: 2%;
}

/* 3D grid */

 .compare_landmark_grid_container , .user_landmark_grid_container {
	height: 100%;
	width: 100%;
	background-color: #e9ebec;
}
.square-box {
	width: 100%;
	aspect-ratio: 1/1;
	background-color: #e9ebec;
}
.compare_landmark_grid_container div{
	width: 100% ;
	aspect-ratio: 1/1;
	background-color: #e9ebec;
}
.viewer-widget-js canvas{
	width: 100% !important;
	aspect-ratio: 1/1;
}
.controls{
	background-color: #e9ebec;
}


/* check box*/

label {
	display: inline-flex;
	align-items: center;
	gap: 0.5rem;
	cursor: pointer;
}

[type="checkbox"] {
	appearance: none;
	position: relative;
	border: max(2px, 0.1em) solid gray;
	border-radius: 1.25em;
	width: 2.25em;
	height: 1.25em;
}

[type="checkbox"]::before {
	content: "";
	position: absolute;
	left: 0;
	width: 1em;
	height: 1em;
	border-radius: 50%;
	transform: scale(0.8);
	background-color: gray;
	transition: left 100ms linear;
}

[type="checkbox"]:checked {
	background-color: #5f89e6;
	border-color: #5f89e6;
}

[type="checkbox"]:checked::before {
	background-color: white;
	left: 1em;
}

[type="checkbox"]:disabled {
	border-color: lightgray;
	opacity: 0.7;
	cursor: not-allowed;
}

[type="checkbox"]:disabled:before {
	background-color: lightgray;
}

[type="checkbox"]:disabled + span {
	opacity: 0.7;
	cursor: not-allowed;
}

[type="checkbox"]:focus-visible {
	outline-offset: max(2px, 0.1em);
	outline: max(2px, 0.1em) solid tomato;
}

[type="checkbox"]:enabled:hover {
	box-shadow: 0 0 0 max(4px, 0.2em) lightgray;
}



</style>

<!-- media pipe -->
<script
	src="https://cdn.jsdelivr.net/npm/@mediapipe/camera_utils/camera_utils.js"
	crossorigin="anonymous"></script>
<%--    <script src="https://cdn.jsdelivr.net/npm/@mediapipe/control_utils/control_utils.js" crossorigin="anonymous"></script>--%>
    <script src="https://cdn.jsdelivr.net/npm/@mediapipe/control_utils_3d/control_utils_3d.js" crossorigin="anonymous"></script>
<script
	src="https://cdn.jsdelivr.net/npm/@mediapipe/drawing_utils/drawing_utils.js"
	crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/@mediapipe/pose/pose.js"
	crossorigin="anonymous"></script>

</head>
<body>

	<section class="body_section">
		<h1 style="color: white; position: absolute; top:-0px; font-size: 40px; margin-left: 30px;"> OX-POSE</h1>
		<div class="container">
			<div class="video_analyze_box">
				<div class="video_box compare_box">
					<div class="compare_button_box">
						<button id="compare_video_btn">비교 영상 직접 선택</button>
						<input id="compare_input_video" class="input_video" type="file"
							accept="video/mp4,video/mkv,video/x-m4,video/*">
						<button class="modal_btn">샘플 영상 선택</button>
					</div>
					<div class="compare_video_box">
						<!-- <video class="video compare_video" src="./video/test4.mp4" controls></video> -->
						<button class="compare_video_back">뒤로가기</button>
						<!-- 비교 영상 Video 태그 부분 -->
						<canvas class="compare_canvas"></canvas>

					</div>

					<%-- 로딩 중 --%>
					<div class="compare_loading_background"></div>
					<div class="compare_loading">
						<div class="loading_body">
							<div class="progress-bar">
								<div class="progress-bar__bar"></div>
							</div>
						</div>
					</div>


				</div>
				<div class="video_box user_box">
					<div class="user_button_box">
						<button id="user_live_button">실시간</button>
						<button id="user_video_btn">영상 선택</button>
						<input id="user_input_video" class="input_video" type="file"
							accept="video/mp4,video/mkv,video/x-m4,video/*">
					</div>
					<div class="user_video_box">
						<button class="user_video_back">뒤로가기</button>
						<!-- 사용자 영상 Video 태그 부분 -->

						<canvas class="user_canvas"></canvas>

					</div>
					<%-- 로딩 중 --%>
					<div class="user_loading_background"></div>
					<div class="user_loading">
						<div class="loading_body">
							<div class="progress-bar">
								<div class="progress-bar__bar"></div>
							</div>
						</div>
					</div>
				</div>
			</div>
		<%--3D grid 출력 부분--%>
			<div class="video_analyze_canvas">
					<div class="canvas_box">
						<div class='square-box'>
							<div class="compare_landmark_grid_container">
							</div>
						</div>
						<div class='square-box'>
							<div class="user_landmark_grid_container">
							</div>
						</div>
					</div>
			</div>
		</div>

<%-- 비디오 옵션	--%>
		<div class="video_option">
			<div class ="video_action">
				<div>
					<label for="video_ratio">비율</label>
					<input id="video_ratio" type="range" min="0.5" max="1.6" step="any">
				</div>
				<div><button id = "analyze_btn"> 현재 자세 측정</button></div>
				<div><button id = "analyzeAll_btn"> 전체 자세 측정</button></div>
				<div>
					<label>
						<input role="switch" type="checkbox" id="isCanvas" checked />
						<span>Canvas</span>
					</label>
				</div>
				<div>
					<label>
						<input role="switch" type="checkbox" id="is3DGrid" checked />
						<span>3D Grid</span>
					</label>
				</div>

				<div>
					<label>
						<input role="switch" type="checkbox" disabled />
						<span>알람 (비활성화)</span>
					</label>
				</div>

			</div>
			<div class="video_analyze_result">
				<h3> 결과 </h3>
			</div>
		</div>
	</section>






	

<%-- 영상선택 모달 --%>
	<div class="modal_background"></div>
	<div class="modal">
		<div class="modal_body">
			<div class="modal_close">X</div>
			<div>샘플 영상 선택</div>
			<hr>
			<div class="modal_video_box">
				<div class="modal_video_thum"></div>
				<div class="modal_video_info">
					<div class="modal_video_title">title</div>
					<div class="modal_video_contents">contents</div>
				</div>
			</div>

		</div>
	</div>


	<script src="https://code.jquery.com/jquery-1.12.4.min.js"></script>
	<script type="text/javascript" src="/js/index.js"></script>
	<script>
      const modal = document.querySelector('.modal');
      const modal_background = document.querySelector('.modal_background')
      const modal_body = document.querySelector(".modal_body");
      
      document.querySelector('.modal_btn').addEventListener('click', () => {
    	  open();
      });
    	//Hide modal
     document.querySelector('.modal_close').addEventListener('click', () => {
 		 close();
		})
  
      //Hide modal
      window.addEventListener('click', (e) => {
      	e.target === modal_background ?  close() : false;
      })
      
	  function close(){
   	  	modal.classList.remove('show-modal');
        modal_background.classList.remove('show-modal');
        document.body.style.overflow = 'auto';
       };
	  function open(){
  		modal.classList.add('show-modal');
        modal_background.classList.add('show-modal');
        document.body.style.overflow = 'hidden';
       };


	</script>
</body>
</html>