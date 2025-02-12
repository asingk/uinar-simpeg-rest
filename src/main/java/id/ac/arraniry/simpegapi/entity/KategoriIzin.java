package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "izinCategory")
public class KategoriIzin {
    private String id;
    private String desc;
}
