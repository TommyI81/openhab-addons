/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information!
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.apsystems.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link apsystemsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
public class apsystemsBindingConstants {

    private static final String BINDING_ID = "apsystems";
    private static final String DEVICE_BRIDGE = "bridge";
    private static final String DEVICE_DS3 = "DS3";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_BRIDGE);
    public static final ThingTypeUID DS3INVERTER_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_DS3);

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    // ECU specific Constants
    public static final String ECU_PATTERN_STRART = "APS";
    public static final String ECU_PATTERN_END = "END\n";

    public static final int COMMAND_GROUP_11 = 11;

    public static final int COMMAND_CODE_GET_SYSTEM_INFO = 1;
    public static final int COMMAND_CODE_GET_REALTIME_DATA = 2;
    public static final int COMMAND_CODE_GET_POWER_OF_DAY = 3;
    public static final int COMMAND_CODE_GET_ENERGY_OF_WEEK_MONTH_YEAR = 4;
    public static final int COMMAND_CODE_GET_INVERTER_SIGNAL = 30;

    public static final Integer ECU_RESPONSE_OK = 0;
    public static final Integer ECU_RESPONSE_NoData = 1;
    public static final Integer ECU_RESPONSE_Unknown = 2;

    public static final String PERIODE_WEEK = "00";
    public static final String PERIODE_MONTH = "01";
    public static final String PERIODE_YEAR = "02";

    public static enum InverterState {
        Unknown, // = -1,
        Offline, // = 0,
        Online, // = 1,
    }

    public static InverterState inverterStateFromIntValue(int state) {
        switch (state) {
            case -1:
                return InverterState.Unknown;
            case 0:
                return InverterState.Offline;
            case 1:
                return InverterState.Online;
            default:
                return InverterState.Unknown;
        }
    }

    public static int intValueFromInverterState(InverterState state) {
        switch (state) {
            case Unknown:
                return -1;
            case Offline:
                return 0;
            case Online:
                return 1;
            default:
                return -1;
        }
    }

    public enum InverterType {
        unknown, // = 0,
        YC600orDS3, // = 1,
        YC100, // = 2,
        QS1, // = 3,
        DS3, // = 4,
        Y600, // = 5,
    }

    public static InverterType inverterTypeFromIntValue(int value) {
        switch (value) {
            case 0:
                return InverterType.unknown;
            case 1:
                return InverterType.YC600orDS3;
            case 2:
                return InverterType.YC100;
            case 3:
                return InverterType.QS1;
            case 4:
                return InverterType.DS3;
            case 5:
                return InverterType.Y600;

            default:
                return InverterType.unknown;
        }
    }

    public static int intValueFromInverterType(InverterType t) {
        switch (t) {
            case unknown:
                return 0;
            case YC600orDS3:
                return 1;
            case YC100:
                return 2;
            case QS1:
                return 3;
            case DS3:
                return 4;
            case Y600:
                return 5;
            default:
                return 0;
        }
    }
}
