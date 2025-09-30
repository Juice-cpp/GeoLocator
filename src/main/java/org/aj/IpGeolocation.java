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

    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setStatus(String status) { this.status = status; }
    public void setCountry(String country) { this.country = country; }
    public void setRegionName(String regionName) { this.regionName = regionName; }
    public void setCity(String city) { this.city = city; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLon(double lon) { this.lon = lon; }
    public void setIsp(String isp) { this.isp = isp; }

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
