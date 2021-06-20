package com.dercio.algonated_scales_service.verifier;

import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ImportVerifier implements Verifier {

    private static final Pattern IMPORT_PATTERN = Pattern.compile("import (.)*;");
    private final List<String> importsAllowed;

    @Override
    public List<String> verify(String code) {
        if (importsAllowed.isEmpty()) {
            return Collections.emptyList();
        }

        return IMPORT_PATTERN.matcher(code)
                .results()
                .map(MatchResult::group)
                .filter(s -> !importsAllowed.contains(s))
                .collect(Collectors.toList());

    }
}
