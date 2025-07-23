package gift.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

public final class LocationGenerator {

    private LocationGenerator() {
        throw new AssertionError("utility class");
    }

    public static URI generate(Long id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id.toString())
                .toUri();
    }
}
