package com.mss301.watch.service.saga;

// [TODO-6.8] Position and definition of a committed transaction.
public record CompletedStep(int stepNo, SagaStep step) { }
