package ru.bozhov.waterlevelbot.sensor.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GeocodeResponse {

    private Response response;

    @Data
    public static class Response {
        @JsonProperty("GeoObjectCollection")
        private GeoObjectCollection geoObjectCollection;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObjectCollection {
        private List<FeatureMember> featureMember;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeatureMember {
        @JsonProperty("GeoObject")
        private GeoObject geoObject;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeoObject {
        private MetaDataProperty metaDataProperty;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class MetaDataProperty {
            @JsonProperty("GeocoderMetaData")
            private GeocoderMetaData geocoderMetaData;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeocoderMetaData {
        private String text;
        private Address Address;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("Components")
        private List<Component> components;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
        private String kind;
        private String name;
    }

    public String getTitleAddress() {
        if (response != null &&
                response.getGeoObjectCollection() != null &&
                response.getGeoObjectCollection().getFeatureMember() != null &&
                !response.getGeoObjectCollection().getFeatureMember().isEmpty()) {

            FeatureMember feature = response.getGeoObjectCollection().getFeatureMember().get(0);
            if (feature != null &&
                    feature.getGeoObject() != null &&
                    feature.getGeoObject().getMetaDataProperty() != null &&
                    feature.getGeoObject().getMetaDataProperty().getGeocoderMetaData() != null) {

                return feature.getGeoObject().getMetaDataProperty().getGeocoderMetaData().getText();
            }
        }
        return null;
    }


}
