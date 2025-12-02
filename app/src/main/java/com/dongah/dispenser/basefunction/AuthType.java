package com.dongah.dispenser.basefunction;

public enum AuthType {
    MAC(0),
    MEMBER(1),
    MACMEMBER(2);
    private final int value;
    AuthType(int value) {
        this.value = value;
    }
    public int value() {
        return value;
    }
}
