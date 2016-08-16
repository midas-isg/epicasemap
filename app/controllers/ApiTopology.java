package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.wordnik.swagger.annotations.*;
import gateways.database.VizTopologyDao;
import gateways.webservice.AlsDao;
import interactors.ClientRule;
import models.entities.VizTopology;
import models.exceptions.NotFound;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.ws.rs.PathParam;

import static controllers.ApiAid.toJsonNode;

@Api(value = "/vizs/topojson",
        description = "Endpoints for TopoJSON linking to Visualization")
public class ApiTopology extends Controller {
    private static final String TopoJsonContentType = "application/geo+json";

    @VisibleForTesting
    public static String linkToViz(long vizId, JsonNode bodyJson) {
        final WSResponse response = toTopology(bodyJson);
        assureResponseIsValidJson(response);
        final String result = response.getBody();
        save(vizId, result);
        return result;
    }

    private static void save(long vizId, String topoJson) {
        final VizTopology data = wireVizTopology(vizId);
        data.setTopoJson(topoJson);
        new VizTopologyDao(JPA.em()).update(data.getId(), data);
    }

    private static VizTopology wireVizTopology(long vizId) {
        VizTopology data = readByVizId(vizId);
        if (data == null) {
            data = new VizTopology();
            data.setVizId(vizId);
            data.setTopoJson("");
            new VizTopologyDao(JPA.em()).create(data);
        }
        return data;
    }

    private static VizTopology readByVizId(long vizId) {
        return new VizTopologyDao(JPA.em()).readByVizId(vizId);
    }

    private static WSResponse toTopology(JsonNode json) {
        ClientRule topo = new AlsDao().makeTopoJsonClient();
        return topo.post(json);
    }

    private static void assureResponseIsValidJson(WSResponse response) {
        if (response.asJson() == null)
            throw new RuntimeException("LS didn't return a valid JSON");
    }

    @ApiOperation(httpMethod = "GET", nickname = "read",
            value = "Returns the TopoJSON by Visualization ID")
    @ApiResponses({ @ApiResponse(code = OK, message = "Success") })
    @Transactional
    public static Result read(long vizId){
        final VizTopology vizTopology = readByVizId(vizId);
        if (vizTopology == null)
            throw new NotFound(VizTopology.class.getSimpleName() + ": not found where Visualization ID = " + vizId);
        return ok(vizTopology.getTopoJson()).as(TopoJsonContentType);
    }

     @ApiOperation(httpMethod = "POST", nickname = "link",
            value = "Link the Visualization with TopoJSON",
            notes = "This endpoint links a Visualization to TopoJSON generated " +
                    "from submitted JSON object in body ")
    @ApiResponses({
            @ApiResponse(code = OK, message = "Success")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(required = true, value = "{\"gids\":[1, 2]}",
                    paramType = "body")
    })
    @Transactional
    public static Result postLinkToViz(
            @ApiParam(value = "ID of the Visualization", required = true)
            @PathParam("id") long vizId) {
        final JsonNode bodyJson = toJsonNode(request());
        return ok(linkToViz(vizId, bodyJson)).as(TopoJsonContentType);

    }
}