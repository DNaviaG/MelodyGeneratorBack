package com.danielnavia.melodygenerator.model;

import lombok.Getter;

@Getter
public enum ChordQuality {
    MAJOR(4, 7),
    MINOR(3, 7),
    DIMINISHED(3, 6);

    private final int thirdInterval;
    private final int fifthInterval;

    ChordQuality(int thirdInterval, int fifthInterval) {
        this.thirdInterval = thirdInterval;
        this.fifthInterval = fifthInterval;
    }

}