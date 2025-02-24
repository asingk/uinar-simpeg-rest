package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UangMakan {
    private String id;
    private String golongan;
    private Integer jumlah;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
