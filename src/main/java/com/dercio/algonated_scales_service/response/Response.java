package com.dercio.algonated_scales_service.response;

import com.dercio.algonated_scales_service.verticles.analytics.CodeRunnerSummary;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.Json;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@ToString
public class Response {

    private boolean isSuccess;
    private String consoleOutput;
    private List<Integer> result = Collections.emptyList();
    private Object data = Collections.emptyList();
    private CodeRunnerSummary summary = new CodeRunnerSummary();
    private List<List<Integer>> solutions = Collections.emptyList();

    @JsonProperty("isSuccess")
    public boolean isSuccess() {
        return isSuccess;
    }

    public Response setSuccess(boolean success) {
        isSuccess = success;
        return this;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }

    public Response setConsoleOutput(String consoleOutput) {
        this.consoleOutput = consoleOutput;
        return this;
    }

    public List<Integer> getResult() {
        return result;
    }

    public Response setResult(List<Integer> result) {
        this.result = result;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Response setData(Object data) {
        this.data = data;
        return this;
    }

    public CodeRunnerSummary getSummary() {
        return summary;
    }

    public Response setSummary(CodeRunnerSummary summary) {
        this.summary = summary;
        return this;
    }

    public List<List<Integer>> getSolutions() {
        return solutions;
    }

    public Response setSolutions(List<List<Integer>> solutions) {
        this.solutions = solutions;
        return this;
    }

    public String encode() {
        return Json.encode(this);
    }
}
