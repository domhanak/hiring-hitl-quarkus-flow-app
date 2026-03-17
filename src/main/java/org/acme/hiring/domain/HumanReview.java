package org.acme.hiring.domain;

public record HumanReview(String candidateId, ReviewStatus status, String comments) {

    public HumanReview(String candidateId, ReviewStatus status, String comments) {
        this.candidateId = candidateId;
        this.status = status;
        this.comments = comments;
    }
}
