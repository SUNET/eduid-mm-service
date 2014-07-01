package se.gov.minameddelanden.common.exception;

import static java.util.logging.Level.*;

import java.util.logging.Level;

public enum ApplicationErrorCode implements ErrorCode {
	//
	//  NAME									CODE	LOGLEVEL	DESCRIPTION
	//

	//Min error Code - do not use
	MIN(										5000, 				"Unknown error"),
	
	INVALID_INPUT(								5001, 				"Invalid input"),
	ACCESS_DENIED(								5002, 				"Access denied"),
	OBJECT_NOT_FOUND(							5003, 				"Object not found"),
	JOB_NOT_COMPLETE(							5004, 				"Job not complete"),
	MAX_EXCEEDED(								5005,				"Max exceeded"),
	ACCOUNT_IS_REFERENCED(						5006,				"Account is referenced"),
	OBJECT_ALREADY_EXISTS(						5007,				"Object already exists"),
	CANNOT_REMOVE_LAST_CONTACT(					5008, 	FINE,		"Can not remove last contact"),
	REGISTER_ACCOUNT_ERROR(						5009, 				"Error when registering an account."),
	REGISTER_ACCOUNT_SIGNATURE_ERROR(			5010, 				"Signature in signature when registering an account."),
	ILLEGAL_STATE(								5011, 				"Illegal state"),
	SIGNATURE_ERROR(							5012, 				"Signature error"),
	ERROR_SENDING_EMAIL(						5013, 				"Error sending email."),
	ERROR_SENDING_SMS(							5013, 				"Error sending sms."),
	UNKNOWN_SENDER(								5014,				"Sender is not registered as authorized sender."),	
	FILTER_ERROR(								5015,				"Filter error"),
	NULL_VALUE(									5016,				"Unhandled null value"),
	SPAR_ERROR(									5017,				"Error getting info from Spar."),	
	BOLAGSVERKET_ERROR(							5018,				"Error getting info from Bolagsverket."),
	UNSUPPORTED_MIME_TYPE(						5019,				"Content has a mime type that is not supported."),
	FORETAGSFORMEN_STODS_EJ(					6004, 				"The company form is not supported."),
	DISPATCHER_NOT_APPROVED(				    6005, 				"The dispatcher is not approved."),
	PROTECTED_IDENTITY(							6006,   			"Specified person has protected identity."),
	NOT_EXIST_IDENTITY(							6007,   			"Specified person dose not exist."),
	TDC_ERROR(									6008,				"Error when sending SMS to TDC service."),	

	//Web errors
	WEB_ERROR(									7000, 				"Unknown web error"),
	WEB_FLOW_ILLEGAL_STATE(						7001,	FINE, 		"Web flow is in an illegal state"),
	WEB_SIGNATURE_ERROR(						7002,	FINE, 		"Signature error"),
	WEB_ABORT_SIGN(								7003,	FINE, 		"Signing was aborted"),
	WEB_CREATE_ACCOUNT_REGISTER(				7004,	FINE, 		"Error when calling register in create account flow."),
	WEB_DELETE_ACCOUNT_DEREGISTER(				7005,	FINE, 		"Error when calling deregister in delete account flow."),
	WEB_ERROR_COMMUNICATION_RECIPIENT(			7006,	FINE, 		"Error when calling Recipient services."),
	WEB_ERROR_COMMUNICATION_AUTHORITY(			7007,	FINE, 		"Error when calling Authority services."),
	WEB_ERROR_COMMUNICATION_EXTERN(				7008,	FINE, 		"Error when calling External services."),
	WEB_ERROR_COMMUNICATION_PROFILE(			7009,	FINE, 		"Error when calling Profile services."),
	WEB_ERROR_COMMUNICATION_SERVICE(			7009,	FINE, 		"Error when calling Service services."),
	WEB_ACCESS_DENIED_COMPANY(					7010,	FINE, 		"The current user does not access to a specific company."),
	WEB_ILLEGAL_STATE(							7011,	FINE, 		"The web application is in an illegal state"),
	WEB_ILLEGAL_ARGUMENT(						7012,	FINE, 		"Illegal argument"),
	WEB_ACCOUNT_DOES_NOT_EXIST(					7013,	FINE, 		"Account does not exist"),
	WEB_CREATE_CONSENT_REGISTER(				7014,	FINE, 		"Error when calling register1 in create consent flow."),
	WEB_DELETE_CONSENT_ERROR(					7015,	FINE, 		"Error when calling cancel in delete consent flow."),
	SEARCH_ERROR(								8000,	FINE,		"Unknown search error"),
	INSUFFICIENT_SEARCH_QUERY(					8001,	FINE,		"The search query is insufficient."),

	//API errors
	SYSTEM_ACCOUNT_DOES_NOT_EXIST(				8002,   FINE,		"Account does not exist."),	
	SYSTEM_USER_NOT_AUTHORIZED(					8003,   FINE,		"The user is not authorized."),	
	SYSTEM_SERVICE_PROVIDER_DOES_NOT_EXIST(		8004,   FINE,   	"Service provider does not exist."),

	//Max error code - do not use
	MAX(										9999,				"Unknown error");
	
	
	private final int value;
	private final Level logLevel;
	private final String description;
	
	ApplicationErrorCode(int v, Level logLevel, String description) {
        this.value = v;
        this.logLevel = logLevel;
        this.description = description;
    }
	
	ApplicationErrorCode(int v, String description) {
        this.value = v;
        this.logLevel = WARNING;
        this.description = description;
    }

    public int value() {
        return value;
    }
    
    public Level getLogLevel(){
    	return logLevel;
    }
    
    public String getDescription(){
    	return  description;
    }
}
