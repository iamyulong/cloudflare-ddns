package com.yulongcloud.cloudflareddns.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class AwsCheckIpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsCheckIpClient.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final String ENDPOINT = "https://checkip.amazonaws.com/";

    /**
     * Gets the public IP address of this machine.
     *
     * @return an IP address
     * @throws IOException
     */
    public String getIp() throws IOException {
        try (InputStream in = new URL(ENDPOINT).openStream()) {
            String ip = new String(in.readAllBytes(), CHARSET).trim();
            LOGGER.debug("Public IP address: {}", ip);
            return ip;
        }
    }
}
