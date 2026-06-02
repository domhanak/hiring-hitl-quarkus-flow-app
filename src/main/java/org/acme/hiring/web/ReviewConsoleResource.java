package org.acme.hiring.web;

import java.net.URI;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.reactive.messaging.ce.OutgoingCloudEventMetadata;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.hiring.console.ReviewStateStore;
import org.acme.hiring.domain.HumanReview;
import org.acme.hiring.domain.ReviewStatus;
import org.acme.hiring.domain.db.CVAnalysisResult;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@Path("/console")
public class ReviewConsoleResource {

    @Inject
    Template hiringConsole;

    @Inject
    ReviewStateStore stateStore;

    @Inject
    ObjectMapper mapper; // Inject Jackson Mapper

    @Inject
    @Channel("hiring-reviews-out") // To signal back 'review.done'
    Emitter<String> reviewEmitter;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showConsole() {
        String html = hiringConsole.data("candidates", stateStore.getPending())
                                   .data("history", CVAnalysisResult.list("order by completedAt desc"))
                                   .render();
        return Response.ok(html).build();
    }

    @POST
    @Path("/decide/{id}")
    public Response submitDecision(@PathParam("id") String id,
                                   @FormParam("approved") boolean approved,
                                   @FormParam("comments") String comments,
                                   @HeaderParam("X-Flow-Instance-Id") String instanceId) throws JsonProcessingException {
        HumanReview review = new HumanReview(id,
                                             approved ? ReviewStatus.APPROVED : ReviewStatus.DENIED,
                                             comments != null && !comments.isEmpty() ? comments : "No comments provided.",
                                             instanceId);

        byte[] body = mapper.writeValueAsBytes(review);

        System.out.println(">>>> Submitting decision to workflow: " + instanceId);
        System.out.println(">>>> Human Review sent to worfklow is: " + review);

        // 2. MANUALLY build the "Structured" CloudEvent JSON
        // This puts the specversion exactly where the engine is looking for it.
        ObjectNode ce = mapper.createObjectNode();
        ce.put("specversion", "1.0");
        ce.put("id", java.util.UUID.randomUUID().toString());
        ce.put("source", "hiring-console");
        ce.put("type", "org.acme.hiring.review.done");
        ce.put("businesskey", id); // Required for Flow to find the instance
        ce.put("flowinstanceid", instanceId);
        ce.set("data", mapper.valueToTree(review));

        String jsonPayload = mapper.writeValueAsString(ce);

        System.out.println(">>>> JsonPayload sent to workflow is: " + jsonPayload);

        // 3. Send to Kafka
        reviewEmitter.send(jsonPayload);

        stateStore.remove(id);
        return Response.ok("Human decision sent with success")
                .header("HX-Redirect", "/console")
                .build();
    }
}
