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

import static org.openhab.binding.apsystems.internal.apsystemsBindingConstants.*;

import java.time.LocalTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.apsystems.internal.ECU.ECUConnector;
import org.openhab.binding.apsystems.internal.ECU.ECUResponse;
import org.openhab.binding.apsystems.internal.ECU.InverterRealtimeData;
import org.openhab.binding.apsystems.internal.ECU.InverterSignalPayload;
import org.openhab.binding.apsystems.internal.ECU.RealtimeDataPayload;
import org.openhab.binding.apsystems.internal.ECU.SystemInfoPayload;
import org.openhab.core.i18n.ConnectionException;
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

    // private apsystemsECUConfiguration config;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ECUResponse lastSystemInfoResponse;

    public apsystemsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command == RefreshType.REFRESH && lastSystemInfoResponse != null) {

            SystemInfoPayload payload = (SystemInfoPayload) lastSystemInfoResponse.getECUResponsePayload();

            switch (channelUID.getId()) {
                case CHANNEL_ECU_CURRENT_DAY_ENERGY:
                    updateState(CHANNEL_ECU_CURRENT_DAY_ENERGY, new DecimalType(payload.getCurrentDayEnergy()));
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

        logger.info("Initializing Bridge");

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::loadBridgeData);

        apsystemsECUConfiguration config = getConfigAs(apsystemsECUConfiguration.class);

        pollingJob = scheduler.scheduleAtFixedRate(this::loadBridgeData, config.pollingInterval, config.pollingInterval,
                TimeUnit.SECONDS);
    }

    private void loadBridgeData() {

        apsystemsECUConfiguration config = getConfigAs(apsystemsECUConfiguration.class);
        ECUConnector connector = new ECUConnector(config.ipAddress, config.port);

        try {

            /*- 
             * check for downtime
             * 4 cases:
             *              Day1 | Day2
             * Case 1:   S --> E | 0        S < E
             * Case 2:     S --> | E        S > E       Start till End on the next Day ( End < Start - otherwise Case 1)
             * Case 3:         E | S -->    S > E       End reached, Start on next Day ( Same as Case 2 POV)
             * Case 4:       S=E |          S = E       Useless but configurable
             */
            boolean bDowntimeIsRunning = isDowntimeRunning(config.getDowntimeStart(), config.getDowntimeEnd());

            if (config.forceNightlyDowntime == true && bDowntimeIsRunning == true) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                logger.info("No ECU update during downtime");
                return;
            }

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
                updateState(CHANNEL_ECU_CURRENT_DAY_ENERGY, new DecimalType(systemInfoPayload.getCurrentDayEnergy()));
                updateState(CHANNEL_ECU_NO_OF_INVERTERS, new DecimalType(systemInfoPayload.getNoOfInverters()));
                updateState(CHANNEL_ECU_NO_OF_INVERTERS_ONLINE,
                        new DecimalType(systemInfoPayload.getInvertersOnline()));

                // Update Inverters
                ECUResponse realtimeResponse = connector.fetchRealTimeData(systemInfoPayload.getECUId());
                RealtimeDataPayload realtimeDataPayload = (RealtimeDataPayload) realtimeResponse
                        .getECUResponsePayload();

                ECUResponse inverterSignalResponse = connector.fetchInverterSignals(systemInfoPayload.getECUId());
                InverterSignalPayload inverterSignalPayload = (InverterSignalPayload) inverterSignalResponse
                        .getECUResponsePayload();

                for (Thing inverterThing : getThing().getThings()) {
                    if (inverterThing.getThingTypeUID().equals(DS3INVERTER_THING_TYPE)) {
                        apsystemsDS3Handler ds3Handler = (apsystemsDS3Handler) inverterThing.getHandler();

                        if (ds3Handler != null) {

                            // Update Realtime Data
                            InverterRealtimeData inverterRealtimeData = null;

                            inverterRealtimeData = realtimeDataPayload.getInverter().stream()
                                    .filter(t -> t.getInverterID().equals(ds3Handler.getSerialNumber())).findFirst()
                                    .orElse(null);

                            ds3Handler.updateRealTimeData(inverterRealtimeData);

                            // Update Inverter Signals
                            Entry<String, Float> inverterSignal = inverterSignalPayload.getInverterSignals().stream()
                                    .filter(t -> t.getKey().equals(ds3Handler.getSerialNumber())).findFirst()
                                    .orElse(null);

                            ds3Handler.updateInverterSignal(inverterSignal);
                        }
                    }
                }
            } else {
                logger.error("failed to load system info");
            }

        } catch (ConnectionException conEx) {
            logger.error("Failed to establish a connection to the bridge: {}", conEx.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } catch (Exception ex) {
            logger.error("Error fetching system info: {}", ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
    }

    public static boolean isDowntimeRunning(LocalTime fromTime, LocalTime toTime, LocalTime... timeToTest) {
        LocalTime testTime = (timeToTest.length >= 1) ? timeToTest[0] : LocalTime.now();

        /*- 
        * check for downtime
        * 4 cases:
        *          |--- Day ---|   
        * Case 1:   S --> E         S < E
        * Case 2:   E     S -->     S > E       Start till End on the next Day ( End < Start - otherwise Case 1)
        * Case 3:       S=E         S = E       Useless but configurable
        */

        boolean bDowntimeIsRunning = false;
        boolean bStartTimePassed = testTime.compareTo(fromTime) > 0 ? true : false;
        boolean bEndTimePassed = testTime.compareTo(toTime) > 0 ? true : false;

        if (fromTime.compareTo(toTime) < 0) {
            // Case 1
            bDowntimeIsRunning = (bStartTimePassed && !bEndTimePassed);
        } else if (fromTime.compareTo(toTime) > 0) {
            // Case 2

            if (testTime.compareTo(toTime) > 0 && testTime.compareTo(fromTime) < 0) {
                bDowntimeIsRunning = false;
            } else {
                bDowntimeIsRunning = true;
            }

        } else {
            // Case 3
            bDowntimeIsRunning = false;
        }

        return bDowntimeIsRunning;
    }
}
