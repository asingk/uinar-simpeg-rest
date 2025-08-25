package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import id.ac.arraniry.simpegapi.service.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/rekap")
@CrossOrigin
public class RekapRest {

    private static final Logger log = LoggerFactory.getLogger(RekapRest.class);

    private static final String PDF_FILE_TYPE = ".pdf";
    private static final String XLSX_FILE_TYPE = ".xlsx";

    private final RekapUMPegawaiService rekapUMPegawaiService;
    private final JabatanBulananService jabatanBulananService;
    private final RekapRemunPegawaiService rekapRemunPegawaiService;
    private final RekapService rekapService;
    private final KehadiranUtils kehadiranUtils;
    private final LaporanService laporanService;
    private final RekapBaruUMPegawaiService rekapBaruUMPegawaiService;
    private final RekapRemunGradeService rekapRemunGradeService;
    private final GajiService gajiService;
    private final Environment environment;

    public RekapRest(RekapUMPegawaiService rekapUMPegawaiService, JabatanBulananService jabatanBulananService,
                     RekapRemunPegawaiService rekapRemunPegawaiService, RekapService rekapService, KehadiranUtils kehadiranUtils,
                     LaporanService laporanService, RekapBaruUMPegawaiService rekapBaruUMPegawaiService, RekapRemunGradeService rekapRemunGradeService,
                     GajiService gajiService, Environment environment) {
        this.rekapUMPegawaiService = rekapUMPegawaiService;
        this.jabatanBulananService = jabatanBulananService;
        this.rekapRemunPegawaiService = rekapRemunPegawaiService;
        this.rekapService = rekapService;
        this.kehadiranUtils = kehadiranUtils;
        this.laporanService = laporanService;
        this.rekapBaruUMPegawaiService = rekapBaruUMPegawaiService;
        this.rekapRemunGradeService = rekapRemunGradeService;
        this.gajiService = gajiService;
        this.environment = environment;
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

    @GetMapping("/gaji")
    public List<Rekap> gaji(@RequestParam Integer tahun, @RequestParam(required = false) String unitGajiId) {
        if (unitGajiId == null) {
            return rekapService.findByJenisRekapAndTahun("gaji", tahun);
        } else {
            return rekapService.findByJenisRekapAndTahunAndUnitGajiId("gaji", tahun, unitGajiId);
        }
    }

    @GetMapping("/uang-makan")
    public Rekap getUangMakan(@RequestParam Integer tahun, @RequestParam Integer bulan, @RequestParam(required = false) String unitGajiId) {
        return rekapService.findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId("um", tahun, bulan, unitGajiId, null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "uang makan tidak ditemukan"));
    }

