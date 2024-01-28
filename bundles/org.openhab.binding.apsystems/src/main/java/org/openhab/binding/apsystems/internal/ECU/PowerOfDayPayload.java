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

import java.time.LocalDateTime;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This Class has not been so well tested - ...so far
 * 
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class PowerOfDayPayload extends ECUResponsePayload {

    private List<Map.Entry<LocalDateTime, Integer>> powerValues;

    private PowerOfDayPayload() {
        powerValues = new ArrayList<>();
    }

    public List<Map.Entry<LocalDateTime, Integer>> getPowerValues() {
        return powerValues;
    }

    public static PowerOfDayPayload createFromBytes(byte[] bytes) {
        PowerOfDayPayload payload = new PowerOfDayPayload();

        for (int iBinPos = 0; iBinPos < bytes.length;) {
            LocalDateTime dateTime = BinaryTools.bcdToDateTime(Arrays.copyOfRange(bytes, iBinPos, iBinPos + 2));
            iBinPos += 2;
            int iPower = BinaryTools.TwoBytesToInt(Arrays.copyOfRange(bytes, iBinPos, iBinPos + 2));
            iBinPos += 2;

            payload.getPowerValues().add(new SimpleEntry<>(dateTime, iPower));
        }

        return payload;
    }
}
