package com.redhat.developer.demos.customer.rest;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.lang.model.util.ElementScanner6;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class CustomerResource {

    private static final String RESPONSE_STRING_FORMAT = "customer => %s\n";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ConfigProperty(name = "destination.service", defaultValue="RecommendationService")
    String targetService;

    @Inject
    @RestClient
    RecommendationService recommendationService;

    @Inject
    @RestClient
    PreferenceService preferenceService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCustomer() {

        try {
            String response;

            if (targetService.equals("RecommendationService")) {
                response = recommendationService.getRecommendation().trim();
            }
            else if (targetService.equals("PreferenceService"))
            {
                response = preferenceService.getPreference().trim();
            }
            else
            {
                return Response
                    .status(Response.Status.NOT_IMPLEMENTED)
                    .entity(String.format(RESPONSE_STRING_FORMAT, 
                            String.format("Service %s is unknown", targetService)))
                    .build();
            }

            return Response.ok(String.format(RESPONSE_STRING_FORMAT, response)).build();
         } catch (WebApplicationException ex) {
            Response response = ex.getResponse();
            logger.warn(String.format("Non HTTP 20x trying to get the response from %s service: %s", targetService, response.getStatus()));
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(String.format(RESPONSE_STRING_FORMAT,
                            String.format("Error: %d - %s", response.getStatus(), response.readEntity(String.class)))
                    )
                    .build();
        } catch (ProcessingException ex) {
            logger.warn(String.format("Exception trying to get the response from %s service.", targetService), ex);
            return Response
                    .status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(String.format(RESPONSE_STRING_FORMAT, ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage()))
                    .build();
        }
    }

}