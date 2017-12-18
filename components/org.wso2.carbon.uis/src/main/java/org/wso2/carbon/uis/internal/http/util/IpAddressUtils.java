/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.uis.internal.http.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Optional;

/**
 * Utility functions for Internet Protocol  addresses.
 *
 * @since 0.18.0
 */
public class IpAddressUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpAddressUtils.class);
    private static String localInternetAddress;

    /**
     * Returns Internet Protocol version 4 (IPv4) address of this computer.
     *
     * @return IP address of this computer
     */
    public static Optional<String> getLocalIpAddress() {
        if (localInternetAddress != null) {
            return Optional.of(localInternetAddress);
        }

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            outer_loop:
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if ((inetAddress instanceof Inet4Address) && !inetAddress.isLoopbackAddress()) {
                        localInternetAddress = inetAddress.getHostAddress();
                        break outer_loop;
                    }
                }
            }
        } catch (SocketException e) {
            // Log level DEBUG since this is not a 'breaking' error.
            LOGGER.debug("Cannot access information on network interfaces.", e);
        }
        return Optional.ofNullable(localInternetAddress);
    }
}
