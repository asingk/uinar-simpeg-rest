package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.assembler.UsulIzinModelAssembler;
import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.helper.GlobalConstants;
import id.ac.arraniry.simpegapi.helper.KehadiranUtils;
import id.ac.arraniry.simpegapi.service.*;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/pegawai")
public class PegawaiRest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String FILETYPE_PDF = ".pdf";

    private final LaporanService laporanService;
    private final JabatanBulananService jabatanBulananService;
    private final UsulIzinService usulIzinService;
    private final PagedResourcesAssembler<UsulIzin> pagedResourcesAssembler;
    private final UsulIzinModelAssembler usulIzinModelAssembler;
    private final RekapUMPegawaiService rekapUMPegawaiService;
    private final RekapRemunPegawaiService rekapRemunPegawaiService;
    private final KehadiranUtils kehadiranUtils;
    private final KategoriIzinService kategoriIzinService;
    private final Environment environment;

    public PegawaiRest(LaporanService laporanService, JabatanBulananService jabatanBulananService, UsulIzinService usulIzinService,
                       PagedResourcesAssembler<UsulIzin> pagedResourcesAssembler, UsulIzinModelAssembler usulIzinModelAssembler,
                       RekapUMPegawaiService rekapUMPegawaiService, RekapRemunPegawaiService rekapRemunPegawaiService, KehadiranUtils kehadiranUtils,
                       KategoriIzinService kategoriIzinService, Environment environment) {
        this.laporanService = laporanService;
        this.jabatanBulananService = jabatanBulananService;
        this.usulIzinService = usulIzinService;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
        this.usulIzinModelAssembler = usulIzinModelAssembler;
        this.rekapUMPegawaiService = rekapUMPegawaiService;
        this.rekapRemunPegawaiService = rekapRemunPegawaiService;
        this.kehadiranUtils = kehadiranUtils;
        this.kategoriIzinService = kategoriIzinService;
        this.environment = environment;
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

    @Operation(summary = "Mendapatkan daftar usul izin pegawai")
    @GetMapping("/{idPegawai}/usul-izin")
    public PagedModel<UsulIzinModel> getUsulIzinByNip(@PathVariable String idPegawai, @RequestParam(name = "status", defaultValue = "0,1,2,3") String status,
                                                      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        if(size > 100) size = 100;
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startDate"));
        String[] statusSplitted = status.split(",");
        Set<Integer> statusSet = new HashSet<>();
        for(String i: statusSplitted) {
            try {
                statusSet.add(Integer.parseInt(i));
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status usulan izin salah!");
            }
        }
        Page<UsulIzin> usulIzinPage = usulIzinService.findByNipAndStatusIn(idPegawai, statusSet, pageable);
        return pagedResourcesAssembler.toModel(usulIzinPage, usulIzinModelAssembler);
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
        usulIzin.setNama(kehadiranUtils.getProfilPegawaiFromSimpegGraphql(idPegawai).getNama());
        usulIzin.setIzinCategoryId(izinCategoryId);
        usulIzin.setIzinCategoryDesc(kategoriIzinService.findById(izinCategoryId).getDesc());
        usulIzin.setStartDate(startDate);
        usulIzin.setEndDate(endDate);
        usulIzin.setStatus(GlobalConstants.STATUS_USUL_IZIN_DIPROSES);
        usulIzin.setFileName(fileName);
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
        List<RekapRemunPegawai> result = new ArrayList<>();
        if (null != bulan) {
            rekapRemunPegawaiService.findByNipAndTahunAndBulan(idPegawai, tahun, bulan).ifPresent(result::add);
        } else {
            result = rekapRemunPegawaiService.findByNipAndTahun(idPegawai, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
        }
        return result;
    }

}
