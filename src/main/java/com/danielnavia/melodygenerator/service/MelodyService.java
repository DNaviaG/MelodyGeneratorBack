package com.danielnavia.melodygenerator.service;

import com.danielnavia.melodygenerator.dto.melody.MelodyRequest;
import com.danielnavia.melodygenerator.dto.melody.MelodyResponse;
import com.danielnavia.melodygenerator.model.Melody;
import com.danielnavia.melodygenerator.model.Scale;
import com.danielnavia.melodygenerator.musicLogic.MelodyMaker;
import org.springframework.stereotype.Service;

@Service
public class MelodyService {

    private final MelodyMaker melodyMaker;

    public MelodyService(MelodyMaker melodyMaker) {
        this.melodyMaker = melodyMaker;
    }

    public MelodyResponse generateMelody(MelodyRequest request) {
        Scale scale = new Scale(request.getRootNote(), request.getMode());

        long seed = System.nanoTime();
        Melody melody = melodyMaker.generateMelody(scale, seed);

        return new MelodyResponse(melody);
    }
}
