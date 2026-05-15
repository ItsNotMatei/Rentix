package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.model.Anunt;
import com.example.demo.model.ImagineAnunt;
import com.example.demo.repository.AnuntRepository;
import com.example.demo.repository.ImagineRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class ImagineService {

    private final ImagineRepository imagineRepo;
    private final AnuntRepository anuntRepo;
    private final Cloudinary cloudinary;

    public ImagineService(ImagineRepository imagineRepo,
                          AnuntRepository anuntRepo,
                          Cloudinary cloudinary) {
        this.imagineRepo = imagineRepo;
        this.anuntRepo = anuntRepo;
        this.cloudinary = cloudinary;
    }

    public ImagineAnunt uploadImage(Long anuntId, MultipartFile file) {

        Anunt anunt = anuntRepo.findById(anuntId).orElseThrow();

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            String url = uploadResult.get("url").toString();

            ImagineAnunt img = new ImagineAnunt();
            img.setUrl(url);
            img.setAnunt(anunt);

            return imagineRepo.save(img);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed");
        }
    }
}