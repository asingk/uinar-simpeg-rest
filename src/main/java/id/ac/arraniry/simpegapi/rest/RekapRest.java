package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.service.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.*;

@RestController
@RequestMapping("/rekap")
@CrossOrigin
public class RekapRest {
    private final RekapService rekapService;
    private final GajiService gajiService;
    private final PotonganUnitGajiService potonganUnitGajiService;
    private final RekapRemunPegawaiService rekapRemunPegawaiService;

    public RekapRest(RekapService rekapService, GajiService gajiService, PotonganUnitGajiService potonganUnitGajiService,
                     RekapRemunPegawaiService rekapRemunPegawaiService) {
        this.rekapService = rekapService;
        this.gajiService = gajiService;
        this.potonganUnitGajiService = potonganUnitGajiService;
        this.rekapRemunPegawaiService = rekapRemunPegawaiService;
    }

    @Operation(summary = "Melihat uang makan detail bulanan pegawai")
    @GetMapping("/uang-makan-pegawai/{id}")
    public UangMakanPegawaiVO getUangMakanByNipDetail(@PathVariable String id) {
        return rekapService.getUangMakanByNipDetail(id);
    }

    @Operation(summary = "Melihat remun detail bulanan pegawai")
    @GetMapping("/remun-pegawai/{id}")
    public RekapRemunPegawai getDetailRemunPegawai(@PathVariable String id) {
        return rekapRemunPegawaiService.findById(id);
    }

    @GetMapping("/gaji")
    public List<Rekap> gaji(@RequestParam Integer tahun, @RequestParam(required = false) String kodeAnakSatker) {
        if (kodeAnakSatker == null || kodeAnakSatker.isEmpty()) {
            return rekapService.findByJenisRekapAndTahun("gaji", tahun);
        } else {
            return rekapService.findByJenisRekapAndTahunAndKodeAnakSatker("gaji", tahun, kodeAnakSatker);
        }
    }

    @GetMapping("/potongan-gaji")
    public List<Rekap> potonganGaji(@RequestParam Integer tahun, @RequestParam(required = false) String unitGajiId) {
        if (unitGajiId == null || unitGajiId.isEmpty()) {
            return rekapService.findByJenisRekapAndTahun("pot", tahun);
        } else {
            return rekapService.findByJenisRekapAndTahunAndUnitGajiId("pot", tahun, unitGajiId);
        }
    }

