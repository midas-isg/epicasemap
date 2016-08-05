package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.wordnik.swagger.annotations.*;
import gateways.webservice.AlsDao;
import interactors.ClientRule;
import models.exceptions.NotFound;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.ws.rs.PathParam;
import java.util.HashMap;
import java.util.Map;

import static controllers.ApiAid.toJsonNode;

@Api(value = "/series/topojson",
        description = "Endpoints for TopoJSON linking to Series")
public class ApiTopology extends Controller {
    private static final String TopoJsonContentType = "application/geo+json";

    private static Result linkToSeries(long seriesId, JsonNode bodyJson) {
        final WSResponse response = toTopology(bodyJson);
        assureResponseIsValidJson(response);
        final String result = response.getBody();
        new TopoJsonDao().save(seriesId, result);
        return ok(result).as(TopoJsonContentType);
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
            value = "Returns the TopoJSON by Series ID")
    @ApiResponses({ @ApiResponse(code = OK, message = "Success") })
    public static Result read(long seriesId){
        return ok(new TopoJsonDao().read(seriesId)).as(TopoJsonContentType);
    }

    @ApiOperation(httpMethod = "POST", nickname = "link",
            value = "Link the Series with TopoJSON",
            notes = "This endpoint links a Series to TopoJSON generated " +
                    "from submitted JSON object in body ")
    @ApiResponses({
            @ApiResponse(code = OK, message = "Success")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(required = true, value = "{\"gids\":[1, 2]}",
                    paramType = "body")
    })
    public static Result postLinkToSeries(
            @ApiParam(value = "ID of the Series", required = true)
            @PathParam("id") long seriesId) {
        final JsonNode bodyJson = toJsonNode(request());
        return linkToSeries(seriesId, bodyJson);
    }

    static class TopoJsonDao {
        private static Map<Long, String> hackingMap = new HashMap<>();

        public String read(long seriesId) {
            final String json = hackingMap.get(seriesId);
            if (json == null) throw new NotFound();
            return json;
        }

        public void save(long seriesId, String json) {
            hackingMap.put(seriesId, json);
        }
    }
}