package com.mss301.brand.dto;

// [TODO-5.2] Also used by the idempotent unassign operation.
public record AssignmentResponse(Integer watchId, Integer brandId,
                                 Integer previousBrandId, String status) {
}
