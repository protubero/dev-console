package de.gebit.rp.tool.workbench.viewercommon;

import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RawContentLog {

    @NotNull
    private String label;

    @NotNull
    private String text;

    public static RawContentLog of(String label, String text) {
        RawContentLog log = new RawContentLog();
        log.setLabel(Objects.requireNonNull(label));
        log.setText(Objects.requireNonNull(text));
        return log;
    }
}
