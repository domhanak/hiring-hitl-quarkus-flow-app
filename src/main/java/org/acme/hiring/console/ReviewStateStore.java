package org.acme.hiring.console;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.hiring.domain.CVAnalyzerReview;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Collection;

@ApplicationScoped
public class ReviewStateStore {

    @Inject
    ObjectMapper mapper;

    // A simple in-memory store for pending reviews
    // Key: candidateId, Value: CVAnalyzerReview
    private final Map<String, CVAnalyzerReview> pending = new ConcurrentHashMap<>();

    @Incoming("reviews-ready-in")
    public CompletionStage<Void> onEvent(Message<String> msg) {
        System.out.println(">>> Console Logic: Captured message with payload:\n " + msg.getPayload());
        try {
            // 1. Manually parse the full CloudEvent JSON
            String payload = msg.getPayload();
            JsonNode root = mapper.readTree(payload);

            // 2. Filter by the CloudEvent 'type' field in the JSON body
            String type = root.path("type").asText();

            if ("org.acme.hiring.review.ready".equals(type)) {
                // 3. Extract the 'data' node and map it to our POJO
                JsonNode dataNode = root.path("data");
                CVAnalyzerReview review = mapper.treeToValue(dataNode, CVAnalyzerReview.class);

                if (review != null && review.getCandidateId() != null) {
                    System.out.println(">>> Console Logic: Captured " + review.getCandidateId());
                    pending.put(review.getCandidateId(), review);
                }
            }
            msg.ack();
        } catch (Exception e) {
            System.err.println(">>> Console Error: " + e.getMessage());
            // It's a good practice to still ack to avoid blocking the partition
            return msg.ack();
        }

        return msg.ack();
    }

    public Collection<CVAnalyzerReview> getPending() {
        return pending.values();
    }

    public void remove(String id) {
        pending.remove(id);
    }
}