package com.yulongcloud.cloudflareddns.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulongcloud.cloudflareddns.exception.CloudflareException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class CloudflareClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudflareClient.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final String apiToken;
    private final ObjectMapper mapper;

    public CloudflareClient(String apiToken, ObjectMapper mapper) {
        this.apiToken = apiToken;
        this.mapper = mapper;
    }

    /**
     * Finds the record ID(s) by name and type.
     *
     * @param zoneId the domain zone ID
     * @param type   the record type
     * @param name   the record name
     * @return a list of record ID(s) if any
     * @throws IOException
     * @throws CloudflareException
     */
    public List<String> findRecordIds(String zoneId, String type, String name)
            throws IOException, CloudflareException {
        String endpoint = format("https://api.cloudflare.com/client/v4/zones/%s/dns_records", zoneId);
        Map<String, Object> parameters = Map.of(
                "type", type,
                "name", name
        );

        List<Map<String, Object>> result = request("GET", endpoint, parameters, null);

        return result.stream()
                .map(m -> (String) m.get("id"))
                .collect(Collectors.toList());
    }

    /**
     * Creates a new DNS record.
     *
     * @param zoneId           the domain zone ID
     * @param recordAttributes the attributes of the record to create
     * @return a record ID
     * @throws IOException
     * @throws CloudflareException
     */
    public String createRecord(String zoneId, Map<String, Object> recordAttributes)
            throws IOException, CloudflareException {
        String endpoint = format("https://api.cloudflare.com/client/v4/zones/%s/dns_records", zoneId);
        Map<String, Object> parameters = Map.of();
        String requestBody = mapper.writeValueAsString(recordAttributes);

        Map<String, Object> result = request("POST", endpoint, parameters, requestBody);

        return (String) result.get("id");
    }

    /**
     * Updates a DNS record.
     *
     * @param zoneId           the domain zone ID
     * @param recordId         the record ID
     * @param recordAttributes the new attributes
     * @throws IOException
     * @throws CloudflareException
     */
    public void updateRecord(String zoneId, String recordId, Map<String, Object> recordAttributes)
            throws IOException, CloudflareException {
        String endpoint = format("https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s", zoneId, recordId);
        Map<String, Object> parameters = Map.of();
        String requestBody = mapper.writeValueAsString(recordAttributes);

        request("PUT", endpoint, parameters, requestBody);
    }

    @SuppressWarnings("unchecked")
    private <T> T request(String method, String endpoint, Map<String, Object> parameters, String requestBody)
            throws IOException, CloudflareException {
        String url = buildUrl(endpoint, parameters);
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestProperty("Authorization", "Bearer " + apiToken);
        con.setRequestProperty("Content-Type", "application/json");

        LOGGER.debug("Request: method = {}, url = {}, body = {}", method, url, requestBody);
        con.setRequestMethod(method);
        if (requestBody != null && !requestBody.isEmpty()) {
            con.setDoOutput(true);
            con.getOutputStream().write(requestBody.getBytes(CHARSET));
        }

        String response = new String(con.getInputStream().readAllBytes(), CHARSET);
        LOGGER.debug("Response: {}", response);

        HashMap<String, Object> map = mapper.readValue(response, new TypeReference<>() {
        });
        if (Boolean.FALSE.equals(map.get("success"))) {
            throw new CloudflareException(response);
        }
        return (T) map.get("result");

        // TODO add support for paging
    }

    private String buildUrl(String endpoint, Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder(endpoint);
        sb.append("?");
        parameters.forEach((name, value) -> sb
                .append(URLEncoder.encode(name, CHARSET))
                .append("=")
                .append(URLEncoder.encode(String.valueOf(value), CHARSET))
                .append("&")
        );

        return sb.substring(0, sb.length() - 1);
    }
}
