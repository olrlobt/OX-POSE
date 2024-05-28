![OX-POSE logo](https://github.com/olrlobt/OX-POSE/assets/99643732/ce2d57e8-23ee-46a3-b87f-94b8f6403d68)


**기간 : 2023.02.23 ~. 2023.5.24 (13주)**

기관 : 개인 프로젝트

URL : https://github.com/olrlobt/OX-POSE

---

# **OX-POSE**

Pose Estimation을 이용한 영상 자세 비교 분석 서비스

Google 머신러닝 솔루션 Mediapipe를 이용하여 두 비디오상의 인체포즈를 분석한다. 비교할 영상과 내 영상 자세의 차이를 분석하고, 자세의 차이를 시각적으로 오버레이 위에 표기한다. 

<br><br>

## 구현

- Google 머신러닝 솔루션 Mediapipe를 이용하여 비디오상의 인체포즈 분석 구현
- 영상마다 정확한 자세 분석을 위한, 벡터 좌표 정규화
- Mediapipe 분석시 FFmpeg를 이용한 영상 배속으로 분석 과정 시간 1/2 단축
- 중간 프레임 추가 연산 보정 작업으로 정확도 보정 ( 프레임 수 3배 증가 )
- 코사인 유사도와 가중치 거리를 활용하여, 두 영상의 자세 유사도 분석
- 유사도 기준 보정 오버레이 표기
- **LCSubstr([Longest Common Substring](https://www.geeksforgeeks.org/longest-common-substring-dp-29/))** 알고리즘을 이용하여 두 영상에서 가장 비슷한 자세 추적 기능 구현

<br><br>

---

# 주요기능 구현 화면

### 영상 업로드

- 비교 대상이 될 영상과 비교하고싶은 영상을 업로드한다.
- FFmpeg 라이브러리를 이용하여 업로드 영상을 2배속한 후, Mediapipe를 이용하여 자세 분석을 실행한다. 이 과정을 통해 분석 시간을 1/2 단축하였다.

![1영상업](https://github.com/olrlobt/OX-POSE/assets/99643732/2a0d68c0-aa80-498b-b97b-ed0f86395918)


### 영상 자세 측정 결과

- Mediapipe 분석 결과를 캐싱하고, 이를 33개의 키 포인트로 비디오 위에 표기한다.
- 영상 배속에 따라 떨어지는 프레임을 보정하기 위해, 중간 프레임을 계산하는 연산 로직을 추가하였다. 이 과정을 통해 프레임 수를 3배 증가시켜 정확도를 향상시켰다.

![2D자세비교](https://github.com/olrlobt/OX-POSE/assets/99643732/53f84b5c-8039-4e91-9da1-bf76dd3e658f)


### 영상 자세 3D 측정 결과

- 자세 분석 결과를 Mediapipe에서 제공하는 3D canvas 위에 표기한다.
- 벡터 정규화를 통해 분석 좌표를 표준화하여, 어느 영상이던 같은 크기의 오버레이를 제공한다.
- 벡터의 내적과 외적을 통해 기존 좌표계를 새로운 좌표계로 변환할 수 있는 축을 정의하고, 각 포인트를 이 새로운 축에 투영하여 좌표를 변환하였다. 이 과정을 통해, 영상 속 사람이 어디를 보던 정면을 보는 좌표로 표현된다.

![6 3D측정](https://github.com/olrlobt/OX-POSE/assets/99643732/0126488c-64fe-4707-b651-7489bc8de6e8)


### 영상 자세 비교

- 코사인 유사도를 이용하여  두 자세 사이의 가중치 거리를 계산한다.
- 비디오 위에서 두 영상의 자세 차이를 오버레이로 표기한다.

![2D자세분석](https://github.com/olrlobt/OX-POSE/assets/99643732/8208428e-90b0-43f3-bfa2-502fe22db2c4)


### 영상 자세 3D 비교

![7 3D분석](https://github.com/olrlobt/OX-POSE/assets/99643732/d62c811a-d0b3-4830-b9fc-0f935721173f)


### 가장 비슷한 자세 찾기

- **LCSubstr([Longest Common Substring](https://www.geeksforgeeks.org/longest-common-substring-dp-29/))** 알고리즘을 이용하여 두 영상에서 가장 비슷한 자세를 찾아 이동한다.

![전체자세 (1)](https://github.com/olrlobt/OX-POSE/assets/99643732/b91e5efa-d418-46a0-a81d-a842fa9a1d95)


<br><br>

---

## 기타 구현 화면

### 2D 캔버스 끄고 키기

![5 2D캔버스](https://github.com/olrlobt/OX-POSE/assets/99643732/0ab6a37f-0c97-4cb2-9ae5-0c8c3436957b)


### 영상 이동

![3 영상이동](https://github.com/olrlobt/OX-POSE/assets/99643732/7909fcd7-cadb-437e-9875-8e8042ed05d0)


### 캔버스 표기, 표기 해제 자세 분석


![5 2D캔버스 (1)](https://github.com/olrlobt/OX-POSE/assets/99643732/14f099ea-3691-4eec-9538-c14a7ccda3c2)


---

### 현재 자세 비교

- 두 영상에서 현재 자세의 유사도를 로그로 출력한다.

![Untitled (74)](https://github.com/olrlobt/OX-POSE/assets/99643732/aa577ca1-0d63-4fbb-8ec8-e9ac7da01b78)

```python
03:58:33 INFO  index = 0   consine = 0.999833069445869
03:58:33 INFO  index = 1   consine = 0.999893285971672
03:58:33 INFO  index = 2   consine = 0.9998933121142322
03:58:33 INFO  index = 3   consine = 0.9998967243887652
03:58:33 INFO  index = 4   consine = 0.999908998830449
03:58:33 INFO  index = 5   consine = 0.9999113901251429
03:58:33 INFO  index = 6   consine = 0.9999149250714463
03:58:33 INFO  index = 7   consine = 0.9997738435780764
03:58:33 INFO  index = 8   consine = 0.9999299359649373
03:58:33 INFO  index = 9   consine = 0.9996821357232817
03:58:33 INFO  index = 10   consine = 0.9997868836808683
03:58:33 INFO  index = 11   consine = 0.9998916150735325
03:58:33 INFO  index = 12   consine = 0.9998796930933705
03:58:33 INFO  index = 13   consine = 0.9895599080289101
03:58:33 INFO  index = 14   consine = 0.9822946672485332
03:58:33 INFO  index = 15   consine = 0.9634070738228799
03:58:33 INFO  index = 16   consine = 0.9535542704268294
03:58:33 INFO  index = 17   consine = 0.9574236716940878
03:58:33 INFO  index = 18   consine = 0.9469899543592669
03:58:33 INFO  index = 19   consine = 0.95640538424222
03:58:33 INFO  index = 20   consine = 0.9467056702645741
03:58:33 INFO  index = 21   consine = 0.9614552694477486
03:58:33 INFO  index = 22   consine = 0.9513249296330809
03:58:33 INFO  index = 23   consine = 0.9969387068140517
03:58:33 INFO  index = 24   consine = 0.9968004945355536
03:58:33 INFO  index = 25   consine = 0.9934090709138634
03:58:33 INFO  index = 26   consine = 0.9817407394855612
03:58:33 INFO  index = 27   consine = 0.9952033412674897
03:58:33 INFO  index = 28   consine = 0.9873870513653208
03:58:33 INFO  index = 29   consine = 0.9955595311775821
03:58:33 INFO  index = 30   consine = 0.9872676289821054
03:58:33 INFO  index = 31   consine = 0.9946937342397866
03:58:33 INFO  index = 32   consine = 0.9780208555678469
```

---

### 관련 포스팅

[![[Spring boot] 카카오 포즈 / RestTemplate으로 Kakao Pose API 호출하기](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/46)](https://olrlobt.tistory.com/46)
[![[Pose Estimation] 다양한 Pose Estimation API 비교와 정리](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/49)](https://olrlobt.tistory.com/49)
[![[Spring boot] 카카오 포즈 / RestTemplate으로 Kakao Pose API 호출하기](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/50)](https://olrlobt.tistory.com/50)
[![[Pose Estimation] YOLOv5, MediaPipe로 Multi Pose 구현 시도해보기](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/51)](https://olrlobt.tistory.com/51)
[![[Spring boot] FFmpeg로 영상 배속 설정하기](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/53)](https://olrlobt.tistory.com/53)
[![[Pose Estimation] Mediapipe Pose 분석 결과 3D grid로 렌더링하기](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/54)](https://olrlobt.tistory.com/54)
[![[Pose Estimation] Mediapipe Pose 3D grid에 새로운 축을 적용하고, 새로운 축으로 좌표 변환하기](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/55)](https://olrlobt.tistory.com/55)
[![[Pose Estimation] Mediapipe 2배속 분석과 분석 결과 중간 부족한 프레임 채우기](https://blogwidget.com/api/fix?theme=w&url=https://olrlobt.tistory.com/56)](https://olrlobt.tistory.com/56)
