package com.dercio.algonated_scales_service.verticles.runner;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
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
