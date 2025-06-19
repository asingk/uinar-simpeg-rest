package id.ac.arraniry.simpegapi.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UsulIzin {
    private String id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String nip;
    private String nama;
    private String izinCategoryId;
    private String izinCategoryDesc;
    private Integer status;
    private String fileName;
    private String ket;
    private LocalDateTime updatedDate;
    private Pegawai updatedBy;
    private LocalDateTime createdDate;
    private String createdBy;
}
