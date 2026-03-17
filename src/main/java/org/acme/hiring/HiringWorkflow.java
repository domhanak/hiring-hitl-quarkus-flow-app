package org.acme.hiring;

import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.agent;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.consume;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.emitJson;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.event;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.function;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.listen;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.switchWhenOrElse;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.to;
import static io.serverlessworkflow.fluent.func.dsl.FuncDSL.withContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.flow.Flow;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.fluent.func.FuncWorkflowBuilder;
import io.serverlessworkflow.impl.TaskContextData;
import io.serverlessworkflow.impl.WorkflowContext;
import io.serverlessworkflow.impl.WorkflowContextData;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.hiring.agents.CVAnalyzerAgent;
import org.acme.hiring.domain.CVAnalyzerReview;
import org.acme.hiring.domain.HumanReview;
import org.acme.hiring.domain.ReviewStatus;
import org.acme.hiring.domain.db.CVAnalysisResult;

import java.util.Collection;
import java.util.Map;

@ApplicationScoped
public class HiringWorkflow extends Flow {

    @Inject
    CVAnalyzerAgent analyzerAgent;

    @Override
    public Workflow descriptor() {
        return FuncWorkflowBuilder
                .workflow("hiring-process")
                .tasks(
                        agent("cvAnalyzer", analyzerAgent::analyze, String.class)
                                .exportAs(".task.output"),
                        emitJson("readyForHumanReview", "org.acme.hiring.review.ready", CVAnalyzerReview.class)
                                .exportAs((Object payload, WorkflowContextData wfcd, TaskContextData tcd) -> {
                                    System.out.println(" PAYLOAD IS: " + payload);
                                    System.out.println(" WorkflowContextData IS: " + wfcd);
                                    System.out.println(" TaskContextData IS: " + tcd);
                                    return tcd.input().as(CVAnalyzerReview.class);
                                }, Object.class),
                        listen("waitHumanReview", to().one(event("org.acme.hiring.review.done")))
                                .outputAs((Collection<Object> c) -> c.iterator().next()),
                        // FINAL TASK: Persist the combined data
                        withContext("persistResult", (HumanReview listenTaskOutput, WorkflowContextData wfData) -> {
                            // 1. Fetch the AI Analysis from a previous step's output
                            System.out.println(">>>>> WFData\n:" + wfData.context());

                            CVAnalyzerReview aiReview = wfData.context().as(CVAnalyzerReview.class).orElseThrow();

                            if (listenTaskOutput == null) {
                                throw new IllegalStateException("Workflow state is missing data. Keys available: " + wfData.context().asMap().get().keySet());
                            }

                            // 2. Prepare the AI reasons for the DB (comma-separated string)
                            String flattenedReasons = String.join("; ", aiReview.getReasons());

                            // 3. Create and save the entity
                            CVAnalysisResult finalRecord = new CVAnalysisResult(
                                    listenTaskOutput.candidateId(),
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