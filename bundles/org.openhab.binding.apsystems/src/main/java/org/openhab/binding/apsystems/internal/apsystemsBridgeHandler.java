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

import static org.openhab.binding.apsystems.internal.apsystemsBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.apsystems.internal.ECU.ECUConnector;
import org.openhab.binding.apsystems.internal.ECU.ECUResponse;
import org.openhab.binding.apsystems.internal.ECU.InverterRealtimeData;
import org.openhab.binding.apsystems.internal.ECU.RealtimeDataPayload;
import org.openhab.binding.apsystems.internal.ECU.SystemInfoPayload;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link apsystemsBridgeHandler} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
public class apsystemsBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(apsystemsBridgeHandler.class);

    private apsystemsECUConfiguration config;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ECUResponse lastSystemInfoResponse;

    public apsystemsBridgeHandler(Bridge bridge) {
        super(bridge);

        config = getConfigAs(apsystemsECUConfiguration.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command == RefreshType.REFRESH && lastSystemInfoResponse != null) {

            SystemInfoPayload payload = (SystemInfoPayload) lastSystemInfoResponse.getECUResponsePayload();

            switch (channelUID.getId()) {
                case CHANNEL_ECU_CURRENT_DAY_ENERGY:
                    updateState(CHANNEL_ECU_CURRENT_DAY_ENERGY, new DecimalType(payload.getLifeTimeEnergy()));
                    break;
                case CHANNEL_ECU_LAST_SYSTEM_POWER:
                    updateState(CHANNEL_ECU_LAST_SYSTEM_POWER,
                            new QuantityType<>(payload.getLastSystemPower(), Units.WATT));
                    break;
                case CHANNEL_ECU_LIFETIME_ENERGY:
                    updateState(CHANNEL_ECU_LIFETIME_ENERGY, new DecimalType(payload.getLifeTimeEnergy()));
                    break;
                case CHANNEL_ECU_NO_OF_INVERTERS:
                    updateState(CHANNEL_ECU_NO_OF_INVERTERS, new DecimalType(payload.getNoOfInverters()));
                    break;
                case CHANNEL_ECU_NO_OF_INVERTERS_ONLINE:
                    updateState(CHANNEL_ECU_NO_OF_INVERTERS_ONLINE, new DecimalType(payload.getInvertersOnline()));
                    break;
                default:
                    logger.error("no such channel: {}", channelUID.getId());
            }
        }
    }

    @Override
    public void initialize() {

        logger.debug("Initializing Bridge {}", getThing().getLabel());

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::loadBridgeData);

        pollingJob = scheduler.scheduleAtFixedRate(this::loadBridgeData, 3, 3, TimeUnit.MINUTES);
    }

    private void loadBridgeData() {

        ECUConnector connector = new ECUConnector(config.ipAddress, config.port);

        try {
            lastSystemInfoResponse = connector.fetchSystemInfo();

            if (lastSystemInfoResponse != null) {

                SystemInfoPayload systemInfoPayload = (SystemInfoPayload) lastSystemInfoResponse
                        .getECUResponsePayload();

                logger.info("ECU Data loaded");
                updateStatus(ThingStatus.ONLINE);

                // Update Bridge properties and channels

                Map<String, String> proerties = editProperties();
                proerties.put(PROPERTY_ECU_SERIALNUMBER, systemInfoPayload.getECUId());
                proerties.put(PROPERTY_ECU_VERSION, systemInfoPayload.getVersion());
                proerties.put(PROPERTY_ECU_MODEL, systemInfoPayload.getECUModel());
                proerties.put(PROPERTY_ECU_TIME_ZONE, systemInfoPayload.getTimeZone());
                proerties.put(PROPERTY_ECU_CHANNEL, systemInfoPayload.getEcuChannel());
                updateProperties(proerties);

                updateState(CHANNEL_ECU_LAST_SYSTEM_POWER,
                        new QuantityType<>(systemInfoPayload.getLastSystemPower(), Units.WATT));
                updateState(CHANNEL_ECU_LIFETIME_ENERGY, new DecimalType(systemInfoPayload.getLifeTimeEnergy()));
                updateState(CHANNEL_ECU_CURRENT_DAY_ENERGY, new DecimalType(systemInfoPayload.getLifeTimeEnergy()));
                updateState(CHANNEL_ECU_NO_OF_INVERTERS, new DecimalType(systemInfoPayload.getNoOfInverters()));
                updateState(CHANNEL_ECU_NO_OF_INVERTERS_ONLINE,
                        new DecimalType(systemInfoPayload.getInvertersOnline()));

                // Update Inverters
                ECUResponse response = connector.fetchRealTimeData(systemInfoPayload.getECUId());
                RealtimeDataPayload realtimeDataPayload = (RealtimeDataPayload) response.getECUResponsePayload();

                for (InverterRealtimeData inverterData : realtimeDataPayload.getInverter()) {

                    String inverterID = inverterData.getInverterID();
                    @Nullable
                    apsystemsDS3Handler inverterHandler = findInverter(inverterID);

                    if (inverterHandler != null) {
                        inverterHandler.update(inverterData);
                    }
                }

                // set all configured Inverters without data to offline
                for (Thing inverterThing : getThing().getThings()) {
                    if (inverterThing.getThingTypeUID().equals(DS3INVERTER_THING_TYPE)) {
                        apsystemsDS3Handler ds3Handler = (apsystemsDS3Handler) inverterThing.getHandler();
                        if (ds3Handler != null) {
                            ds3Handler.onUpdateDone();
                        }
                    }
                }
            } else {
                logger.error("failed to load system info");
            }

        } catch (Exception ex) {
            logger.error("Error fetching system info", ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private @Nullable apsystemsDS3Handler findInverter(String inverterId) {

        List<Thing> configuredInverters = getThing().getThings();

        for (Thing thing : configuredInverters) {
            if (thing.getThingTypeUID().equals(DS3INVERTER_THING_TYPE)) {
                apsystemsDS3Handler ds3Handler = (apsystemsDS3Handler) thing.getHandler();
                if (ds3Handler != null) {
                    String inverterSerial = ds3Handler.getSerialNumber();
                    if (inverterSerial.equals(inverterId)) {
                        return ds3Handler;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }
}
