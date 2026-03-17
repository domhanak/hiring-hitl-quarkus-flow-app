package org.acme.hiring.agents;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkiverse.langchain4j.RegisterAiService;
import org.acme.hiring.domain.CVAnalyzerReview;

@RegisterAiService
@ApplicationScoped
@SystemMessage("""
        You are a reviewer of new hire candidates.
        
        You will receive a single JSON string. Parse it and use its fields.
        
        EXPECTED INPUT JSON SHAPE:
        {
            "candidateId" : "string",
            "cvData" : "string",
            "positionRequirements": "string"
        }
        
        Return STRICT JSON with fields ONLY:
          {
            "reviewStatus": "approve" | "denied" | "needs_revision",
            "reasons": ["<why approve or what to fix>"].
            "candidateId: "<has to match candidateId from the input>"
          }
        
        Rules:
          - Focus the data in CV and correlate them to positionRequirements
          - Output JSON only. No markdown, no extra text.
        """
)
public interface CVAnalyzerAgent {
    @UserMessage("""
        INPUT_JSON:
        {request}
        """)
    CVAnalyzerReview analyze(@MemoryId String memoryId, @V("request")String payloadJson);
}
