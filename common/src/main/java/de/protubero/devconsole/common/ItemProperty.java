package de.protubero.devconsole.common;

import java.util.Objects;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemProperty {

    private String label;
    private String value;

    public static ItemProperty of(String aLabel, String aValue) {
        ItemProperty prop = new ItemProperty();
        prop.label = Objects.requireNonNull(aLabel);
        prop.value = Objects.requireNonNull(aValue);
        return prop;
    }
}
