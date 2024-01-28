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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.openhab.binding.apsystems.internal.ECU.BinaryTools;

/**
 *
 * @author Thomas Ilzhoefer - Initial contribution
 */
public class BinToolsTest {

    // Java signed byte really freaks me out

    @Test
    public void FourBytesToInt1() {
        String b = "00000000000000000000000000000001";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(1, iConverted);
    }

    @Test
    public void FourBytesToInt255() {
        String b = "00000000000000000000000011111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(255, iConverted);
    }

    @Test
    public void FourBytesToInt511() {
        String b = "00000000000000000000000111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(511, iConverted);
    }

    @Test
    public void FourBytesToInt131071() {
        String b = "00000000000000011111111111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(131071, iConverted);
    }

    @Test
    public void FourBytesToInt16777215() {
        String b = "00000000111111111111111111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(16777215, iConverted);
    }

    @Test
    public void FourBytesToInt33554431() {
        String b = "00000001111111111111111111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(33554431, iConverted);
    }

    @Test
    public void FourBytesToIntMaximum() {
        String b = "01111111111111111111111111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);

        assertEquals(2147483647, iConverted);
    }

    @Test
    public void FourBytesToIntOverflow() {
        String b = "11111111111111111111111111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.FourBytesToInt(bytes);

        assertEquals(Integer.MAX_VALUE, iConverted);
    }

    @Test
    public void FourBytesToIntMinimum() {
        byte[] bytes = new byte[] { 0, 0, 0, 0 };
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(0, iConverted);
    }

    @Test
    public void FourBytesToIntSample836() {
        // 0 0 3 68 --> 836
        byte[] bytes = new byte[] { 0, 0, 3, 68 };
        int iConverted = BinaryTools.FourBytesToInt(bytes);

        assertEquals(836, iConverted);
    }

    @Test
    public void FourBytesToIntSample326() {
        // 0 0 1 70 --> 326
        byte[] bytes = new byte[] { 0, 0, 1, 70 };
        int iConverted = BinaryTools.FourBytesToInt(bytes);
        assertEquals(326, iConverted);
    }

    @Test
    public void TwoBytesToInt1() {
        String b = "0000000000000001";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.TwoBytesToInt(bytes);
        assertEquals(1, iConverted);
    }

    @Test
    public void TwoBytesToInt255() {
        String b = "0000000011111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.TwoBytesToInt(bytes);
        assertEquals(255, iConverted);
    }

    @Test
    public void TwoBytesToInt511() {
        String b = "0000000111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.TwoBytesToInt(bytes);
        assertEquals(511, iConverted);
    }

    @Test
    public void TwoBytesToInt32767() {
        String b = "0111111111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.TwoBytesToInt(bytes);
        assertEquals(32767, iConverted);
    }

    @Test
    public void TwoBytesToIntOverflow() {
        String b = "1111111111111111";
        byte[] bytes = new BigInteger(b, 2).toByteArray();
        int iConverted = BinaryTools.TwoBytesToInt(bytes);
        assertEquals(65535, iConverted); // 0xFFFF
    }

    @Test
    public void BcdToStringExample1() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("TimeExample_20230727145920.bin");

            String result = BinaryTools.bcd2Str(testbytes);

            assertEquals("20230727145920", result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void BcdToStringExample2() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("TimeExample_20230727151420.bin");

            String result = BinaryTools.bcd2Str(testbytes);

            assertEquals("20230727151420", result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void BcdToStringExample3() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("TimeExample_20230727163922.bin");

            String result = BinaryTools.bcd2Str(testbytes);

            assertEquals("20230727163922", result);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void BcdToDateTimeTest() {
        byte[] testbytes;
        try {
            testbytes = getRessourceAsBytes("TimeExample_20230727163922.bin");

            LocalDateTime result = BinaryTools.bcdToDateTime(testbytes);

            assertEquals(result.getYear(), 2023);
            assertEquals(result.getMonthValue(), 7);
            assertEquals(result.getDayOfMonth(), 27);
            assertEquals(result.getHour(), 16);
            assertEquals(result.getMinute(), 39);
            assertEquals(result.getSecond(), 22);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private static byte[] getRessourceAsBytes(@NonNull String ressourceName) throws IOException {
        InputStream inStream = BinToolsTest.class.getResourceAsStream(ressourceName);
        if (inStream != null) {
            return inStream.readAllBytes();
        }

        return new byte[] {};
    }
}
