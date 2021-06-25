package com.dercio.algonated_scales_service.verticles.analytics.calculator;


import com.dercio.algonated_scales_service.algorithms.ScalesSolution;
import com.dercio.algonated_scales_service.algorithms.Solution;

import java.util.List;

public class ScalesFitnessCalculator implements Calculator<List<Double>> {

    @Override
    public double calculate(List<Double> data, List<Integer> solution) {
        Solution scalesSolution = new ScalesSolution(solution);

        return scalesSolution.calculateFitness(data);
    }
}
