package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import id.ac.arraniry.simpegapi.service.*;
import id.ac.arraniry.simpegapi.utils.SimpegGraphUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.apache.tika.Tika;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/pegawai")
public class PegawaiRest {

    private static final String FILETYPE_PDF = ".pdf";

    private final LaporanService laporanService;
    private final JabatanBulananService jabatanBulananService;
    private final UsulIzinService usulIzinService;
    private final RekapUMPegawaiService rekapUMPegawaiService;
    private final RekapRemunPegawaiService rekapRemunPegawaiService;
    private final KehadiranUtils kehadiranUtils;
    private final KategoriIzinService kategoriIzinService;
    private final Environment environment;
    private final PemutihanService pemutihanService;
    private final KehadiranService kehadiranService;
    private final GajiService gajiService;
    private final SurveyService surveyResultService;
    private final PotonganUnitGajiService potonganUnitGajiService;

    public PegawaiRest(LaporanService laporanService, JabatanBulananService jabatanBulananService, UsulIzinService usulIzinService,
                       RekapUMPegawaiService rekapUMPegawaiService, RekapRemunPegawaiService rekapRemunPegawaiService, KehadiranUtils kehadiranUtils,
                       KategoriIzinService kategoriIzinService, Environment environment, PemutihanService pemutihanService, KehadiranService kehadiranService,
                       GajiService gajiService, SurveyService surveyResultService, PotonganUnitGajiService potonganUnitGajiService) {
        this.laporanService = laporanService;
        this.jabatanBulananService = jabatanBulananService;
        this.usulIzinService = usulIzinService;
        this.rekapUMPegawaiService = rekapUMPegawaiService;
        this.rekapRemunPegawaiService = rekapRemunPegawaiService;
        this.kehadiranUtils = kehadiranUtils;
        this.kategoriIzinService = kategoriIzinService;
        this.environment = environment;
        this.pemutihanService = pemutihanService;
        this.kehadiranService = kehadiranService;
        this.gajiService = gajiService;
        this.surveyResultService = surveyResultService;
        this.potonganUnitGajiService = potonganUnitGajiService;
    }

    @Operation(summary = "Melihat riwayat kehadiran bulanan pegawai")
    @GetMapping("/{idPegawai}/riwayat-kehadiran")
    public List<LaporanKehadiranVO> getLaporanBulananByNip(@PathVariable String idPegawai, @RequestParam("bulan") int bulan, @RequestParam("tahun") int tahun) {
        return laporanService.findLaporanBulananByNip(idPegawai, bulan, tahun);
    }

