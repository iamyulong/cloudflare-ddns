package com.yulongcloud.cloudflareddns;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulongcloud.cloudflareddns.client.AwsCheckIpClient;
import com.yulongcloud.cloudflareddns.client.CloudflareClient;
import com.yulongcloud.cloudflareddns.exception.CloudflareException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "cloudflare-ddns", description = "Update Cloudflare DNS records using dynamic IP.")
public class Main implements Callable<Integer> {

    @Option(names = {"--apiKey"}, description = "Cloudflare API key", required = true)
    private String apiKey;

    @Option(names = {"--zoneId"}, description = "Domain zone ID", required = true)
    private String zoneId;

    @Option(names = {"--type"}, description = "DNS record type", required = true)
    private String type;

    @Option(names = {"--name"}, description = "DNS record name", required = true)
    private String name;

    @Override
    public Integer call() throws Exception {
        while (true) {
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Thread.sleep(10_000L);
        }
    }

    private void update() throws IOException, CloudflareException {
        CloudflareClient cloudflareClient = new CloudflareClient(apiKey, new ObjectMapper());
        AwsCheckIpClient checkIpClient = new AwsCheckIpClient();

        Map<String, Object> attributes = Map.of(
                "type", type,
                "name", name,
                "content", checkIpClient.getIp(),
                "ttl", 120,
                "priority", 10,
                "proxied", false
        );

        List<String> zoneIds = cloudflareClient.findRecordIds(zoneId, "A", name);
        if (zoneIds.isEmpty()) {
            cloudflareClient.createRecord(zoneId, attributes);
        } else {
            cloudflareClient.updateRecord(zoneId, zoneIds.get(0), attributes);
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
