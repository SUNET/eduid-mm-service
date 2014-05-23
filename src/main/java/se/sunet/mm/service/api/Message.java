package se.sunet.mm.service.api;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sunet.mm.service.api.exceptions.RestException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

/**
 * Created by lundberg on 2014-05-23.
 */

@Path("/message")
@Produces(MediaType.APPLICATION_JSON)
public class Message {

    private final Logger slf4jLogger = LoggerFactory.getLogger(Message.class);
    private final Gson gson = new Gson();

    public class SendRequest {

        String recipient;
        String subject;
        String message;
        String language;
        String content_type;

        public SendRequest(String recipient, String subject, String message, String language, String content_type) {
            this.recipient = recipient;
            this.subject = subject;
            this.message = message;
            this.language = language;
            this.content_type = content_type;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getSubject() {
            return subject;
        }

        public String getMessage() {
            return message;
        }

        public String getLanguage() {
            return language;
        }

        public String getContentType() {
            return content_type;
        }

        public void validate() throws WebApplicationException {
            ArrayList<String> errors = new ArrayList<>();
            if (this.recipient == null) {
                errors.add("Missing \"recipient\": \"<string>\"");
            }
            if (this.subject == null) {
                errors.add("Missing \"subject\": \"<string>\"");
            }
            if (this.message == null) {
                errors.add("Missing \"message\": \"<string>\"");
            }
            if (this.language == null) {
                errors.add("Missing \"language\": \"<string>\"");
            }
            if (this.content_type == null) {
                errors.add("Missing \"content_type\": \"<string>\"");
            }
            if (!errors.isEmpty()) {
                throw new RestException(javax.ws.rs.core.Response.Status.BAD_REQUEST, errors.toString());
            }
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/send")
    public String send(String json) {
        try {
            SendRequest request = gson.fromJson(json, SendRequest.class);
            slf4jLogger.info("API send message request received");
            request.validate();
            // TODO: Send message with mmclient and return message ID
            return gson.toJson(request);
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
            slf4jLogger.error("Could not return Message.SendResponse", e);
            throw new RestException(e);
        }

    }
}
