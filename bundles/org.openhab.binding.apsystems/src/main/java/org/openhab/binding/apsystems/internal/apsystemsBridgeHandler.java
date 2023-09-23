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

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.apsystems.internal.ECU.ECUConnector;
import org.openhab.binding.apsystems.internal.ECU.ECUResponse;
import org.openhab.binding.apsystems.internal.ECU.SystemInfoPayload;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.lang.Nullable;

/**
 * The {@link apsystemsBridgeHandler} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class apsystemsBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(apsystemsBridgeHandler.class);

    private apsystemsECUConfiguration config;
    private @Nullable ScheduledFuture<?> pollingJob;
    private boolean initialized = false;

    public apsystemsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID arg0, @NonNull Command arg1) {
        // TODO Auto-generated method stub
        logger.info("Hier gibt's was zu handlen {} --> {}", arg0, arg1);
    }

    @Override
    public void initialize() {

        // pollingJob = scheduler.s(this::loadBridgeData, 0, 10, TimeUnit.SECONDS);

        logger.debug("Initializing Bridge {}", getThing().getLabel());

        updateStatus(ThingStatus.UNKNOWN);

        if (!this.initialized) {
            this.initialized = true;
            loadBridgeData();
        } else {
            logger.warn("Bridge initialize called again");
        }
    }

    private void loadBridgeData() {

        config = getConfigAs(apsystemsECUConfiguration.class);
        ECUConnector connector = new ECUConnector(config.ipAddress, config.port);

        try {
            ECUResponse response = connector.fetchSystemInfo();
            SystemInfoPayload payload = (SystemInfoPayload) response.getECUResponsePayload();

            logger.info("ECU Data loaded");

            logger.info("Bridge is inizialized");
            updateStatus(ThingStatus.ONLINE);

            Map<@NonNull String, @NonNull String> proerties = editProperties();
            proerties.put("ECU_Serialnumber", payload.getECUId());
            proerties.put("ECU_Version", payload.getVersion());
            proerties.put("ECU_Model", payload.getECUModel());
            proerties.put("ECU_TimeZone", payload.getTimeZone());
            updateProperties(proerties);

            updateState("ECU_lastSystemPower", new StringType("so much power!"));

        } catch (Exception ex) {
            logger.error("Error fetching system info", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }
}
