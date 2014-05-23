package se.sunet.mm.service.api.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by lundberg on 2014-05-21.
 */

public class RestException extends WebApplicationException {

    public RestException (Response.Status status, String message) {
        super(Response.status(status)
                .entity(new ErrorBean(status.getStatusCode(), message).toJson())
                .type(MediaType.APPLICATION_JSON)
                .build());
    }

    public RestException (Exception exception) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorBean(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getMessage()).toJson())
                .type(MediaType.APPLICATION_JSON)
                .build());
    }
}
