package com.combo.oxpose.mediapose;

public enum KeyPointName {
    NOSE(0, -1, "코"),
    LEFT_SHOULDER(11, 0, "왼쪽 어깨"),
    RIGHT_SHOULDER(12, 1, "오른쪽 어깨"),
    LEFT_ELBOW(13, 2, "왼쪽 팔꿈치"),
    RIGHT_ELBOW(14, 3, "오른쪽 팔꿈치"),
    LEFT_WRIST(15, 4, "왼쪽 손"),
    RIGHT_WRIST(16, 5, "오른쪽 손"),
    LEFT_HIP(23, 6, "왼쪽 골반"),
    RIGHT_HIP(24, 7, "오른쪽 골반"),
    LEFT_KNEE(25, 8, "왼쪽 무릎"),
    RIGHT_KNEE(26, 9, "오른쪽 무릎"),
    LEFT_ANKLE(27, 10, "왼쪽 발"),
    RIGHT_ANKLE(28, 11, "오른쪽 발");

    private final int value;
    private final int correctIndex;
    private final String name;

    KeyPointName(int value, int correctIndex, String name) {
        this.value = value;
        this.correctIndex = correctIndex;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static KeyPointName correctValueOf(int correctIndex) {
        for (KeyPointName keyPointName : KeyPointName.values()) {
            if (keyPointName.correctIndex == correctIndex) {
                return keyPointName;
            }
        }
        throw new IllegalArgumentException("Invalid correctIndex: " + correctIndex);
    }
}
