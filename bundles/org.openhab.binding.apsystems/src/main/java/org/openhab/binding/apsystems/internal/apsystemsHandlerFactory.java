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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link apsystemsHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.apsystems", service = ThingHandlerFactory.class)
public class apsystemsHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(DS3INVERTER_THING_TYPE,
            BRIDGE_THING_TYPE);

    private static final Logger logger = LoggerFactory.getLogger(apsystemsHandlerFactory.class);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean retVal = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        return retVal;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            logger.info("New Bridge created!");
            return new apsystemsBridgeHandler((Bridge) thing);
        } else if (DS3INVERTER_THING_TYPE.equals(thingTypeUID)) {
            logger.info("New Inverter created");
            return new apsystemsDS3Handler(thing);
        }

        logger.warn("unknwon thing requested {}", thing.getThingTypeUID());
        return null;
    }
}
