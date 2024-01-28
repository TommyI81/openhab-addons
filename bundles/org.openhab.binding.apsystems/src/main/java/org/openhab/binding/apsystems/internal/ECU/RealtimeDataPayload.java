/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.apsystems.internal.ECU;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class RealtimeDataPayload extends ECUResponsePayload {

    private String ECUModel;
    private int NoOfInverters;
    private LocalDateTime RealTimeDate;
    private List<InverterRealtimeData> Inverter;

    /**
     * @return the eCUModel
     */
    public String getECUModel() {
        return ECUModel;
    }

    /**
     * @return the noOfInverters
     */
    public int getNoOfInverters() {
        return NoOfInverters;
    }

    /**
     * @return the realTimeDate
     */
    public LocalDateTime getRealTimeDate() {
        return RealTimeDate;
    }

    /**
     * @return the inverter
     */
    public List<InverterRealtimeData> getInverter() {
        return Inverter;
    }

    private RealtimeDataPayload() {
    }

    @SuppressWarnings("null")
    public static RealtimeDataPayload createFromBytes(byte[] bytes) {
        RealtimeDataPayload payload = new RealtimeDataPayload();

        payload.ECUModel = new String(Arrays.copyOfRange(bytes, 0, 2), StandardCharsets.US_ASCII);
        payload.NoOfInverters = BinaryTools.TwoBytesToInt(Arrays.copyOfRange(bytes, 2, 4));
        payload.RealTimeDate = BinaryTools.bcdToDateTime(Arrays.copyOfRange(bytes, 4, 11));

        payload.Inverter = InverterRealtimeData.createFromBytes(Arrays.copyOfRange(bytes, 11, bytes.length),
                payload.getNoOfInverters());

        return payload;
    }
}
