package com.combo.oxpose.mediapose;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

import com.combo.oxpose.mediapose.PoseVO.PoseKeyPoint;
import com.combo.oxpose.mediapose.PoseVO.PoseTheta;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PoseService {

    private final int[][] joints = {{11, 12, 13}, {12, 11, 14}, {13, 11, 15}, {14, 12, 16}, {23, 24, 25},
            {24, 23, 26}, {25, 23, 27}, {26, 24, 28}};
    private final int[] armsAndLegs = new int[]{11, 12, 13, 14, 15, 16, 23, 24, 25, 26, 27, 28};

    /**
     * 포즈의 키 포인트 데이터를 정규화, 각 관절의 각도를 구하는 함수
     *
     * @return (임시)
     */
    public List<PoseVO> setAnalyzePose(List<PoseVO> poseVOS) {

        for (PoseVO poseVO : poseVOS) {
            ArrayList<PoseKeyPoint> poseWorldLandmarks = poseVO.getPoseWorldLandmarks();

            double timestamp = poseVO.getTimeStamp();

            if (timestamp < 0.0) {
                continue;
            }

            poseVO.setTimeStamp(timestamp * 2);
            normalizeData(poseWorldLandmarks);

            ArrayList<PoseTheta> poseThetas = new ArrayList<>();
            for (int[] joint : joints) {
                PoseVO.PoseTheta poseTheta = new PoseTheta();

                poseTheta.setKeyPoint(joint[0]);
                poseTheta.setTheta(getTheta(joint, poseVO));
                poseThetas.add(poseTheta);
            }
            poseVO.setPoseTheta(poseThetas);
        }

        addMidAnalyze(poseVOS);
        log.info("poseVOS.size = {} ", poseVOS.size());
        return poseVOS;
    }


    /**
     * 부족한 프레임을 보충하는 함수
     */
    public void addMidAnalyze(List<PoseVO> poseVOs) {

        for (int i = poseVOs.size() - 1; i > 0; i--) {
            PoseVO curPoseVO = poseVOs.get(i);
            PoseVO previousPoseVO = poseVOs.get(i - 1);

            for (int count = 2; count > 0; count--) {
                PoseVO midPoseVO = new PoseVO();

                midPoseVO.setTimeStamp(previousPoseVO.getTimeStamp()
                        + (curPoseVO.getTimeStamp() - previousPoseVO.getTimeStamp()) * count / 3);
                midPoseVO.setPoseLandmarks(
                        getMiddlePose(curPoseVO.getPoseLandmarks(), previousPoseVO.getPoseLandmarks(), count));
                midPoseVO.setPoseWorldLandmarks(
                        getMiddlePose(curPoseVO.getPoseWorldLandmarks(), previousPoseVO.getPoseWorldLandmarks(),
                                count));

                poseVOs.add(i, midPoseVO);
            }
        }
    }

    /**
     * 좌표의 사잇값을 반환하는 함수
     * @param curPoseKeyPoints 2번 포즈
     * @param previousPoseKeyPoint 1번 포즈
     * @param count index
     * @return
     */
    private ArrayList<PoseKeyPoint> getMiddlePose(ArrayList<PoseKeyPoint> curPoseKeyPoints,
                                                  ArrayList<PoseKeyPoint> previousPoseKeyPoint, int count) {

        ArrayList<PoseKeyPoint> midPoseLandmarks = new ArrayList<>();
        for (int keyPoint = 0; keyPoint < curPoseKeyPoints.size(); keyPoint++) {

            PoseVO.PoseKeyPoint poseKeyPoint = new PoseKeyPoint();
            poseKeyPoint.setX(previousPoseKeyPoint.get(keyPoint).getX() +
                    ((curPoseKeyPoints.get(keyPoint).getX() - previousPoseKeyPoint.get(keyPoint).getX())
                            * count / 3));
            poseKeyPoint.setY(previousPoseKeyPoint.get(keyPoint).getY() +
                    ((curPoseKeyPoints.get(keyPoint).getY() - previousPoseKeyPoint.get(keyPoint).getY())
                            * count / 3));
            poseKeyPoint.setZ(previousPoseKeyPoint.get(keyPoint).getZ() +
                    ((curPoseKeyPoints.get(keyPoint).getZ() - previousPoseKeyPoint.get(keyPoint).getZ())
                            * count / 3));
            poseKeyPoint.setVisibility(previousPoseKeyPoint.get(keyPoint).getVisibility() +
                    ((curPoseKeyPoints.get(keyPoint).getVisibility() - previousPoseKeyPoint.get(keyPoint)
                            .getVisibility()) * count / 3));

            midPoseLandmarks.add(poseKeyPoint);
        }
        return midPoseLandmarks;
    }

    /**
     * 데이터를 신체 기준의 새로운 축을 기준으로 정규화하는 함수 좌어깨 : 11 / 우어깨 : 12 / 좌엉 : 23 / 우엉 : 24
     *
     */
    public double[][] normalizeData(ArrayList<PoseKeyPoint> poseKeyPoints) {

        double[] shoulderCenter = {
                (poseKeyPoints.get(11).getX() + poseKeyPoints.get(12).getX()) / 2,
                (poseKeyPoints.get(11).getY() + poseKeyPoints.get(12).getY()) / 2,
                (poseKeyPoints.get(11).getZ() + poseKeyPoints.get(12).getZ()) / 2};
        double[] hipCenter = {
                (poseKeyPoints.get(23).getX() + poseKeyPoints.get(24).getX()) / 2,
                (poseKeyPoints.get(23).getY() + poseKeyPoints.get(24).getY()) / 2,
                (poseKeyPoints.get(23).getZ() + poseKeyPoints.get(24).getZ()) / 2};
        double[] leftSideCenter = {
                (poseKeyPoints.get(11).getX() + poseKeyPoints.get(23).getX()) / 2,
                (poseKeyPoints.get(11).getY() + poseKeyPoints.get(23).getY()) / 2,
                (poseKeyPoints.get(11).getZ() + poseKeyPoints.get(23).getZ()) / 2};
        double[] rightSideCenter = {
                (poseKeyPoints.get(12).getX() + poseKeyPoints.get(24).getX()) / 2,
                (poseKeyPoints.get(12).getY() + poseKeyPoints.get(24).getY()) / 2,
                (poseKeyPoints.get(12).getZ() + poseKeyPoints.get(24).getZ()) / 2};

        double[] yAxis = {hipCenter[0] - shoulderCenter[0], hipCenter[1] - shoulderCenter[1],
                hipCenter[2] - shoulderCenter[2]};
        yAxis = normalize(yAxis);

        double[] leftToRight = {rightSideCenter[0] - leftSideCenter[0], rightSideCenter[1] - leftSideCenter[1],
                rightSideCenter[2] - leftSideCenter[2]};
        double[] zAxis = crossProduct(leftToRight, yAxis);
        zAxis = normalize(zAxis);

        double[] xAxis = crossProduct(yAxis, zAxis);
        xAxis = normalize(xAxis);

        double[][] M = {
                {xAxis[0], yAxis[0], zAxis[0]},
                {xAxis[1], yAxis[1], zAxis[1]},
                {xAxis[2], yAxis[2], zAxis[2]}
        };

        for (PoseKeyPoint keyPoint : poseKeyPoints) {
            double[] point = {keyPoint.getX(),
                    keyPoint.getY(), keyPoint.getZ()};

            keyPoint.setX(dotProduct(xAxis, point));
            keyPoint.setY(dotProduct(yAxis, point));
            keyPoint.setZ(dotProduct(zAxis, point));
        }

        return M;
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
     *
     * @return 벡터 v1,v2의 내적
     */
    public static double dotProduct(double[] v1, double[] v2) {
        double dotProduct = 0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += (v1[i] * v2[i]);
        }
        return dotProduct;
    }

    /**
     * 벡터의 외적 (두 벡터에 수직인 벡터)
     *
     * @return : 벡터 v1,v2 외적
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
     * @return (pointKey - > sideKey1, pointKey - > sideKey2) 사이 각
     */
    public double getTheta(int[] joint, PoseVO poseVO) {
        double[] vector1 = calVector(joint[0], joint[1], poseVO);
        double[] vector2 = calVector(joint[0], joint[2], poseVO);

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
    public double vectorSize(double[] vector) {
        return Math.sqrt(Math.pow(vector[0], 2) + Math.pow(vector[1], 2) + Math.pow(vector[2], 2));
    }

    /**
     * 분석이 끝난 비디오를 제거하는 함수
     */
    public void removeVideo(String src) {
        File file = new File(src.replace("http://localhost", "src/main/webapp"));
        if (file.delete()) {
            log.debug("삭제 성공");
        } else {
            log.error("삭제 실패 : {}", file);
        }
    }

    /**
     * 코사인 유사도를 계산하는 함수
     *
     * @return 벡터 A와 벡터 B의 유사도
     */
    public double cosineSimilarity(PoseKeyPoint poseWorldLandmarks1, PoseKeyPoint poseWorldLandmarks2) {
        double[] A = {poseWorldLandmarks1.getX(), poseWorldLandmarks1.getY()
                , poseWorldLandmarks1.getZ()};
        double[] B = {poseWorldLandmarks2.getX(), poseWorldLandmarks2.getY()
                , poseWorldLandmarks2.getZ()};

        return (dotProduct(A, B)) / ((Math.sqrt(dotProduct(A, A))) * (Math.sqrt(dotProduct(B, B))));
    }

    /**
     * 가중치 유사도를 계산하는 함수
     *
     * @param poseVO  자세 1
     * @param poseVO2 자세 2
     * @return 가중치 유사도
     */
    public double weightedDistanceMatching(PoseVO poseVO, PoseVO poseVO2) {
        double vector1ConfidenceSum = 0;

        for (PoseKeyPoint poseKeyPoint : poseVO.getPoseWorldLandmarks()) {
            vector1ConfidenceSum += poseKeyPoint.getVisibility();
        }
        // First summation
        double summation1 = 1 / vector1ConfidenceSum;

        // Second summation
        double summation2 = 0;
        for (int i = 0; i < poseVO.getPoseWorldLandmarks().size(); i++) {
            double tempX = poseVO.getPoseWorldLandmarks().get(i).getVisibility() *
                    Math.abs(poseVO.getPoseWorldLandmarks().get(i).getX() - poseVO2.getPoseWorldLandmarks().get(i)
                            .getX());
            double tempY = poseVO.getPoseWorldLandmarks().get(i).getVisibility() *
                    Math.abs(poseVO.getPoseWorldLandmarks().get(i).getY() - poseVO2.getPoseWorldLandmarks().get(i)
                            .getY());
            double tempZ = poseVO.getPoseWorldLandmarks().get(i).getVisibility() *
                    Math.abs(poseVO.getPoseWorldLandmarks().get(i).getZ() - poseVO2.getPoseWorldLandmarks().get(i)
                            .getZ());

            summation2 += tempX;
            summation2 += tempY;
            summation2 += tempZ;
        }
        return summation1 * summation2;
    }

    /**
     * 현재 비디오 영상의 자세를 분석하여 출력
     */
    public void matchCurrentPose(List<PoseVO> poseVOs) {
        PoseVO poseVO1 = poseVOs.get(0);
        PoseVO poseVO2 = poseVOs.get(1);

        log.info("weight = {}", weightedDistanceMatching(poseVO1, poseVO2));
        for (int i = 0; i < poseVO1.getPoseWorldLandmarks().size(); i++) {

            log.info("index = {}   consine = {}", i,
                    cosineSimilarity(poseVO1.getPoseWorldLandmarks().get(i), poseVO2.getPoseWorldLandmarks().get(i)));
        }
    }

    /**
     * 현재 사용자 영상과 비교 영상의 가중치거리를 측정하는 함수
     */
    public List<PoseVO> matchAllPose(List<List<PoseVO>> poseVOs) {
        List<PoseVO> userPoseData = poseVOs.get(0);
        List<PoseVO> comparePoseData = poseVOs.get(1);

        double[][] weightedDistance = new double[userPoseData.size() + 1][comparePoseData.size() + 1];
        int[][] dp = new int[userPoseData.size() + 1][comparePoseData.size() + 1];
        int max = 0;
        int maxI = 0;
        int maxJ = 0;

        for (int i = 1; i <= userPoseData.size(); i++) {
            for (int j = 1; j <= comparePoseData.size(); j++) {
                weightedDistance[i][j] = weightedDistanceMatching(userPoseData.get(i - 1), comparePoseData.get(j - 1));
                if (weightedDistance[i][j] < 0.2) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (dp[i][j] < max) {
                        continue;
                    }
                    max = dp[i][j];
                    maxI = i;
                    maxJ = j;
                }

            }
        }

        for (int i = 0; i < userPoseData.size(); i++) {
            log.debug("distance = {}", Arrays.toString(weightedDistance[i]));
        }

        log.debug("max = {}  i = {}   j = {} ", max, maxI, maxJ);
        log.debug("LastUserTime = {}   LastCompareTime = {} ", poseVOs.get(0).get(maxI).getTimeStamp(),
                poseVOs.get(1).get(maxJ).getTimeStamp());
        log.debug("start userTime = {}   start compareTime = {} ", poseVOs.get(0).get(maxI - max).getTimeStamp(),
                poseVOs.get(1).get(maxJ - max).getTimeStamp());
        return Arrays.asList(poseVOs.get(0).get(maxI - max), poseVOs.get(1).get(maxJ - max));
    }

    /**
     * data에 따라 코멘트를 추가하는 함수
     *
     * @return 3D grid에 따른 자세 차이 코멘트
     */
    public List<CommandVO> requestCommand(List<PoseVO> compareCorrectPose, List<PoseVO> userPoseVO) {

        List<CommandVO> commandVOS = new ArrayList<>();

        for (int index = 0 ; index < compareCorrectPose.size(); index++) {
            CommandVO commandVO = new CommandVO();
            commandVO.setTimeStamp(compareCorrectPose.get(index).getTimeStamp());
            commandVO.setPoseWorldLandmarks(compareCorrectPose.get(index).getPoseWorldLandmarks());
            commandVO.setPoseLandmarks(compareCorrectPose.get(index).getPoseLandmarks());

            List<String> command = new ArrayList<>();

            for (int i = 0;  i <  compareCorrectPose.get(index).getPoseWorldLandmarks().size() ; i ++) {
                PoseKeyPoint correctKeyPoint = compareCorrectPose.get(index).getPoseWorldLandmarks().get(i);
                PoseKeyPoint userKeyPoint = userPoseVO.get(index).getPoseWorldLandmarks().get(i);

                double XGap = correctKeyPoint.getX() - userKeyPoint.getX();
                double YGap = correctKeyPoint.getY() - userKeyPoint.getY();
                double ZGap = correctKeyPoint.getZ() - userKeyPoint.getZ();


                if (compareCorrectPose.get(index).getPoseWorldLandmarks().isEmpty() || correctKeyPoint.getX() == 0) {
                    continue;
                }

                StringBuilder sb = new StringBuilder();

                double Max = Math.max(Math.max(Math.abs(XGap), Math.abs(YGap)), Math.abs(ZGap));

                if(Max > 0.4){
                    log.info("Max = {}" , Max);
                    sb.append(KeyPointName.correctValueOf(i).getName()).append(" ");
                    if (Max == Math.abs(XGap)) {
                        if (XGap > 0) {
                            sb.append("왼쪽 ");
                        } else {
                            sb.append("오른쪽 ");
                        }
                    }

                    if (Max == Math.abs(YGap)) {
                        if (YGap > 0) {
                            sb.append("아래로 ");
                        } else {
                            sb.append("위로 ");
                        }

                    } else {
                        if (ZGap > 0) {
                            sb.append("앞으로 ");
                        } else {
                            sb.append("뒤로 ");
                        }
                    }

                }

                command.add(String.valueOf(sb));
            }
            commandVO.setCommand(command);
            commandVOS.add(commandVO);
        }

        return commandVOS;
    }

    /**
     * 3D와 2D의 분석점 차이를 12개의 점으로 반환
     *
     * @return 팔 다리 12개의 3D점, 2D점
     */
    public List<PoseVO> requestCorrectivePoseLandmarks(List<List<PoseVO>> poseVOs) {
        List<PoseVO> userPoseData = poseVOs.get(0);
        List<PoseVO> comparePoseData = poseVOs.get(1);

        for (int i = 0; i < userPoseData.size(); i++) {
            ArrayList<PoseKeyPoint> landmarks = userPoseData.get(i).getPoseLandmarks();
            ArrayList<PoseKeyPoint> worldLandmarks = userPoseData.get(i).getPoseWorldLandmarks();
            double[][] M = normalizeData(landmarks);

            ArrayList<PoseKeyPoint> compareWorldLandmarks = comparePoseData.get(i).getPoseWorldLandmarks();
            double XLandIncreased = landmarks.get(11).getX() - landmarks.get(12).getX();
            double XWorldIncreased = worldLandmarks.get(11).getX() - worldLandmarks.get(12).getX();
            double XIncreased = XWorldIncreased / XLandIncreased;

            double YLandIncreased = landmarks.get(11).getY() - landmarks.get(23).getY();
            double YWorldIncreased = worldLandmarks.get(11).getY() - worldLandmarks.get(23).getY();
            double YIncreased = YWorldIncreased / YLandIncreased;

            double ZLandIncreased = landmarks.get(11).getZ() - landmarks.get(23).getZ();
            double ZWorldIncreased = worldLandmarks.get(11).getZ() - worldLandmarks.get(23).getZ();
            double ZIncreased = ZWorldIncreased / ZLandIncreased;

            ArrayList<PoseKeyPoint> correctLandmarks = new ArrayList<>();
            ArrayList<PoseKeyPoint> correctWorldLandmarks = new ArrayList<>();
            for (int keypoint : armsAndLegs) {

                PoseKeyPoint poseKeyPoint = new PoseKeyPoint();
                double X13WorldGap = compareWorldLandmarks.get(keypoint).getX() - worldLandmarks.get(11).getX();
                double Y13WorldGap = compareWorldLandmarks.get(keypoint).getY() - worldLandmarks.get(11).getY();
                double Z13WorldGap = compareWorldLandmarks.get(keypoint).getZ() - worldLandmarks.get(11).getZ();
                poseKeyPoint.setX(X13WorldGap / XIncreased + landmarks.get(11).getX());
                poseKeyPoint.setY(Y13WorldGap / YIncreased + landmarks.get(11).getY());
                poseKeyPoint.setZ(Z13WorldGap / ZIncreased + landmarks.get(11).getZ());
                poseKeyPoint.setVisibility(0.9);

                correctLandmarks.add(poseKeyPoint);
                correctWorldLandmarks.add(compareWorldLandmarks.get(keypoint));
            }

            for (PoseKeyPoint keyPoint : correctLandmarks) {
                double[] point = {keyPoint.getX(), keyPoint.getY(), keyPoint.getZ()};
                double[] P = matrixMultiply(M, point);

                keyPoint.setX(P[0]);
                keyPoint.setY(P[1]);
                keyPoint.setZ(P[2]);
            }

            comparePoseData.get(i).setPoseLandmarks(correctLandmarks);
            comparePoseData.get(i).setPoseWorldLandmarks(correctWorldLandmarks);
        }

        return comparePoseData;
    }

    /**
     * 행렬 곱셈
     */
    public static double[] matrixMultiply(double[][] matrixA, double[] matrixB) {
        int rowsA = matrixA.length;
        int colsA = matrixA[0].length;
        int rowsB = matrixB.length;

        if (colsA != rowsB) {
            throw new IllegalArgumentException(
                    "The number of columns in matrix A must be equal to the number of rows in matrix B.");
        }

        double[] result = new double[rowsA];

        for (int i = 0; i < rowsA; i++) {
            double sum = 0;
            for (int j = 0; j < colsA; j++) {
                sum += matrixA[i][j] * matrixB[j];
            }
            result[i] = sum;
        }
        return result;
    }
}
