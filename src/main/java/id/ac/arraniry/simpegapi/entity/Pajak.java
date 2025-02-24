package id.ac.arraniry.simpegapi.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Pajak {
    private String id;
    private Integer statusPegawaiId;
    private String statusPegawaiNama;
    private String golongan;
    private Integer persen;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
}
