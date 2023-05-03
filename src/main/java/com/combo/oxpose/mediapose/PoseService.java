package com.combo.oxpose.mediapose;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.springframework.stereotype.Service;

import com.combo.oxpose.mediapose.PoseVO.PoseKeyPoint;
import com.combo.oxpose.mediapose.PoseVO.PoseTheta;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PoseService {

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

            PoseVO poseVO = new PoseVO();

            poseVO.setFrame(frame * 3);
            poseVO.setTimeStamp(timestamp * 2);

            ArrayList<PoseKeyPoint> poseLandmarks = new ArrayList<>();

            for (int keyPoint = 0; keyPoint < poseLandmarksData.size(); keyPoint++) {

                PoseVO.PoseKeyPoint poseKeyPoint = new PoseKeyPoint();
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

                PoseVO.PoseKeyPoint poseKeyPoint = new PoseKeyPoint();
                poseKeyPoint.setX(poseWorldLandmarksData.get(keyPoint).get("x"));
                poseKeyPoint.setY(poseWorldLandmarksData.get(keyPoint).get("y"));
                poseKeyPoint.setZ(poseWorldLandmarksData.get(keyPoint).get("z"));
                poseKeyPoint.setVisibility(poseWorldLandmarksData.get(keyPoint).get("visibility"));

                poseKeyPoints.add(poseKeyPoint);
            }
            poseVO.setPoseWorldLandmarks(poseKeyPoints);

            ArrayList<PoseTheta> poseThetas = new ArrayList<>();
            for (int[] joint : joints) {
                PoseVO.PoseTheta poseTheta = new PoseTheta();

                poseTheta.setKeyPoint(joint[0]);
                poseTheta.setTheta(getTheta(joint[0], joint[1], joint[2], poseVO));
                poseThetas.add(poseTheta);
            }
            poseVO.setPoseTheta(poseThetas);
            addMidAnalyze(poseData, poseVO, frame);
            poseData.add(poseVO);

            frame++;
            log.info("frame : {} , time : {} size = {}", frame, timestamp, poseData.size());
        }
        return poseData;
    }


    /**
     * 부족한 프레임을 보충하는 함수
     */
    public void addMidAnalyze(List<PoseVO> poseData , PoseVO poseVO,int frame) {

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

                    PoseVO.PoseKeyPoint poseKeyPoint = new PoseKeyPoint();
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

                    PoseVO.PoseKeyPoint poseKeyPoint = new PoseKeyPoint();
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
    public double getTheta(int pointKey, int sideKey1, int sideKey2, PoseVO poseVO) {

        double[] vector1 = calVector(pointKey, sideKey1, poseVO);
        double[] vector2 = calVector(pointKey, sideKey2, poseVO);

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
    public double[] calVector(int key1, int key2, PoseVO poseVO) {

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
     * @return 벡터 A와 벡터 B의 유사도
     */
    public double cosineSimilarity(PoseKeyPoint poseWorldLandmarks1 , PoseKeyPoint poseWorldLandmarks2) {
        double[] A = {poseWorldLandmarks1.getX() , poseWorldLandmarks1.getY()
                ,poseWorldLandmarks1.getZ()};
        double[] B = {poseWorldLandmarks2.getX() , poseWorldLandmarks2.getY()
                ,poseWorldLandmarks2.getZ()};

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
     * @param
     */
    public void matchCurrentPose(List<PoseVO> poseVOs){
        PoseVO poseVO1 = poseVOs.get(0);
        PoseVO poseVO2 = poseVOs.get(1);

        log.info("weight = {}", weightedDistanceMatching(poseVO1, poseVO2));
        for(int i = 0 ; i < poseVO1.getPoseWorldLandmarks().size(); i++){

            log.info("index = {}   consine = {}", i, cosineSimilarity(poseVO1.getPoseWorldLandmarks().get(i), poseVO2.getPoseWorldLandmarks().get(i)));
        }
    }

    /**
     * 현재 사용자 영상과 비교 영상의 가중치거리를 측정하는 함수
     *
     * @return
     */
    public List<PoseVO> matchAllPose(List<List<PoseVO>> poseVOs){
        List<PoseVO> userPoseData = poseVOs.get(0);
        List<PoseVO> comparePoseData = poseVOs.get(1);

        double [][] weightedDistance = new double[userPoseData.size()+1][comparePoseData.size()+1];
        int [][] dp = new int[userPoseData.size()+1][comparePoseData.size()+1];
        int max = 0;
        int maxI = 0;
        int maxJ = 0;
        List<PoseVO> result = new ArrayList<>();

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
        log.info("LastUserTime = {}   LastCompareTime = {} " ,  poseVOs.get(0).get(maxI).getTimeStamp(), poseVOs.get(1).get(maxJ).getTimeStamp());
        log.info("start userTime = {}   start compareTime = {} " ,  poseVOs.get(0).get(maxI - max).getTimeStamp(), poseVOs.get(1).get(maxJ - max).getTimeStamp());
        return Arrays.asList(poseVOs.get(0).get(maxI - max) , poseVOs.get(1).get(maxJ - max));
    }

    /**
     * 비슷하지 않은 포즈를 선별하는 함수
     * @param poseVOs
     * @return
     */
    public List<PoseVO> requestComparePose(List<List<PoseVO>> poseVOs){
        List<PoseVO> userPoseData = poseVOs.get(0);
        List<PoseVO> comparePoseData = poseVOs.get(1);
        List<PoseVO> result = new ArrayList<>();

        for(int i = 0; i < userPoseData.size(); i ++){

            PoseVO userPose = userPoseData.get(i);
            PoseVO comparePose = comparePoseData.get(i);
            PoseVO resultPose = new PoseVO();

            resultPose.setTimeStamp(comparePose.getTimeStamp());

            ArrayList<PoseKeyPoint> resultWorldLandmarks = new ArrayList<>();
            for(int j = 0 ; j < 33; j ++){

                if(weightedDistanceMatching(userPose, comparePose) < 0.2){
                    break;
                }

                PoseKeyPoint userPoseKey = userPose.getPoseWorldLandmarks().get(j);
                PoseKeyPoint comparePoseKey = comparePose.getPoseWorldLandmarks().get(j);

                PoseKeyPoint resultPoseKey = new PoseKeyPoint();

                resultPoseKey.setX(comparePoseKey.getX() - userPoseKey.getX());
                resultPoseKey.setY(comparePoseKey.getY() - userPoseKey.getY());
                resultPoseKey.setZ(comparePoseKey.getZ() - userPoseKey.getZ());

                resultWorldLandmarks.add(resultPoseKey);

            }
            resultPose.setPoseWorldLandmarks(resultWorldLandmarks);
            result.add(resultPose);
        }

        return result;
    }


    /**
     * data에 따라 command를 추가하는 함수
     * @param data
     * @return
     */
    public List<CommandVO> requestCommand(List<PoseVO> data) {

        List<CommandVO> commandVOS = new ArrayList<>();

        int []test = new int[]{11,12,13,14,15,16,23,24,25,26,27,28};

        for(PoseVO poseVO : data){
            CommandVO commandVO = new CommandVO();
            commandVO.setTimeStamp(poseVO.getTimeStamp());
            commandVO.setPoseWorldLandmarks(poseVO.getPoseWorldLandmarks());
            commandVO.setPoseLandmarks(poseVO.getPoseLandmarks());
            List<String> command = new ArrayList<>();
            for(int i = 0 ; i < 33 ; i ++){

                if(poseVO.getPoseWorldLandmarks().isEmpty()){
                    continue;
                }

                PoseKeyPoint poseKeyPoint = poseVO.getPoseWorldLandmarks().get(i);
                if(poseKeyPoint.getX() == 0){
                    continue;
                }

                int finalI = i;
                if(Arrays.stream(test).anyMatch(index -> index == finalI)){
                    StringBuffer sb = new StringBuffer();
                    double Max = Math.max(Math.max(Math.abs(poseKeyPoint.getX()) , Math.abs(poseKeyPoint.getY())),Math.abs(poseKeyPoint.getZ()));
                    sb.append(KeyPointName.valueOf(i).getName()).append(" ");
                    if(Max == Math.abs(poseKeyPoint.getX())){
                        if(Math.abs(poseKeyPoint.getX()) > 0){
                            sb.append("왼쪽 ");
                        }else{
                            sb.append("오른쪽 ");
                        }
                    }

                    if(Max == Math.abs(poseKeyPoint.getY())){
                        if(Math.abs(poseKeyPoint.getY()) > 0){
                            sb.append("아래로 ");
                        }else{
                            sb.append("위로 ");
                        }

                    }else {
                        if(Math.abs(poseKeyPoint.getZ()) > 0){
                            sb.append("앞으로 ");
                        }else{
                            sb.append("뒤로 ");
                        }
                    }

                    command.add(String.valueOf(sb));
                }else{
                    continue;
                }
            }
            commandVO.setCommand(command);
            commandVOS.add(commandVO);
        }

        return commandVOS;
    }

    public void requestCorrectivePoseLandmarks(List<List<PoseVO>> poseVOs, List<PoseVO> data) {
        List<PoseVO> userPoseData = poseVOs.get(0);
        List<PoseVO> comparePoseData = poseVOs.get(1);

        log.info("size = {} datas = {}",comparePoseData.size() , data.size());

        for(int i = 0 ; i < userPoseData.size(); i ++){

            ArrayList<PoseKeyPoint> landmarks = userPoseData.get(i).getPoseLandmarks();
            ArrayList<PoseKeyPoint> worldLandmarks = userPoseData.get(i).getPoseWorldLandmarks();
            ArrayList<PoseKeyPoint> compareWorldLandmarks = comparePoseData.get(i).getPoseWorldLandmarks();
            double XLandIncreased = landmarks.get(11).getX() - landmarks.get(12).getX() ;
            double XWorldIncreased = worldLandmarks.get(11).getX() - worldLandmarks.get(12).getX() ;

            double XIncreased = XWorldIncreased / XLandIncreased;

            double YLandIncreased = landmarks.get(11).getY() - landmarks.get(23).getY() ;
            double YWorldIncreased = worldLandmarks.get(11).getY() - worldLandmarks.get(23).getY() ;

            double YIncreased = YWorldIncreased / YLandIncreased;

            double ZLandIncreased = landmarks.get(11).getZ() - landmarks.get(23).getZ() ;
            double ZWorldIncreased = worldLandmarks.get(11).getZ() - worldLandmarks.get(23).getZ() ;

            double ZIncreased = ZWorldIncreased / ZLandIncreased;

            //11 을 기준으로 // data world com - world user
            // 11 13 15 12 14 16 / 23 25 27 24 26 28

            int [] legarm = new int[]{11, 12, 13, 14, 15, 16, 23, 24, 25, 26, 27, 28};
            ArrayList<PoseKeyPoint> pkList = new ArrayList<>();

            for(int j : legarm){

                PoseKeyPoint pk = new PoseKeyPoint();
                double X13WorldGap = compareWorldLandmarks.get(j).getX() - worldLandmarks.get(11).getX();
                double Y13WorldGap = compareWorldLandmarks.get(j).getY() - worldLandmarks.get(11).getY();
                double Z13WorldGap = compareWorldLandmarks.get(j).getZ() - worldLandmarks.get(11).getZ();
                pk.setX(X13WorldGap / XIncreased + landmarks.get(11).getX());
                pk.setY(Y13WorldGap / YIncreased + landmarks.get(11).getY());
                pk.setZ(Z13WorldGap / ZIncreased + landmarks.get(11).getZ());
                pk.setVisibility(0.9);

                pkList.add(pk);
            }

            data.get(i).setPoseLandmarks(pkList);
        }

    }
    

}

