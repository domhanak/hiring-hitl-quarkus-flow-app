package org.acme.hiring.domain.db;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class CVAnalysisResult extends PanacheEntity {
    public String candidateId;
    public String aiStatus;
    public String aiReasons; // Storing the list of reasons as a flattened string or JSON
    public boolean humanValidated;
    public String humanComments;
    public LocalDateTime completedAt;

    public CVAnalysisResult() {}

    public CVAnalysisResult(String id, String aiStatus, String aiReasons, boolean validated, String comments) {
        this.candidateId = id;
        this.aiStatus = aiStatus;
        this.aiReasons = aiReasons;
        this.humanValidated = validated;
        this.humanComments = comments;
        this.completedAt = LocalDateTime.now();
    }
}