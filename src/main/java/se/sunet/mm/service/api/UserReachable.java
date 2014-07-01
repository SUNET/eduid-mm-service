package se.sunet.mm.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.gov.minameddelanden.schema.recipient.ReachabilityStatus;
import se.sunet.mm.service.api.exceptions.RestException;
import se.sunet.mm.service.mmclient.RecipientService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;


/**
 * Created by lundberg on 2014-05-21.
 */

@Path("/user/reachable")
@Produces(MediaType.APPLICATION_JSON)
public class UserReachable {

    private final Logger slf4jLogger = LoggerFactory.getLogger(UserReachable.class);
    private final Gson gson = new Gson();
    private final RecipientService service = new RecipientService(System.getProperty("se.sunet.mm.service.senderOrganisationNumber"));

    public static class Request {
        private String identity_number;

        public Request(String identity_number) {
            this.identity_number = identity_number;
        }

        public String getIdentityNumber() {
            return identity_number;
        }

        public void validate() throws WebApplicationException {
            if (this.identity_number == null) {
                throw new RestException(javax.ws.rs.core.Response.Status.BAD_REQUEST, "Missing \"identity_number\": \"<string>\"");
            }
        }
    }

    public static class Response {

        private Boolean SenderAccepted;
        private AccountStatus AccountStatus = new AccountStatus();

        public Response(Boolean senderAccepted, String recipient, String accountStatusType, String serviceAddress) {
            this.setSenderAccepted(senderAccepted);
            this.AccountStatus.setRecipient(recipient);
            this.AccountStatus.setType(accountStatusType);
            this.AccountStatus.ServiceSupplier.setServiceAddress(serviceAddress);
        }

        public Response(ReachabilityStatus status) {
            this.setSenderAccepted(status.isSenderAccepted());
            this.AccountStatus.setRecipient(status.getAccountStatus().getRecipientId());
            this.AccountStatus.setType(status.getAccountStatus().getType().value());
            if (status.getAccountStatus().getServiceSupplier() != null) {
                this.AccountStatus.ServiceSupplier.setServiceAddress(
                    status.getAccountStatus().getServiceSupplier().getServiceAdress());
            }
        }

        public static class AccountStatus {
            private String RecipientId;
            private String Type;
            private ServiceSupplier ServiceSupplier = new ServiceSupplier();

            public static class ServiceSupplier {
                private String ServiceAddress = "";

                public void setServiceAddress(String serviceAddress) {
                    ServiceAddress = serviceAddress;
                }

                public String getServiceAddress() {
                    return ServiceAddress;
                }
            }

            public void setType(String type) {
                Type = type;
            }

            public String getType() {
                return Type;
            }

            public String getRecipient() {
                return RecipientId;
            }

            public void setRecipient(String recipient) {
                this.RecipientId = recipient;
            }

            public Response.AccountStatus.ServiceSupplier getServiceSupplier() {
                return ServiceSupplier;
            }
        }

        public void setSenderAccepted(Boolean senderAccepted) {
            SenderAccepted = senderAccepted;
        }

        public Boolean getSenderAccepted() {
            return SenderAccepted;
        }

        public Response.AccountStatus getAccountStatus() {
            return AccountStatus;
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String isReachable(String json) {
        try {
            Request request = gson.fromJson(json, Request.class);
            slf4jLogger.info("API user reachable request received");
            request.validate();
            ReachabilityStatus status = service.isReachable(request.getIdentityNumber());
            slf4jLogger.info("mmclient response received");
            Response response = new Response(status);
            slf4jLogger.info("API user reachable response created");
            return gson.toJson(response);
        } catch (RestException e) {
            throw e;
        } catch (NullPointerException e) {
            String message = "Received empty POST data";
            slf4jLogger.error(message, e);
            throw new RestException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR, message);
        } catch (JsonSyntaxException e) {
            slf4jLogger.error(e.getMessage());
            throw new RestException(javax.ws.rs.core.Response.Status.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            slf4jLogger.error("Could not return UserReachable.Response", e);
            throw new RestException(e);
        }
    }
}
