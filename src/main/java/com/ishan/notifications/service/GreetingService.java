package com.ishan.notifications.service;

import com.ishan.notifications.dto.GreetingResponse;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public GreetingResponse greet(String name) {
        return new GreetingResponse("Hello, " + name.trim() + "!");
    }
}
