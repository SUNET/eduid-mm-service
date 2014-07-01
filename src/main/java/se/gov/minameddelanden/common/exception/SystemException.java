package se.gov.minameddelanden.common.exception;

public class SystemException extends ErrorCodeException {

	private static final long serialVersionUID = 7527246118569822441L;

//	public SystemException(String msg) {
//		super(SystemErrorCode.MIN.value(), msg);
//	}
	
	public SystemException(SystemErrorCode code) {
		super(code.getLogLevel(), code, code.getDescription());
	}
	
	public SystemException(SystemErrorCode code, String msg) {
		super(code.getLogLevel(), code, code.getDescription(), msg);
	}
	
	public SystemException(SystemErrorCode code, String msg, Throwable cause) {
		super(code.getLogLevel(), code, code.getDescription(), msg, cause);
	}
}
