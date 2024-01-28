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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNull;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class ECURequest {

    private Integer CodeGroup;
    private Integer CommandCode;
    private String ECUId = "";
    private String Extension = "";

    private ECURequest(Integer commandCode) {
        this.CodeGroup = COMMAND_GROUP_11;
        this.CommandCode = commandCode;
    }

    public Integer getCommandCode() {
        return this.CommandCode;
    }

    public String getECUId() {
        return this.ECUId;
    }

    public static ECURequest createGestSystemInfoRequest() {
        ECURequest request = new ECURequest(COMMAND_CODE_GET_SYSTEM_INFO);
        return request;
    }

    public static ECURequest createGetRealTimeDataRequest(String ECUId) {
        ECURequest request = new ECURequest(COMMAND_CODE_GET_REALTIME_DATA);
        request.ECUId = ECUId;
        return request;
    }

    public static ECURequest CreateGetPowerOfDayRequest(String strECUId, @NonNull LocalDate today) {
        ECURequest request = new ECURequest(COMMAND_CODE_GET_POWER_OF_DAY);
        request.ECUId = strECUId;
        DateTimeFormatter payloadFormat = DateTimeFormatter.ofPattern("yyyyMMdd");
        request.Extension = payloadFormat.format(today) + "END";

        return request;
    }

    public static ECURequest CreateGetEnergyOfWeekMonthYear(String strECUId, String periode) {
        ECURequest request = new ECURequest(COMMAND_CODE_GET_ENERGY_OF_WEEK_MONTH_YEAR);
        request.ECUId = strECUId;
        request.Extension = periode + "END";

        return request;
    }

    public static ECURequest CreateGetInverterSignalLevel(String strECUId) {
        ECURequest request = new ECURequest(COMMAND_CODE_GET_INVERTER_SIGNAL);
        request.ECUId = strECUId;
        return request;
    }

    @Override
    @SuppressWarnings("null")
    public String toString() {
        String retValue = String.format("APS%dXXXX%04d%sEND%s\n", this.CodeGroup, this.CommandCode, this.ECUId,
                this.Extension);

        String lengthString = String.format("%04d", retValue.length() - 1);
        retValue = retValue.replace("XXXX", lengthString);

        return retValue;
    }

    @SuppressWarnings("null")
    public byte[] GetBytes() {
        return this.toString().getBytes(StandardCharsets.UTF_8);
    }
}
