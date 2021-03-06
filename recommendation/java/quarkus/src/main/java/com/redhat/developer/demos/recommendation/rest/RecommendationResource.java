package com.redhat.developer.demos.recommendation.rest;

import java.io.ByteArrayInputStream;
import javax.json.Json;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jboss.logging.Logger;

@Path("/")
public class RecommendationResource {

    private static final String RESPONSE_STRING_FORMAT = "recommendation v3 from '%s': %d\n";

    private final Logger logger = Logger.getLogger(getClass());

    /**
     * Counter to help us see the lifecycle
     */
    private int count = 0;

    /**
     * Flag for throwing a 503 when enabled
     */
    private boolean misbehave = false;

    public RecommendationResource()
    {
        try {
            misbehave = Boolean.parseBoolean(System.getenv().getOrDefault("MISBEHAVE","false"));
        }
        catch(Exception e)
        {
            logger.info(String.format("Error reading MISBEHAVE environment variable: %s", e.getMessage()));
        }
    }

    private static final String HOSTNAME = parseContainerIdFromHostname(
            System.getenv().getOrDefault("HOSTNAME", "unknown"));

    static String parseContainerIdFromHostname(String hostname) {
        return hostname.replaceAll("recommendation-v\\d+-", "");
    }



    @GET
    public Response getRecommendations() {
        count++;
        logger.info(String.format("recommendation request from %s: %d", HOSTNAME, count));

        // timeout();
        logger.debug("recommendation service ready to return");
        if (misbehave) {
            return doMisbehavior();
        }
        return Response.ok(String.format(RESPONSE_STRING_FORMAT, HOSTNAME, count)).build();
    }

    private void timeout() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            logger.info("Thread interrupted");
        }
    }

    private Response doMisbehavior() {
        logger.debug(String.format("Misbehaving %d", count));
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(String.format("recommendation misbehavior from '%s'\n", HOSTNAME)).build();
    }

    @GET
    @Path("/misbehave")
    public Response flagMisbehave() {
        this.misbehave = true;
        logger.debug("'misbehave' has been set to 'true'");
        return Response.ok("Following requests to / will return a 503\n").build();
    }

    @GET
    @Path("/behave")
    public Response flagBehave() {
        this.misbehave = false;
        logger.debug("'misbehave' has been set to 'false'");
        return Response.ok("Following requests to / will return 200\n").build();
    }

}