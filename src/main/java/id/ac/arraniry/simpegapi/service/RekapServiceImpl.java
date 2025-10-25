package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.repo.RekapRepo;
import id.ac.arraniry.simpegapi.utils.ExcelUtils;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import id.ac.arraniry.simpegapi.utils.KehadiranUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class RekapServiceImpl implements RekapService {

    private static final Logger log = LoggerFactory.getLogger(RekapServiceImpl.class);
    private static final String PDF_FILE_TYPE = ".pdf";
    private static final String XLSX_FILE_TYPE = ".xlsx";

    private final RekapRepo rekapRepo;
    private final GajiService gajiService;
    private final KehadiranUtils kehadiranUtils;
    private final JabatanBulananService jabatanBulananService;
    private final LaporanService laporanService;
    private final RekapBaruUMPegawaiService rekapBaruUMPegawaiService;
    private final RekapUMPegawaiService rekapUMPegawaiService;
    private final RekapRemunPegawaiService rekapRemunPegawaiService;
    private final RekapRemunGradeService rekapRemunGradeService;
    private final Environment environment;
    private final PotonganUnitGajiService potonganUnitGajiService;

    public RekapServiceImpl(RekapRepo rekapRepo, GajiService gajiService, KehadiranUtils kehadiranUtils, JabatanBulananService jabatanBulananService,
                            LaporanService laporanService, RekapBaruUMPegawaiService rekapBaruUMPegawaiService, RekapUMPegawaiService rekapUMPegawaiService,
                            RekapRemunPegawaiService rekapRemunPegawaiService, RekapRemunGradeService rekapRemunGradeService, Environment environment,
                            PotonganUnitGajiService potonganUnitGajiService) {
        this.rekapRepo = rekapRepo;
        this.gajiService = gajiService;
        this.kehadiranUtils = kehadiranUtils;
        this.jabatanBulananService = jabatanBulananService;
        this.laporanService = laporanService;
        this.rekapBaruUMPegawaiService = rekapBaruUMPegawaiService;
        this.rekapUMPegawaiService = rekapUMPegawaiService;
        this.rekapRemunPegawaiService = rekapRemunPegawaiService;
        this.rekapRemunGradeService = rekapRemunGradeService;
        this.environment = environment;
        this.potonganUnitGajiService = potonganUnitGajiService;
    }

    @Override
    public Rekap save(Rekap rekap) {
        return rekapRepo.save(rekap);
    }

    @Override
    public Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitGaji(String jenisRekap, Integer tahun, Integer bulan, String unitGajiId) {
        return rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitGajiId(jenisRekap, tahun, bulan, unitGajiId);
    }

    @Override
    public Optional<Rekap> findByJenisRekapAndTahunAndBulanAndUnitRemun(String jenisRekap, Integer tahun, Integer bulan, String unitRemunId) {
        return rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitRemunId(jenisRekap, tahun, bulan, unitRemunId);
    }

    @Override
    public List<Rekap> findByJenisRekapAndTahun(String jenisRekap, Integer tahun) {
        return rekapRepo.findByJenisRekapAndTahun(jenisRekap, tahun, Sort.by(Sort.Direction.DESC, "bulan"));
    }

    @Override
    public void processGajiFile(MultipartFile file, String createdBy) {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(createdBy);

        // Validasi file
        validateGajiExcel(file);

        // Baca data dari Excel
        List<Map<String, String>> excelData = readGajiXlsx(file);

        if (excelData.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidak ada data yang dapat dibaca dari file");
        }

        // Validasi konsistensi kdanak, bulan, dan tahun
        validateConsistency(excelData);

        List<Gaji> gajiList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Process dan simpan data
        for (Map<String, String> row : excelData) {
            Gaji data = convertToEntity(row);
            validateRequiredFields(data);
            data.setCreatedBy(pegawaiSimpegVO.getNama());
            data.setCreatedDate(now);
            gajiList.add(data);
        }
        saveRekapGaji(gajiList.get(0).getTahun(), gajiList.get(0).getBulan(), gajiList.get(0).getKodeAnakSatker(), pegawaiSimpegVO.getNama(), now);
        gajiService.deleteByBulanAndTahunAndKodeAnakSatker(gajiList.get(0).getBulan(), gajiList.get(0).getTahun(), gajiList.get(0).getKodeAnakSatker());
        gajiService.saveAll(gajiList);
    }

    @Override
    public List<Rekap> findByJenisRekapAndTahunAndKodeAnakSatker(String jenisRekap, Integer tahun, String kodeAnakSatker) {
        return rekapRepo.findByJenisRekapAndTahunAndKodeAnakSatker(jenisRekap, tahun, kodeAnakSatker, Sort.by(Sort.Direction.DESC, "bulan"));
    }

    @Override
    public List<Rekap> findByJenisRekapAndTahunAndUnitGajiId(String jenisRekap, Integer tahun, String unitGajiId) {
        return rekapRepo.findByJenisRekapAndTahunAndUnitGajiId(jenisRekap, tahun, unitGajiId, Sort.by(Sort.Direction.DESC, "bulan"));
    }

    @Override
    public void processRekap(UangMakanCreateRequest request) {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(request.getCreatedBy());
        switch (request.getJenisRekap()) {
            case "um":
                if (null == request.getUnitGaji()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unit gaji wajib diisi");
                generateRekapUangMakan(request, pegawaiSimpegVO);
                break;
            case "remun":
                if (null == request.getUnitRemun()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unit remun wajib diisi");
                generateRekapRemun(request, pegawaiSimpegVO);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jenis rekap salah!");
        }
    }

    @Override
    public SseEmitter streamGenerate(Integer tahun, Integer bulan, String unitGajiId, String jenisRekap, String unitRemunId) {
        SseEmitter emitter = new SseEmitter();
        emitter.onCompletion(() -> log.info("SseEmitter is completed"));
        emitter.onTimeout(() -> log.info("SseEmitter is timed out"));
        emitter.onError((ex) -> log.info("SseEmitter got error:", ex));
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                Rekap rekap = new Rekap();
                do {
                    if (null != unitGajiId) {
                        Optional<Rekap> opt = findByJenisRekapAndTahunAndBulanAndUnitGaji(jenisRekap, tahun, bulan, unitGajiId);
                        if (opt.isPresent()) {
                            rekap = opt.get();
                        }
                    } else if (null != unitRemunId) {
                        Optional<Rekap> opt = findByJenisRekapAndTahunAndBulanAndUnitRemun(jenisRekap, tahun, bulan, unitRemunId);
                        if (opt.isPresent()) {
                            rekap = opt.get();
                        }
                    } else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salah satu dari unit gaji atau unit remun wajib diisi!");
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

    @Override
    public UangMakanPegawaiVO getUangMakanByNipDetail(String id) {
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

    @Override
    public RemunPegawaiVO getRemunByNipDetail(String id) {
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

    @Override
    public File generateFile(String jenisRekap, String fileType, String unitGaji, String unitRemun, Integer tahun, Integer bulan) throws JRException, FileNotFoundException {
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
                reportStream = getClass().getResourceAsStream("/kehadiran_api_uang_makan_baru.jasper");
                List<RekapBaruUMPegawai> rekapBaruUMPegawaiList;
                if (null != unitGaji && !unitGaji.isEmpty()) {
                    rekapBaruUMPegawaiList = rekapBaruUMPegawaiService.findByTahunAndBulanAndUnitGaji(tahun, bulan, judulUnitGaji);
                } else {
                    rekapBaruUMPegawaiList = rekapBaruUMPegawaiService.findByTahunAndBulan(tahun, bulan);
                }
                beanCollectionDataSource = new JRBeanCollectionDataSource(rekapBaruUMPegawaiList);
            }
            case "um_lama" -> {
                reportStream = getClass().getResourceAsStream("/kehadiran_api_uang_makan_lama.jasper");
                List<RekapUMPegawai> rekapUMPegawaiList;
                if (null != unitGaji && !unitGaji.isEmpty()) {
                    rekapUMPegawaiList = rekapUMPegawaiService.findByTahunAndBulanAndUnitGaji(tahun, bulan, judulUnitGaji);
                } else {
                    rekapUMPegawaiList = rekapUMPegawaiService.findByTahunAndBulan(tahun, bulan);
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
        String fileName = jenisRekap + "_" + judulUnitGaji + "_" + tahun + bulan;
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
        return ResourceUtils.getFile(GlobalConstants.SAVE_FOLDER + File.separator + fileName);
    }

    @Override
    public void processPotonganGajiFile(MultipartFile file, String createdBy, String unitGajiId, Integer tahun, Integer bulan) {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(createdBy);

        // Validasi file
        validateGajiExcel(file);

        LocalDateTime now = LocalDateTime.now();

        // Baca data dari Excel menggunakan ExcelUtils
        List<PotonganUnitGaji> excelData = ExcelUtils.readPotonganExcel(file, unitGajiId, tahun, bulan, now, pegawaiSimpegVO.getNama(), environment);

        if (excelData.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidak ada data yang dapat dibaca dari file");
        }

        saveRekapPotonganGaji(tahun, bulan, unitGajiId, pegawaiSimpegVO.getNama(), now);
        potonganUnitGajiService.deleteByBulanAndTahunAndUnitGajiId(bulan, tahun, unitGajiId);
        potonganUnitGajiService.saveAll(excelData);
    }

    @Override
    public void deletePotonganGaji(String unitGajiId, Integer tahun, Integer bulan) {
        potonganUnitGajiService.deleteByBulanAndTahunAndUnitGajiId(bulan, tahun, unitGajiId);
        rekapRepo.deleteByJenisRekapAndTahunAndBulanAndUnitGajiId("pot", tahun, bulan, unitGajiId);
    }

    private void randomDelay() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generateRekapUangMakan(UangMakanCreateRequest request, PegawaiSimpegVO pegawaiSimpegVO) {
        Rekap rekap = new Rekap();
        Optional<Rekap> rekapOpt = rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitGajiId(request.getJenisRekap(), request.getTahun(),
                request.getBulan(), request.getUnitGaji());
        if (rekapOpt.isPresent()) {
            rekap = rekapOpt.get();
        } else {
            rekap.setTahun(request.getTahun());
            rekap.setBulan(request.getBulan());
            rekap.setJenisRekap(request.getJenisRekap());
            rekap.setCreatedBy(pegawaiSimpegVO.getNama());
            rekap.setCreatedDate(LocalDateTime.now());
            rekap.setUnitGajiId(request.getUnitGaji());
        }
        rekap.setLastModifiedBy(pegawaiSimpegVO.getNama());
        rekap.setLastModifiedDate(LocalDateTime.now());
        rekap.setProgress(0);
        save(rekap);
        List<Integer> idStatusAsnList = Arrays.asList(GlobalConstants.ID_STATUS_PEGAWAI_PNS, GlobalConstants.ID_STATUS_PEGAWAI_CPNS,
                GlobalConstants.ID_STATUS_PEGAWAI_PNSDPB, GlobalConstants.ID_STATUS_PEGAWAI_PPPK);
        List<JabatanBulanan> jabatanBulanansUmAsn = jabatanBulananService.findByUnitGajiAndTahunAndBulanAndIdStatusPegawaiIn(request.getUnitGaji(),
                request.getTahun(), request.getBulan(), idStatusAsnList);
        rekap.setProgress(25);
        save(rekap);
        List<KehadiranVO> rekapUangMakanAsnBaruVOS = laporanService.findLaporanUangMakanBaru(request.getBulan(), request.getTahun(), jabatanBulanansUmAsn);
        List<RekapUMPegawai> rekapUangMakanAsnLamaVOS = laporanService.generateUangMakanFormatLama(rekapUangMakanAsnBaruVOS, jabatanBulanansUmAsn);
        rekap.setProgress(50);
        save(rekap);
        rekapBaruUMPegawaiService.deleteByTahunAndBulanAndUnitGaji(request.getTahun(), request.getBulan(), request.getUnitGaji());
        rekapBaruUMPegawaiService.saveAll(rekapUangMakanAsnBaruVOS, jabatanBulanansUmAsn);
        rekap.setProgress(75);
        save(rekap);
        rekapUMPegawaiService.deleteByTahunAndBulanAndUnitGaji(request.getTahun(), request.getBulan(), request.getUnitGaji());
        rekapUMPegawaiService.saveAll(rekapUangMakanAsnLamaVOS);
        rekap.setProgress(100);
        save(rekap);
    }

    private void generateRekapRemun(UangMakanCreateRequest request, PegawaiSimpegVO pegawaiSimpegVO) {
        Rekap rekap = new Rekap();
        Optional<Rekap> rekapOpt = rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitRemunId(request.getJenisRekap(), request.getTahun(),
                request.getBulan(), request.getUnitRemun());
        if (rekapOpt.isPresent()) {
            rekap = rekapOpt.get();
        } else {
            rekap.setTahun(request.getTahun());
            rekap.setBulan(request.getBulan());
            rekap.setJenisRekap(request.getJenisRekap());
            rekap.setCreatedBy(pegawaiSimpegVO.getNama());
            rekap.setCreatedDate(LocalDateTime.now());
            rekap.setUnitRemunId(request.getUnitRemun());
        }
        rekap.setLastModifiedBy(pegawaiSimpegVO.getNama());
        rekap.setLastModifiedDate(LocalDateTime.now());
        rekap.setProgress(0);
        save(rekap);
        List<JabatanBulanan> jabatanBulanans = jabatanBulananService.findRemunByUnitRemunAndTahunAndBulan(request.getUnitRemun(), request.getTahun(),
                request.getBulan());
        jabatanBulanans.removeIf(row -> row.getJenisJabatan().equals(GlobalConstants.TENAGA_KEPENDIDIKAN_CODE)
                && row.getIdStatusPegawai() == GlobalConstants.ID_STATUS_PEGAWAI_NONASN);
        rekap.setProgress(20);
        save(rekap);
        List<RekapRemunPegawai> rekapRemunPegawaiList = laporanService.findLaporanRemon(request.getBulan(), request.getTahun(), jabatanBulanans);
        rekap.setProgress(40);
        save(rekap);
        rekapRemunPegawaiService.deleteByTahunAndBulanAndUnitRemun(request.getTahun(), request.getBulan(), request.getUnitRemun());
        rekapRemunPegawaiService.saveAll(rekapRemunPegawaiList);
        rekap.setProgress(60);
        save(rekap);
        List<RekapRemunGrade> rekapRemunGradeList = laporanService.findRekapRemonGrade(request.getBulan(), request.getTahun(), request.getUnitRemun(),
                rekapRemunPegawaiList);
        rekap.setProgress(80);
        save(rekap);
        rekapRemunGradeService.deleteByTahunAndBulanAndUnitRemun(request.getTahun(), request.getBulan(), request.getUnitRemun());
        rekapRemunGradeService.saveAll(rekapRemunGradeList);
        rekap.setProgress(100);
        save(rekap);
    }

    private void validateGajiExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File tidak boleh kosong");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama file tidak valid");
        }

        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hanya file XLSX yang didukung. File yang diupload: " + fileName);
        }
    }

    private List<Map<String, String>> readGajiXlsx(MultipartFile file) {
        List<Map<String, String>> result = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getLastRowNum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File Excel kosong atau format tidak valid");
            }

            // Baca header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Header row tidak ditemukan");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell).toLowerCase());
            }

            // Baca data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    Map<String, String> rowData = new HashMap<>();

                    for (int j = 0; j < headers.size(); j++) {
                        if (j < row.getLastCellNum()) {
                            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            rowData.put(headers.get(j), getCellValue(cell));
                        }
                    }

                    result.add(rowData);
                }
            }

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error membaca file XLSX: " + e.getMessage());
        }

        return result;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((int) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    // Method untuk validasi konsistensi kdanak, bulan, dan tahun
    private void validateConsistency(List<Map<String, String>> excelData) {

        // Ambil nilai dari baris pertama sebagai referensi
        Map<String, String> firstRow = excelData.get(0);
        String firstKdanak = firstRow.getOrDefault("kdanak", "").trim();
        String firstBulan = firstRow.getOrDefault("bulan", "").trim();
        String firstTahun = firstRow.getOrDefault("tahun", "").trim();

        // Validasi setiap baris
        for (int i = 1; i < excelData.size(); i++) {
            Map<String, String> row = excelData.get(i);
            String currentKdanak = row.getOrDefault("kdanak", "").trim();
            String currentBulan = row.getOrDefault("bulan", "").trim();
            String currentTahun = row.getOrDefault("tahun", "").trim();

            if (!firstKdanak.equals(currentKdanak)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Kolom kdanak tidak konsisten. Baris 1: " + firstKdanak +
                                ", Baris " + (i+1) + ": " + currentKdanak);
            }

            if (!firstBulan.equals(currentBulan)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Kolom bulan tidak konsisten. Baris 1: " + firstBulan +
                                ", Baris " + (i+1) + ": " + currentBulan);
            }

            if (!firstTahun.equals(currentTahun)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Kolom tahun tidak konsisten. Baris 1: " + firstTahun +
                                ", Baris " + (i+1) + ": " + currentTahun);
            }
        }
    }

    private Gaji convertToEntity(Map<String, String> row) {
        Gaji data = new Gaji();

        // Mapping semua kolom
        data.setKodeAnakSatker(getStringValue(row, "kdanak"));
        data.setBulan(getIntegerValue(row, "bulan"));
        data.setTahun(getIntegerValue(row, "tahun"));
        data.setNip(getStringValue(row, "nip"));
        data.setNama(getStringValue(row, "nmpeg"));
        data.setGajiPokok(getBigDecimalValue(row, "gjpokok"));
        data.setTunjanganIstri(getBigDecimalValue(row, "tjistri"));
        data.setTunjanganAnak(getBigDecimalValue(row, "tjanak"));
        data.setTunjanganUmum(getBigDecimalValue(row, "tjupns"));
        data.setTunjanganStruktural(getBigDecimalValue(row, "tjstruk"));
        data.setTunjanganFungsional(getBigDecimalValue(row, "tjfungs"));
        data.setPembulatan(getBigDecimalValue(row, "pembul"));
        data.setTunjanganBeras(getBigDecimalValue(row, "tjberas"));
        data.setTunjanganPajak(getBigDecimalValue(row, "tjpph"));
        data.setIwp(getBigDecimalValue(row, "potpfk10"));
        data.setPph(getBigDecimalValue(row, "potpph"));
        data.setNetto(getBigDecimalValue(row, "bersih"));
        data.setBpjs(getBigDecimalValue(row, "bpjs"));

        return data;
    }

    private void validateRequiredFields(Gaji data) {
        if (data.getNip() == null || data.getNip().trim().isEmpty()) {
            throw new RuntimeException("NIP tidak boleh kosong");
        }
        if (data.getBulan() == null) {
            throw new RuntimeException("Bulan tidak boleh kosong");
        }
        if (data.getTahun() == null) {
            throw new RuntimeException("Tahun tidak boleh kosong");
        }
        if (data.getKodeAnakSatker() == null || data.getKodeAnakSatker().trim().isEmpty()) {
            throw new RuntimeException("Kode anak satker tidak boleh kosong");
        }
    }

    private String getStringValue(Map<String, String> row, String key) {
        return row.getOrDefault(key, "").trim();
    }

    private Integer getIntegerValue(Map<String, String> row, String key) {
        try {
            String value = row.get(key);
            if (value != null && !value.trim().isEmpty()) {
                return Integer.parseInt(value.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            throw new RuntimeException("Format " + key + " tidak valid: " + row.get(key));
        }
        return null;
    }

    private BigDecimal getBigDecimalValue(Map<String, String> row, String key) {
        try {
            String value = row.get(key);
            if (value != null && !value.trim().isEmpty()) {
                String cleanValue = value.replaceAll("[^0-9.]", "");
                if (!cleanValue.isEmpty()) {
                    return new BigDecimal(cleanValue);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Format " + key + " tidak valid: " + row.get(key));
        }
        return BigDecimal.ZERO;
    }

    private void saveRekapGaji(Integer tahun, Integer bulan, String kodeAnakSatker, String nama, LocalDateTime now) {
        Optional<Rekap> rekapOpt = rekapRepo.findByJenisRekapAndTahunAndBulanAndKodeAnakSatker("gaji", tahun, bulan, kodeAnakSatker);
        Rekap rekap = new Rekap();
        if (rekapOpt.isPresent()) {
            rekap = rekapOpt.get();
        } else {
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setJenisRekap("gaji");
            rekap.setCreatedBy(nama);
            rekap.setCreatedDate(now);
            rekap.setKodeAnakSatker(kodeAnakSatker);
        }
        rekap.setLastModifiedBy(nama);
        rekap.setLastModifiedDate(now);
        rekap.setProgress(100);
        save(rekap);
    }

    private void saveRekapPotonganGaji(Integer tahun, Integer bulan, String unitGajiId, String nama, LocalDateTime now) {
        Optional<Rekap> rekapOpt = rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitGajiId("pot", tahun, bulan, unitGajiId);
        Rekap rekap = new Rekap();
        if (rekapOpt.isPresent()) {
            rekap = rekapOpt.get();
        } else {
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setJenisRekap("pot");
            rekap.setCreatedBy(nama);
            rekap.setCreatedDate(now);
            rekap.setUnitGajiId(unitGajiId);
        }
        rekap.setLastModifiedBy(nama);
        rekap.setLastModifiedDate(now);
        rekap.setProgress(100);
        save(rekap);
    }

}
