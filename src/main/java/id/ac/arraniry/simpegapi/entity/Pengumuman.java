package id.ac.arraniry.simpegapi.entity;

import lombok.Data;

import java.util.List;

@Data
public class Pengumuman {
    private String id;
    private String nama;
    private Boolean isActive;
    private String message;
    private List<StatusPegawai> statusPegawai;
    private List<String> jenisJabatan;
}
