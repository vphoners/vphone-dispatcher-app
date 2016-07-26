package io.vphone.vphonedispatcher;

import android.util.Log;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by mohsen on 7/26/16.
 */
public class BackendController {

    public final static String SERVICE_URL = "https://vphone.io/api/sms";
    private RestTemplate restTemplate;
    private String serviceUrl;

    public BackendController(RestTemplate restTemplate, String serviceUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
    }

    public BackendController() {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        serviceUrl = SERVICE_URL;
    }

    public boolean dispatch(VPhoneSMS sms) {
        Message message = new Message(sms.getSmsfrom(),
                sms.getSmstimestamp(),
                sms.getSmsbody(),
                "mohsen");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity(message, headers);

        try {
            restTemplate.postForObject(serviceUrl, request, Void.class);
        } catch (RestClientException e) {
            Log.i("BackendController", "Could not send the message");
            Log.d("BackendController", "Error", e);
            return false;
        }
        return true;
    }

}
