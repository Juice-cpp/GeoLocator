package org.aj;

import com.google.gson.annotations.SerializedName;

public class IpGeolocation {

    @SerializedName("query")
    private String ipAddress;

    private String status;
    private String country;
    private String regionName;
    private String city;
    private double lat;
    private double lon;
    private String isp;

    public String getStatus() { return status; }
    public String getIpAddress() { return ipAddress; }
    public String getCountry() { return country; }
    public String getRegionName() { return regionName; }
    public String getCity() { return city; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public String getIsp() { return isp; }

    @Override
    public String toString() {
        if ("success".equals(status)) {
            return "Informações de Geolocalização para o IP: " + ipAddress + "\n" +
                    "  País: " + country + "\n" +
                    "  Estado: " + regionName + "\n" +
                    "  Cidade: " + city + "\n" +
                    "  Provedor de Internet: " + isp + "\n" +
                    "  Latitude: " + lat + "\n" +
                    "  Longitude: " + lon;
        } else {
            return "Não foi possível obter informações para o IP: " + ipAddress;
        }
    }
}
