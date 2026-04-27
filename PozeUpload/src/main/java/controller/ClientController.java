package controller;

import model.Image;
import service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;

@Controller
public class ClientController {

    @Autowired
    private ImageService imageService;

    @GetMapping("/ping")
    @ResponseBody
    public String hello_world() {
        return "Hello World!";
    }

    @GetMapping("/display")
    public ResponseEntity<byte[]> displayImage(@RequestParam("id") long id) {
        Image image = imageService.viewById(id);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image.getImage());
    }

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView mv = new ModelAndView("index");
        List<Image> imageList = imageService.viewAll();
        mv.addObject("imageList", imageList);
        return mv;
    }

    @GetMapping("/add")
    public ModelAndView addImage() {
        return new ModelAndView("addimage");
    }

    @PostMapping("/add")
    public String addImagePost(HttpServletRequest request, @RequestParam("image") MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();

        Image image = new Image();
        image.setImage(bytes);
        imageService.create(image);
        return "redirect:/";
    }
}
