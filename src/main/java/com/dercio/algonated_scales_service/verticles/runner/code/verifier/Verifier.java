package com.dercio.algonated_scales_service.verticles.runner.code.verifier;

import java.util.List;

public interface Verifier {

    List<String> verify(String code);
}
