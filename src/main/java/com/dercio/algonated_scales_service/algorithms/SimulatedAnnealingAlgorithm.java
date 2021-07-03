package com.dercio.algonated_scales_service.algorithms;

import com.dercio.algonated_scales_service.random.UniformRandomGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class SimulatedAnnealingAlgorithm implements Algorithm {

    private final UniformRandomGenerator randomGenerator = new UniformRandomGenerator();
    private final List<List<Integer>> solutions = new ArrayList<>();
    private Double optionalTemp;
    private Double optionalCR;

    @Override
    public Solution run(List<Double> weights, int iterations) {
        log.info("Running SA");

        double temperature = Optional.ofNullable(optionalTemp).orElse(1000.0);
        double coolingRate = Optional.ofNullable(optionalCR).orElse(calcCR(temperature, iterations));

        Solution finalSolution = new ScalesSolution(weights.size());

        for (var i = 0; i < iterations; i++) {
            finalSolution = calculateNewSolution(weights, temperature, finalSolution);
            temperature = coolingRate * temperature;
            solutions.add(finalSolution.getSolution());
        }

        return finalSolution;
    }

    @Override
    public List<List<Integer>> getSolutions() {
        return solutions;
    }


    public SimulatedAnnealingAlgorithm setOptionalTemp(double optionalTemp) {
        this.optionalTemp = optionalTemp;
        return this;
    }

    public SimulatedAnnealingAlgorithm setOptionalCR(double optionalCR) {
        this.optionalCR = optionalCR;
        return this;
    }

    private Solution calculateNewSolution(List<Double> weights, double temperature, Solution finalSolution) {
        var temporarySolution = finalSolution.copy();
        temporarySolution.makeSmallChange();
        double temporarySolutionFitness = temporarySolution.calculateFitness(weights);

        double finalSolutionFitness = finalSolution.calculateFitness(weights);

        if (temporarySolutionFitness > finalSolutionFitness) {
            double changeProbability = acceptanceFunction(temporarySolutionFitness, finalSolutionFitness, temperature);

            if (changeProbability > randomGenerator.generateDouble(0, 1)) {
                finalSolution = temporarySolution.copy();
            }

        } else {
            finalSolution = temporarySolution.copy();
        }

        return finalSolution;
    }

    private double acceptanceFunction(double newFitness, double oldFitness, double temperature) {
        double delta = Math.abs(oldFitness - newFitness);
        delta = -1 * delta;
        return Math.exp(delta / temperature);
    }

    private double calcCR(double temperature, int nIterations) {
        var tIter = 0.001;
        double power = 1.0 / nIterations;
        double tValue = tIter / temperature;

        return Math.pow(tValue, power);
    }

}
