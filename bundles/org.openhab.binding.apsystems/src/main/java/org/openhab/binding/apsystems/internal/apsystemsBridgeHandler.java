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

import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
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

    private @Nullable apsystemsECUConfiguration config;

    public apsystemsBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID arg0, Command arg1) {
        // TODO Auto-generated method stub
        logger.info("Hier gibt's was zu handlen {} --> {}", arg0, arg1);
    }

    @Override
    public void initialize() {
        config = getConfigAs(apsystemsECUConfiguration.class);

        // TODO Auto-generated method stub
        logger.info("Bridge is inizialized");
        updateStatus(ThingStatus.ONLINE);
    }
}
