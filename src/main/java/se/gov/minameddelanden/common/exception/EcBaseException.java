package se.gov.minameddelanden.common.exception;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for exceptions. 
 * 
 * This class makes a unified log-entry, depending on a log-level ({@link Level}), when it is created.
 * 
 * @author Anders Johansson (AKNX)
 */
public abstract class EcBaseException extends Exception {

	private static final long serialVersionUID = 6357965498020735414L;

    private static final String SEPARATOR = " - ";

    private Level logLevel;

	private Logger logger;

    /**
	 * Constructor.  
	 * 
	 * @param logLevel log-level to use when logging this exception.
	 * @param message message to set and log.
	 */
	public EcBaseException(Level logLevel, String message) {
		super(message);
		this.logLevel = logLevel;
		logMessage(message);
	}
	
	/**
	 * Constructor.  
	 * 
	 * @param logLevel log-level to use when logging this exception.
	 * @param message message to set and log.
	 * @param t cause to set.
	 */
	public EcBaseException(Level logLevel, String message, Throwable t) {
		super(message, t);
		this.logLevel = logLevel;
		logMessage(message);
	}
	
	/**
	 * Logs a message and this exception to the log on a specific log-level. 
	 * No log-entry will be made if the specific log-level is not active.
	 * 
	 * @param message
	 */
	protected void logMessage(String message) {
		if(logLevel != Level.OFF){					
			
			if(logger==null){
				logger = Logger.getLogger(getLoggerName());
			}
			
			
			if(logger.isLoggable(logLevel)){
		        
	            String cname = this.getClass().getName();
	            String method = "<init>";
	            
	            String logMessage = cname + SEPARATOR + message;
	            
	            logger.logp(logLevel,cname,method,logMessage,this);            	            
				
			}
			
		}
		
	}
	
	protected void setLogger(Logger logger) {
		this.logger = logger;
	}

	private String getLoggerName() {
		return this.getClass().getName();
	}

}
