package service;

import model.Image;
import java.util.List;

public interface ImageService {
    Image create(Image image);
    List<Image> viewAll();
    Image viewById(long id);
}