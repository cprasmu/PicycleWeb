package com.cprasmu.picycle.io.btle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadParser
implements BluezConnector.CharacteristicsListener {
    private static final Logger log = LoggerFactory.getLogger((Class)ReadParser.class);
    int i = 0;

    @Override
    public boolean onSuccess(String address, byte[] rawMessage) {
        return false;
    }

    @Override
    public boolean onFailed(String address, String msg, Exception e) {
        return false;
    }

    @Override
    public boolean onRetry(String address, int count, Exception e) {
        return true;
    }
}