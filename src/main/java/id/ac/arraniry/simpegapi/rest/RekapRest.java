package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.entity.RekapUMPegawai;
import id.ac.arraniry.simpegapi.dto.RemunPegawaiVO;
import id.ac.arraniry.simpegapi.dto.UangMakanPegawaiVO;
import id.ac.arraniry.simpegapi.entity.JabatanBulanan;
import id.ac.arraniry.simpegapi.entity.RekapRemunPegawai;
import id.ac.arraniry.simpegapi.service.JabatanBulananService;
import id.ac.arraniry.simpegapi.service.RekapRemunPegawaiService;
import id.ac.arraniry.simpegapi.service.RekapUMPegawaiService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/rekap")
@CrossOrigin
public class RekapRest {

    private final RekapUMPegawaiService rekapUMPegawaiService;
    private final JabatanBulananService jabatanBulananService;
    private final RekapRemunPegawaiService rekapRemunPegawaiService;

    public RekapRest(RekapUMPegawaiService rekapUMPegawaiService, JabatanBulananService jabatanBulananService, RekapRemunPegawaiService rekapRemunPegawaiService) {
        this.rekapUMPegawaiService = rekapUMPegawaiService;
        this.jabatanBulananService = jabatanBulananService;
        this.rekapRemunPegawaiService = rekapRemunPegawaiService;
    }

    @Operation(summary = "Melihat uang makan detail bulanan pegawai")
    @GetMapping("/uang-makan-pegawai/{id}")
    public UangMakanPegawaiVO getUangMakanByNipDetail(@PathVariable String id) {
        UangMakanPegawaiVO result = new UangMakanPegawaiVO();
        Optional<RekapUMPegawai> opt = rekapUMPegawaiService.findById(id);
        if (opt.isPresent()) {
            RekapUMPegawai rekapUMPegawai = opt.get();
            result.setNip(rekapUMPegawai.getNip());
            result.setNama(rekapUMPegawai.getNama());
            result.setTahun(rekapUMPegawai.getTahun());
            result.setBulan(rekapUMPegawai.getBulan());
            result.setJumlahHari(rekapUMPegawai.getJumlahHari());
            JabatanBulanan jabatanBulanan = jabatanBulananService.findByNipAndTahunAndBulan(rekapUMPegawai.getNip(), rekapUMPegawai.getTahun(), rekapUMPegawai.getBulan())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "riwayat profil tidak ditemukan"));
            result.setRupiahHarian(jabatanBulanan.getUangMakanHarian());
            result.setRupiahBulanan(result.getRupiahHarian() * result.getJumlahHari());
            result.setPersenPajak(jabatanBulanan.getPajak());
            result.setRupiahPajakBulanan(result.getRupiahBulanan() * result.getPersenPajak()/100);
            result.setThp(result.getRupiahBulanan() - result.getRupiahPajakBulanan());
            result.setCreatedDate(rekapUMPegawai.getCreatedDate());
        }
        return result;
    }

    @Operation(summary = "Melihat remun detail bulanan pegawai")
    @GetMapping("/remun-pegawai/{id}")
    public RemunPegawaiVO getRemunByNipDetail(@PathVariable String id) {
        RemunPegawaiVO result = new RemunPegawaiVO();
        Optional<RekapRemunPegawai> opt = rekapRemunPegawaiService.findById(id);
        if (opt.isPresent()) {
            RekapRemunPegawai rekapRemunPegawai = opt.get();
            result.setNip(rekapRemunPegawai.getNip());
            result.setNama(rekapRemunPegawai.getNama());
            result.setTahun(rekapRemunPegawai.getTahun());
            result.setBulan(rekapRemunPegawai.getBulan());
            result.setD1(rekapRemunPegawai.getD1());
            result.setD2(rekapRemunPegawai.getD2());
            result.setD3(rekapRemunPegawai.getD3());
            result.setD4(rekapRemunPegawai.getD4());
            result.setP1(rekapRemunPegawai.getP1());
            result.setP2(rekapRemunPegawai.getP2());
            result.setP3(rekapRemunPegawai.getP3());
            result.setP4(rekapRemunPegawai.getP4());
            result.setPersenPotongan(rekapRemunPegawai.getPersenPotongan());
            JabatanBulanan jabatanBulanan = jabatanBulananService.findByNipAndTahunAndBulan(rekapRemunPegawai.getNip(), rekapRemunPegawai.getTahun(), rekapRemunPegawai.getBulan())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "riwayat profil tidak ditemukan"));
            result.setGrade(jabatanBulanan.getGrade());
            result.setRemunGrade(jabatanBulanan.getRemunGrade());
            result.setImplementasiRemunPersen(jabatanBulanan.getImplementasiRemun());
            result.setImplementasiRemun(result.getRemunGrade() * result.getImplementasiRemunPersen()/100);
            result.setRemunP1(result.getImplementasiRemun() * 30/100);
            result.setRupiahPotongan((int) Math.round(result.getRemunP1() * result.getPersenPotongan()/100));
            result.setSetelahPotongan(result.getRemunP1() - result.getRupiahPotongan());
            result.setPersenPajak(jabatanBulanan.getPajak());
            result.setRupiahPajak(result.getSetelahPotongan() * result.getPersenPajak()/100);
            result.setNetto(result.getSetelahPotongan() - result.getRupiahPajak());
            result.setCreatedDate(rekapRemunPegawai.getCreatedDate());
        }
        return result;
    }
}
