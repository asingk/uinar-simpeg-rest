package id.ac.arraniry.simpegapi.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class Izin {
    private String id;
    private LocalDate tanggal;
    private String nip;
    private String nama;
    private String izinCategoryId;
    private String izinCategoryDesc;
    private String dateString;

    public Izin(String nip, LocalDate tanggal, KategoriIzin izinCategory, String nama) {
        this.nip = nip;
        this.izinCategoryId = izinCategory.getId();
        this.izinCategoryDesc = izinCategory.getDesc();
        this.tanggal = tanggal;
        this.nama = nama;
    }

    public Izin(UsulIzin usulIzin, LocalDate tanggal) {
        this.tanggal = tanggal;
        this.nip = usulIzin.getNip();
        this.nama = usulIzin.getNama();
        this.izinCategoryId = usulIzin.getIzinCategoryId();
        this.izinCategoryDesc = usulIzin.getIzinCategoryDesc();
        this.dateString = tanggal.toString();
    }
}
