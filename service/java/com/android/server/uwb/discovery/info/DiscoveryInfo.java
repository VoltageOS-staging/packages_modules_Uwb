/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.android.server.uwb.discovery.info;

/**
 * Holds information about the discovery request.
 */
public class DiscoveryInfo {

    public DiscoveryInfo(TransportType transportType, ScanInfo scanInfo) {
        this.transportType = transportType;
        this.scanInfo = scanInfo;
    }

    /** A definition of discovery transport type. */
    public enum TransportType {
        BLE,
    }

    public final TransportType transportType;

    public final ScanInfo scanInfo;
}
