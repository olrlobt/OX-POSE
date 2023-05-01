package com.combo.oxpose.mediapose;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.combo.oxpose.ffmpeg.VideoFileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class PoseController {

	@Autowired
	private VideoFileUtils videoFileUtils;
	@Autowired
	private PoseService poseService;

	@GetMapping("/")
	public String Home() {
		return "index";
	}

	@GetMapping("/live")
	public String live() {
		return "mediapipe_live";
	}

	@GetMapping("/pose")
	public String pose() {
		return "mediapipe_video";
	}

	@GetMapping("/multi")
	public String multi() {
		return "mediapipe_multi";
	}

	@GetMapping("/multiVideo")
	public String multiVideo() {
		return "mediapipe_multiVideo";
	}

	@ResponseBody
	@PostMapping("/setAnalyzePose")
	public List<PoseVO> setAnalyzePose(@RequestBody Map<String,Object> data) {

		return poseService.setAnalyzePose(data);
	}
	
	@ResponseBody
	@PostMapping("/changePlaybackRate")
	public String changePlaybackRate(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
		
		return videoFileUtils.changePlaybackRate(file, 2);
	}

	@ResponseBody
	@PostMapping("/removeVideo")
	public void removeVideo(@RequestBody String src){
		log.info("anla src = {}" , src);
		poseService.removeVideo(src);
	}

	@ResponseBody
	@PostMapping("/matchCurrentPose")
	public void matchCurrentPose(@RequestBody List<PoseVO> poseVOs){

		poseService.matchCurrentPose(poseVOs);
	}

	@ResponseBody
	@PostMapping("/matchAllPose")
	public List<PoseVO> matchAllPose(@RequestBody List<List<PoseVO>> poseVOs){

		return poseService.matchAllPose(poseVOs);
	}

	@ResponseBody
	@PostMapping("/requestComparePose")
	public List<CommandVO> requestComparePose(@RequestBody List<List<PoseVO>> poseVOs){

		List<PoseVO> data = poseService.requestComparePose(poseVOs);


		return poseService.requestCommand(data);
	}



}
