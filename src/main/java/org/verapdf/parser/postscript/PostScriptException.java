package org.verapdf.parser.postscript;

/**
 * @author Sergey Shemyakov
 */
public class PostScriptException extends Exception {

    public PostScriptException() {
    }

    public PostScriptException(String message) {
        super(message);
    }

    public PostScriptException(String message, Throwable cause) {
        super(message, cause);
    }
}
