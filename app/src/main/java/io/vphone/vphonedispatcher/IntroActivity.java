package io.vphone.vphonedispatcher;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        // Note here that we DO NOT use setContentView();

        addSlide(AppIntroFragment.newInstance("Always Accessible", "You won't need to carry this phone with you anymore, you can read " +
                "SMSs as emails and make phone calls from any connected device, including your other phone",
                R.drawable.vphone, Color.WHITE, Color.BLUE, Color.BLUE));
        addSlide(AppIntroFragment.newInstance("Activate in One Step", "Next, you will activate this phone to forward all the calls and SMSs",
                R.drawable.ic_ring_volume_black, Color.parseColor("#2196F3")));

        // OPTIONAL METHODS
        setBarColor(Color.parseColor("#2196F3"));


        showSkipButton(true);
        setProgressButtonEnabled(true);
        showStatusBar(false);
        setGoBackLock(true);

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        activate();
    }

    private void activate() {
        Intent i = new Intent(this, ActivationActivity.class);
        startActivity(i);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        activate();
    }
}
