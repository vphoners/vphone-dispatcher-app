package io.vphone.vphonedispatcher;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mh on 2016-10-28.
 */
public class ActivationResponse {
    private String device;

    @JsonProperty("validation_code")
    private String validationCode;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }
}
