package se.gov.minameddelanden.common.exception;

import java.util.logging.Level;

public interface ErrorCode {

    public int value() ;

    public Level getLogLevel() ;

    public String getDescription();
}
