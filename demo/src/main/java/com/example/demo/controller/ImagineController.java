package com.example.demo.controller;

import com.example.demo.model.ImagineAnunt;
import com.example.demo.service.ImagineService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/imagini")
@CrossOrigin
public class ImagineController {

    private final ImagineService service;

    public ImagineController(ImagineService service) {
        this.service = service;
    }

    @PostMapping("/{anuntId}")
    public ImagineAnunt upload(@PathVariable Long anuntId,
                           @RequestParam MultipartFile file) {
    return service.uploadImage(anuntId, file);
    }
}