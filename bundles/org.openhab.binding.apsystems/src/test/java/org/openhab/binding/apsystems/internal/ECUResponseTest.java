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
package org.openhab.binding.apsystems.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.openhab.binding.apsystems.internal.apsystemsBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.openhab.binding.apsystems.internal.ECU.ECUResponse;
import org.openhab.binding.apsystems.internal.ECU.InverterRealtimeData;
import org.openhab.binding.apsystems.internal.ECU.InverterSignalPayload;
import org.openhab.binding.apsystems.internal.ECU.RealtimeDataPayload;
import org.openhab.binding.apsystems.internal.ECU.SystemInfoPayload;
import org.openhab.binding.apsystems.internal.apsystemsBindingConstants.InverterState;
import org.openhab.binding.apsystems.internal.apsystemsBindingConstants.InverterType;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class ECUResponseTest {

    @Test
    void SystemInfoResponseExample() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("sysinf.bin");

            ECUResponse response = ECUResponse.CreateFromBytes(testbytes);
            assertNotNull(response);

            SystemInfoPayload payload = (SystemInfoPayload) response.getECUResponsePayload();

            assertEquals("216300090693", payload.getECUId());
            assertEquals("01", payload.getECUModel());
            assertEquals(115.0f, payload.getLifeTimeEnergy(), 0.001f);
            assertEquals((Integer) 836, payload.getLastSystemPower());
            assertEquals(3.43f, payload.getCurrentDayEnergy(), 0.001f);
            // assertEquals("130130130130130130130", payload.getLastTimeConnectedEMA()); //Not usefull at all
            assertEquals((Integer) 2, payload.getNoOfInverters());
            assertEquals((Integer) 2, payload.getInvertersOnline());
            assertEquals("ECU_B_1.2.26E", payload.getVersion());
            assertEquals("10", payload.getEcuChannel());
            assertEquals("Etc/GMT-8", payload.getTimeZone());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void RealTimeDataResponseExample1() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("nfvnfyal.0rd");

            ECUResponse response = ECUResponse.CreateFromBytes(testbytes);
            assertNotNull(response);
            assertEquals(response.getMatchStatus(), ECU_RESPONSE_OK);
            assertEquals(response.getCommandCode(), (Integer) COMMAND_CODE_GET_REALTIME_DATA);
            assertEquals(response.getCommandGroup(), (Integer) COMMAND_GROUP_11);

            RealtimeDataPayload payload = (RealtimeDataPayload) response.getECUResponsePayload();

            assertEquals(payload.getECUModel(), "01");
            assertEquals(payload.getNoOfInverters(), 2);
            assertEquals(payload.getRealTimeDate(), LocalDateTime.of(2023, 07, 31, 17, 44, 22));

            InverterRealtimeData inverter1 = payload.getInverter().get(0);
            InverterRealtimeData inverter2 = payload.getInverter().get(1);

            assertEquals(inverter1.getAcVoltage1(), 237);
            assertEquals(inverter1.getAcVoltage2(), 237);
            assertEquals(inverter1.getFrequency(), 49.9f, 0.001f);
            assertEquals(inverter1.getInverterID(), "707000017197");
            assertEquals(inverter1.getPower1(), 73);
            assertEquals(inverter1.getPower2(), 58);
            assertEquals(inverter1.getState(), InverterState.Online);
            assertEquals(inverter1.getTemperature(), 59.44f, 0.01f);
            assertEquals(inverter1.getType(), InverterType.DS3);

            assertEquals(inverter2.getAcVoltage1(), 238);
            assertEquals(inverter2.getAcVoltage2(), 238);
            assertEquals(inverter2.getFrequency(), 49.9f, 0.001f);
            assertEquals(inverter2.getInverterID(), "702000337185");
            assertEquals(inverter2.getPower1(), 51);
            assertEquals(inverter2.getPower2(), 0);
            assertEquals(inverter2.getState(), InverterState.Online);
            assertEquals(inverter2.getTemperature(), 57.77f, 0.01f);
            assertEquals(inverter2.getType(), InverterType.DS3);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void RealTimeDataResponseExample2() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("pql5ou3s.i5o");

            ECUResponse response = ECUResponse.CreateFromBytes(testbytes);
            assertNotNull(response);
            assertEquals(response.getMatchStatus(), ECU_RESPONSE_OK);
            assertEquals(response.getCommandCode(), (Integer) COMMAND_CODE_GET_REALTIME_DATA);
            assertEquals(response.getCommandGroup(), (Integer) COMMAND_GROUP_11);

            RealtimeDataPayload payload = (RealtimeDataPayload) response.getECUResponsePayload();

            assertEquals(payload.getECUModel(), "01");
            assertEquals(payload.getNoOfInverters(), 2);
            assertEquals(payload.getRealTimeDate(), LocalDateTime.of(2023, 07, 27, 14, 59, 20));

            InverterRealtimeData inverter1 = payload.getInverter().get(0);
            InverterRealtimeData inverter2 = payload.getInverter().get(1);

            assertEquals(inverter1.getAcVoltage1(), 237);
            assertEquals(inverter1.getAcVoltage2(), 237);
            assertEquals(inverter1.getFrequency(), 50f, 0.001f);
            assertEquals(inverter1.getInverterID(), "707000017197");
            assertEquals(inverter1.getPower1(), 55);
            assertEquals(inverter1.getPower2(), 66);
            assertEquals(inverter1.getState(), InverterState.Online);
            assertEquals(inverter1.getTemperature(), 55.55f, 0.01f);
            assertEquals(inverter1.getType(), InverterType.DS3);

            assertEquals(inverter2.getAcVoltage1(), 237);
            assertEquals(inverter2.getAcVoltage2(), 237);
            assertEquals(inverter2.getFrequency(), 50f, 0.001f);
            assertEquals(inverter2.getInverterID(), "702000337185");
            assertEquals(inverter2.getPower1(), 60);
            assertEquals(inverter2.getPower2(), 0);
            assertEquals(inverter2.getState(), InverterState.Online);
            assertEquals(inverter2.getTemperature(), 57.22f, 0.01f);
            assertEquals(inverter2.getType(), InverterType.DS3);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void RealTimeDataResponseExample3() {
        byte[] testbytes;
        try {

            testbytes = getRessourceAsBytes("oiiplg45.igp");

            ECUResponse response = ECUResponse.CreateFromBytes(testbytes);
            assertNotNull(response);
            assertEquals(response.getMatchStatus(), ECU_RESPONSE_OK);
            assertEquals(response.getCommandCode(), (Integer) COMMAND_CODE_GET_REALTIME_DATA);
            assertEquals(response.getCommandGroup(), (Integer) COMMAND_GROUP_11);

            RealtimeDataPayload payload = (RealtimeDataPayload) response.getECUResponsePayload();

            assertEquals(payload.getECUModel(), "01");
            assertEquals(payload.getNoOfInverters(), 2);
            assertEquals(payload.getRealTimeDate(), LocalDateTime.of(2023, 8, 6, 16, 00, 51));

            InverterRealtimeData inverter1 = payload.getInverter().get(0);
            InverterRealtimeData inverter2 = payload.getInverter().get(1);

            assertEquals(inverter1.getAcVoltage1(), 235);
            assertEquals(inverter1.getAcVoltage2(), 235);
            assertEquals(inverter1.getFrequency(), 49.9f, 0.001f);
            assertEquals(inverter1.getInverterID(), "707000017197");
            assertEquals(inverter1.getPower1(), 42);
            assertEquals(inverter1.getPower2(), 41);
            assertEquals(inverter1.getState(), InverterState.Online);
            assertEquals(inverter1.getTemperature(), 51.11f, 0.01f);
            assertEquals(inverter1.getType(), InverterType.DS3);

            assertEquals(inverter2.getAcVoltage1(), 0);
            assertEquals(inverter2.getAcVoltage2(), 0);
            assertEquals(inverter2.getFrequency(), 0f, 0.0f);
            assertEquals(inverter2.getInverterID(), "702000337185");
            assertEquals(inverter2.getPower1(), 0);
            assertEquals(inverter2.getPower2(), 0);
            assertEquals(inverter2.getState(), InverterState.Offline);
            assertEquals(inverter2.getTemperature(), 37.77f, 0.01f);
            assertEquals(inverter2.getType(), InverterType.DS3);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void InverterSignalExample() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("invertersignal.bin");

            ECUResponse response = ECUResponse.CreateFromBytes(testbytes);
            assertNotNull(response);
            assertEquals(response.getMatchStatus(), ECU_RESPONSE_OK);
            assertEquals(response.getCommandCode(), (Integer) COMMAND_CODE_GET_INVERTER_SIGNAL);
            assertEquals(response.getCommandGroup(), (Integer) COMMAND_GROUP_11);

            InverterSignalPayload payload = (InverterSignalPayload) response.getECUResponsePayload();

            Entry<String, Float> inverter1 = payload.getInverterSignals().get(0);
            Entry<String, Float> inverter2 = payload.getInverterSignals().get(1);

            assertEquals(inverter1.getKey(), "702000337185");
            assertEquals(inverter2.getKey(), "707000017197");

            assertEquals(inverter1.getValue(), 0f, 0.0f);
            assertEquals(inverter2.getValue(), 82.7451f, 0.0f);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static byte[] getRessourceAsBytes(@NonNull String ressourceName) throws IOException {
        InputStream inStream = ECUResponseTest.class.getResourceAsStream(ressourceName);
        if (inStream != null) {
            return inStream.readAllBytes();
        }

        return new byte[] {};
    }
}
