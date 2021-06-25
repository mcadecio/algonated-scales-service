package com.dercio.algonated_scales_service.verticles.runner.code;

import lombok.Builder;
import lombok.Getter;
import org.joor.Reflect;

@Getter
@Builder
public class CompileResult {
    private Reflect compiledClass;
    private boolean isSuccess;
    private String errorMessage;
}
