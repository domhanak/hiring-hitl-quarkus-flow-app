package org.acme.hiring.domain;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CVAnalyzerReview {

    @JsonProperty("reviewStatus")
    private ReviewStatus reviewStatus;

    /** Required non-empty array of reasons */
    @JsonProperty("reasons")
    private List<String> reasons;

    @JsonProperty("candidateId")
    private String candidateId;

    @JsonProperty("flowinstanceid")
    private String flowInstanceId;

    public CVAnalyzerReview() {
    }

    public CVAnalyzerReview(ReviewStatus reviewStatus, List<String> reasons) {
        this.reviewStatus = reviewStatus;
        this.reasons = reasons;
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getFlowInstanceId() {
        return flowInstanceId;
    }

    public void setFlowInstanceId(String flowInstanceId) {
        this.flowInstanceId = flowInstanceId;
    }

    @Override
    public String toString() {
        return "CVAnalyzerReview{" +
                "reviewStatus=" + reviewStatus +
                ", reasons=" + reasons +
                ", candidateId:" + candidateId +
                ", flowInstanceId:" + flowInstanceId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CVAnalyzerReview that)) {
            return false;
        }
        return reviewStatus == that.reviewStatus
                && Objects.equals(reasons, that.reasons)
                && Objects.equals(candidateId, that.candidateId)
                && Objects.equals(flowInstanceId, that.flowInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewStatus, reasons, candidateId, flowInstanceId);
    }
}