    @GetMapping("/uang-makan")
    public Rekap getUangMakan(@RequestParam Integer tahun, @RequestParam Integer bulan, @RequestParam(required = false) String unitGajiId) {
        return rekapService.findByJenisRekapAndTahunAndBulanAndUnitGaji("um", tahun, bulan, unitGajiId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "uang makan tidak ditemukan"));
    }

    @GetMapping("/remun")
    public List<Rekap> getRemun(@RequestParam Integer tahun, @RequestParam(required = false) String unitRemunId) {
        if (unitRemunId == null || unitRemunId.isEmpty()) {
            return rekapService.findByJenisRekapAndTahun("remun", tahun);
        } else {
            return rekapService.findByJenisRekapAndTahunAndUnitRemunId("remun", tahun, unitRemunId);
        }
    }

    @GetMapping("/selisih-remun")
    public List<Rekap> getSelisihRemun(@RequestParam Integer tahun, @RequestParam(required = false) String unitRemunId) {
        if (unitRemunId == null || unitRemunId.isEmpty()) {
            return rekapService.findByJenisRekapAndTahun("selisih", tahun);
        } else {
            return rekapService.findByJenisRekapAndTahunAndUnitRemunId("selisih", tahun, unitRemunId);
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void generateRekap(@Valid @RequestBody UangMakanCreateRequest request) {
        rekapService.processRekap(request);
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam("jenisRekap") String jenisRekap, @RequestParam("fileType") String fileType,
                                                          @RequestParam(name = "unitGaji", required = false) String unitGaji,
                                                          @RequestParam(name = "unitRemun", required = false) String unitRemun,
                                                          @RequestParam("tahun") Integer tahun, @RequestParam("bulan") Integer bulan) throws JRException, FileNotFoundException {
        File file = rekapService.generateFile(jenisRekap, fileType, unitGaji, unitRemun, tahun, bulan);
        StreamingResponseBody responseBody = outputStream -> Files.copy(file.toPath(), outputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

//    @GetMapping("/stream-generate")
//    public SseEmitter streamGenerate(@RequestParam Integer tahun, @RequestParam Integer bulan, @RequestParam(required = false) String unitGajiId,
//                                     @RequestParam String jenisRekap, @RequestParam(required = false) String unitRemunId) {
//        return rekapService.streamGenerate(tahun, bulan, unitGajiId, jenisRekap, unitRemunId);
//    }

    @GetMapping("/gaji-pegawai/{id}")
    public Gaji getDetailGajiPegawai(@PathVariable String id) {
        return gajiService.findById(id);
    }

    @Operation(summary = "Meng-upload file gaji")
    @PostMapping("/gaji/upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadGajiFile(@RequestParam MultipartFile file, @RequestParam String createdBy) {
        rekapService.processGajiFile(file, createdBy);
    }

    @Operation(summary = "Meng-upload file potongan unit gaji")
    @PostMapping("/potongan-gaji/upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadPotonganGajiFile(@RequestParam MultipartFile file, @RequestParam String createdBy, @RequestParam String unitGajiId,
                                       @RequestParam Integer tahun, @RequestParam Integer bulan) {
        rekapService.processPotonganGajiFile(file, createdBy, unitGajiId, tahun, bulan);
    }

    @DeleteMapping("/{id}/potongan-gaji")
    public void deletePotonganGaji(@PathVariable String id) {
        rekapService.deleteRekapPotonganGaji(id);
    }

    @GetMapping("/{id}/potongan-gaji-pegawai")
    public List<PotonganUnitGaji> getRekapPotonganGajiPegawai(@PathVariable String id) {
        return potonganUnitGajiService.findByRekapId(id);
    }

    @GetMapping("/potongan-gaji-pegawai/{id}")
    public PotonganUnitGaji getDetailPotonganGajiPegawai(@PathVariable String id) {
        return potonganUnitGajiService.findById(id);
    }

    @Operation(summary = "Meng-upload file remun")
    @PostMapping("/remun/upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadRemunFile(@RequestParam MultipartFile file, @RequestParam String createdBy) {
        rekapService.processRemunFile(file, createdBy);
    }

    @DeleteMapping("/{id}/remun")
    public void deleteRemun(@PathVariable String id) {
        rekapService.deleteRemun(id);
    }

    @GetMapping("/{id}/remun-pegawai")
    public List<RekapRemunPegawai> getRekapRemunPegawai(@PathVariable String id) {
        return rekapRemunPegawaiService.findByRekapId(id);
    }

    @Operation(summary = "Meng-upload file selisih remun")
    @PostMapping("/selisih-remun/upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadSelisihRemunFile(@RequestParam MultipartFile file, @RequestParam String createdBy, @RequestParam Integer tahun,
                                       @RequestParam Integer bulan, @RequestParam String unitRemunId) {
        rekapService.processSelisihRemunFile(file, createdBy, tahun, bulan, unitRemunId);
    }

    @DeleteMapping("/{id}/selisih-remun")
    public void deleteSelisihRemun(@PathVariable String id) {
        rekapService.deleteSelisihRemun(id);
    }

    @GetMapping("/{id}/selisih-remun-pegawai")
    public List<SelisihRekapRemunPegawai> getRekapSelisihRemunPegawai(@PathVariable String id) {
        return rekapRemunPegawaiService.findByRekapIdSelisih(id);
    }

    @Operation(summary = "Melihat selisih remun detail bulanan pegawai")
    @GetMapping("/selisih-remun-pegawai/{id}")
    public SelisihRekapRemunPegawai getDetailSelisihRemunPegawai(@PathVariable String id) {
        return rekapRemunPegawaiService.findSelisihRemunPegawaiById(id);
    }

}
