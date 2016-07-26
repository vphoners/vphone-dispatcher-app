package io.vphone.vphonedispatcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by mohsen on 7/26/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class BackendControllerTest {

    @Mock
    RestTemplate restTemplate;
    String url = "http://here.com";
    String device = "mmm";


    @Test
    public void testDispatch() throws Exception {
        BackendController b = new BackendController(restTemplate, url, device);

        VPhoneSMS sms = new VPhoneSMS();
        sms.setId(123);
        sms.setSmsbody("test");
        sms.setSmsfrom("1234");
        sms.setSmstimestamp("now");
        b.dispatch(sms);

        ArgumentCaptor<HttpEntity> m = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(eq(url), m.capture(), Mockito.<Class<Object>>any());

        HttpHeaders headers = m.getValue().getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());

        Message msg = (Message) m.getValue().getBody();
        assertEquals(sms.getSmsfrom(), msg.getFrom());
        assertEquals(sms.getSmstimestamp(), msg.getTimestamp());
        assertEquals(sms.getSmsbody(), msg.getBody());
        assertEquals(device, msg.getDevice());
    }

    @Test
    public void testDispatchFailure() throws Exception {
        BackendController b = new BackendController(restTemplate, url, device);
        when(restTemplate.postForObject(eq(url), any(), Mockito.<Class<Object>>any()))
                .thenThrow(new ResourceAccessException("error"));

        VPhoneSMS sms = new VPhoneSMS();
        sms.setId(123);
        sms.setSmsbody("test");
        sms.setSmsfrom("1234");
        sms.setSmstimestamp("now");
        boolean ret = b.dispatch(sms);
        assertEquals(false, ret);
    }

    @Test
    public void testNoDevice() throws Exception {
        BackendController b = new BackendController(restTemplate, url, null);

        VPhoneSMS sms = new VPhoneSMS();
        sms.setId(123);
        sms.setSmsbody("test");
        sms.setSmsfrom("1234");
        sms.setSmstimestamp("now");
        boolean r = b.dispatch(sms);

        assertEquals(false, r);

    }

}