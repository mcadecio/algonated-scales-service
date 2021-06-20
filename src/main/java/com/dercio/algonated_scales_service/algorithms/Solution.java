package com.dercio.algonated_scales_service.algorithms;

import java.util.List;

public interface Solution {
    double calculateFitness(List<Double> weights);

    void makeSmallChange();

    List<Integer> getSolution();

    Solution copy();
}