    @GetMapping("/remun")
    public Rekap getRemun(@RequestParam Integer tahun, @RequestParam Integer bulan, @RequestParam(required = false) String unitRemunId) {
        return rekapService.findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId("remun", tahun, bulan, null, unitRemunId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "remun tidak ditemukan"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void generateRekap(@Valid @RequestBody UangMakanCreateRequest request) {
        Rekap rekap = new Rekap();
        Optional<Rekap> rekapOpt = rekapService.findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId(request.getJenisRekap(), request.getTahun(), request.getBulan(), request.getUnitGaji(), request.getUnitRemun());
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(request.getCreatedBy());
        if (rekapOpt.isPresent()) {
            rekap = rekapOpt.get();
        } else {
            rekap.setTahun(request.getTahun());
            rekap.setBulan(request.getBulan());
            rekap.setJenisRekap(request.getJenisRekap());
            rekap.setCreatedBy(pegawaiSimpegVO.getNama());
            rekap.setCreatedDate(LocalDateTime.now());
            if (request.getJenisRekap().equals("um")) {
                if (null == request.getUnitGaji()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unit gaji wajib diisi");
                rekap.setUnitGajiId(request.getUnitGaji());
            } else if (request.getJenisRekap().equals("remun")) {
                if (null == request.getUnitRemun()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unit remun wajib diisi");
                rekap.setUnitRemunId(request.getUnitRemun());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jenis rekap salah!");
            }
        }
        rekap.setLastModifiedBy(pegawaiSimpegVO.getNama());
        rekap.setLastModifiedDate(LocalDateTime.now());
        rekap.setProgress(0);
        rekapService.save(rekap);
        List<Integer> idStatusAsnList = Arrays.asList(GlobalConstants.ID_STATUS_PEGAWAI_PNS, GlobalConstants.ID_STATUS_PEGAWAI_CPNS,
                GlobalConstants.ID_STATUS_PEGAWAI_PNSDPB, GlobalConstants.ID_STATUS_PEGAWAI_PPPK);
        if (request.getJenisRekap().equals("um")) {
            List<JabatanBulanan> jabatanBulanansUmAsn = jabatanBulananService.findByUnitGajiAndTahunAndBulanAndIdStatusPegawaiIn(request.getUnitGaji(),
                    request.getTahun(), request.getBulan(), idStatusAsnList);
            rekap.setProgress(25);
            rekapService.save(rekap);
            List<KehadiranVO> rekapUangMakanAsnBaruVOS = laporanService.findLaporanUangMakanBaru(request.getBulan(), request.getTahun(), jabatanBulanansUmAsn);
            List<RekapUMPegawai> rekapUangMakanAsnLamaVOS = laporanService.generateUangMakanFormatLama(rekapUangMakanAsnBaruVOS, jabatanBulanansUmAsn);
            rekap.setProgress(50);
            rekapService.save(rekap);
            rekapBaruUMPegawaiService.deleteByTahunAndBulanAndUnitGaji(request.getTahun(), request.getBulan(), request.getUnitGaji());
            rekapBaruUMPegawaiService.saveAll(rekapUangMakanAsnBaruVOS, jabatanBulanansUmAsn);
            rekap.setProgress(75);
            rekapService.save(rekap);
            rekapUMPegawaiService.deleteByTahunAndBulanAndUnitGaji(request.getTahun(), request.getBulan(), request.getUnitGaji());
            rekapUMPegawaiService.saveAll(rekapUangMakanAsnLamaVOS);
            rekap.setProgress(100);
            rekapService.save(rekap);
        } else if (request.getJenisRekap().equals("remun")) {
            List<JabatanBulanan> jabatanBulanans = jabatanBulananService.findRemunByUnitRemunAndTahunAndBulan(request.getUnitRemun(), request.getTahun(),
                    request.getBulan());
            jabatanBulanans.removeIf(row -> row.getJenisJabatan().equals(GlobalConstants.TENAGA_KEPENDIDIKAN_CODE)
                    && row.getIdStatusPegawai() == GlobalConstants.ID_STATUS_PEGAWAI_NONASN);
            rekap.setProgress(20);
            rekapService.save(rekap);
            List<RekapRemunPegawai> rekapRemunPegawaiList = laporanService.findLaporanRemon(request.getBulan(), request.getTahun(), jabatanBulanans);
            rekap.setProgress(40);
            rekapService.save(rekap);
            rekapRemunPegawaiService.deleteByTahunAndBulanAndUnitRemun(request.getTahun(), request.getBulan(), request.getUnitRemun());
            rekapRemunPegawaiService.saveAll(rekapRemunPegawaiList);
            rekap.setProgress(60);
            rekapService.save(rekap);
            List<RekapRemunGrade> rekapRemunGradeList = laporanService.findRekapRemonGrade(request.getBulan(), request.getTahun(), request.getUnitRemun(),
                    rekapRemunPegawaiList);
            rekap.setProgress(80);
            rekapService.save(rekap);
            rekapRemunGradeService.deleteByTahunAndBulanAndUnitRemun(request.getTahun(), request.getBulan(), request.getUnitRemun());
            rekapRemunGradeService.saveAll(rekapRemunGradeList);
            rekap.setProgress(100);
            rekapService.save(rekap);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jenis rekap salah!");
        }
    }

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@RequestParam("jenisRekap") String jenisRekap, @RequestParam("fileType") String fileType,
                                                          @RequestParam(name = "unitGaji", required = false) String unitGaji,
                                                          @RequestParam(name = "unitRemun", required = false) String unitRemun,
                                                          @RequestParam(name = "statusPegawai", required = false) String statusPegawai,
                                                          @RequestParam("tahun") Integer tahun, @RequestParam("bulan") Integer bulan) throws JRException, FileNotFoundException {
        Map<String, Object> parameters = new HashMap<>();
        String judulUnitGaji;
        String judulJenisRekap;
        if (jenisRekap.equals("remun")) {
            judulUnitGaji = null != unitRemun ? unitRemun : "UIN Ar-Raniry";
            judulJenisRekap = "Remun";
        } else if (jenisRekap.equals("remun_grade")) {
            judulUnitGaji = null != unitRemun ? unitRemun : "UIN Ar-Raniry";
            judulJenisRekap = "Remun Grade";
        } else {
            judulUnitGaji = null != unitGaji ? unitGaji : "UIN Ar-Raniry";
            judulJenisRekap = "Uang Makan";
        }
        parameters.put("title", judulJenisRekap + " " + judulUnitGaji + " " + kehadiranUtils.namaBulan(bulan) + " " + tahun);
        parameters.put("ttdNama", environment.getProperty("env.data.ttd-nama"));
        parameters.put("ttdNip", environment.getProperty("env.data.ttd-nip"));
        parameters.put("ttdKota", environment.getProperty("env.data.ttd-kota"));
        parameters.put("ttdUrl", environment.getProperty("env.data.ttd-url"));
        if (fileType.equalsIgnoreCase("xlsx")) parameters.put(JRParameter.IS_IGNORE_PAGINATION, true);
        InputStream reportStream;
        JRBeanCollectionDataSource beanCollectionDataSource;
        switch (jenisRekap) {
            case "um_baru" -> {
                if (null == statusPegawai) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status pegawai wajib diisi!");
                }
                reportStream = getClass().getResourceAsStream("/kehadiran_api_uang_makan_baru.jasper");
                List<RekapBaruUMPegawai> rekapBaruUMPegawaiList;
                if (null != unitGaji && !unitGaji.isEmpty()) {
                    rekapBaruUMPegawaiList = rekapBaruUMPegawaiService.findByTahunAndBulanAndStatusPegawaiAndUnitGaji(tahun, bulan, statusPegawai, judulUnitGaji);
                } else {
                    rekapBaruUMPegawaiList = rekapBaruUMPegawaiService.findByTahunAndBulanAndStatusPegawai(tahun, bulan, statusPegawai);
                }
                beanCollectionDataSource = new JRBeanCollectionDataSource(rekapBaruUMPegawaiList);
            }
            case "um_lama" -> {
                if (null == statusPegawai) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "status pegawai wajib diisi!");
                }
                reportStream = getClass().getResourceAsStream("/kehadiran_api_uang_makan_lama.jasper");
                List<RekapUMPegawai> rekapUMPegawaiList;
                if (null != unitGaji && !unitGaji.isEmpty()) {
                    rekapUMPegawaiList = rekapUMPegawaiService.findByTahunAndBulanAndStatusPegawaiAndUnitGaji(tahun, bulan, statusPegawai, judulUnitGaji);
                } else {
                    rekapUMPegawaiList = rekapUMPegawaiService.findByTahunAndBulanAndStatusPegawai(tahun, bulan, statusPegawai);
                }
                beanCollectionDataSource = new JRBeanCollectionDataSource(rekapUMPegawaiList);
            }
            case "remun" -> {
                List<RekapRemunPegawai> rekapRemunPegawaiList;
                if (fileType.equalsIgnoreCase("pdf")) {
                    reportStream = getClass().getResourceAsStream("/kehadiran_api_remon_pdf.jasper");
                } else if (fileType.equalsIgnoreCase("xlsx")) {
                    reportStream = getClass().getResourceAsStream("/kehadiran_api_remon_xlsx.jasper");
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fileType salah!");
                }
                if (null != unitRemun && !unitRemun.isEmpty()) {
                    rekapRemunPegawaiList = rekapRemunPegawaiService.findByTahunAndBulanAndUnitRemun(tahun, bulan, unitRemun);
                } else {
                    rekapRemunPegawaiList = rekapRemunPegawaiService.findByTahunAndBulan(tahun, bulan);
                }
                beanCollectionDataSource = new JRBeanCollectionDataSource(rekapRemunPegawaiList);
            }
            case "remun_grade" -> {
                List<RekapRemunGrade> rekapRemunGradeList;
                reportStream = getClass().getResourceAsStream("/kehadiran_api_remon_grade.jasper");
                if (null != unitRemun && !unitRemun.isEmpty()) {
                    rekapRemunGradeList = rekapRemunGradeService.findByTahunAndBulanAndUnitRemun(tahun, bulan, unitRemun);
                } else {
                    rekapRemunGradeList = rekapRemunGradeService.findByTahunAndBulan(tahun, bulan);
                }
                beanCollectionDataSource = new JRBeanCollectionDataSource(rekapRemunGradeList);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jenis rekap salah!");
        }
        parameters.put("CollectionBeanParam", beanCollectionDataSource);
        JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, new JREmptyDataSource());
        String fileName = jenisRekap + "_" + (null != statusPegawai ? statusPegawai + "_" : "") + judulUnitGaji + "_" + tahun + bulan;
        if (fileType.equalsIgnoreCase("pdf")) {
            fileName += PDF_FILE_TYPE;
            JasperExportManager.exportReportToPdfFile(jasperPrint, GlobalConstants.SAVE_FOLDER + File.separator + fileName);
        } else if (fileType.equalsIgnoreCase("xlsx")) {
            fileName += XLSX_FILE_TYPE;
            JRXlsxExporter exporter = new JRXlsxExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(GlobalConstants.SAVE_FOLDER + File.separator + fileName));
            exporter.exportReport();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type " + fileType + " is not supported");
        }
        File file = ResourceUtils.getFile(GlobalConstants.SAVE_FOLDER + File.separator + fileName);
        StreamingResponseBody responseBody = outputStream -> Files.copy(file.toPath(), outputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @GetMapping("/stream-generate")
    public SseEmitter streamGenerate(@RequestParam Integer tahun, @RequestParam Integer bulan, @RequestParam(required = false) String unitGajiId,
                                     @RequestParam String jenisRekap, @RequestParam(required = false) String unitRemunId) {
        SseEmitter emitter = new SseEmitter();
        emitter.onCompletion(() -> log.info("SseEmitter is completed"));
        emitter.onTimeout(() -> log.info("SseEmitter is timed out"));
        emitter.onError((ex) -> log.info("SseEmitter got error:", ex));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                Rekap rekap = new Rekap();
                do {
                    Optional<Rekap> opt = rekapService.findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId(jenisRekap, tahun, bulan, unitGajiId, unitRemunId);
                    if (opt.isPresent()) {
                        rekap = opt.get();
                    }
                    randomDelay();
                    emitter.send(rekap);
                } while (null == rekap.getProgress() || rekap.getProgress() < 100);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        log.info("Controller exits");
        executorService.shutdown();
        return emitter;
    }

    private void randomDelay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @GetMapping("/gaji-pegawai/{id}")
    public Gaji getDetailGajiPegawai(@PathVariable String id) {
        return gajiService.findById(id);
    }

    @Operation(summary = "Meng-upload file gaji")
    @PostMapping("/gaji/upload")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uploadFile(@RequestParam MultipartFile file, @RequestParam String createdBy) throws IOException {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(createdBy);
        List<Gaji> gajiList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        // Baca file Excel langsung dari InputStream
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Ambil sheet pertama

            List<String> targetColumns = Arrays.asList("kdanak", "bulan", "tahun", "nip", "nmpeg", "gjpokok", "tjistri", "tjanak", "tjupns", "tjstruk", "tjfungs",
                    "pembul", "tjberas", "tjpph", "potpfk10", "potpph", "bersih", "bpjs");
            Map<String, Integer> columnIndices = new HashMap<>();

            // Cari index untuk setiap kolom target
            Row headerRow = sheet.getRow(0);
            for (Cell cell : headerRow) {
                String columnName = cell.getStringCellValue();
                if (targetColumns.contains(columnName)) {
                    columnIndices.put(columnName, cell.getColumnIndex());
                }
            }
            // Ambil data dari kolom-kolom target
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Gaji gaji = new Gaji();
                    gaji.setCreatedBy(createdBy);
                    gaji.setCreatedDate(now);
                    for (String columnName : targetColumns) {
                        int colIndex = columnIndices.get(columnName);
                        Cell cell = row.getCell(colIndex);
                        String cellStringValue = getCellValueAsString(cell);
                        switch (columnName) {
                            case  "kdanak":
                                gaji.setKodeAnakSatker(cellStringValue);
                                break;
                            case "bulan":
                                gaji.setBulan(Integer.valueOf(cellStringValue));
                                break;
                            case "tahun":
                                gaji.setTahun(Integer.valueOf(cellStringValue));
                                break;
                            case "nip":
                                gaji.setNip(cellStringValue);
                                break;
                            case "nmpeg":
                                gaji.setNama(cellStringValue);
                                break;
                            case "gjpokok":
                                int gajiPokok = (int) Double.parseDouble(cellStringValue);
                                gaji.setGajiPokok(gajiPokok);
                                break;
                            case "tjistri":
                                int tjIstri = (int) Double.parseDouble(cellStringValue);
                                gaji.setTunjanganIstri(tjIstri);
                                break;
                            case "tjanak":
                                int tjAnak = (int) Double.parseDouble(cellStringValue);
                                gaji.setTunjanganAnak(tjAnak);
                                break;
                            case "tjupns":
                                int tjUmum = (int) Double.parseDouble(cellStringValue);
                                gaji.setTunjanganUmum(tjUmum);
                                break;
                            case "tjstruk":
                                int tjStruk = (int) Double.parseDouble(cellStringValue);
                                gaji.setTunjanganStruktural(tjStruk);
                                break;
                            case "tjfungs":
                                int tjFungs = (int) Double.parseDouble(cellStringValue);
                                gaji.setTunjanganFungsional(tjFungs);
                                break;
                            case "pembul":
                                int pembul = (int) Double.parseDouble(cellStringValue);
                                gaji.setPembulatan(pembul);
                                break;
                            case "tjberas":
                                int tjBeras = (int) Double.parseDouble(cellStringValue);
                                gaji.setTunjanganBeras(tjBeras);
                                break;
                            case "tjpph":
                                int tjPph = (int) Double.parseDouble(cellStringValue);
                                gaji.setTunjanganPajak(tjPph);
                                break;
                            case "potpfk10":
                                int iwp = (int) Double.parseDouble(cellStringValue);
                                gaji.setIwp(iwp);
                                break;
                            case "potpph":
                                int pph = (int) Double.parseDouble(cellStringValue);
                                gaji.setPph(pph);
                                break;
                            case "bersih":
                                int bersih = (int) Double.parseDouble(cellStringValue);
                                gaji.setNetto(bersih);
                                break;
                            case "bpjs":
                                int bpjs = (int) Double.parseDouble(cellStringValue);
                                gaji.setBpjs(bpjs);
                                break;
                            default:
                                break;
                        }
                    }
                    gajiList.add(gaji);
                }
            }
            Rekap rekap = new Rekap();
            Gaji gajiFirst = gajiList.get(0);
            String unitGaji = switch (gajiFirst.getKodeAnakSatker()) {
                case "01" -> "BAUPK";
                case "02" -> "FSH";
                case "03" -> "FTK";
                case "04" -> "FUF";
                case "05" -> "FAH";
                case "06" -> "FDK";
                case "07" -> "FEBI";
                case "08" -> "FST";
                case "09" -> "FPSI";
                case "10" -> "FISIP";
                default -> throw new IllegalStateException("Unexpected value: " + gajiFirst.getKodeAnakSatker());
            };
            Optional<Rekap> rekapOpt = rekapService.findByJenisRekapAndTahunAndBulanAndUnitGajiIdAndUnitRemunId("gaji", gajiFirst.getTahun(), gajiFirst.getBulan(), unitGaji, null);
            if (rekapOpt.isPresent()) {
                rekap = rekapOpt.get();
            } else {
                rekap.setTahun(gajiFirst.getTahun());
                rekap.setBulan(gajiFirst.getBulan());
                rekap.setJenisRekap("gaji");
                rekap.setCreatedBy(pegawaiSimpegVO.getNama());
                rekap.setCreatedDate(now);
                rekap.setUnitGajiId(unitGaji);
            }
            rekap.setLastModifiedBy(pegawaiSimpegVO.getNama());
            rekap.setLastModifiedDate(LocalDateTime.now());
            gajiService.deleteByBulanAndTahunAndKodeAnakSatker(gajiFirst.getBulan(), gajiFirst.getTahun(), gajiFirst.getKodeAnakSatker());
            gajiService.saveAll(gajiList);
            rekap.setProgress(100);
            rekapService.save(rekap);
        }
    }

    // Helper method untuk baca nilai cell
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

}
