package org.example.what_seoul.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Component
public class GeoJsonLoader {
    public String loadGeoJson() throws IOException {
        ClassPathResource resource = new ClassPathResource("data/seoul_zones.geojson");

        Path tempFile = Files.createTempFile("geojson", ".json");
        Files.copy(resource.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        return new String(Files.readAllBytes(tempFile));
    }
}
