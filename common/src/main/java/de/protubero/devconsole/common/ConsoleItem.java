package de.protubero.devconsole.common;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ConsoleItem {

    private long id;

    private int version;

    @NotNull
    private String clientId;

    @NotNull
    @Size(min = 2, max=40, message = "Session id length min = 2, max = 40")
    private String sessionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @NotNull
    @Size(min = 2, max=50, message = "Item type length min = 2, max = 50")
    private String type;

    @NotNull
    @Size(min = 2, max=50, message = "Item name length min = 5, max = 50")
    private String name;

    private List<RawContent> raw;

    private String htmlText;

}
