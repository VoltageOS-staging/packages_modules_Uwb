/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.uwb.cts;

import static android.Manifest.permission.UWB_PRIVILEGED;
import static android.Manifest.permission.UWB_RANGING;
import static android.uwb.UwbManager.AdapterStateCallback.STATE_DISABLED;
import static android.uwb.UwbManager.AdapterStateCallback.STATE_ENABLED_INACTIVE;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import static com.google.common.truth.Truth.assertThat;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.UiAutomation;
import android.content.AttributionSource;
import android.content.Context;
import android.content.ContextParams;
import android.os.CancellationSignal;
import android.os.PersistableBundle;
import android.os.Process;
import android.permission.PermissionManager;
import android.platform.test.annotations.AppModeFull;
import android.util.Log;
import android.uwb.RangingReport;
import android.uwb.RangingSession;
import android.uwb.UwbManager;

import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.android.compatibility.common.util.ShellIdentityUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test of {@link UwbManager}.
 */
@SmallTest
@RunWith(AndroidJUnit4.class)
@AppModeFull(reason = "Cannot get UwbManager in instant app mode")
public class UwbManagerTest {
    private static final String TAG = "UwbManagerTest";

    private final Context mContext = InstrumentationRegistry.getContext();
    private UwbManager mUwbManager;
    private String mDefaultChipId;

    @Before
    public void setup() {
        mUwbManager = mContext.getSystemService(UwbManager.class);
        assumeTrue(UwbTestUtils.isUwbSupported(mContext));
        assertThat(mUwbManager).isNotNull();

        // Ensure UWB is toggled on.
        ShellIdentityUtils.invokeWithShellPermissions(() -> {
            if (!mUwbManager.isUwbEnabled()) {
                mUwbManager.setUwbEnabled(true);
            }
            mDefaultChipId = mUwbManager.getDefaultChipId();
        });
    }

    @Test
    public void testGetSpecificationInfo() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            PersistableBundle persistableBundle = mUwbManager.getSpecificationInfo();
            assertThat(persistableBundle).isNotNull();
            assertThat(persistableBundle.isEmpty()).isFalse();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testGetSpecificationInfoWithChipId() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            PersistableBundle persistableBundle =
                    mUwbManager.getSpecificationInfo(mDefaultChipId);
            assertThat(persistableBundle).isNotNull();
            assertThat(persistableBundle.isEmpty()).isFalse();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testGetSpecificationInfoWithInvalidChipId() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            assertThrows(IllegalArgumentException.class,
                    () -> mUwbManager.getSpecificationInfo("invalidChipId"));
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testGetSpecificationInfoWithoutUwbPrivileged() {
        try {
            mUwbManager.getSpecificationInfo();
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        }
    }

    @Test
    public void testGetSpecificationInfoWithChipIdWithoutUwbPrivileged() {
        try {
            mUwbManager.getSpecificationInfo(mDefaultChipId);
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        }
    }


    @Test
    public void testElapsedRealtimeResolutionNanos() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            assertThat(mUwbManager.elapsedRealtimeResolutionNanos() >= 0L).isTrue();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testElapsedRealtimeResolutionNanosWithChipId() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            assertThat(mUwbManager.elapsedRealtimeResolutionNanos(mDefaultChipId) >= 0L)
                    .isTrue();
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testElapsedRealtimeResolutionNanosWithInvalidChipId() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            assertThrows(IllegalArgumentException.class,
                    () -> mUwbManager.elapsedRealtimeResolutionNanos("invalidChipId"));
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testElapsedRealtimeResolutionNanosWithoutUwbPrivileged() {
        try {
            mUwbManager.elapsedRealtimeResolutionNanos();
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        }
    }

    @Test
    public void testElapsedRealtimeResolutionNanosWithChipIdWithoutUwbPrivileged() {
        try {
            mUwbManager.elapsedRealtimeResolutionNanos(mDefaultChipId);
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        }
    }

    @Test
    public void testAddServiceProfileWithoutUwbPrivileged() {
        try {
            mUwbManager.addServiceProfile(new PersistableBundle());
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        }
    }

    @Test
    public void testRemoveServiceProfileWithoutUwbPrivileged() {
        try {
            mUwbManager.removeServiceProfile(new PersistableBundle());
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        }
    }


    @Test
    public void testGetAllServiceProfilesWithoutUwbPrivileged() {
        try {
            mUwbManager.getAllServiceProfiles();
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        }
    }

    private class RangingSessionCallback implements RangingSession.Callback {
        private final CountDownLatch mCountDownLatch;

        public boolean onOpenedCalled;
        public boolean onOpenFailedCalled;
        public RangingSession rangingSession;

