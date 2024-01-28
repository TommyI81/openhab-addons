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

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

/**
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class BridgeHandlerTest {

    @Test
    public void DowntimeTestCase1() {
        LocalTime fromTime = LocalTime.of(8, 0);
        LocalTime toTime = LocalTime.of(20, 0);

        LocalTime test1Time = LocalTime.of(6, 0);
        LocalTime test2Time = LocalTime.of(8, 1);
        LocalTime test3Time = LocalTime.of(20, 1);

        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test1Time), false);
        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test2Time), true);
        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test3Time), false);
    }

    @Test
    public void DowntimeTestCase2() {
        LocalTime fromTime = LocalTime.of(22, 0);
        LocalTime toTime = LocalTime.of(7, 0);

        LocalTime test1Time = LocalTime.of(6, 0);
        LocalTime test2Time = LocalTime.of(7, 1);
        LocalTime test3Time = LocalTime.of(23, 0);

        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test1Time), true);
        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test2Time), false);
        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test3Time), true);
    }

    @Test
    public void DowntimeTestCase3() {
        LocalTime fromTime = LocalTime.of(22, 0);
        LocalTime toTime = LocalTime.of(22, 0);

        LocalTime test1Time = LocalTime.of(6, 0);
        LocalTime test2Time = LocalTime.of(22, 0);
        LocalTime test3Time = LocalTime.of(22, 1);

        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test1Time), false);
        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test2Time), false);
        assertEquals(apsystemsBridgeHandler.isDowntimeRunning(fromTime, toTime, test3Time), false);
    }
}
