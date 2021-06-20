package com.dercio.algonated_scales_service.verticles.runner;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExecutionResult {
    private final List<Integer> solution;
    private final List<List<Integer>> solutions;
    private final boolean isSuccess;
    private final String errorMessage;
    private final long timeElapsed;
}
