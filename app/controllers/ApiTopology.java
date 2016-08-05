package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.wordnik.swagger.annotations.*;
import gateways.database.SeriesTopologyDao;
import gateways.webservice.AlsDao;
import interactors.ClientRule;
import models.entities.SeriesTopology;
import models.exceptions.NotFound;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.libs.ws.WSResponse;
import play.mvc.Controller;
import play.mvc.Result;

import javax.ws.rs.PathParam;

import static controllers.ApiAid.toJsonNode;

@Api(value = "/series/topojson",
        description = "Endpoints for TopoJSON linking to Series")
public class ApiTopology extends Controller {
    private static final String TopoJsonContentType = "application/geo+json";

    @VisibleForTesting
    public static String linkToSeries(long seriesId, JsonNode bodyJson) {
        final WSResponse response = toTopology(bodyJson);
        assureResponseIsValidJson(response);
        final String result = response.getBody();
        save(seriesId, result);
        return result;
    }

    private static void save(long seriesId, String topoJson) {
        final SeriesTopology data = wireSeriesTopology(seriesId);
        data.setTopoJson(topoJson);
        new SeriesTopologyDao(JPA.em()).update(data.getId(), data);
    }

    private static SeriesTopology wireSeriesTopology(long seriesId) {
        SeriesTopology data = readBySeriesId(seriesId);
        if (data == null) {
            data = new SeriesTopology();
            data.setSeriesId(seriesId);
            data.setTopoJson("");
            new SeriesTopologyDao(JPA.em()).create(data);
        }
        return data;
    }

    private static SeriesTopology readBySeriesId(long seriesId) {
        return new SeriesTopologyDao(JPA.em()).readBySeriesId(seriesId);
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
    @Transactional
    public static Result read(long seriesId){
        final SeriesTopology seriesTopology = readBySeriesId(seriesId);
        if (seriesTopology == null)
            throw new NotFound(SeriesTopology.class.getSimpleName() + ": not found where Series ID = " + seriesId);
        return ok(seriesTopology.getTopoJson()).as(TopoJsonContentType);
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
    @Transactional
    public static Result postLinkToSeries(
            @ApiParam(value = "ID of the Series", required = true)
            @PathParam("id") long seriesId) {
        final JsonNode bodyJson = toJsonNode(request());
        return ok(linkToSeries(seriesId, bodyJson)).as(TopoJsonContentType);

    }
}