package de.protubero.devconsole.common;

import java.time.LocalDateTime;
import java.util.Arrays;
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

    @NotNull
    @Size(min = 2, max=40, message = "Session id length min = 2, max = 40")
    //TODO: @Pattern -> no whitespace allowed
    private String sessionId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @NotNull
    @Size(min = 2, max=50, message = "Item type length min = 2, max = 50")
    private String type;

    @NotNull
    @Size(min = 5, max=50, message = "Item name length min = 5, max = 50")
    private String name;

    private List<ItemProperty> itemProperties;

    @Size(max=4000, message = "Raw text max = 4000")
    private String raw;


/*
    @Override
    public String toString() {
        return "ConsoleItem{" +
                "id=" + id +
                ", sessionId='" + sessionId + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", itemProperties=" + Arrays.toString(itemProperties) +
                ", raw='" + raw + '\'' +
                '}';
    }

 */
}
