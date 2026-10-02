package com.danielnavia.melodygenerator.controller;

import com.danielnavia.melodygenerator.dto.melody.MelodyRequest;
import com.danielnavia.melodygenerator.dto.melody.MelodyResponse;
import com.danielnavia.melodygenerator.service.MelodyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/melody")
public class MelodyController {

    private final MelodyService melodyService;

    public MelodyController(MelodyService melodyService) {
        this.melodyService = melodyService;
    }

    @PostMapping("/generate")
    public ResponseEntity<MelodyResponse> generateMelody(@Valid @RequestBody MelodyRequest request) {

        MelodyResponse melody = melodyService.generateMelody(request);
        return ResponseEntity.status(HttpStatus.OK).body(melody);
    }
}
