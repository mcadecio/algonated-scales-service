package com.dercio.algonated_scales_service.algorithms;

import com.dercio.algonated_scales_service.verticles.runner.demo.DemoOptions;
import java.util.List;

public interface Algorithm {

    Solution run(List<Double> weights, int iterations);

    List<List<Integer>> getSolutions();

    static Algorithm getScalesAlgorithm(DemoOptions request) {
        Algorithm algorithm;
        String requestedAlgorithm = request.getAlgorithm();
        switch (requestedAlgorithm) {
            case "sa":
                algorithm = new SimulatedAnnealingAlgorithm()
                        .setOptionalCR(request.getCoolingRate())
                        .setOptionalTemp(request.getTemperature());
                break;
            case "rrhc":
                algorithm = new RandomRestartHillClimbing()
                        .setRestarts(request.getRestarts());
                break;
            case "shc":
                algorithm = new StochasticHillClimbing()
                        .setDelta(request.getDelta());
                break;
            default:
                algorithm = new RandomHillClimbingAlgorithm();
                break;
        }

        return algorithm;
    }

}

