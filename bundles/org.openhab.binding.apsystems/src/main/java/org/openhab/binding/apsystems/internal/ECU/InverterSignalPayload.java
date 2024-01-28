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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
public class InverterSignalPayload extends ECUResponsePayload {

    private InverterSignalPayload() {
        InverterSignals = new ArrayList<>();
    }

    private List<Map.Entry<String, Float>> InverterSignals;

    public List<Map.Entry<String, Float>> getInverterSignals() {
        return InverterSignals;
    }

    public static InverterSignalPayload createFromBytes(byte[] bytes) {
        InverterSignalPayload payload = new InverterSignalPayload();

        for (int iBinPos = 0; iBinPos < bytes.length;) {
            String strInverterId = BinaryTools.bcd2Str(Arrays.copyOfRange(bytes, iBinPos, iBinPos += 6));
            int iSignal = BinaryTools.TwoBytesToInt(Arrays.copyOfRange(bytes, iBinPos, iBinPos += 1));

            float fSignalPercentage = iSignal / 255f * 100f;

            if (strInverterId != null) {
                payload.getInverterSignals().add(new SimpleEntry<>(strInverterId, fSignalPercentage));
            }
        }

        return payload;
    }
}
