package com.dercio.algonated_scales_service.runner;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class CodeOptions {
    private String className;
    private String packageName;
    private String methodToCall;
    private int iterations;
    private List<String> importsAllowed;
    private List<String> illegalMethods;
    private List<Double> weights;
    private String code;
}
