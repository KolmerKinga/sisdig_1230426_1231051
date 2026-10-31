package pt.isep.aisafe.aircraft.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "aircraft_models_catalog")
public class AircraftModel {

    @Id
    @Column(name = "model_name", nullable = false, updatable = false)
    private String modelName;

    @Column(nullable = false)
    private String manufacturer;

    @Embedded
    private AircraftSpecs aircraftModelSpecs;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "aircraft_images",
            joinColumns = @JoinColumn(name = "model_name")
    )
    @Column(name = "image_url")
    private List<String> modelImages = new ArrayList<>();

    protected AircraftModel() {}

    public AircraftModel(String modelName, String manufacturer,
                         AircraftSpecs aircraftModelSpecs,
                         List<String> modelImages) {
        if (modelName == null || modelName.isBlank()) {
            throw new IllegalArgumentException("Model name cannot be empty");
        }
        if (manufacturer == null || manufacturer.isBlank()) {
            throw new IllegalArgumentException("Manufacturer cannot be empty");
        }

        this.modelName = modelName;
        this.manufacturer = manufacturer;
        this.aircraftModelSpecs = aircraftModelSpecs != null ? aircraftModelSpecs : new AircraftSpecs();
        if (modelImages != null) {
            this.modelImages.addAll(modelImages);
        }
    }

    public void changeManufacturer(String newManufacturer) {
        if (newManufacturer == null || newManufacturer.isBlank()) {
            throw new IllegalArgumentException("Manufacturer cannot be empty");
        }
        this.manufacturer = newManufacturer;
    }

    public void updateSpecs(AircraftSpecs newSpecs) {
        if (newSpecs == null) {
            throw new IllegalArgumentException("Specs cannot be null");
        }
        this.aircraftModelSpecs = newSpecs;
    }

    public void addImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }
        if (!this.modelImages.contains(imageUrl)) {
            this.modelImages.add(imageUrl);
        }
    }

    public void removeImage(String imageUrl) {
        if (imageUrl != null) {
            this.modelImages.remove(imageUrl);
        }
    }

    public String getModelName() {
        return modelName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public AircraftSpecs getAircraftModelSpecs() {
        return aircraftModelSpecs;
    }

    public List<String> getModelImages() {
        return Collections.unmodifiableList(modelImages);
    }
}
