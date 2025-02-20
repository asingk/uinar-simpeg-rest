package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class KehadiranUpdateRequest {
    private String idPegawai;
    private LocalDate tanggal;
    private String updatedBy;
}
