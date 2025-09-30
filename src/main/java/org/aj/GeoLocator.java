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

    // Injetando o HttpClient para permitir testes com mocks
    private final HttpClient httpClient;

    public GeoLocator(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    // Construtor padrão
    public GeoLocator() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public static void main(String[] args) {
        GeoLocator geoLocator = new GeoLocator();
        try {
            System.out.println("1. Buscando seu IP público...");
            String publicIp = geoLocator.getPublicIp();
            System.out.println("   -> Seu IP público é: " + publicIp);

            System.out.println("\n2. Buscando dados de geolocalização para este IP...");
            IpGeolocation location = geoLocator.getLocationForIp(publicIp);
            System.out.println("   -> Informações encontradas:\n");

            System.out.println(location);

        } catch (IOException | InterruptedException e) {
            System.err.println("Ocorreu um erro durante o processo: " + e.getMessage());
        }
    }

    public String getPublicIp() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(IPIFY_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new IOException("Falha ao obter IP público. Status: " + response.statusCode());
        }
    }

    public IpGeolocation getLocationForIp(String ipAddress) throws IOException, InterruptedException {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("O endereço de IP não pode ser nulo ou vazio.");
        }

        URI uri = URI.create(String.format(IP_API_URL, ipAddress));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Falha na requisição de geolocalização. Código de status: " + response.statusCode());
        }

        Gson gson = new Gson();
        return gson.fromJson(response.body(), IpGeolocation.class);
    }
}
