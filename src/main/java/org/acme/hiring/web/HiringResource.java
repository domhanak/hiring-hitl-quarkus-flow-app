package org.acme.hiring.web;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.serverlessworkflow.impl.WorkflowInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.hiring.HiringWorkflow;
import org.acme.hiring.domain.HiringRequest;

@Path("/hiring")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HiringResource {

    @Inject
    HiringWorkflow hiringWorkflow;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/newHire")
    public Response newHire(HiringRequest request) throws JsonProcessingException {
        if (request.candidateId() == null || request.candidateId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "candidateId is required"))
                    .build();
        }

        if (request.cvData() == null || request.cvData().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "cvData is required"))
                    .build();
        }

        final String payload = objectMapper.writeValueAsString(request);
        final WorkflowInstance instance = hiringWorkflow.instance(payload);

        instance.start();

        return Response.accepted(Map.of("instanceId", instance.id())).build();
    }
}
