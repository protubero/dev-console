package de.gebit.rp.tool.workbench.viewercommon;

import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemBadge {

    @NotNull
    private ItemBadgeType type;

    @Size(max=100, message = "badge label text max = 100")
    @NotNull
    private String label;

    @Size(max=1000, message = "badge label text max = 1000")
    private String text;

    public static ItemBadge of(ItemBadgeType aType, String aLabel, String aText) {
        ItemBadge badge = new ItemBadge();
        badge.type = Objects.requireNonNull(aType);
        badge.label = Objects.requireNonNull(aLabel);
        badge.text = aText;
        return badge;
    }


}
