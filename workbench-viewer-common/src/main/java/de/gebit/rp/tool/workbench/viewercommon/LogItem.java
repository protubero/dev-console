package de.gebit.rp.tool.workbench.viewercommon;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LogItem {

    @NotNull
    private String clientId;

    @NotNull
    @Size(min = 2, max=40, message = "Session id length min = 2, max = 40")
    private String sessionId;

    @NotNull
    private String label;

    @NotNull
    private String text;

}