        RangingSessionCallback(@NonNull CountDownLatch countDownLatch) {
            mCountDownLatch = countDownLatch;
        }

        public void onOpened(@NonNull RangingSession session) {
            onOpenedCalled = true;
            rangingSession = session;
            mCountDownLatch.countDown();
        }

        public void onOpenFailed(@Reason int reason, @NonNull PersistableBundle params) {
            onOpenFailedCalled = true;
            mCountDownLatch.countDown();
        }

        public void onStarted(@NonNull PersistableBundle sessionInfo) { }

        public void onStartFailed(@Reason int reason, @NonNull PersistableBundle params) { }

        public void onReconfigured(@NonNull PersistableBundle params) { }

        public void onReconfigureFailed(@Reason int reason, @NonNull PersistableBundle params) { }

        public void onStopped(@Reason int reason, @NonNull PersistableBundle parameters) { }

        public void onStopFailed(@Reason int reason, @NonNull PersistableBundle params) { }

        public void onClosed(@Reason int reason, @NonNull PersistableBundle parameters) { }

        public void onReportReceived(@NonNull RangingReport rangingReport) { }
    }

    @Test
    public void testOpenRangingSessionWithInvalidChipId() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        RangingSessionCallback rangingSessionCallback = new RangingSessionCallback(countDownLatch);
        try {
            // Needs UWB_PRIVILEGED & UWB_RANGING permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            // Try to start a ranging session with invalid params, should fail.
            assertThrows(IllegalArgumentException.class, () -> mUwbManager.openRangingSession(
                    new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    rangingSessionCallback,
                    "invalidChipId"));
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testOpenRangingSessionWithChipIdWithBadParams() throws Exception {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        CancellationSignal cancellationSignal = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        RangingSessionCallback rangingSessionCallback = new RangingSessionCallback(countDownLatch);
        try {
            // Needs UWB_PRIVILEGED & UWB_RANGING permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            // Try to start a ranging session with invalid params, should fail.
            cancellationSignal = mUwbManager.openRangingSession(
                    new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    rangingSessionCallback,
                    mDefaultChipId);
            // Wait for the on start failed callback.
            assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(rangingSessionCallback.onOpenedCalled).isFalse();
            assertThat(rangingSessionCallback.onOpenFailedCalled).isTrue();
        } finally {
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testOpenRangingSessionWithInvalidChipIdWithBadParams() throws Exception {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        CancellationSignal cancellationSignal = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        RangingSessionCallback rangingSessionCallback = new RangingSessionCallback(countDownLatch);
        try {
            // Needs UWB_PRIVILEGED & UWB_RANGING permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            // Try to start a ranging session with invalid params, should fail.
            cancellationSignal = mUwbManager.openRangingSession(
                    new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    rangingSessionCallback,
                    mDefaultChipId);
            // Wait for the on start failed callback.
            assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(rangingSessionCallback.onOpenedCalled).isFalse();
            assertThat(rangingSessionCallback.onOpenFailedCalled).isTrue();
        } finally {
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
            }
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    /**
     * Simulates the app holding UWB_RANGING permission, but not UWB_PRIVILEGED.
     */
    @Test
    public void testOpenRangingSessionWithoutUwbPrivileged() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Only hold UWB_RANGING permission
            uiAutomation.adoptShellPermissionIdentity(UWB_RANGING);
            mUwbManager.openRangingSession(new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    new RangingSessionCallback(new CountDownLatch(1)));
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testOpenRangingSessionWithChipIdWithoutUwbPrivileged() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Only hold UWB_RANGING permission
            uiAutomation.adoptShellPermissionIdentity(UWB_RANGING);
            mUwbManager.openRangingSession(new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    new RangingSessionCallback(new CountDownLatch(1)),
                    mDefaultChipId);
            // should fail if the call was successful without UWB_PRIVILEGED permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    /**
     * Simulates the app holding UWB_PRIVILEGED permission, but not UWB_RANGING.
     */
    @Test
    public void testOpenRangingSessionWithoutUwbRanging() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity(UWB_PRIVILEGED);
            mUwbManager.openRangingSession(new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    new RangingSessionCallback(new CountDownLatch(1)));
            // should fail if the call was successful without UWB_RANGING permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testOpenRangingSessionWithChipIdWithoutUwbRanging() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity(UWB_PRIVILEGED);
            mUwbManager.openRangingSession(new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    new RangingSessionCallback(new CountDownLatch(1)),
                    mDefaultChipId);
            // should fail if the call was successful without UWB_RANGING permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    private AttributionSource getShellAttributionSourceWithRenouncedPermissions(
            @Nullable Set<String> renouncedPermissions) {
        try {
            AttributionSource shellAttributionSource =
                    new AttributionSource.Builder(Process.SHELL_UID)
                            .setPackageName("com.android.shell")
                            .setRenouncedPermissions(renouncedPermissions)
                            .build();
            PermissionManager permissionManager =
                    mContext.getSystemService(PermissionManager.class);
            permissionManager.registerAttributionSource(shellAttributionSource);
            return shellAttributionSource;
        } catch (SecurityException e) {
            fail("Failed to create shell attribution source" + e);
            return null;
        }
    }

    private Context createShellContextWithRenouncedPermissionsAndAttributionSource(
            @Nullable Set<String> renouncedPermissions) {
        return mContext.createContext(new ContextParams.Builder()
                .setRenouncedPermissions(renouncedPermissions)
                .setNextAttributionSource(
                        getShellAttributionSourceWithRenouncedPermissions(renouncedPermissions))
                .build());
    }

    /**
     * Simulates the calling app holding UWB_PRIVILEGED permission and UWB_RANGING permission, but
     * the proxied app not holding UWB_RANGING permission.
     */
    @Test
    public void testOpenRangingSessionWithoutUwbRangingInNextAttributeSource() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Only hold UWB_PRIVILEGED permission
            uiAutomation.adoptShellPermissionIdentity();
            Context shellContextWithUwbRangingRenounced =
                    createShellContextWithRenouncedPermissionsAndAttributionSource(
                            Set.of(UWB_RANGING));
            UwbManager uwbManagerWithUwbRangingRenounced =
                    shellContextWithUwbRangingRenounced.getSystemService(UwbManager.class);
            uwbManagerWithUwbRangingRenounced.openRangingSession(new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    new RangingSessionCallback(new CountDownLatch(1)));
            // should fail if the call was successful without UWB_RANGING permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    @Test
    public void testOpenRangingSessionWithChipIdWithoutUwbRangingInNextAttributeSource() {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        try {
            // Only hold UWB_PRIVILEGED permission
            uiAutomation.adoptShellPermissionIdentity();
            Context shellContextWithUwbRangingRenounced =
                    createShellContextWithRenouncedPermissionsAndAttributionSource(
                            Set.of(UWB_RANGING));
            UwbManager uwbManagerWithUwbRangingRenounced =
                    shellContextWithUwbRangingRenounced.getSystemService(UwbManager.class);
            uwbManagerWithUwbRangingRenounced.openRangingSession(new PersistableBundle(),
                    Executors.newSingleThreadExecutor(),
                    new RangingSessionCallback(new CountDownLatch(1)),
                    mDefaultChipId);
            // should fail if the call was successful without UWB_RANGING permission.
            fail();
        } catch (SecurityException e) {
            /* pass */
            Log.i(TAG, "Failed with expected security exception: " + e);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }

    private class AdapterStateCallback implements UwbManager.AdapterStateCallback {
        private final CountDownLatch mCountDownLatch;
        private final @State Integer mWaitForState;
        public int state;
        public int reason;

        AdapterStateCallback(@NonNull CountDownLatch countDownLatch,
                @Nullable @State Integer waitForState) {
            mCountDownLatch = countDownLatch;
            mWaitForState = waitForState;
        }

        public void onStateChanged(@State int state, @StateChangedReason int reason) {
            this.state = state;
            this.reason = reason;
            if (mWaitForState != null) {
                if (mWaitForState == state) {
                    mCountDownLatch.countDown();
                }
            } else {
                mCountDownLatch.countDown();
            }
        }
    }

    @Test
    public void testUwbStateToggle() throws Exception {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AdapterStateCallback adapterStateCallback =
                new AdapterStateCallback(countDownLatch, STATE_DISABLED);
        try {
            // Needs UWB_PRIVILEGED permission which is held by shell.
            uiAutomation.adoptShellPermissionIdentity();
            mUwbManager.registerAdapterStateCallback(
                    Executors.newSingleThreadExecutor(), adapterStateCallback);
            assertThat(mUwbManager.isUwbEnabled()).isTrue();
            assertThat(mUwbManager.getAdapterState()).isEqualTo(STATE_ENABLED_INACTIVE);
            // Toggle the state
            mUwbManager.setUwbEnabled(false);
            // Wait for the on start failed callback.
            assertThat(countDownLatch.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(mUwbManager.isUwbEnabled()).isFalse();
            assertThat(adapterStateCallback.state).isEqualTo(STATE_DISABLED);
            assertThat(mUwbManager.getAdapterState()).isEqualTo(STATE_DISABLED);
        } finally {
            uiAutomation.dropShellPermissionIdentity();
        }
    }
}
