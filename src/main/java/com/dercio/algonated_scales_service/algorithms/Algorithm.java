package com.dercio.algonated_scales_service.algorithms;

import com.dercio.algonated_scales_service.verticles.runner.demo.DemoRequest;
import java.util.List;

public interface Algorithm<T, D> {

    T run(D weights, int iterations);

    List<List<Integer>> getSolutions();

    static Algorithm<Solution, List<Double>> getScalesAlgorithm(DemoRequest<List<Double>> request) {
        Algorithm<Solution, List<Double>> algorithm;
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

