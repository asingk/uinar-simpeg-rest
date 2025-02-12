package id.ac.arraniry.simpegapi.dto;

import id.ac.arraniry.simpegapi.entity.JamKerja;
import lombok.Data;

import java.time.LocalTime;

@Data
public class JamKerjaResponse {
    private String id;
    private Integer hari;
    private LocalTime jamDatangStart;
    private LocalTime jamDatangEnd;
    private LocalTime jamPulangStart;
    private LocalTime jamPulangEnd;
    private LocalTime jamLemburStart;
    private LocalTime jamLemburEnd;
    private Boolean isRamadhan;
    private LocalTime jamDatangBatas;
    private LocalTime jamPulangBatas;

    public JamKerjaResponse(JamKerja jamKerja) {
        this.id = jamKerja.getId();
        this.hari = jamKerja.getHari();
        this.jamDatangStart = LocalTime.parse(jamKerja.getJamDatangStart());
        this.jamDatangEnd = LocalTime.parse(jamKerja.getJamDatangEnd());
        this.jamPulangStart = LocalTime.parse(jamKerja.getJamPulangStart());
        this.jamPulangEnd = LocalTime.parse(jamKerja.getJamPulangEnd());
        this.jamLemburStart = LocalTime.parse(jamKerja.getJamLemburStart());
        this.jamLemburEnd = LocalTime.parse(jamKerja.getJamLemburEnd());
        this.isRamadhan = jamKerja.getIsRamadhan();
        this.jamDatangBatas = LocalTime.parse(jamKerja.getJamDatangBatas());
        this.jamPulangBatas = LocalTime.parse(jamKerja.getJamPulangBatas());
    }
}
