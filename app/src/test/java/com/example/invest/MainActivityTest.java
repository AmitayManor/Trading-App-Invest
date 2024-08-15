package com.example.invest;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.example.invest.appPages.MainActivity;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class MainActivityTest {

    private MainActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(MainActivity.class).create().get();
    }

    @Test
    public void testActivityCreation() {
        assertNotNull(activity);
    }

    @Test
    public void testAlphaVantageSetup() {
        assertNotNull(activity.cfg);
        assertEquals("MESG2D7QDONF28QE", activity.apiKey);
        assertEquals(10, activity.cfg.getTimeOut());
    }

    @Test
    public void testContentViewSet() {
        assertNotNull(activity.findViewById(android.R.id.content));
    }
}