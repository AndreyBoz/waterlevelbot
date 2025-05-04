package ru.bozhov.waterlevelbot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public ResponseEntity test() {
        return ResponseEntity.ok("Hello, dude");
    }
}
