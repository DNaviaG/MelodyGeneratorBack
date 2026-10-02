package com.danielnavia.melodygenerator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Note {

    private NoteName note;
    private int octave;
    private Duration duration;
}