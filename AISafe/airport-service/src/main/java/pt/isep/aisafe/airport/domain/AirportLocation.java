package pt.isep.aisafe.airport.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value object with the geographic data of an airport.
 */
@Embeddable
public class AirportLocation {

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    protected AirportLocation() {
        // for JPA
    }

    public AirportLocation(String city, String country, String region, String timezone,
                           Double latitude, Double longitude) {
        if (isBlank(city) || isBlank(country) || isBlank(region) || isBlank(timezone)) {
            throw new InvalidAirportException("City, country, region and timezone are required");
        }
        if (latitude == null || latitude < -90 || latitude > 90) {
            throw new InvalidAirportException("Latitude must be between -90 and 90");
        }
        if (longitude == null || longitude < -180 || longitude > 180) {
            throw new InvalidAirportException("Longitude must be between -180 and 180");
        }
        this.city = city;
        this.country = country;
        this.region = region;
        this.timezone = timezone;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getTimezone() {
        return timezone;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}
