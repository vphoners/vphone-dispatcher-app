package io.vphone.vphonedispatcher;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by mh on 2016-10-28.
 */
public class ActivationRequest {
    @JsonProperty("phone_number")
    String phoneNumber;

    @JsonProperty("forward_phone_number")
    String forwardPhoneNumber;
    String email;

    public ActivationRequest() {
    }

    public ActivationRequest(String phoneNumber, String forwardPhoneNumber, String email) {
        this.phoneNumber = phoneNumber;
        this.forwardPhoneNumber = forwardPhoneNumber;
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getForwardPhoneNumber() {
        return forwardPhoneNumber;
    }

    public void setForwardPhoneNumber(String forwardPhoneNumber) {
        this.forwardPhoneNumber = forwardPhoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
