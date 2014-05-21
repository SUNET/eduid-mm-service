package se.sunet.mm.service.api.exceptions;

import com.google.gson.Gson;

/**
 * Created by lundberg on 2014-05-21.
 */

public class ErrorBean {

    private Integer status;
    private String message;

    public ErrorBean(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
