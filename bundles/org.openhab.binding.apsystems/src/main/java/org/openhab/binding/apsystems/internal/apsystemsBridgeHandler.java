package org.openhab.binding.apsystems.internal;

import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.lang.Nullable;

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
