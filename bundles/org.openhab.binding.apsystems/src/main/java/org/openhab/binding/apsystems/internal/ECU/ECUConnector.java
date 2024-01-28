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
package org.openhab.binding.apsystems.internal.ECU;

import static org.openhab.binding.apsystems.internal.apsystemsBindingConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class ECUConnector {

    private int Port;
    private String Host;
    private @NonNull LocalDateTime LastRequest = LocalDateTime.MIN;
    private final Logger logger = LoggerFactory.getLogger(ECUConnector.class);

    public ECUConnector(String host, int port) {
        this.Host = host;
        this.Port = port;
    }

    public ECUResponse fetchRealTimeData(String ECUId) throws Exception {
        ECURequest ecuRequest = ECURequest.createGetRealTimeDataRequest(ECUId);

        byte[] binResponse = sendRequest(ecuRequest);

        if (binResponse.length > 0) {
            ECUResponse response = ECUResponse.CreateFromBytes(binResponse);
            return response;
        }

        return null;
    }

    public ECUResponse fetchSystemInfo() throws Exception {
        ECURequest ecuRequest = ECURequest.createGestSystemInfoRequest();

        byte[] binResponse = sendRequest(ecuRequest);

        if (binResponse.length > 0) {
            ECUResponse response = ECUResponse.CreateFromBytes(binResponse);
            return response;
        }

        return null;
    }

    public ECUResponse fetchInverterSignals(String ECUId) throws Exception {

        ECURequest ecuRequest = ECURequest.CreateGetInverterSignalLevel(ECUId);

        byte[] binResponse = sendRequest(ecuRequest);

        if (binResponse.length > 0) {
            ECUResponse response = ECUResponse.CreateFromBytes(binResponse);
            return response;
        }

        return null;
    }

    private byte[] sendRequest(ECURequest ecuRequest) throws InterruptedException, UnknownHostException, IOException {

        // relaxation for ECU traffic
        if (Duration.between(this.LastRequest, LocalDateTime.now()).toSeconds() < 3) {
            logger.info("Delaying request for 3 seconds to relax network");
            Thread.sleep(3000);
        }

        Socket socket = new Socket(this.Host, this.Port);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();

        // Send the Request through the socket
        out.write(ecuRequest.GetBytes());
        out.flush();

        int nRead;
        int iEndFlagPos = 0;
        byte[] data = new byte[1];

        @SuppressWarnings("null")
        byte[] streamEndPattern = ECU_PATTERN_END.getBytes(StandardCharsets.UTF_8);

        while (iEndFlagPos < streamEndPattern.length && (nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);

            // Debug only
            // String stroutput = new String(data, StandardCharsets.UTF_8);
            // System.out.println(stroutput);
            // logger.debug(stroutput);

            if (data[0] == streamEndPattern[iEndFlagPos]) {
                iEndFlagPos++;
            } else {
                iEndFlagPos = 0;
            }
        }

        in.close();
        out.close();
        socket.close();

        this.LastRequest = LocalDateTime.now();

        buffer.flush();
        byte[] binResponse = buffer.toByteArray();
        return binResponse;
    }
}
