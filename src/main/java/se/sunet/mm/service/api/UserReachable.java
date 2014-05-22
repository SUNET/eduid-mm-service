package se.sunet.mm.service.api;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sunet.mm.service.api.exceptions.RestException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Created by lundberg on 2014-05-21.
 */


@Path("/user/reachable")
@Produces(MediaType.APPLICATION_JSON)
public class UserReachable {

    private final Logger slf4jLogger = LoggerFactory.getLogger(UserReachable.class);
    private final Gson gson = new Gson();

    public static class UserReachableRequest {
        private Integer identity_number;

        public UserReachableRequest(Integer identity_number) {
            this.identity_number = identity_number;
        }

        public Integer getIdentityNumber() {
            return identity_number;
        }

        public void validate() throws WebApplicationException {
            if (this.identity_number == null) {
                throw new RestException(Response.Status.BAD_REQUEST, "Missing \"identity_number\": <integer>");
            }
        }
    }

    public static class UserReachableResponse {

        private Boolean SenderAccepted;
        private AccountStatus AccountStatus = new AccountStatus();

        public UserReachableResponse (Boolean senderAccepted, String accountStatusType, String serviceAddress) {
            this.setSenderAccepted(senderAccepted);
            this.AccountStatus.setType(accountStatusType);
            this.AccountStatus.ServiceSupplier.setServiceAddress(serviceAddress);
        }

        public static class AccountStatus {
            private String Type;
            private ServiceSupplier ServiceSupplier = new ServiceSupplier();

            public static class ServiceSupplier {
                private String ServiceAddress;

                public void setServiceAddress(String serviceAddress) {
                    ServiceAddress = serviceAddress;
                }
            }

            public void setType(String type) {
                Type = type;
            }
        }

        public void setSenderAccepted(Boolean senderAccepted) {
            SenderAccepted = senderAccepted;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String isReachable(String json) {
        UserReachableRequest request = gson.fromJson(json, UserReachableRequest.class);
        request.validate();
        try  {
            //TODO: Get and instantiate a UserReachableResponse from MM service
            UserReachableResponse response = new UserReachableResponse(Boolean.TRUE, "Secure", "mailbox_url");
            return gson.toJson(response);
        } catch (Exception e) {
            slf4jLogger.error("Could not return UserReachableResponse", e);
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
