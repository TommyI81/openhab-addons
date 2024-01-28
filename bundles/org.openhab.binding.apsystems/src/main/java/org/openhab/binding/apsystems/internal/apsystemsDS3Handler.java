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

import java.util.Map;
import java.util.Map.Entry;

import javax.measure.quantity.Frequency;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.apsystems.internal.ECU.InverterRealtimeData;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link apsystemsDS3Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
public class apsystemsDS3Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(apsystemsDS3Handler.class);
    private apsystemsDS3Configuration config;
    private @Nullable InverterRealtimeData lastInverterRealtimeData;
    private float lastInverterSignal;

    public apsystemsDS3Handler(Thing thing) {
        super(thing);
        config = getConfigAs(apsystemsDS3Configuration.class);
    }

    @Override
    @SuppressWarnings("null")
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command == RefreshType.REFRESH && lastInverterRealtimeData != null) {
            // lastInverterRealtimeData won't be null for sure...

            switch (channelUID.getId()) {
                case CHANNEL_DS3_INVERTER_STATE:
                    updateState(CHANNEL_DS3_INVERTER_STATE,
                            new StringType(lastInverterRealtimeData.getState().toString()));
                    break;

                case CHANNEL_DS3_INVERTER_FREQUENCY:
                    updateState(CHANNEL_DS3_INVERTER_FREQUENCY,
                            new QuantityType<Frequency>(lastInverterRealtimeData.getFrequency(), Units.HERTZ));
                    break;

                case CHANNEL_DS3_INVERTER_TEMPERATURE:
                    updateState(CHANNEL_DS3_INVERTER_TEMPERATURE,
                            new QuantityType<>(lastInverterRealtimeData.getTemperature(), SIUnits.CELSIUS));
                    break;

                case CHANNEL_DS3_INVERTER_POWER1:
                    updateState(CHANNEL_DS3_INVERTER_POWER1,
                            new QuantityType<>(lastInverterRealtimeData.getPower1(), Units.WATT));
                    break;

                case CHANNEL_DS3_INVERTER_POWER2:
                    updateState(CHANNEL_DS3_INVERTER_POWER2,
                            new QuantityType<>(lastInverterRealtimeData.getPower2(), Units.WATT));
                    break;

                case CHANNEL_DS3_INVERTER_VOLTAGE1:
                    updateState(CHANNEL_DS3_INVERTER_VOLTAGE1,
                            new QuantityType<>(lastInverterRealtimeData.getAcVoltage1(), Units.VOLT));
                    break;

                case CHANNEL_DS3_INVERTER_VOLTAGE2:
                    updateState(CHANNEL_DS3_INVERTER_VOLTAGE2,
                            new QuantityType<>(lastInverterRealtimeData.getAcVoltage2(), Units.VOLT));
                    break;

                case CHANNEL_DS3_INVERTER_SIGNAL:
                    updateState(CHANNEL_DS3_INVERTER_SIGNAL,
                            new QuantityType<>(this.lastInverterSignal, Units.PERCENT));
                    break;
            }
        }
    }

    public String getSerialNumber() {
        return getConfigAs(apsystemsDS3Configuration.class).serial;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(apsystemsDS3Configuration.class);

        Map<String, String> proerties = editProperties();
        proerties.put(PROPERTY_DS3_SERIALNUMBER, config.serial);
        updateProperties(proerties);

        logger.info("DS3 SN {} initialized", config.serial);
    }

    public void updateRealTimeData(@Nullable InverterRealtimeData inverterData) {

        if (inverterData == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            this.lastInverterRealtimeData = inverterData;

            Map<String, String> proerties = editProperties();
            proerties.put(PROPERTY_DS3_TYPE, inverterData.getType().toString());
            updateProperties(proerties);

            updateState(CHANNEL_DS3_INVERTER_STATE, new StringType(inverterData.getState().toString()));
            updateState(CHANNEL_DS3_INVERTER_FREQUENCY,
                    new QuantityType<Frequency>(inverterData.getFrequency(), Units.HERTZ));
            updateState(CHANNEL_DS3_INVERTER_TEMPERATURE,
                    new QuantityType<>(inverterData.getTemperature(), SIUnits.CELSIUS));
            updateState(CHANNEL_DS3_INVERTER_POWER1, new QuantityType<>(inverterData.getPower1(), Units.WATT));
            updateState(CHANNEL_DS3_INVERTER_POWER2, new QuantityType<>(inverterData.getPower2(), Units.WATT));
            updateState(CHANNEL_DS3_INVERTER_VOLTAGE1, new QuantityType<>(inverterData.getAcVoltage1(), Units.VOLT));
            updateState(CHANNEL_DS3_INVERTER_VOLTAGE2, new QuantityType<>(inverterData.getAcVoltage2(), Units.VOLT));

            logger.info("DS3 SN {} update data received", config.serial);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    public void updateInverterSignal(@Nullable Entry<String, Float> inverterSignal) {

        if (inverterSignal == null) {
            lastInverterSignal = 0f;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        } else {
            lastInverterSignal = inverterSignal.getValue();
            updateState(CHANNEL_DS3_INVERTER_SIGNAL, new QuantityType<>(this.lastInverterSignal, Units.PERCENT));
        }
    }
}
