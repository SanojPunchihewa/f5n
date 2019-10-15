package com.mobilegenomics.f5n.dto;

import java.io.Serializable;

public enum State implements Serializable {

    NORMAL("Normal"),
    CONNECT("Connect"),
    DISCONNECT("Disconnect"),
    IDLE("Idle"),
    PENDING("Pending"),
    SUCCESS("Success"),
    FAILURE("Failure"),
    ACK("Acknowledgement");

    private String state;

    State(String state) {
        this.state = state;
    }

    private String getState(State state) {
        return state.state;
    }
}
