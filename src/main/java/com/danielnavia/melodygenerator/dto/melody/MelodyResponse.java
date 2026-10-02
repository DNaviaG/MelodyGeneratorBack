package com.danielnavia.melodygenerator.dto.melody;

import com.danielnavia.melodygenerator.model.Melody;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MelodyResponse {

    private Melody melody;
}
