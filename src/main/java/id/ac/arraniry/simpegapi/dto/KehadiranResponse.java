package id.ac.arraniry.simpegapi.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KehadiranResponse {
    private String id;
    private String idPegawai;
    private String nama;
    private LocalDateTime waktu;
    private String status;
    private String jam;

    public KehadiranResponse(KehadiranVO kehadiranVO) {
        this.id = kehadiranVO.getId();
        this.idPegawai = kehadiranVO.getIdPegawai();
        this.nama = kehadiranVO.getNamaPegawai();
        this.waktu = kehadiranVO.getWaktu();
        this.status = kehadiranVO.getStatus();
        this.jam = kehadiranVO.getJam();
    }
}
