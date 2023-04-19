package com.combo.oxpose.mediapose;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import com.combo.oxpose.mediapose.PoseVO.PoseKeyPoint;
import com.combo.oxpose.mediapose.PoseVO.PoseTheta;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PoseService {

    private List<PoseVO> userPoseData = new ArrayList<>();
    private List<PoseVO> comparePoseData = new ArrayList<>();
    private PoseVO poseVO;

    private final int[][] joints = {{11, 12, 13}, {12, 11, 14}, {13, 11, 15}, {14, 12, 16}, {23, 24, 25},
            {24, 23, 26}, {25, 23, 27}, {26, 24, 28}};

    /**
     * 포즈의 키 포인트 데이터를 정규화, 각 관절의 각도를 구하는 함수
     *
     * @param data : 분석 결과
     * @return (임시)
     */
    public List<PoseVO> setAnalyzePose(Map<String, Object> data) {
        int frame = 0;
        String part = (String) data.get("part");
        List<Map<String, Object>> poseList = (List<Map<String, Object>>) data.get("data");
        log.info("size = {}", poseList.size());

        List<PoseVO> poseData = new ArrayList<>();
        for (Map<String, Object> pose : poseList) {
            List<Map<String, Double>> poseWorldLandmarksData = (List<Map<String, Double>>) pose.get(
                    "poseWorldLandmarks");
            List<Map<String, Double>> poseLandmarksData = (List<Map<String, Double>>) pose.get("poseLandmarks");
            double timestamp = Double.parseDouble(pose.get("timestamp").toString());

            if (timestamp < 0.0) {
                log.info("time return");
                continue;
            }

            if (part.equals("user")) {
                poseData = userPoseData;
            } else {
                poseData = comparePoseData;
            }
            poseVO = new PoseVO();

            poseVO.setFrame(frame * 3);
            poseVO.setTimeStamp(timestamp * 2);

            ArrayList<PoseKeyPoint> poseLandmarks = new ArrayList<>();

            for (int keyPoint = 0; keyPoint < poseLandmarksData.size(); keyPoint++) {

                PoseVO.PoseKeyPoint poseKeyPoint = poseVO.new PoseKeyPoint();
                poseKeyPoint.setX(poseLandmarksData.get(keyPoint).get("x"));
                poseKeyPoint.setY(poseLandmarksData.get(keyPoint).get("y"));
                poseKeyPoint.setZ(poseLandmarksData.get(keyPoint).get("z"));
                poseKeyPoint.setVisibility(poseLandmarksData.get(keyPoint).get("visibility"));

                poseLandmarks.add(poseKeyPoint);
            }
            poseVO.setPoseLandmarks(poseLandmarks);
            normalizeData(poseWorldLandmarksData);

            ArrayList<PoseKeyPoint> poseKeyPoints = new ArrayList<>();
            for (int keyPoint = 0; keyPoint < poseWorldLandmarksData.size(); keyPoint++) {

                PoseVO.PoseKeyPoint poseKeyPoint = poseVO.new PoseKeyPoint();
                poseKeyPoint.setX(poseWorldLandmarksData.get(keyPoint).get("x"));
                poseKeyPoint.setY(poseWorldLandmarksData.get(keyPoint).get("y"));
                poseKeyPoint.setZ(poseWorldLandmarksData.get(keyPoint).get("z"));
                poseKeyPoint.setVisibility(poseWorldLandmarksData.get(keyPoint).get("visibility"));

                poseKeyPoints.add(poseKeyPoint);
            }
            poseVO.setPoseWorldLandmarks(poseKeyPoints);

            ArrayList<PoseTheta> poseThetas = new ArrayList<>();
            for (int[] joint : joints) {
                PoseVO.PoseTheta poseTheta = poseVO.new PoseTheta();

                poseTheta.setKeyPoint(joint[0]);
                poseTheta.setTheta(getTheta(joint[0], joint[1], joint[2]));
                poseThetas.add(poseTheta);
            }
            poseVO.setPoseTheta(poseThetas);
            addMidAnalyze(poseData, frame);
            poseData.add(poseVO);

            frame++;
            log.info("frame : {} , time : {} size = {}", frame, timestamp, poseData.size());
        }
        return poseData;
    }


    /**
     * 부족한 프레임을 보충하는 함수
     */
    public void addMidAnalyze(List<PoseVO> poseData ,int frame) {

        if (!poseData.isEmpty()) {
            PoseVO previousPoseVO = poseData.get(poseData.size() - 1);

            for (int count = 1; count < 3; count++) {
                PoseVO midPoseVO = new PoseVO();
                midPoseVO.setFrame(frame * 3 - (3 - count));

                midPoseVO.setTimeStamp(previousPoseVO.getTimeStamp() + (poseVO.getTimeStamp() - previousPoseVO.getTimeStamp()) * count / 3);

                ArrayList<PoseKeyPoint> poseLandmarks = poseVO.getPoseLandmarks();
                ArrayList<PoseKeyPoint> previousPoseLandmarks = previousPoseVO.getPoseLandmarks();
                ArrayList<PoseKeyPoint> midPoseLandmarks = new ArrayList<>();
                for (int keyPoint = 0; keyPoint < poseVO.getPoseLandmarks().size(); keyPoint++) {

                    PoseVO.PoseKeyPoint poseKeyPoint = poseVO.new PoseKeyPoint();
                    poseKeyPoint.setX(
                            previousPoseLandmarks.get(keyPoint).getX() +
                                    ((poseLandmarks.get(keyPoint).getX() - previousPoseLandmarks.get(keyPoint).getX())
                                            * count / 3));
                    poseKeyPoint.setY(
                            poseLandmarks.get(keyPoint).getY() +
                                    ((poseLandmarks.get(keyPoint).getY() - previousPoseLandmarks.get(keyPoint).getY())
                                            * count / 3));
                    poseKeyPoint.setZ(
                            poseLandmarks.get(keyPoint).getZ() +
                                    ((poseLandmarks.get(keyPoint).getZ() - previousPoseLandmarks.get(keyPoint).getZ())
                                            * count / 3));
                    poseKeyPoint.setVisibility(
                            poseLandmarks.get(keyPoint).getVisibility() +
                                    ((poseLandmarks.get(keyPoint).getVisibility() - previousPoseLandmarks.get(keyPoint)
                                            .getVisibility()) * count / 3));

                    midPoseLandmarks.add(poseKeyPoint);
                }
                midPoseVO.setPoseLandmarks(midPoseLandmarks);

                ArrayList<PoseKeyPoint> poseKeyPoints = poseVO.getPoseWorldLandmarks();
                ArrayList<PoseKeyPoint> previousPoseKeyPoints = previousPoseVO.getPoseWorldLandmarks();
                ArrayList<PoseKeyPoint> midPoseKeyPoints = new ArrayList<>();
                for (int keyPoint = 0; keyPoint < poseVO.getPoseWorldLandmarks().size(); keyPoint++) {

                    PoseVO.PoseKeyPoint poseKeyPoint = poseVO.new PoseKeyPoint();
                    poseKeyPoint.setX(previousPoseKeyPoints.get(keyPoint).getX() +
                            ((poseKeyPoints.get(keyPoint).getX() - previousPoseKeyPoints.get(keyPoint).getX()) * count
                                    / 3));
                    poseKeyPoint.setY(previousPoseKeyPoints.get(keyPoint).getY() +
                            ((poseKeyPoints.get(keyPoint).getY() - previousPoseKeyPoints.get(keyPoint).getY()) * count
                                    / 3));
                    poseKeyPoint.setZ(previousPoseKeyPoints.get(keyPoint).getZ() +
                            ((poseKeyPoints.get(keyPoint).getZ() - previousPoseKeyPoints.get(keyPoint).getZ()) * count
                                    / 3));
                    poseKeyPoint.setVisibility(previousPoseKeyPoints.get(keyPoint).getVisibility() +
                            ((poseKeyPoints.get(keyPoint).getVisibility() - previousPoseKeyPoints.get(keyPoint)
                                    .getVisibility()) * count / 3));

                    midPoseKeyPoints.add(poseKeyPoint);
                }

                midPoseVO.setPoseWorldLandmarks(poseKeyPoints);
                poseData.add(midPoseVO);

            }
        }
    }


    /**
     * 데이터를 신체 기준의 새로운 축을 기준으로 정규화하는 함수 좌어깨 : 11 / 우어깨 : 12 / 좌엉 : 23 / 우엉 : 24
     *
     * @param data : poseWorldLandmarksData
     */
    public void normalizeData(List<Map<String, Double>> data) {

        // 어깨 중앙선과 엉덩이 중앙선을 구합니다.
        double[] shoulderCenter = {
                (data.get(11).get("x") + data.get(12).get("x")) / 2,
                (data.get(11).get("y") + data.get(12).get("y")) / 2,
                (data.get(11).get("z") + data.get(12).get("z")) / 2};
        double[] hipCenter = {
                (data.get(23).get("x") + data.get(24).get("x")) / 2,
                (data.get(23).get("y") + data.get(24).get("y")) / 2,
                (data.get(23).get("z") + data.get(24).get("z")) / 2};

        // 옆구리 중앙선을 구합니다.
        double[] leftSideCenter = {
                (data.get(11).get("x") + data.get(23).get("x")) / 2,
                (data.get(11).get("y") + data.get(23).get("y")) / 2,
                (data.get(11).get("z") + data.get(23).get("z")) / 2};
        double[] rightSideCenter = {
                (data.get(12).get("x") + data.get(24).get("x")) / 2,
                (data.get(12).get("y") + data.get(24).get("y")) / 2,
                (data.get(12).get("z") + data.get(24).get("z")) / 2};

        // 어깨 중앙선과 엉덩이 중앙선을 기준으로 하는 새로운 Y축을 계산합니다.
        double[] yAxis = {hipCenter[0] - shoulderCenter[0], hipCenter[1] - shoulderCenter[1],
                hipCenter[2] - shoulderCenter[2]};
        yAxis = normalize(yAxis);

        double[] leftToRight = {rightSideCenter[0] - leftSideCenter[0], rightSideCenter[1] - leftSideCenter[1],
                rightSideCenter[2] - leftSideCenter[2]};
        double[] zAxis = crossProduct(leftToRight, yAxis);
        zAxis = normalize(zAxis);

        double[] xAxis = crossProduct(yAxis, zAxis);
        xAxis = normalize(xAxis);

        for (Map<String, Double> keyPoint : data) {
            double[] point = {keyPoint.get("x"),
                    keyPoint.get("y"), keyPoint.get("z")};

            keyPoint.put("x", dotProduct(xAxis, point));
            keyPoint.put("y", dotProduct(yAxis, point));
            keyPoint.put("z", dotProduct(zAxis, point));
        }
    }

    /**
     * 벡터를 단위 벡터로 정규화 하는 함수
     *
     * @param v : 벡터
     * @return : 정규화 벡터
     */
    public double[] normalize(double[] v) {
        double[] unitVector = new double[3];
        double magnitude = vectorSize(v);

        if (magnitude > 0) {
            unitVector[0] = v[0] / magnitude;
            unitVector[1] = v[1] / magnitude;
            unitVector[2] = v[2] / magnitude;
        }
        return unitVector;
    }

    /**
     * 벡터의 내적
     */
    public static double dotProduct(double[] v1, double[] v2) {
        double dotProduct = 0;

        for(int i = 0; i < v1.length; i ++){
            dotProduct += (v1[i] * v2[i]);
        }
        return dotProduct;
    }

    /**
     * 벡터의 외적
     *
     * @return : 벡터 v1,v2와 수직인 벡터
     */
    public static double[] crossProduct(double[] v1, double[] v2) {
        double[] verticalVector = new double[3];
        verticalVector[0] = v1[1] * v2[2] - v1[2] * v2[1];
        verticalVector[1] = v1[2] * v2[0] - v1[0] * v2[2];
        verticalVector[2] = v1[0] * v2[1] - v1[1] * v2[0];
        return verticalVector;
    }

    /**
     * 관절의 각도를 구하는 함수
     *
     * @param pointKey : 관절 중앙
     * @param sideKey1 : pointKey 주위 key1
     * @param sideKey2 : pointKey 주위 key2
     * @return (pointKey - > sideKey1, pointKey - > sideKey2) 사이 각
     */
    public double getTheta(int pointKey, int sideKey1, int sideKey2) {

        double[] vector1 = calVector(pointKey, sideKey1);
        double[] vector2 = calVector(pointKey, sideKey2);

        return calTheta(vector1, vector2);
    }

    /**
     * 두 벡터 사이의 각도를 구하는 함수
     *
     * @return (degree) v1, v2 사이 각
     */
    public double calTheta(double[] v1, double[] v2) {

        double cosTheta = dotProduct(v1, v2) / (vectorSize(v1) * vectorSize(v2));
        return Math.acos(cosTheta) * 180 / Math.PI;
    }

    /**
     * 두 키 포인트 사이의 벡터를 구하는 함수
     *
     * @return vector (v1 -> v2)
     */
    public double[] calVector(int key1, int key2) {

        double[] vector = new double[3];
        PoseKeyPoint poseKeyPoint1 = poseVO.getPoseWorldLandmarks().get(key1);
        PoseKeyPoint poseKeyPoint2 = poseVO.getPoseWorldLandmarks().get(key2);

        vector[0] = poseKeyPoint2.getX() - poseKeyPoint1.getX();
        vector[1] = poseKeyPoint2.getY() - poseKeyPoint1.getY();
        vector[2] = poseKeyPoint2.getZ() - poseKeyPoint1.getZ();

        return vector;
    }

    /**
     * 벡터의 크기를 구하는 함수
     *
     * @return 벡터 크기
     */
    public double vectorSize(PoseKeyPoint poseKeyPoint) {
        return Math.sqrt(
                Math.pow(poseKeyPoint.getX(), 2) + Math.pow(poseKeyPoint.getY(), 2) + Math.pow(poseKeyPoint.getZ(), 2));
    }

    public double vectorSize(double[] vector) {
        return Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2) + Math.pow(vector[2], 2));
    }


    /**
     * 주어진 timeStamp와 가장 가까운 Pose 결과를 return 하는 함수
     */
    public PoseVO getTimeStampAnalyze(Map<String, Object> data) {
        double timeStamp = (double) data.get("timeStamp");
        List<PoseVO> poseData;

        if (data.get("part").equals("user")) {
            poseData = userPoseData;
        } else {
            poseData = comparePoseData;
        }

        int low = 0;
        int high = poseData.size() - 1;
        int mid = 0;
        double closest = poseData.get(0).getTimeStamp();

        while (low <= high) {
            mid = (low + high) / 2;

            if (timeStamp == poseData.get(mid).getTimeStamp()) {
                closest = poseData.get(mid).getTimeStamp();
                break;
            }

            if (timeStamp < poseData.get(mid).getTimeStamp()) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }

            if (Math.abs(poseData.get(mid).getTimeStamp() - timeStamp) < Math.abs(closest - timeStamp)) {
                closest = poseData.get(mid).getTimeStamp();
            }
        }
        log.info("timeStamp = {} closest = {} mid = {}", timeStamp, closest, mid);
        return poseData.get(mid);
    }

    public PoseVO getTimeStampAnalyze(List<PoseVO> poseData, double timeStamp){
        int low = 0;
        int high = poseData.size() - 1;
        int mid = 0;
        double closest = poseData.get(0).getTimeStamp();

        while (low <= high) {
            mid = (low + high) / 2;

            if (timeStamp == poseData.get(mid).getTimeStamp()) {
                closest = poseData.get(mid).getTimeStamp();
                break;
            }

            if (timeStamp < poseData.get(mid).getTimeStamp()) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }

            if (Math.abs(poseData.get(mid).getTimeStamp() - timeStamp) < Math.abs(closest - timeStamp)) {
                closest = poseData.get(mid).getTimeStamp();
            }
        }
        log.info("timeStamp = {} closest = {} mid = {}", timeStamp, closest, mid);
        return poseData.get(mid);
    }


    /**
     * 분석이 끝난 비디오를 제거하는 함수
     * @param src
     */
    public void removeVideo(String src) {

        File file = new File(src.replace("http://localhost", "src/main/webapp"));
        if (file.delete()) {
            log.info("삭제 성공 !");
        } else {
            log.info("삭제 실패 ! = {}", file);
        }
    }

    /**
     * 코사인 유사도를 계산하는 함수
     * @param A : 벡터 A
     * @param B : 벡터 B
     * @return 벡터 A와 벡터 B의 유사도
     */
    public double cosineSimilarity(double[] A, double[] B) {
        double dotProduct = dotProduct(A , B);
        double mA = Math.sqrt(dotProduct(A, A));
        double mB = Math.sqrt(dotProduct(B, B));

        return (dotProduct) / ((mA) * (mB));
    }


    /**
     * 가중치 유사도를 계산하는 함수
     * @param poseVO 자세 1
     * @param poseVO2 자세 2
     * @return 가중치 유사도
     */
    public double weightedDistanceMatching(PoseVO poseVO , PoseVO poseVO2) {
        double vector1ConfidenceSum = 0;

        for(PoseKeyPoint poseKeyPoint : poseVO.getPoseWorldLandmarks()){
            vector1ConfidenceSum += poseKeyPoint.getVisibility();
        }
        // First summation
        double summation1 = 1 / vector1ConfidenceSum;

        // Second summation
        double summation2 = 0;
        for (int i = 0; i < poseVO.getPoseWorldLandmarks().size(); i++) {
            double tempX = poseVO.getPoseWorldLandmarks().get(i).getVisibility() *
                    Math.abs(poseVO.getPoseWorldLandmarks().get(i).getX() - poseVO2.getPoseWorldLandmarks().get(i).getX());
            double tempY = poseVO.getPoseWorldLandmarks().get(i).getVisibility() *
                    Math.abs(poseVO.getPoseWorldLandmarks().get(i).getY() - poseVO2.getPoseWorldLandmarks().get(i).getY());
            double tempZ = poseVO.getPoseWorldLandmarks().get(i).getVisibility() *
                    Math.abs(poseVO.getPoseWorldLandmarks().get(i).getZ() - poseVO2.getPoseWorldLandmarks().get(i).getZ());

            summation2 += tempX;
            summation2 += tempY;
            summation2 += tempZ;
        }
        return summation1 * summation2;
    }

    /**
     * 분석하기 버튼으로, 비교영상의 현재 자세와, 사용자 영상의
     * @param data
     */
    public void matchCurrentPose(Map<String, Double> data){
        PoseVO poseVO1 = getTimeStampAnalyze(comparePoseData, data.get("compareTimeStamp"));
        PoseVO poseVO2 =  getTimeStampAnalyze(userPoseData, data.get("userTimeStamp"));


        log.info("weight = {}", weightedDistanceMatching(poseVO1, poseVO2));

        for(int i = 0 ; i < poseVO1.getPoseWorldLandmarks().size(); i++){

            double[] newData = {poseVO1.getPoseWorldLandmarks().get(i).getX() , poseVO1.getPoseWorldLandmarks().get(i).getY()
            ,poseVO1.getPoseWorldLandmarks().get(i).getZ()};

            double[] newData2 = {poseVO2.getPoseWorldLandmarks().get(i).getX() , poseVO2.getPoseWorldLandmarks().get(i).getY()
                    ,poseVO2.getPoseWorldLandmarks().get(i).getZ()};


            log.info("index = {}   consine = {}",i, cosineSimilarity(newData, newData2));
        }
    }

    /**
     *  현재 사용자 영상과 비교 영상의 가중치거리를 측정하는 함수
     */
    public void matchAllPose(){

        double [][] weightedDistance = new double[userPoseData.size()+1][comparePoseData.size()+1];
        int [][] dp = new int[userPoseData.size()+1][comparePoseData.size()+1];
        int max = 0;
        int maxI = 0;
        int maxJ = 0;

        for(int i = 1 ;  i <= userPoseData.size(); i++){
            for(int j = 1 ;  j <= comparePoseData.size(); j++){
                weightedDistance[i][j] = weightedDistanceMatching(userPoseData.get(i-1) , comparePoseData.get(j-1));
                if(weightedDistance[i][j] < 0.2){
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if(dp[i][j] < max){
                        continue;
                    }
                    max = dp[i][j];
                    maxI = i;
                    maxJ = j;
                }

            }
        }

        for(int i = 0 ;  i < userPoseData.size(); i++){
            log.info("distance = {}" , Arrays.toString(weightedDistance[i]));
        }

        log.info("max = {}  i = {}   j = {} " , max, maxI, maxJ);
        log.info("start userFrame = {}   start compareFrame = {} " ,  maxI - max, maxJ - max);
    }

}