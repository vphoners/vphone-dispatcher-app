package io.vphone.vphonedispatcher;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

public class ActivationActivity extends AppCompatActivity {

    private int permRequestId = 0;

    ProgressDialog progress = null;
    private Thread callerIdActivationThread = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(havePermission(Manifest.permission.READ_PHONE_STATE)) {
            fillInMyPhoneNumber();
        }

        autoPhoneNoFormat((EditText) findViewById(R.id.phone_number));
        autoPhoneNoFormat((EditText) findViewById(R.id.phone_number_to_divert_to));
    }

    private void autoPhoneNoFormat(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    try {
                        ((EditText)view).setText(formatPhoneNo(((EditText)view).getText().toString()));
                    } catch (NumberParseException e) {
                    }
                }
            }
        });
    }

    public void activate(View view) {

        String phoneNumber = getPhoneNumber();

        if(!Patterns.PHONE.matcher(phoneNumber).matches() || phoneNumber.length() < 10) {
            Toast.makeText(this, "Invalid phone number for this phone.", Toast.LENGTH_SHORT).show();
            return;
        }

        String forwardTo = getForwardPhoneNumber();

        if(!Patterns.PHONE.matcher(forwardTo).matches() || forwardTo.length() < 10) {
            Toast.makeText(this, "Invalid phone number to forward calls to.", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = getEmail();
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if(!havePermission(Manifest.permission.CALL_PHONE)) {
            Toast.makeText(this, "Need calling phones permission to set up call forwarding", Toast.LENGTH_LONG).show();
            return;
        }

        if(!havePermission(Manifest.permission.RECEIVE_SMS)) {
            Toast.makeText(this, "Need read sms permission to set up SMS forwarding", Toast.LENGTH_LONG).show();
            return;
        }

        progress = new ProgressDialog(this);

        progress.setTitle("Activation Progress");
        progress.setMessage("Setting up call forwarding ...");
        progress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(null != callerIdActivationThread) {
                    callerIdActivationThread.interrupt();
                }
            }
        });
        progress.setButton(DialogInterface.BUTTON_POSITIVE, "Skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        progress.setCancelable(false);
        progress.show();
        Button skipButton = progress.getButton(DialogInterface.BUTTON_POSITIVE);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activateCallerId();
            }
        });

        setupForwarding(forwardTo);
    }

    private void setupForwarding(String forwardTo) {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) ==
                PackageManager.PERMISSION_GRANTED) {

            PhoneCallListener phoneListener = new PhoneCallListener();
            TelephonyManager telephonyManager = (TelephonyManager)
                    this.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);

            Intent intentCallForward = new Intent(Intent.ACTION_CALL);
            Uri mmiCode = Uri.fromParts("tel", "**21*" + forwardTo + "#", "#");
            intentCallForward.setData(mmiCode);
            startActivity(intentCallForward);
        }
    }

    @NonNull
    private String getEmail() {
        EditText emailField = (EditText) findViewById(R.id.email);
        return emailField.getText().toString().trim();
    }

    @NonNull
    private String getForwardPhoneNumber() {
        EditText forwardToField = (EditText) findViewById(R.id.phone_number_to_divert_to);
        return forwardToField.getText().toString().trim();
    }

    @NonNull
    private String getPhoneNumber() {
        EditText phoneNumberField = (EditText) findViewById(R.id.phone_number);
        return phoneNumberField.getText().toString().trim();
    }

    private String formatPhoneNo(String phoneNo) throws NumberParseException {
        PhoneNumberUtil pnu = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber pn = pnu.parse(phoneNo, "SE");
        return pnu.format(pn, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Map<String, Integer> perms = new HashMap<>();
        for(int i=0; i<permissions.length; i++) {
            perms.put(permissions[i], grantResults[i]);
        }

        if(perms.get(Manifest.permission.READ_PHONE_STATE) != null &&
                perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            fillInMyPhoneNumber();
        }

    }

    private void fillInMyPhoneNumber() {
        TelephonyManager tMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = tMgr.getLine1Number();
        if (phoneNumber != null && phoneNumber.length() > 0) {
            EditText phoneNumberField = (EditText) findViewById(R.id.phone_number);
            phoneNumberField.setText(phoneNumber);
        }
    }

    private boolean havePermission(String perm) {
        if(ActivityCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.RECEIVE_SMS
                    }, 0);
            return false;
        }
        return true;
    }


    private class PhoneCallListener extends PhoneStateListener
    {
        Boolean originalState = null;
        @Override
        public void onCallForwardingIndicatorChanged(final boolean cfi) {
            // if called for the first time, this is the state before "our" forwarding activation
            if(originalState == null) {
                originalState = cfi;
                return;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(cfi) {
                        progress.setMessage("Call forwarding succeeded. Validating phone number ...");
                        activateCallerId();
                    }else {
                        progress.dismiss();
                        Toast.makeText(ActivationActivity.this, "Call forwarding failed", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void activateCallerId() {
        progress.getButton(ProgressDialog.BUTTON_POSITIVE).setEnabled(false);
        callerIdActivationThread = new Thread(new PhoneNumberValidator(new ActivationRequest(getPhoneNumber(), getForwardPhoneNumber(), getEmail())));
        callerIdActivationThread.start();
    }

    private class PhoneNumberValidator implements Runnable {

        private ActivationRequest activationRequest;
        String activationsUrl = "https://vphone.io/api/activations";

        public PhoneNumberValidator(ActivationRequest activationRequest) {

            this.activationRequest = activationRequest;
        }

        @Override
        public void run() {
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity request = new HttpEntity(activationRequest, headers);

            try {
                // Execute HTTP Post Request
                final ActivationResponse activationResponse = restTemplate.postForObject(activationsUrl, request, ActivationResponse.class);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setMessage("You'll shortly receive a call, enter this number for activation:\n\n" +
                                activationResponse.getValidationCode());
                    }
                });
                HttpEntity pollingRequest = new HttpEntity(headers);
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(activationsUrl)
                        .queryParam("device", activationResponse.getDevice());
                restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                    @Override
                    protected boolean hasError(HttpStatus statusCode) {
                        return false;
                    }
                });
                while(true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ActivationActivity.this, "Activation interrupted", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }

                    ResponseEntity<String> isActivated = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, pollingRequest, String.class);
                    if (isActivated.getStatusCode() == HttpStatus.OK) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                enableServiceWithDeviceKey(activationResponse.getDevice());
                                Intent i = new Intent(ActivationActivity.this, SettingsActivity.class);
                                startActivity(i);
                                finish();
                            }
                        });
                        break;
                    }
                }

            }catch(final RestClientException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(e instanceof HttpClientErrorException) {
                            if(((HttpClientErrorException) e).getStatusCode() == HttpStatus.CONFLICT) {
                                Toast.makeText(ActivationActivity.this, "Your phone number is already registered, please contact support.", Toast.LENGTH_LONG).show();
                            }

                        }else {
                            Toast.makeText(ActivationActivity.this, "Error communicating with vphone: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        progress.dismiss();
                    }
                });
            }finally {
                callerIdActivationThread = null;
            }
        }
    }

    private void enableServiceWithDeviceKey(String deviceKey) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor e = prefs.edit();
        e.putString(getString(R.string.device_key), deviceKey);
        e.putBoolean(getString(R.string.service_enabled), true);
        e.apply();

        startService(new Intent(this, DispatcherService.class));
    }

    @Override
    protected void onStop() {
        if(null != callerIdActivationThread) {
            callerIdActivationThread.interrupt();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        // disabled
    }
}

