import org.aj.*;

import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoLocatorTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private GeoLocator geoLocator;

    @BeforeEach
    void setUp() {
        geoLocator = new GeoLocator(mockHttpClient);
    }

    // --- Cenários 1-5: Testes Base ---

    @Test
    @DisplayName("Cenário 1: Deve retornar o IP público com sucesso quando a API responde 200")
    void getPublicIp_ShouldReturnIpAddress_WhenApiSucceeds() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("123.45.67.89");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        String publicIp = geoLocator.getPublicIp();

        assertEquals("123.45.67.89", publicIp);
    }

    @Test
    @DisplayName("Cenário 2: Deve lançar IOException quando a API de IP público retorna erro 500")
    void getPublicIp_ShouldThrowIOException_WhenApiReturnsError() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(500);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        assertThrows(IOException.class, () -> geoLocator.getPublicIp());
    }

    @Test
    @DisplayName("Cenário 3: Deve retornar objeto IpGeolocation com sucesso")
    void getLocationForIp_ShouldReturnGeolocationObject_WhenApiSucceeds() throws IOException, InterruptedException {
        String jsonResponse = "{\"status\":\"success\",\"country\":\"Brazil\",\"regionName\":\"Sao Paulo\",\"city\":\"Sao Paulo\",\"query\":\"200.147.3.10\"}";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        IpGeolocation location = geoLocator.getLocationForIp("200.147.3.10");

        assertNotNull(location);
        assertEquals("success", location.getStatus());
        assertEquals("Brazil", location.getCountry());
        assertEquals("Sao Paulo", location.getCity());
    }

    @Test
    @DisplayName("Cenário 4: Deve retornar objeto com status 'fail' quando a API informa falha")
    void getLocationForIp_ShouldReturnFailStatus_WhenApiIndicatesFailure() throws IOException, InterruptedException {
        String jsonResponse = "{\"status\":\"fail\",\"message\":\"invalid query\"}";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        IpGeolocation location = geoLocator.getLocationForIp("invalid-ip");

        assertNotNull(location);
        assertEquals("fail", location.getStatus());
    }

    @Test
    @DisplayName("Cenário 5: Deve lançar IOException quando a API de geolocalização retorna erro 404")
    void getLocationForIp_ShouldThrowIOException_WhenApiReturnsError() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        assertThrows(IOException.class, () -> geoLocator.getLocationForIp("8.8.8.8"));
    }

    // --- Cenários 6-8: Testes do Método toString() ---

    @Test
    @DisplayName("Cenário 6: toString() deve formatar a saída de sucesso corretamente")
    void toString_ShouldFormatSuccessMessageCorrectly() {
        IpGeolocation location = new IpGeolocation();
        location.setStatus("success");
        location.setIpAddress("8.8.8.8");
        location.setCountry("United States");
        location.setRegionName("California");
        location.setCity("Mountain View");
        location.setIsp("Google LLC");
        location.setLat(37.422);
        location.setLon(-122.084);

        String result = location.toString();

        assertTrue(result.contains("País: United States"));
        assertTrue(result.contains("Cidade: Mountain View"));
    }

    @Test
    @DisplayName("Cenário 7: toString() deve formatar a saída de falha corretamente")
    void toString_ShouldFormatFailureMessageCorrectly() {
        IpGeolocation location = new IpGeolocation();
        location.setStatus("fail");
        location.setIpAddress("127.0.0.1");

        String result = location.toString();

        assertEquals("Não foi possível obter informações para o IP: 127.0.0.1", result);
    }

    @Test
    @DisplayName("Cenário 8: toString() deve lidar com campos nulos em uma resposta de sucesso")
    void toString_ShouldHandleNullFieldsGracefully() {
        IpGeolocation location = new IpGeolocation();
        location.setStatus("success");
        location.setIpAddress("1.2.3.4");
        location.setCountry("Brazil");
        location.setCity(null); // Cidade é nula

        String result = location.toString();

        assertTrue(result.contains("País: Brazil"));
        assertTrue(result.contains("Cidade: null"));
        assertDoesNotThrow(() -> location.toString());
    }

    // --- Cenários 9-12: Testes de Respostas JSON Variadas ---

    @Test
    @DisplayName("Cenário 9: Deve processar JSON com campos faltando (ex: sem regionName)")
    void getLocationForIp_ShouldHandleMissingJsonFields() throws IOException, InterruptedException {
        String jsonWithMissingFields = "{\"status\":\"success\",\"country\":\"Brazil\",\"city\":\"Brasilia\",\"query\":\"200.147.3.10\"}";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonWithMissingFields);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        IpGeolocation location = geoLocator.getLocationForIp("200.147.3.10");

        assertEquals("Brazil", location.getCountry());
        assertNull(location.getRegionName(), "RegionName deve ser nulo pois não estava no JSON");
    }

    @Test
    @DisplayName("Cenário 10: Deve ignorar campos extras no JSON")
    void getLocationForIp_ShouldIgnoreExtraJsonFields() throws IOException, InterruptedException {
        String jsonWithExtraFields = "{\"status\":\"success\",\"country\":\"Canada\",\"extraField\":123,\"query\":\"1.2.3.4\"}";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonWithExtraFields);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        IpGeolocation location = geoLocator.getLocationForIp("1.2.3.4");

        assertEquals("Canada", location.getCountry());
    }

    @Test
    @DisplayName("Cenário 11: Deve lidar com um objeto JSON vazio")
    void getLocationForIp_ShouldHandleEmptyJsonObject() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("{}");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        IpGeolocation location = geoLocator.getLocationForIp("1.2.3.4");

        assertNotNull(location);
        assertNull(location.getStatus());
        assertNull(location.getCountry());
    }

    @Test
    @DisplayName("Cenário 12: Deve lançar exceção para JSON malformado")
    void getLocationForIp_ShouldThrowExceptionForMalformedJson() throws IOException, InterruptedException {
        // Gson lança JsonSyntaxException para JSON inválido
        String malformedJson = "{\"status\":\"success\", \"country\":\"Canada\""; // Falta '}'
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(malformedJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        assertThrows(JsonSyntaxException.class, () -> geoLocator.getLocationForIp("1.2.3.4"));
    }

    // --- Cenários 13-15: Testes de Exceções de Rede ---

    @Test
    @DisplayName("Cenário 13: getPublicIp deve propagar InterruptedException")
    void getPublicIp_ShouldPropagateInterruptedException() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("A thread foi interrompida"));

        assertThrows(InterruptedException.class, () -> geoLocator.getPublicIp());
    }

    @Test
    @DisplayName("Cenário 14: getLocationForIp deve propagar InterruptedException")
    void getLocationForIp_ShouldPropagateInterruptedException() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("A thread foi interrompida"));

        assertThrows(InterruptedException.class, () -> geoLocator.getLocationForIp("8.8.8.8"));
    }

    @Test
    @DisplayName("Cenário 15: Deve lançar IOException para erro 401 Unauthorized")
    void getPublicIp_ShouldThrowIOException_WhenApiReturns401() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(401);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        assertThrows(IOException.class, () -> geoLocator.getPublicIp());
    }

    // --- Cenários 16-17: Testes de Validação de Entradas ---

    @Test
    @DisplayName("Cenário 16: getLocationForIp deve lançar exceção para IP nulo")
    void getLocationForIp_ShouldThrowExceptionForNullIp() {
        assertThrows(IllegalArgumentException.class, () -> geoLocator.getLocationForIp(null));
    }

    @Test
    @DisplayName("Cenário 17: getLocationForIp deve lançar exceção para IP em branco")
    void getLocationForIp_ShouldThrowExceptionForBlankIp() {
        assertThrows(IllegalArgumentException.class, () -> geoLocator.getLocationForIp("   "));
    }

    // --- Cenários 18-20: Testes de Variação ---

    @Test
    @DisplayName("Cenário 18: Deve retornar um endereço IPv6 público com sucesso")
    void getPublicIp_ShouldReturnIPv6Address_WhenApiSucceeds() throws IOException, InterruptedException {
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(ipv6);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        String publicIp = geoLocator.getPublicIp();

        assertEquals(ipv6, publicIp);
    }

    @Test
    @DisplayName("Cenário 19: Deve retornar geolocalização para um IP do Japão com sucesso")
    void getLocationForIp_ShouldReturnGeolocationForJapanIp() throws IOException, InterruptedException {
        String jsonResponse = "{\"status\":\"success\",\"country\":\"Japan\",\"city\":\"Tokyo\",\"query\":\"1.0.0.1\"}";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        IpGeolocation location = geoLocator.getLocationForIp("1.0.0.1");

        assertEquals("Japan", location.getCountry());
        assertEquals("Tokyo", location.getCity());
    }

    @Test
    @DisplayName("Cenário 20: Deve lançar IOException quando a API de geolocalização retorna erro 403 Forbidden")
    void getLocationForIp_ShouldThrowIOException_WhenApiReturns403() throws IOException, InterruptedException {
        when(mockHttpResponse.statusCode()).thenReturn(403);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(mockHttpResponse);

        assertThrows(IOException.class, () -> geoLocator.getLocationForIp("8.8.8.8"));
    }
}