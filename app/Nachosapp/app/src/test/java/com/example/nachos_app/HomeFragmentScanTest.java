package com.example.nachos_app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.nachos_app.ui.home.HomeFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/**
 * Robolectric tests that exercise the QR scan handling flow inside HomeFragment.
 * We use Roboelectric so the QR code scanner can run on the JVM without an emulator/android device.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 34)
public class HomeFragmentScanTest {

    private Method handleScanResult;

    /**
     * Prepares reflective access to the scan handler and resets shared toast state.
     */
    @Before
    public void setUp() throws Exception {
        handleScanResult = HomeFragment.class.getDeclaredMethod("handleScanResult", String.class);
        handleScanResult.setAccessible(true);
        ShadowToast.reset();
    }

    /**
     * Verifies a valid QR code launches the event details screen with the parsed ID.
     */
    @Test
    public void scanValidQr_launchesEventDetailsActivity() throws Exception {
        TestHomeFragment fragment = attachTestFragment();

        handleScanResult.invoke(fragment, "event://eventABC123");

        Intent startedIntent = fragment.lastStartedIntent;
        assertNotNull("Valid QR scans should navigate to event details", startedIntent);
        assertEquals(EventDetailsActivity.class.getName(),
                startedIntent.getComponent().getClassName());
        assertEquals("eventABC123", startedIntent.getStringExtra("eventId"));
    }

    /**
     * Verifies an invalid QR code only shows the error toast and does not navigate.
     */
    @Test
    public void scanInvalidQr_showsErrorToastAndDoesNotNavigate() throws Exception {
        TestHomeFragment fragment = attachTestFragment();

        handleScanResult.invoke(fragment, "https://malicious-link");

        assertNull("Invalid QR scans should not trigger navigation", fragment.lastStartedIntent);
        assertEquals("Invalid QR code", ShadowToast.getTextOfLatestToast());
    }

    /**
     * Builds a hosting activity, attaches the test fragment, and returns it for interaction.
     */
    private TestHomeFragment attachTestFragment() {
        FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class)
                .setup()
                .get();

        TestHomeFragment fragment = new TestHomeFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, fragment, "home")
                .commitNow();
        return fragment;
    }

    /**
     * Test double for HomeFragment that captures navigation and stubs dependencies.
     */
    public static class TestHomeFragment extends HomeFragment {
        Intent lastStartedIntent;

        /**
         * Captures the intent instead of starting a real activity.
         */
        @Override
        public void startActivity(Intent intent) {
            lastStartedIntent = intent;
        }

        /**
         * Stubs dependencies and returns a minimal view for fragment creation.
         */
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            injectFakeViewModel();
            return new View(inflater.getContext());
        }

        /**
         * Replaces the fragment's view model with a mock to avoid real data work.
         */
        private void injectFakeViewModel() {
            try {
                Field homeVmField = HomeFragment.class.getDeclaredField("homeViewModel");
                homeVmField.setAccessible(true);
                homeVmField.set(this, Mockito.mock(com.example.nachos_app.ui.home.HomeViewModel.class));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Unable to stub HomeViewModel", e);
            }
        }
    }
}
