package service;

import model.Image;
import repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRepository imageRepository;

    @Override
    public Image create(Image image) {
        return imageRepository.save(image);
    }

    @Override
    public List<Image> viewAll() {
        List<Image> list = new ArrayList<>();
        imageRepository.findAll().forEach(list::add);
        return list;
    }

    @Override
    public Image viewById(long id) {
        return imageRepository.findById(id).orElse(null);
    }
}