package model;

import jakarta.persistence.*;
import java.sql.Blob;
import java.util.Date;

@Entity
@Table(name = "image_table")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Lob
    private byte[] image;
    private Date date = new Date();
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public byte[] getImage() {
        return image;
    }
    public void setImage(byte[] image) {
        this.image = image;
    }
    public Date getDate() {
        return date;
    }

}
