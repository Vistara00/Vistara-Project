package com.vistara.tourist_tracking_system.controller;

import com.vistara.tourist_tracking_system.service.UserService;
import com.vistara.tourist_tracking_system.service.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visitor")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorService visitorService;
    private final UserService userService;

    // Tourist‑specific endpoints will be added here when needed.
    // For now, check‑in/out are handled by AdminController.
}