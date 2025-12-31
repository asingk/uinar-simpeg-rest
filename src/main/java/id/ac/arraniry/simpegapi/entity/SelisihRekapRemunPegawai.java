package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SelisihRekapRemunPegawai {
    @Id
    private String id;

    // --- Info umum (A–K) ---
    private String jenisJabatan;    // DS / DT / Manejerial / Pelaksana / JF
    private String unitRemun;       // kolom B
    private Integer bulan;           // kolom C
    private String nip;             // kolom E
    private String nama;            // kolom F
    private String jabatan;         // kolom G
    private String kelasJabatan;    // kolom H
    private String kategoriAsn;     // kolom I
    private BigDecimal p1;          // kolom J
    private BigDecimal p2;          // kolom K

    // --- Tarif P2 (L–Q tergantung sheet) ---
    private BigDecimal bkr;              // DS (L)
    private BigDecimal bkd;              // DS (M) / DT (N)
    private BigDecimal skp;              // SKP (var kolom N/O/P tergantung sheet)
    private Integer capaianSkp;          // capaian SKP (kolom O / P etc.)
    private BigDecimal pembayaranSkp;     // DS (P)

    private BigDecimal iku;              // DT/Manejerial (L)
    private BigDecimal pembayaranIku;    // DT/Manejerial (M)
    private BigDecimal pembayaranBkdDt;    // DT (O)
    private BigDecimal lkh;              // Pelaksana (L)
    private BigDecimal pembayaranLkh;    // Pelaksana (M)
    private BigDecimal progress;         // JF (L)
    private BigDecimal pembayaranProgress; // JF (M)
    private Float nilaiProgressMax;       // JF (N)

    // --- Jumlah Tunjangan ---
    private BigDecimal jumlahTunjangan;  // Q / S / R sesuai sheet mapping

    // --- Selisih P2 periode sebelumnya ---
    private Integer volume;                   // R (DS/DT/..)
    private Float capaianBkr;               // S (DS)
    private BigDecimal pembayaranBkr;         // T (DS)
    private Float capaianBkd;               // U (DS) or X/Y (DT)
    private BigDecimal pembayaranBkdDs;  // V / Y (DS)
    private BigDecimal penguranganBkdDt;  // V / Y (DT)
    private Float capaianIku;               // U (DT/Manejerial)
    private Float kelebihanKekuranganIku;   // V (DT/Manejerial)
    private BigDecimal pembayaranKelebihanKekuranganIku; // W (DT) / U (Manejerial)
    private Float capaianLkh;               // S (Pelaksana)
    private Float kelebihanKekuranganLkh;   // T (Pelaksana)
    private BigDecimal pembayaranKelebihanKekuranganLkh; // U (Pelaksana)
    private Float capaianProgress;       // T (JF)
    private Float kelebihanKekuranganProgress; // U (JF)
    private BigDecimal pembayaranKelebihanKekuranganProgress; // V (JF)

    // --- Total & pajak ---
    private BigDecimal bruto;           // W / Z / V / W / W depending on sheet
    private BigDecimal pphRupiah;       // X / AA / W / W / X depending on sheet
    private Float pphPersen;          // AH / AK / AG / AG / AH depending on sheet
    private BigDecimal netto;

    private Integer tahun;
    private String periode;
    private String rekapId;
    private String createdBy;
    private LocalDateTime createdDate;
}
