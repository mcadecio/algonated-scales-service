package com.dercio.algonated_scales_service.verticles.analytics;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
@EqualsAndHashCode
public class AnalyticsRequest {
    private final long timeElapsed;
    private final int iterations;
    private final List<Double> weights;
    private final List<Integer> solution;
}
