package com.dongah.dispenser.rfcard;

public interface RfCardReaderListener {
    void onRfCardDataReceive(String cardNum, boolean value);
}
