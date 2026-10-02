package com.danielnavia.melodygenerator.dto.melody;

import com.danielnavia.melodygenerator.model.Mode;
import com.danielnavia.melodygenerator.model.NoteName;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MelodyRequest {

    @NotNull
    private NoteName rootNote;

    @NotNull
    private Mode mode;
}
