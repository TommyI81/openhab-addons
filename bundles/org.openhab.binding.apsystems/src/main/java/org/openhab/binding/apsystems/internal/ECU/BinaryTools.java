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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class BinaryTools {

    private final static Logger logger = LoggerFactory.getLogger(BinaryTools.class);

    public static Integer TwoBytesToInt(byte[] bytes) {
        if (bytes.length == 1) {
            byte[] newbytes = new byte[2];
            System.arraycopy(bytes, 0, newbytes, 1, 1);
            bytes = newbytes;
        }

        if (bytes.length > 2) {
            // Overflow converting TwoBytesToInt
            logger.warn("BinaryTools.TwoBytesToInt - overflow ({})", bytes.toString());
            return 0xFFFF;
        }

        int i = byteArrayToInt(bytes);
        return i;
    }

    public static LocalDateTime bcdToDateTime(byte[] input) {
        String dateTimeAsString = bcd2Str(input);

        // 2007120612100000 -> 2007-12-06 12:10:00.00
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSS");

        // happens on 2 byte DateCodes like "0715" == 07:15
        if (dateTimeAsString.length() == 4) {
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        }

        // 20230731
        if (dateTimeAsString.length() == 8) {
            formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        }

        // 20230731
        if (dateTimeAsString.length() == 14) {
            formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        }

        return LocalDateTime.parse(dateTimeAsString, formatter);
    }

    /**
     * Converts a byte array to String
     * from www.java2s.com
     * 
     * @param bytes
     * @return
     */
    public static String bcd2Str(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xF0) >>> 4));
            temp.append((byte) (bytes[i] & 0xF));
        }

        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp.toString();
    }

    public static Integer FourBytesToInt(byte[] bytes) {
        if (bytes.length < 4) {
            byte[] newbytes = new byte[4];
            System.arraycopy(bytes, 0, newbytes, 4 - bytes.length, bytes.length);
            bytes = newbytes;
        }

        if (bytes.length > 4) {
            // Overflow converting FourBytesToInt
            logger.warn("BinaryTools.FourBytesToInt - overflow ({})", bytes.toString());
            return Integer.MAX_VALUE;
        }

        int i = byteArrayToInt(bytes);
        return i;
    }

    /**
     * converts a 4 or 2 bytes array to Int
     * copy from a web froum - TODO: find the web page again
     * 
     * @param b
     * @return
     */
    private static int byteArrayToInt(byte[] b) {
        if (b.length == 4)
            return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
        else if (b.length == 2)
            return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

        return 0;
    }
}
