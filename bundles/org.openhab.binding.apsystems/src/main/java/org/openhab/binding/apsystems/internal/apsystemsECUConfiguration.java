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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link apsystemsECUConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
public class apsystemsECUConfiguration {

    public String ipAddress = "";
    public Integer port = 0;
    public Integer pollingInterval = 3000;

    public boolean forceNightlyDowntime = false;
    public String nightlyDowntimeStart = "";
    public String nightlyDowntimeEnd = "";

    public LocalTime getDowntimeStart() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(this.nightlyDowntimeStart, df);
    }

    public LocalTime getDowntimeEnd() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(this.nightlyDowntimeEnd, df);
    }
}
