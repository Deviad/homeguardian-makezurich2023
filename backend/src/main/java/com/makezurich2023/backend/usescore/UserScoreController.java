package com.makezurich2023.backend.usescore;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserScoreController {


@PostMapping
void addTemperatureReading(@RequestBody Map<String, Object> body) {



}


}
