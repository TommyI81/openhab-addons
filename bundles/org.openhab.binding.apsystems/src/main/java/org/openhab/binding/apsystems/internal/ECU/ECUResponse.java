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

import static org.openhab.binding.apsystems.internal.apsystemsBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class ECUResponse {

    private ECUResponsePayload Payload;

    public ECUResponsePayload getECUResponsePayload() {
        return Payload;
    }

    private Integer CommandCode;

    public Integer getCommandCode() {
        return CommandCode;
    }

    private Integer CommandGroup;

    public Integer getCommandGroup() {
        return CommandGroup;
    }

    private Integer MatchStatus;

    public Integer getMatchStatus() {
        return MatchStatus;
    }

    private Integer DataLength;

    public Integer getDataLength() {
        return DataLength;
    }

    private ECUResponse(Integer CommandGroup, Integer CommandCode, Integer MatchStatusCode, Integer DataLength) {
        this.CommandGroup = CommandGroup;
        this.CommandCode = CommandCode;
        this.MatchStatus = MatchStatusCode;
        this.DataLength = DataLength;
    }

    /// <summary>
    /// Deserialize the binary response to ECUResponse Object
    /// </summary>
    /// <param name="bytes"></param>
    /// <returns></returns>
    /// <exception cref="ArgumentNullException"></exception>
    /// <exception cref="ArgumentException"></exception>
    /// <exception cref="Exception"></exception>
    /// <exception cref="NotImplementedException"></exception>
    @SuppressWarnings("null")
    public static ECUResponse CreateFromBytes(byte[] bytes) throws Exception {
        if (bytes == null)
            throw new Exception("No data");

        if (bytes.length < 13)
            throw new Exception("Too less data");

        // Signature Start APS
        String strMessageStart = new String(Arrays.copyOfRange(bytes, 0, 3), StandardCharsets.US_ASCII);
        if (!ECU_PATTERN_STRART.equals(strMessageStart)) {
            throw new Exception("Invalid Response - Signature Start not found");
        }

        // Command Group
        String strCommandGroup = new String(Arrays.copyOfRange(bytes, 3, 5), StandardCharsets.US_ASCII);
        if (!strCommandGroup.equals("11") && !strCommandGroup.equals("12")) // TODO: Double check 12
        {
            throw new Exception("Unsupported Command Group " + strCommandGroup);
        }

        // Command Code
        String strCommandCode = new String(Arrays.copyOfRange(bytes, 9, 13), StandardCharsets.US_ASCII);

        // Data length
        String strDataLength = new String(Arrays.copyOfRange(bytes, 5, 9), StandardCharsets.US_ASCII);
        int iDataLength = Integer.parseInt(strDataLength) + 1; // as string includes \n for termination - value within
                                                               // the data does not

        if (bytes.length < iDataLength) {
            throw new Exception(String.format("Invalid Data size - given:%d vs. header:%d", bytes.length, iDataLength));
        }

        Integer msgEndStartPos = iDataLength - ECU_PATTERN_END.length();
        String strMessageEnd = new String(
                Arrays.copyOfRange(bytes, msgEndStartPos, msgEndStartPos + ECU_PATTERN_END.length()),
                StandardCharsets.US_ASCII);
        if (!ECU_PATTERN_END.equals(strMessageEnd)) {
            throw new Exception("Invalid Response - Signature End not found at given position");
        }

        String strMatchStatusCode = "0002"; // default is unkown
        int iPayloadStart = 0;
        int iPayloadLength = 0;

        ECUResponse response = new ECUResponse(Integer.parseInt(strCommandGroup), Integer.parseInt(strCommandCode),
                Integer.parseInt(strMatchStatusCode), iDataLength);

        switch (Integer.parseInt(strCommandCode)) {
            case COMMAND_CODE_GET_SYSTEM_INFO:
                response.MatchStatus = ECU_RESPONSE_OK; // used as default as there is no MatchStatus
                iPayloadStart = 13;
                iPayloadLength = iDataLength - iPayloadStart - ECU_PATTERN_END.length();
                response.Payload = SystemInfoPayload
                        .createFromBytes(Arrays.copyOfRange(bytes, iPayloadStart, iPayloadStart + iPayloadLength));
                break;

            case COMMAND_CODE_GET_REALTIME_DATA:
                response.MatchStatus = getMatchStatus(bytes);

                if (response.MatchStatus == ECU_RESPONSE_OK) {
                    iPayloadStart = 15;
                    iPayloadLength = iDataLength - iPayloadStart - ECU_PATTERN_END.length();
                    response.Payload = RealtimeDataPayload
                            .createFromBytes(Arrays.copyOfRange(bytes, iPayloadStart, iPayloadStart + iPayloadLength));
                }
                break;

            case COMMAND_CODE_GET_POWER_OF_DAY:
                response.MatchStatus = getMatchStatus(bytes);
                if (response.MatchStatus == ECU_RESPONSE_OK) {
                    iPayloadStart = 15;
                    iPayloadLength = iDataLength - iPayloadStart - ECU_PATTERN_END.length();
                    response.Payload = PowerOfDayPayload
                            .createFromBytes(Arrays.copyOfRange(bytes, iPayloadStart, iPayloadStart + iPayloadLength));
                }
                break;

            case COMMAND_CODE_GET_ENERGY_OF_WEEK_MONTH_YEAR:
                throw new Exception("Not implemented yet");

            case COMMAND_CODE_GET_INVERTER_SIGNAL:
                response.MatchStatus = getMatchStatus(bytes);
                if (response.MatchStatus == ECU_RESPONSE_OK) {
                    iPayloadStart = 15;
                    iPayloadLength = iDataLength - iPayloadStart - ECU_PATTERN_END.length();
                    response.Payload = InverterSignalPayload
                            .createFromBytes(Arrays.copyOfRange(bytes, iPayloadStart, iPayloadStart + iPayloadLength));
                }

                break;

            default:
                throw new Exception("Invalid Command Code " + strCommandCode);

        }

        if (response.Payload != null) {
            return response;
        }

        return null;
    }

    /// <summary>
    /// Helper to convert the MathStatusCode from bytes to Enum value
    /// </summary>
    /// <param name="bytes">2 bytes are expected</param>
    /// <returns></returns>
    @SuppressWarnings("null")
    private static Integer getMatchStatus(byte[] bytes) {
        String strMatchStatus = new String(Arrays.copyOfRange(bytes, 13, 15), StandardCharsets.US_ASCII);
        return Integer.parseInt(strMatchStatus);
    }
}