    @Operation(summary = "Melihat riwayat kehadiran bulanan pegawai")
    @GetMapping("/{idPegawai}/riwayat-profil")
    public List<JabatanBulanan> getProfilRiwayat(@PathVariable String idPegawai, @RequestParam(required = false) Integer bulan,
                                                 @RequestParam("tahun") int tahun) {
        List<JabatanBulanan> result = new ArrayList<>();
        if (null != bulan) {
            jabatanBulananService.findByNipAndTahunAndBulan(idPegawai, tahun, bulan).ifPresent(result::add);
        } else {
            result = jabatanBulananService.findByNipAndTahun(idPegawai, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
        }
        return result;
    }

    @Operation(summary = "Menambah usulan izin pegawai")
    @PostMapping("/{idPegawai}/usul-izin")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateResponse createUsulIzin(@PathVariable String idPegawai, @RequestParam("izinCategoryId") String izinCategoryId,
                                         @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                         @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                         @RequestParam("file") MultipartFile file) throws IOException {

        Tika tika = new Tika();
        String mimetype = tika.detect(file.getBytes());
        if (!MediaType.APPLICATION_PDF_VALUE.equals(mimetype)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hanya file pdf yang diperbolehkan!");
        }
        String fileName = UUID.randomUUID() + FILETYPE_PDF;
        kehadiranUtils.uploadFile(fileName, file.getInputStream(), environment.getProperty("env.data.cdn-usul-izin-folder"));
        UsulIzin usulIzin = new UsulIzin();
        usulIzin.setNip(idPegawai);
        usulIzin.setNama(SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(idPegawai, environment).getNama());
        usulIzin.setIzinCategoryId(izinCategoryId);
        usulIzin.setIzinCategoryDesc(kategoriIzinService.findById(izinCategoryId).getDesc());
        usulIzin.setStartDate(startDate);
        usulIzin.setEndDate(endDate);
        usulIzin.setStatus(GlobalConstants.STATUS_USUL_IZIN_DIPROSES);
        usulIzin.setFileName(fileName);
        usulIzin.setCreatedBy(idPegawai);
        usulIzin.setCreatedDate(LocalDateTime.now());
        return new CreateResponse(usulIzinService.save(usulIzin));
    }

    @Operation(summary = "Melihat daftar uang makan bulanan pegawai")
    @GetMapping("/{idPegawai}/uang-makan")
    public List<RekapUMPegawai> getUangMakanByNip(@PathVariable String idPegawai, @RequestParam(required = false) Integer bulan,
                                                  @RequestParam("tahun") Integer tahun) {
        List<RekapUMPegawai> result = new ArrayList<>();
        if (null != bulan) {
            rekapUMPegawaiService.findByNipAndTahunAndBulan(idPegawai, tahun, bulan).ifPresent(result::add);
        } else {
            result = rekapUMPegawaiService.findByNipAndTahun(idPegawai, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
        }
        return result;
    }

    @Operation(summary = "Mendapatkan daftar riwayat remon bulanan pegawai")
    @GetMapping("/{idPegawai}/remun")
    public List<RekapRemunPegawai> getRemunByIdPegawai(@PathVariable String idPegawai, @RequestParam(required = false) Integer bulan,
                                                       @RequestParam("tahun") Integer tahun) {
        return rekapRemunPegawaiService.findRemunPegawai(idPegawai, tahun, bulan);
    }

    @Operation(summary = "Mendapatkan daftar riwayat selisih remun bulanan pegawai")
    @GetMapping("/{idPegawai}/selisih-remun")
    public List<SelisihRekapRemunPegawai> getSelisihRemunByIdPegawai(@PathVariable String idPegawai, @RequestParam(required = false) Integer bulan,
                                                       @RequestParam("tahun") Integer tahun) {
        return rekapRemunPegawaiService.findSelisihRemunPegawai(idPegawai, tahun, bulan);
    }

    @Operation(summary = "Melihat status kehadiran pengguna saat ini")
    @GetMapping("/{idPegawai}/kehadiran/search")
    public KehadiranResponse cekStatusKehadiran(
            @PathVariable String idPegawai, @RequestParam("status") String status,
            @RequestParam("tanggal") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tanggal) {

//		String nip = "198703222019031010";
//		tanggal = LocalDate.of(2023, 9, 12);
//		status = "PULANG";
        KehadiranVO hadir = new KehadiranVO();
        Pemutihan pemutihan = pemutihanService.findByTanggalAndStatus(tanggal, status);
        if(null != pemutihan) {
            hadir.setId(pemutihan.getId());
            hadir.setStatus(GlobalConstants.STATUS_PEMUTIHAN);
        } else {
            hadir = kehadiranService.findByNipAndStatusAndTanggal(idPegawai, status, tanggal);
        }
        return new KehadiranResponse(hadir);
    }

    @Operation(summary = "Menambah riwayat jabatan pegawai bulanan")
    @PostMapping("/{idPegawai}/riwayat-profil")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateResponse createProfilRiwayat(@PathVariable String idPegawai, @Valid @RequestBody JabBulCreateRequest request) {
        return jabatanBulananService.create(idPegawai, request);
    }

    @GetMapping("/{idPegawai}/gaji")
    public List<Gaji> getGajiByNip(@PathVariable String idPegawai, @RequestParam Integer tahun, @RequestParam(name = "bulan", required = false) Integer bulan) {
        return gajiService.findGajiPegawai(idPegawai, tahun, bulan);
    }

    @GetMapping("/{idPegawai}/findTodayAnswer")
    public SurveyResult findTodayAnswerByNip(@PathVariable String idPegawai) {
        return surveyResultService.findTodayAnswerByNip(idPegawai);
    }

    @PostMapping("/{idPegawai}/survey/{idSurvey}/answer")
    public CreateResponse answerSurvey(@PathVariable String idPegawai, @PathVariable String idSurvey, @RequestParam String answer) {
        return surveyResultService.answer(idPegawai, idSurvey, answer);
    }
    @GetMapping("/{idPegawai}/potongan-gaji")
    public List<PotonganUnitGaji> getPotonganGajiByNip(@PathVariable String idPegawai, @RequestParam Integer tahun) {
        return potonganUnitGajiService.findPotonganGajiPegawai(idPegawai, tahun);
    }

}
