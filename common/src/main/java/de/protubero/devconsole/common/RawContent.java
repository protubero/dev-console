package de.protubero.devconsole.common;

import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RawContent {

    @Size(max=100, message = "Raw label text max = 100")
    @NotNull
    private String label;

    @Size(max=4000, message = "Raw value text max = 4000")
    @NotNull
    private String value;

    public static RawContent of(String aLabel, String aValue) {
        RawContent prop = new RawContent();
        prop.label = Objects.requireNonNull(aLabel);
        prop.value = Objects.requireNonNull(aValue);
        return prop;
    }
}
