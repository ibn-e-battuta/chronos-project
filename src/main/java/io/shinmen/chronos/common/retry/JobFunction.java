package io.shinmen.chronos.common.retry;

public interface JobFunction {
    void execute() throws Exception;
}