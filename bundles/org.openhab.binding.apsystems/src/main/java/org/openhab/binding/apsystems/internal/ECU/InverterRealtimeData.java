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

import static org.openhab.binding.apsystems.internal.apsystemsBindingConstants.inverterStateFromIntValue;
import static org.openhab.binding.apsystems.internal.apsystemsBindingConstants.inverterTypeFromIntValue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.apsystems.internal.apsystemsBindingConstants.InverterState;
import org.openhab.binding.apsystems.internal.apsystemsBindingConstants.InverterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
public class InverterRealtimeData {

    private InverterState State;
    private InverterType Type;
    private float Frequency;
    private float Temperature;
    private int Power1;
    private int AcVoltage1;
    private int Power2;
    private int AcVoltage2;
    private String InverterID;

    private final static Logger logger = LoggerFactory.getLogger(InverterRealtimeData.class);

    /**
     * @return the inverterID
     */
    public String getInverterID() {
        return InverterID;
    }

    /**
     * @return the state
     */
    public InverterState getState() {
        return State;
    }

    /**
     * @return the type
     */
    public InverterType getType() {
        return Type;
    }

    /**
     * @return the frequency
     */
    public float getFrequency() {
        return Frequency;
    }

    /**
     * @return the temperature
     */
    public float getTemperature() {
        return Temperature;
    }

    /**
     * @return the power1
     */
    public int getPower1() {
        return Power1;
    }

    /**
     * @return the acVoltage1
     */
    public int getAcVoltage1() {
        return AcVoltage1;
    }

    /**
     * @return the power2
     */
    public int getPower2() {
        return Power2;
    }

    /**
     * @return the acVoltage2
     */
    public int getAcVoltage2() {
        return AcVoltage2;
    }

    private InverterRealtimeData() {
        InverterID = "";
        State = InverterState.Unknown;
        Type = InverterType.unknown;
    }

    @SuppressWarnings("null")
    public static List<InverterRealtimeData> createFromBytes(byte[] bytes, int iNoOfInverters) {

        List<InverterRealtimeData> retLst = new ArrayList<InverterRealtimeData>();

        int iBinaryPosition = 0;

        for (int i = 0; i < iNoOfInverters; i++) {
            InverterRealtimeData inverter = new InverterRealtimeData();
            inverter.InverterID = BinaryTools.bcd2Str(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 6));
            int iState = bytes[iBinaryPosition];
            inverter.State = inverterStateFromIntValue(iState);
            iBinaryPosition += 1;

            String strInverterType = new String(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 2),
                    StandardCharsets.US_ASCII);
            inverter.Type = inverterTypeFromIntValue(Integer.parseInt(strInverterType));

            // Finetuning YC600 or DS3
            if (inverter.Type == InverterType.YC600orDS3) {
                if (inverter.InverterID.startsWith("40")) {
                    inverter.Type = InverterType.Y600;
                } else if (inverter.InverterID.startsWith("70")) {
                    inverter.Type = InverterType.DS3;
                } else {
                    logger.error("Unknonw Invertertype with ID {}", inverter.Type.toString());
                    inverter.Type = InverterType.unknown;
                }
            }

            if (inverter.Type != InverterType.DS3) {
                logger.error("Inverter with Type {} ist currently not supported", inverter.Type.toString());
            }

            int iFrequncyRaw = BinaryTools
                    .TwoBytesToInt(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 2));
            inverter.Frequency = iFrequncyRaw / 10f;
            int iTempRaw = BinaryTools.TwoBytesToInt(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 2));
            inverter.Temperature = (iTempRaw - 32) * (5f / 9f);

            // DS3 specific
            if (inverter.getType() == InverterType.DS3) {
                inverter.Power1 = BinaryTools
                        .TwoBytesToInt(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 2));
                inverter.AcVoltage1 = BinaryTools
                        .TwoBytesToInt(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 2));
                inverter.Power2 = BinaryTools
                        .TwoBytesToInt(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 2));
                inverter.AcVoltage2 = BinaryTools
                        .TwoBytesToInt(Arrays.copyOfRange(bytes, iBinaryPosition, iBinaryPosition += 2));
            }

            retLst.add(inverter);

        }

        return retLst;
    }
}
