package com.dercio.algonated_scales_service.verticles.runner.demo;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoOptions {
    private String algorithm;
    private List<Double> weights;
    private int iterations;
    private double temperature;
    private double coolingRate;
    private int delta;
    private int restarts;
}
