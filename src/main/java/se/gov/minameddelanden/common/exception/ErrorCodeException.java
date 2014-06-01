package se.gov.minameddelanden.common.exception;

import java.util.logging.Level;

public class ErrorCodeException extends EcBaseException {

	private static final long serialVersionUID = 3969250777257537427L;
	private static final String MESSAGE_FORMAT_WITH_MESSAGE = "ERROR CODE: [%d %s] - ERROR CODE DESCRIPTION: [%s] - MESSAGE: [%s]";
	private static final String MESSAGE_FORMAT_WITHOUT_MESSAGE = "ERROR CODE: [%d %s] - ERROR CODE DESCRIPTION: [%s]";

	private ErrorCode code;
	private String codeDescription;

	public ErrorCodeException(Level logLevel, ErrorCode code, String codeDescription) {
		super(logLevel, getFormatedMessage("", code, codeDescription));
		this.code = code;
		this.codeDescription = codeDescription;
	}

	public ErrorCodeException(Level logLevel, ErrorCode code, String codeDescription, String msg) {
		super(logLevel, getFormatedMessage(msg, code, codeDescription));
		this.code = code;
		this.codeDescription = codeDescription;
	}

	public ErrorCodeException(Level logLevel, ErrorCode code, String codeDescription, String msg, Throwable cause) {
		super(logLevel, getFormatedMessage(msg, code, codeDescription), cause);
		this.code = code;
		this.codeDescription = codeDescription;
	}

	public ErrorCodeException(Level logLevel, ErrorCode code, String codeDescription, Throwable cause) {
		super(logLevel, getFormatedMessage("", code, codeDescription), cause);
		this.code = code;
		this.codeDescription = codeDescription;
	}

	public static String getFormatedMessage(String msg, ErrorCode code, String codeDescription) {
		String formatedMessage;

		if (msg != null && msg != "") {
			formatedMessage = String.format(MESSAGE_FORMAT_WITH_MESSAGE, code.value(), code, codeDescription, msg);
		}
		else {
			formatedMessage = String.format(MESSAGE_FORMAT_WITHOUT_MESSAGE, code.value(), code, codeDescription);
		}

		return formatedMessage;
	}

	public ErrorCode getCode() {
		return code;
	}

	public String getCodeDescription() {
		return codeDescription;
	}
}
