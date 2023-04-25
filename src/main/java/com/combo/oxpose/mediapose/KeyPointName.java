package com.combo.oxpose.mediapose;

public enum KeyPointName {
    NOSE(0, "코"),
    LEFT_SHOULDER(11, "왼쪽 어깨"),
    RIGHT_SHOULDER(12, "오른쪽 어깨"),
    LEFT_ELBOW(13, "왼쪽 팔꿈치"),
    RIGHT_ELBOW(14, "오른쪽 팔꿈치"),
    LEFT_WRIST(15, "왼쪽 손"),
    RIGHT_WRIST(16, "오른쪽 손"),
    LEFT_HIP(23, "왼쪽 골반"),
    RIGHT_HIP(24, "오른쪽 골반"),
    LEFT_KNEE(25, "왼쪽 무릎"),
    RIGHT_KNEE(26, "오른쪽 무릎"),
    LEFT_ANKLE(27, "왼쪽 발"),
    RIGHT_ANKLE(28, "오른쪽 발");

    private int value;
    private String name;

    KeyPointName(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static KeyPointName valueOf(int value) {
        for (KeyPointName keyPointName : KeyPointName.values()) {
            if (keyPointName.value == value) {
                return keyPointName;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
