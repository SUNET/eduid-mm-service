package se.gov.minameddelanden.common.exception;

import static java.util.logging.Level.*;

import java.util.logging.Level;

public enum SystemErrorCode implements ErrorCode {

	MIN(					0,			"Min value"),
	INVALID_INPUT(			1,			"Invalid input"),
	INVALID_CONFIG(			2,			"Invalid config"),
	CERTIFICATE_ERROR(		3,			"Certificate error"),
	SIGN_NOUN_ERROR(		4,			"Error when generating noun"),
	NOT_AUTHORIZED(			5,			"Unauthorized access"),
	NON_UNIQUE(			    6,			"Unique constraint violation"),
	BOLAGSVERKET_ERROR(		7,			"Error when getting information from Bolagsverket."),	
	MAX(					4999,		"Max value");
	
	private final int value;
	private final Level logLevel;
	private final String description;
	
	SystemErrorCode(int v, Level logLevel, String description) {
        this.value = v;
        this.logLevel = logLevel;
        this.description = description;
    }
	
	SystemErrorCode(int v, String description) {
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
