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
	margin: 0 3%;
	width: 100%;
}

.video {
	margin: 0 auto;
	width: 100%;
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
}

#user_input_video, #compare_input_video {
	display: none;
}

.compare_video_back{
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
	background-color: tomato;
	border-color: tomato;
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
						<!-- 					<video class="video compare_video" src="./video/test4.mp4" controls></video> -->
						<button class="compare_video_back">뒤로가기</button>
						<!-- 비교 영상 Video 태그 부분 -->
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
					</div>

				</div>
			</div>
		<%--결과 출력 Canvas--%>
			<div class="video_analyze_canvas">
					<div class="canvas_box">
						<div>
							<canvas class="compare_canvas"></canvas>
						</div>
						<div>
							<canvas class="user_canvas"></canvas>
						</div>
					</div>


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
						<input role="switch" type="checkbox" />
						<span>Canvas</span>
					</label>
				</div>
				<div>
					<label>
						<input role="switch" type="checkbox" />
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