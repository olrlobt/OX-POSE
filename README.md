# OX-POSE
2023.02.23 ~. (진행중)

Pose Estimation을 이용한 실시간 자세 비교 분석 서비스

SpringBoot 2.7.4 / Java 11 / Maven



<br><br>

# 진행상황 (4/10)
<br>

## 분석 옵션
동영상의 재생 바가 움직임에따라, 분석 결과를 매칭해서 보여준다.

![ezgif com-crop (10)](https://user-images.githubusercontent.com/99643732/230785561-b8e54347-4026-4d4b-aa07-b62c4e9deba3.gif)

<br>
## Grid 회전
3D Grid의 경우 회전도 가능하다.

![ezgif com-crop (12)](https://user-images.githubusercontent.com/99643732/230786095-ec618450-274d-4684-862e-24627ae0a73a.gif)



<br><br><br>

## 코사인 유사도, 가중치 거리

자세를 비교하기 위한 방법으로, GooGle Mirror에서 사용된 코사인 유사도와 가중치 거리를 측정하는 방법을 사용한다.

코사인 유사도의 경우, 값이 높을수록 같다는 의미이며,
가중치 거리의 경우, 값이 낮을수록 같다는 의미이다.

<br><br>
### 같은 포즈 비교
예로, 아래의 한 포즈를 이용결과.

<img width = "400" src= "https://user-images.githubusercontent.com/99643732/230785757-125f068e-773e-44dc-a514-f1df4cc343c8.png">


```
01:52:18 INFO  weight = 0.18498925395474147
01:52:18 INFO  index = 0   cosineSimilarity = 0.9948450954454642
01:52:18 INFO  index = 1   cosineSimilarity = 0.9962068372456278
01:52:18 INFO  index = 2   cosineSimilarity = 0.9959829167781977
01:52:18 INFO  index = 3   cosineSimilarity = 0.9959617005557702
01:52:18 INFO  index = 4   cosineSimilarity = 0.9958366053471873
01:52:18 INFO  index = 5   cosineSimilarity = 0.9960187025910687
01:52:18 INFO  index = 6   cosineSimilarity = 0.9958374472852484
01:52:18 INFO  index = 7   cosineSimilarity = 0.996606109451536
01:52:18 INFO  index = 8   cosineSimilarity = 0.9985566152075641
01:52:18 INFO  index = 9   cosineSimilarity = 0.995007539619975
01:52:18 INFO  index = 10   cosineSimilarity = 0.9953177803015198
01:52:18 INFO  index = 11   cosineSimilarity = 0.9999742920827257
01:52:18 INFO  index = 12   cosineSimilarity = 0.9999785798414685
01:52:18 INFO  index = 13   cosineSimilarity = 0.9917273751418623
01:52:18 INFO  index = 14   cosineSimilarity = 0.9963214454862415
01:52:18 INFO  index = 15   cosineSimilarity = 0.9904276952583538
01:52:18 INFO  index = 16   cosineSimilarity = 0.9861348556950206
01:52:18 INFO  index = 17   cosineSimilarity = 0.9855179860463672
01:52:18 INFO  index = 18   cosineSimilarity = 0.9774377468386818
01:52:18 INFO  index = 19   cosineSimilarity = 0.9820336097735572
01:52:18 INFO  index = 20   cosineSimilarity = 0.9743683092758642
01:52:18 INFO  index = 21   cosineSimilarity = 0.9890005244320448
01:52:18 INFO  index = 22   cosineSimilarity = 0.9834883251052694
01:52:18 INFO  index = 23   cosineSimilarity = 0.9669813020411194
01:52:18 INFO  index = 24   cosineSimilarity = 0.9677562889685638
01:52:18 INFO  index = 25   cosineSimilarity = 0.921865542277539
01:52:18 INFO  index = 26   cosineSimilarity = 0.9794387904142872
01:52:18 INFO  index = 27   cosineSimilarity = 0.9632974485457512
01:52:18 INFO  index = 28   cosineSimilarity = 0.9490590866155811
01:52:18 INFO  index = 29   cosineSimilarity = 0.9661340067787552
01:52:18 INFO  index = 30   cosineSimilarity = 0.9481449445002431
01:52:18 INFO  index = 31   cosineSimilarity = 0.9807573871711347
01:52:18 INFO  index = 32   cosineSimilarity = 0.9165229884728486

```

<br><br><br>

### 다른 포즈 비교

위의 두 영상에서, 오른쪽 영상을 좌우 반전 시켜서 비교를 진행했다.
그 결과, 발의 방향이 바뀌었기 때문에 결과가 아래와 같다.


<img width = "400" src= "https://user-images.githubusercontent.com/99643732/230786230-57546e1a-f36d-475b-a106-85447b0b841e.png">

```
02:03:25 INFO  weight = 0.36561105428078083
02:03:25 INFO  index = 0   cosineSimilarity = 0.9980912315348671
02:03:25 INFO  index = 1   cosineSimilarity = 0.9985883490378005
02:03:25 INFO  index = 2   cosineSimilarity = 0.9985960110850347
02:03:25 INFO  index = 3   cosineSimilarity = 0.9985826802257015
02:03:25 INFO  index = 4   cosineSimilarity = 0.9981867098823896
02:03:25 INFO  index = 5   cosineSimilarity = 0.9982140640567083
02:03:25 INFO  index = 6   cosineSimilarity = 0.9981884446652334
02:03:25 INFO  index = 7   cosineSimilarity = 0.9999748035461243
02:03:25 INFO  index = 8   cosineSimilarity = 0.9982314712514577
02:03:25 INFO  index = 9   cosineSimilarity = 0.9988802130859891
02:03:25 INFO  index = 10   cosineSimilarity = 0.9983434796462367
02:03:25 INFO  index = 11   cosineSimilarity = 0.9980320311393134
02:03:25 INFO  index = 12   cosineSimilarity = 0.9981684323364971
02:03:25 INFO  index = 13   cosineSimilarity = 0.9856531460898287
02:03:25 INFO  index = 14   cosineSimilarity = 0.9994959328955617
02:03:25 INFO  index = 15   cosineSimilarity = 0.9736738890407243
02:03:25 INFO  index = 16   cosineSimilarity = 0.9926854472377179
02:03:25 INFO  index = 17   cosineSimilarity = 0.9728758161082495
02:03:25 INFO  index = 18   cosineSimilarity = 0.9897916531855246
02:03:25 INFO  index = 19   cosineSimilarity = 0.9740645267849657
02:03:25 INFO  index = 20   cosineSimilarity = 0.9877157543478541
02:03:25 INFO  index = 21   cosineSimilarity = 0.9741288566600607
02:03:25 INFO  index = 22   cosineSimilarity = 0.9909925888717347
02:03:25 INFO  index = 23   cosineSimilarity = 0.9520060724914182
02:03:25 INFO  index = 24   cosineSimilarity = 0.9520095251127039
02:03:25 INFO  index = 25   cosineSimilarity = 0.050648040689517906 //왼 무릎
02:03:25 INFO  index = 26   cosineSimilarity = 0.17088127877486115  //오른 무릎
02:03:25 INFO  index = 27   cosineSimilarity = 0.34457673262664085  //왼 발목
02:03:25 INFO  index = 28   cosineSimilarity = 0.24547491460867368  //오른 발목
02:03:25 INFO  index = 29   cosineSimilarity = 0.3733371811159797 //왼 발뒷꿈치
02:03:25 INFO  index = 30   cosineSimilarity = 0.2606207812296051 //오른 발뒷꿈치
02:03:25 INFO  index = 31   cosineSimilarity = 0.34192801936047906  //왼 발
02:03:25 INFO  index = 32   cosineSimilarity = 0.30216975309488364  //오른 발
```
