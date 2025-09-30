package org.aj;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeoLocator {

    private static final String IPIFY_URL = "https://api.ipify.org";
    private static final String IP_API_URL = "http://ip-api.com/json/%s";

    public static void main(String[] args) {
        try {
            System.out.println("1. Buscando seu IP público...");
            String publicIp = getPublicIp();
            System.out.println("   -> Seu IP público é: " + publicIp);

            System.out.println("\n2. Buscando dados de geolocalização para este IP...");
            IpGeolocation location = getLocationForIp(publicIp);
            System.out.println("   -> Informações encontradas.\n");

            System.out.println(location);

        } catch (IOException | InterruptedException e) {
            System.err.println("Ocorreu um erro durante o processo: " + e.getMessage());
            // e.printStackTrace();
        }
    }

    public static String getPublicIp() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IPIFY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("Falha ao obter IP público. Status: " + response.statusCode());
        }
    }

    public static IpGeolocation getLocationForIp(String ipAddress) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format(IP_API_URL, ipAddress));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Falha na requisição de geolocalização. Código de status: " + response.statusCode());
        }

        Gson gson = new Gson();
        return gson.fromJson(response.body(), IpGeolocation.class);
    }
}
