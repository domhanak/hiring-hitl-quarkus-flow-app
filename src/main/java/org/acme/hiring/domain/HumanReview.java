package org.acme.hiring.domain;

public record HumanReview(String candidateId, ReviewStatus status, String comments, String flowInstanceId) {

    public HumanReview(String candidateId, ReviewStatus status, String comments, String flowInstanceId) {
        this.candidateId = candidateId;
        this.status = status;
        this.comments = comments;
        this.flowInstanceId = flowInstanceId;
    }
}
