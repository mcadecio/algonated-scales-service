package com.dercio.algonated_scales_service.verticles;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum VerticleAddresses {

    SCALES_ANALYTICS_SUMMARY("scales-analytics-summary"),
    CODE_RUNNER_CONSUMER("code-runner-consumer"),
    DEMO_RUNNER_CONSUMER("demo-runner-consumer"),
    SCALES_SUBMISSION("scales-submission-consumer"),
    SCALES_DEMO("scales-demo-consumer");

    private final String address;

    @Override
    public String toString() {
        return address;
    }
}
