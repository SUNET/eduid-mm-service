package se.gov.minameddelanden.common.exception;

public class ApplicationException extends ErrorCodeException {

	private static final long serialVersionUID = 4995380344170476044L;	
	
//	public ApplicationException(String msg) {
//		super(ApplicationErrorCode.MIN.getLogLevel(), ApplicationErrorCode.MIN.value(), msg);
//	}
	
	public ApplicationException(ApplicationErrorCode code) {
		super(code.getLogLevel(), code, code.getDescription());
	}
	
	public ApplicationException(ApplicationErrorCode code, String msg) {
		super(code.getLogLevel(), code, code.getDescription(), msg);
	}
	
	public ApplicationException(ApplicationErrorCode code, Throwable cause) {
		super(code.getLogLevel(), code, code.getDescription(), cause);
	}
	
	public ApplicationException(ApplicationErrorCode code, String msg, Throwable cause) {
		super(code.getLogLevel(), code, code.getDescription(), msg, cause);
	}
	
}
