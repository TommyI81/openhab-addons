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
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
public class SystemInfoPayload extends ECUResponsePayload {

    private SystemInfoPayload() {
    }

    private String ECUId = "unknown";
    private String ECUModel = "unknown";
    private Float LifeTimeEnergy = 0f;
    private Integer LastSystemPower = 0;
    private Float CurrentDayEnergy = 0f;
    private String LastTimeConnectedEMA = "nver";
    private Integer NoOfInverters = 0;
    private Integer InvertersOnline = 0;
    private String EcuChannel = "unknown";
    private String Version = "unknown";
    private String TimeZone = "unknown";
    private String EthernetMAC = "not implemented";
    private String WirelessMAC = "not implemented";

    /**
     * 
     * @return the ECU Id
     */
    public String getECUId() {
        return ECUId;
    }

    /**
     * 
     * @return the ECU Model
     */
    public String getECUModel() {
        return ECUModel;
    }

    /**
     * @return the lifeTimeEnergy
     */
    public Float getLifeTimeEnergy() {
        return LifeTimeEnergy;
    }

    /**
     * @return the lastSystemPower
     */
    public Integer getLastSystemPower() {
        return LastSystemPower;
    }

    /**
     * @return the currentDayEnergy
     */
    public Float getCurrentDayEnergy() {
        return CurrentDayEnergy;
    }

    /**
     * @return the lastTimeConnectedEMA
     */
    public String getLastTimeConnectedEMA() {
        return LastTimeConnectedEMA;
    }

    /**
     * @return the noOfInverters
     */
    public Integer getNoOfInverters() {
        return NoOfInverters;
    }

    /**
     * @return the invertersOnline
     */
    public Integer getInvertersOnline() {
        return InvertersOnline;
    }

    /**
     * @return the ecuChannel
     */
    public String getEcuChannel() {
        return EcuChannel;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return Version;
    }

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return TimeZone;
    }

    /**
     * @return the ethernetMAC
     */
    public String getEthernetMAC() {
        return EthernetMAC;
    }

    /**
     * @return the wirelessMAC
     */
    public String getWirelessMAC() {
        return WirelessMAC;
    }

    @SuppressWarnings("null")
    public static SystemInfoPayload createFromBytes(byte[] bytes /* , ILogger? logger = null */ ) {
        SystemInfoPayload payload = new SystemInfoPayload();

        payload.ECUId = new String(Arrays.copyOfRange(bytes, 0, 12), StandardCharsets.US_ASCII);
        payload.ECUModel = new String(Arrays.copyOfRange(bytes, 12, 14), StandardCharsets.US_ASCII);
        payload.LifeTimeEnergy = BinaryTools.FourBytesToInt(Arrays.copyOfRange(bytes, 14, 18)) / 10f;
        payload.LastSystemPower = BinaryTools.FourBytesToInt(Arrays.copyOfRange(bytes, 18, 22));
        payload.CurrentDayEnergy = BinaryTools.FourBytesToInt(Arrays.copyOfRange(bytes, 22, 26)) / 100f;
        Arrays.copyOfRange(bytes, 26, 33);
        // payload.LastTimeConnectedEMA = BinaryTools.BcdToString(bytes.Skip(iBinPos).Take(7).ToArray());
        payload.NoOfInverters = BinaryTools.TwoBytesToInt(Arrays.copyOfRange(bytes, 33, 35));
        payload.InvertersOnline = BinaryTools.TwoBytesToInt(Arrays.copyOfRange(bytes, 35, 37));
        payload.EcuChannel = new String(Arrays.copyOfRange(bytes, 37, 39), StandardCharsets.US_ASCII);

        String strVersionLength = new String(Arrays.copyOfRange(bytes, 39, 42), StandardCharsets.US_ASCII);
        int iVersionLenght = Integer.parseInt(strVersionLength);
        int iBinPos = 42 + iVersionLenght;

        payload.Version = new String(Arrays.copyOfRange(bytes, 42, iBinPos));
        String strTimeZoneLength = new String(Arrays.copyOfRange(bytes, iBinPos, iBinPos += 3));
        int iTimeZoneLength = Integer.parseInt(strTimeZoneLength);
        payload.TimeZone = new String(Arrays.copyOfRange(bytes, iBinPos, iBinPos += iTimeZoneLength),
                StandardCharsets.US_ASCII);

        payload.EthernetMAC = "not yet implemented"; // BitConverter.ToString(bytes.Skip(iBinPos).Take(6).ToArray());
                                                     // iBinPos += 6;
        payload.WirelessMAC = "not yet implemented"; // BitConverter.ToString(bytes.Skip(iBinPos).Take(6).ToArray());
                                                     // iBinPos += 6;

        return payload;
    }
}
