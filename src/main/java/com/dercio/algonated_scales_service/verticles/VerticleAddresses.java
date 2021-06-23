package com.dercio.algonated_scales_service.verticles;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum VerticleAddresses {

    SCALES_ANALYTICS_SUMMARY("scales-analytics-summary"),
    CODE_RUNNER_CONSUMER("code-runner-consumer"),
    SCALES_VERTICLE("scales-verticle");

    private final String address;

    @Override
    public String toString() {
        return address;
    }
}
