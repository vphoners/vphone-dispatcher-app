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
    private String device;

    public BackendController(RestTemplate restTemplate, String serviceUrl, String device) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
        this.device = device;
    }

    public BackendController(String device) {
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        serviceUrl = SERVICE_URL;
        this.device = device;
    }

    public boolean dispatch(VPhoneSMS sms) {
        if(device == null) {
            // cannot dispatch without a device
            return false;
        }
        Message message = new Message(sms.getSmsfrom(),
                sms.getSmstimestamp(),
                sms.getSmsbody(),
                device);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity request = new HttpEntity(message, headers);

        try {
            restTemplate.postForObject(serviceUrl, request, Void.class);
        } catch (RestClientException e) {
            Log.i("vphone", "Could not send the message");
            Log.d("vphone", "Error", e);
            return false;
        }
        return true;
    }

}
