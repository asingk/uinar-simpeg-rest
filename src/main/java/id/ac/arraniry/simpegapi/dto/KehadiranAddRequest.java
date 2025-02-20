package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class KehadiranAddRequest {
    private String idPegawai;
    private LocalDate tanggal;
    private String status;
    private String createdBy;
}
