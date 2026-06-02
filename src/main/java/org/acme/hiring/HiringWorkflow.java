package org.acme.hiring;

import java.util.Collection;
import java.util.List;

import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.*;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.fluent.func.FuncWorkflowBuilder;
import io.serverlessworkflow.impl.TaskContextData;
import io.serverlessworkflow.impl.WorkflowContextData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.hiring.agents.CVAnalyzerAgent;
import org.acme.hiring.domain.CVAnalyzerReview;
import org.acme.hiring.domain.HumanReview;
import org.acme.hiring.domain.ReviewStatus;
import org.acme.hiring.domain.db.CVAnalysisResult;


@ApplicationScoped
public class HiringWorkflow extends Flow {

    @Inject
    CVAnalyzerAgent analyzerAgent;

    @Override
    public Workflow descriptor() {
        return FuncWorkflowBuilder
                .workflow("hiring-process")
                .tasks(
                        // Analyze the input of the hiring process - CV Data ( TODO: Update to use CVData.class )
                        // Export the output to context
                        agent("cvAnalyzer", analyzerAgent::analyze, String.class)
                                .exportAs((CVAnalyzerReview c) -> c),

                        // Emit a JSON and export the input to workflow context CVAnalyzerReview
                        emitJson("readyForHumanReview", "org.acme.hiring.review.ready", CVAnalyzerReview.class)
                                .exportAs(
                                        (CVAnalyzerReview payload,
                                         WorkflowContextData wfcd,
                                         TaskContextData tcd) ->
                                                tcd.input().as(CVAnalyzerReview.class).orElseThrow(),
                                        CVAnalyzerReview.class),

                        // Listen for and event fired when the Human Review completes
                        listen("waitHumanReview",
                                        // Extend by instance ID to differentiate events
                                        toOne(consumed("org.acme.hiring.review.done")
                                                      .extensionByInstanceId("flowinstanceid")))
                                        .outputAs((Collection<HumanReview> c) -> (c != null && !c.isEmpty())
                                                ? c.iterator().next() : "{ \"human_review_error\" : \"\"}"),

                        withContext("persistResult",
                                    (HumanReview listenTaskOutput, WorkflowContextData wfData) -> {
                            CVAnalyzerReview aiReview = wfData.context().as(CVAnalyzerReview.class).orElseThrow();

                            if (listenTaskOutput == null) {
                                throw new IllegalStateException("Workflow state is missing data." +
                                                                " Keys available: " + wfData.context().asMap().get().keySet());
                            }

                            // 2. Prepare the AI reasons for the DB (comma-separated string)
                            String flattenedReasons = String.join("; ", aiReview.getReasons());

                            // 3. Create and save the entity
                            CVAnalysisResult finalRecord = new CVAnalysisResult(
                                    listenTaskOutput.flowInstanceId(),
                                    aiReview.getReviewStatus().toString(),
                                    flattenedReasons,
                                    listenTaskOutput.status() == ReviewStatus.APPROVED,
                                    listenTaskOutput.comments()
                            );

                            io.quarkus.narayana.jta.QuarkusTransaction.requiringNew().run(finalRecord::persist);

                            System.out.println(">>> [Database] Final Result Saved for Candidate: " + finalRecord.candidateId);
                            return listenTaskOutput;
                        }, HumanReview.class)
                )
                .build();
    }
}