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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RekapServiceImpl implements RekapService {

    private static final Logger log = LoggerFactory.getLogger(RekapServiceImpl.class);
    private static final String PDF_FILE_TYPE = ".pdf";
    private static final String XLSX_FILE_TYPE = ".xlsx";
    private static final List<String> TARGET_SHEETS = Arrays.asList("DS", "DT", "Manejerial", "Pelaksana", "JF");

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
        validateExcelFile(file);

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

//    @Override
//    public void processRekap(UangMakanCreateRequest request) {
//        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(request.getCreatedBy());
//        switch (request.getJenisRekap()) {
//            case "um":
//                if (null == request.getUnitGaji()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unit gaji wajib diisi");
//                generateRekapUangMakan(request, pegawaiSimpegVO);
//                break;
//            case "remun":
//                if (null == request.getUnitRemun()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unit remun wajib diisi");
//                generateRekapRemun(request, pegawaiSimpegVO);
//                break;
//            default:
//                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "jenis rekap salah!");
//        }
//    }

//    @Override
//    public SseEmitter streamGenerate(Integer tahun, Integer bulan, String unitGajiId, String jenisRekap, String unitRemunId) {
//        SseEmitter emitter = new SseEmitter();
//        emitter.onCompletion(() -> log.info("SseEmitter is completed"));
//        emitter.onTimeout(() -> log.info("SseEmitter is timed out"));
//        emitter.onError((ex) -> log.info("SseEmitter got error:", ex));
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        executorService.execute(() -> {
//            try {
//                Rekap rekap = new Rekap();
//                do {
//                    if (null != unitGajiId) {
//                        Optional<Rekap> opt = findByJenisRekapAndTahunAndBulanAndUnitGaji(jenisRekap, tahun, bulan, unitGajiId);
//                        if (opt.isPresent()) {
//                            rekap = opt.get();
//                        }
//                    } else if (null != unitRemunId) {
//                        Optional<Rekap> opt = findByJenisRekapAndTahunAndBulanAndUnitRemun(jenisRekap, tahun, bulan, unitRemunId);
//                        if (opt.isPresent()) {
//                            rekap = opt.get();
//                        }
//                    } else {
//                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "salah satu dari unit gaji atau unit remun wajib diisi!");
//                    }
//                    randomDelay();
//                    emitter.send(rekap);
//                } while (null == rekap.getProgress() || rekap.getProgress() < 100);
//                emitter.complete();
//            } catch (Exception e) {
//                emitter.completeWithError(e);
//            }
//        });
//        log.info("Controller exits");
//        executorService.shutdown();
//        return emitter;
//    }

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
//        Optional<RekapRemunPegawai> opt = rekapRemunPegawaiService.findById(id);
//        if (opt.isPresent()) {
//            RekapRemunPegawai rekapRemunPegawai = opt.get();
//            result.setNip(rekapRemunPegawai.getNip());
//            result.setNama(rekapRemunPegawai.getNama());
//            result.setTahun(rekapRemunPegawai.getTahun());
//            result.setBulan(rekapRemunPegawai.getBulan());
//            result.setD1(rekapRemunPegawai.getD1());
//            result.setD2(rekapRemunPegawai.getD2());
//            result.setD3(rekapRemunPegawai.getD3());
//            result.setD4(rekapRemunPegawai.getD4());
//            result.setP1(rekapRemunPegawai.getP1());
//            result.setP2(rekapRemunPegawai.getP2());
//            result.setP3(rekapRemunPegawai.getP3());
//            result.setP4(rekapRemunPegawai.getP4());
//            result.setPersenPotongan(rekapRemunPegawai.getPersenPotongan());
//            JabatanBulanan jabatanBulanan = jabatanBulananService.findByNipAndTahunAndBulan(rekapRemunPegawai.getNip(), rekapRemunPegawai.getTahun(), rekapRemunPegawai.getBulan())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "riwayat profil tidak ditemukan"));
//            result.setGrade(jabatanBulanan.getGrade());
//            result.setRemunGrade(jabatanBulanan.getRemunGrade());
//            result.setImplementasiRemunPersen(jabatanBulanan.getImplementasiRemun());
//            result.setImplementasiRemun(result.getRemunGrade() * result.getImplementasiRemunPersen()/100);
//            result.setRemunP1(result.getImplementasiRemun() * 30/100);
//            result.setRupiahPotongan((int) Math.round(result.getRemunP1() * result.getPersenPotongan()/100));
//            result.setSetelahPotongan(result.getRemunP1() - result.getRupiahPotongan());
//            result.setPersenPajak(jabatanBulanan.getPajak());
//            result.setRupiahPajak(result.getSetelahPotongan() * result.getPersenPajak()/100);
//            result.setNetto(result.getSetelahPotongan() - result.getRupiahPajak());
//            result.setCreatedDate(rekapRemunPegawai.getCreatedDate());
//        }
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
        validateExcelFile(file);

        LocalDateTime now = LocalDateTime.now();
        Rekap rekap = saveRekapPotonganGaji(tahun, bulan, unitGajiId, pegawaiSimpegVO.getNama(), now);

        try {
            // Update progress: mulai membaca file (25%)
            rekap.setProgress(25);
            rekap.setErrorMessages(null);
            save(rekap);

            // Baca data dari Excel menggunakan ExcelUtils
            List<PotonganUnitGaji> excelData = ExcelUtils.readPotonganExcel(file, unitGajiId, tahun, bulan, now, pegawaiSimpegVO.getNama(), environment,
                    rekap.getId());

            if (excelData.isEmpty()) {
                rekap.setProgress(0);
                rekap.setErrorMessages("Tidak ada data valid yang dapat dibaca dari file");
                save(rekap);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tidak ada data valid yang dapat dibaca dari file");
            }

            // Update progress: selesai membaca file (50%)
            rekap.setProgress(50);
            save(rekap);

            // Hapus data lama
            potonganUnitGajiService.deleteByRekapId(rekap.getId());

            // Update progress: selesai menghapus data lama (75%)
            rekap.setProgress(75);
            save(rekap);

            // Simpan data baru
            potonganUnitGajiService.saveAll(excelData);

            // Update progress: selesai menyimpan data (100%)
            rekap.setProgress(100);
            rekap.setErrorMessages(null);
            save(rekap);

        } catch (ResponseStatusException e) {
            // Simpan error message ke database
            rekap.setProgress(0);
            rekap.setErrorMessages(e.getReason());
            save(rekap);
            throw e;
        } catch (Exception e) {
            // Simpan error message ke database
            rekap.setProgress(0);
            rekap.setErrorMessages("Error saat memproses file: " + e.getMessage());
            save(rekap);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error saat memproses file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRekapPotonganGaji(String id) {
        potonganUnitGajiService.deleteByRekapId(id);
        rekapRepo.deleteById(id);
    }

    @Override
    @Transactional
    public void processRemunFile(MultipartFile file, String createdBy) {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(createdBy);
        try {
            LocalDateTime now = LocalDateTime.now();

            // First, read the Excel file to get periode and unitRemun info
            Map<String, Object> fileMetadata = extractFileMetadata(file);
            Integer tahun = (Integer) fileMetadata.get("tahun");
            Integer bulan = (Integer) fileMetadata.get("bulan");
            String unitRemunId = (String) fileMetadata.get("unitRemunId");

            if (tahun == null || bulan == null || unitRemunId == null) {
                throw new RuntimeException("Cannot extract tahun, bulan, or unitRemunId from Excel file");
            }

            // Save or update Rekap entity to get rekapId
            Rekap rekap = saveRekapRemun(tahun, bulan, unitRemunId, pegawaiSimpegVO.getNama(), now, false);
            String rekapId = rekap.getId();

            // Delete existing data for this rekapId (for re-upload scenario)
            rekapRemunPegawaiService.deleteByRekapId(rekapId);

            // Read and save all employee data
            List<RekapRemunPegawai> allData = readRemunExcelFile(file, rekapId, pegawaiSimpegVO.getNama(), now);
            rekapRemunPegawaiService.saveAll(allData);

        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
        }
    }

    @Override
    public void processSelisihRemunFile(MultipartFile file, String createdBy, Integer tahun, Integer bulan, String unitRemunId) {
        PegawaiSimpegVO pegawaiSimpegVO = kehadiranUtils.getProfilPegawaiFromSimpegGraphql(createdBy);
        try {
            LocalDateTime now = LocalDateTime.now();

            // Save or update Rekap entity to get rekapId
            Rekap rekap = saveRekapRemun(tahun, bulan, unitRemunId, pegawaiSimpegVO.getNama(), now, true);
            String rekapId = rekap.getId();

            // Delete existing data for this rekapId (for re-upload scenario)
            rekapRemunPegawaiService.deleteSelisihByRekapId(rekapId);

            // Read and save all employee data
            List<SelisihRekapRemunPegawai> allData = readSelisihRemunExcelFile(file, rekapId, tahun, bulan, pegawaiSimpegVO.getNama(), now);
            rekapRemunPegawaiService.saveAllSelisih(allData);

        } catch (IOException e) {
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRemun(String id) {
        rekapRemunPegawaiService.deleteByRekapId(id);
        rekapRepo.deleteById(id);
    }

    @Override
    public void deleteSelisihRemun(String id) {
        rekapRemunPegawaiService.deleteSelisihByRekapId(id);
        rekapRepo.deleteById(id);
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

//    private void generateRekapRemun(UangMakanCreateRequest request, PegawaiSimpegVO pegawaiSimpegVO) {
//        Rekap rekap = new Rekap();
//        Optional<Rekap> rekapOpt = rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitRemunId(request.getJenisRekap(), request.getTahun(),
//                request.getBulan(), request.getUnitRemun());
//        if (rekapOpt.isPresent()) {
//            rekap = rekapOpt.get();
//        } else {
//            rekap.setTahun(request.getTahun());
//            rekap.setBulan(request.getBulan());
//            rekap.setJenisRekap(request.getJenisRekap());
//            rekap.setCreatedBy(pegawaiSimpegVO.getNama());
//            rekap.setCreatedDate(LocalDateTime.now());
//            rekap.setUnitRemunId(request.getUnitRemun());
//        }
//        rekap.setLastModifiedBy(pegawaiSimpegVO.getNama());
//        rekap.setLastModifiedDate(LocalDateTime.now());
//        rekap.setProgress(0);
//        save(rekap);
//        List<JabatanBulanan> jabatanBulanans = jabatanBulananService.findRemunByUnitRemunAndTahunAndBulan(request.getUnitRemun(), request.getTahun(),
//                request.getBulan());
//        jabatanBulanans.removeIf(row -> row.getJenisJabatan().equals(GlobalConstants.TENAGA_KEPENDIDIKAN_CODE)
//                && row.getIdStatusPegawai() == GlobalConstants.ID_STATUS_PEGAWAI_NONASN);
//        rekap.setProgress(20);
//        save(rekap);
//        List<RekapRemunPegawai> rekapRemunPegawaiList = laporanService.findLaporanRemon(request.getBulan(), request.getTahun(), jabatanBulanans);
//        rekap.setProgress(40);
//        save(rekap);
//        rekapRemunPegawaiService.deleteByTahunAndBulanAndUnitRemun(request.getTahun(), request.getBulan(), request.getUnitRemun());
//        rekapRemunPegawaiService.saveAll(rekapRemunPegawaiList);
//        rekap.setProgress(60);
//        save(rekap);
//        List<RekapRemunGrade> rekapRemunGradeList = laporanService.findRekapRemonGrade(request.getBulan(), request.getTahun(), request.getUnitRemun(),
//                rekapRemunPegawaiList);
//        rekap.setProgress(80);
//        save(rekap);
//        rekapRemunGradeService.deleteByTahunAndBulanAndUnitRemun(request.getTahun(), request.getBulan(), request.getUnitRemun());
//        rekapRemunGradeService.saveAll(rekapRemunGradeList);
//        rekap.setProgress(100);
//        save(rekap);
//    }

    private void validateExcelFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File tidak boleh kosong");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nama file tidak valid");
        }

        if (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xlsm")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Hanya file XLSX dan XLSM yang didukung. File yang diupload: " + fileName);
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

    private Rekap saveRekapPotonganGaji(Integer tahun, Integer bulan, String unitGajiId, String nama, LocalDateTime now) {
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
        rekap.setProgress(0);
        rekap.setErrorMessages(null);
        return save(rekap);
    }

    private Rekap saveRekapRemun(Integer tahun, Integer bulan, String unitRemunId, String nama, LocalDateTime now, Boolean isSelisih) {
        Rekap rekap = new Rekap();
        String jenisRekap = isSelisih ? "selisih" : "remun";
        Optional<Rekap> rekapOpt = rekapRepo.findByJenisRekapAndTahunAndBulanAndUnitRemunId(jenisRekap, tahun, bulan, unitRemunId);
        if (rekapOpt.isPresent()) {
            rekap = rekapOpt.get();
        } else {
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setJenisRekap(jenisRekap);
            rekap.setCreatedBy(nama);
            rekap.setCreatedDate(now);
            rekap.setUnitRemunId(unitRemunId);
        }
        rekap.setLastModifiedBy(nama);
        rekap.setLastModifiedDate(now);
        rekap.setProgress(100);
        return save(rekap);
    }

    // check if the row is empty
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell c : row) {
            if (c != null && c.getCellType() != CellType.BLANK) {
                String s = c.toString();
                if (s != null && !s.trim().isEmpty()) return false;
            }
        }
        return true;
    }

    private Object getCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            CellValue evaluated = evaluator.evaluate(cell);
            if (evaluated == null) return null;
            switch (evaluated.getCellType()) {
                case BOOLEAN: return evaluated.getBooleanValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue();
                    double d = evaluated.getNumberValue();
                    // return long if integer
                    if (d == Math.floor(d)) return (long) d;
                    return d;
                case STRING: return evaluated.getStringValue();
                default: return evaluated.formatAsString();
            }
        }
        switch (type) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getDateCellValue();
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) return (long) d;
                return d;
            case BOOLEAN: return cell.getBooleanCellValue();
            case BLANK: return null;
            case ERROR: return null;
            default: return cell.toString();
        }
    }

    // ==============================================================
    // === HELPER ===================================================
    // ==============================================================

    private BigDecimal getDecimal(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;
        CellValue cv = evaluator.evaluate(cell);
        if (cv == null) return null;
        try {
            if (cv.getCellType() == CellType.NUMERIC)
                return BigDecimal.valueOf(cv.getNumberValue());
            String s = cv.formatAsString().replaceAll("[^0-9.-]", "");
            if (s.isEmpty()) return null;
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }

    // Unit Kerja mapping
    private static final Map<String, String> UNIT_KERJA_MAP = new HashMap<String, String>() {{
        put("BIRO AUPK DAN AAKK", "BAUPK");
        put("FAKULTAS SYARIAH DAN HUKUM", "FSH");
        put("FAKULTAS TARBIYAH DAN KEGURUAN", "FTK");
        put("FAKULTAS USHULUDDIN DAN FILSAFAT", "FUF");
        put("FAKULTAS DAKWAH DAN KOMUNIKASI", "FDK");
        put("FAKULTAS ADAB DAN HUMANIORA", "FAH");
        put("FAKULTAS EKONOMI DAN BISNIS ISLAM", "FEBI");
        put("FAKULTAS SAINS DAN TEKNOLOGI", "FST");
        put("FAKULTAS ILMU SOSIAL DAN ILMU PEMERINTAHAN", "FISIP");
        put("FAKULTAS PSIKOLOGI", "FPSI");
    }};

    // Bulan mapping
    private static final Map<String, Integer> BULAN_MAP = new HashMap<String, Integer>() {{
        put("JANUARI", 1);
        put("FEBRUARI", 2);
        put("MARET", 3);
        put("APRIL", 4);
        put("MEI", 5);
        put("JUNI", 6);
        put("JULI", 7);
        put("AGUSTUS", 8);
        put("SEPTEMBER", 9);
        put("OKTOBER", 10);
        put("NOVEMBER", 11);
        put("DESEMBER", 12);
    }};

    // Unit cell position per sheet
    private static final Map<String, String> UNIT_CELL_MAP = new HashMap<String, String>() {{
        put("DS", "W4");
        put("DT", "X4");
        put("Manejerial", "V4");
        put("Pelaksana", "S4");
        put("JF", "U4");
    }};

    private Map<String, Object> extractFileMetadata(MultipartFile file) throws IOException {
        Map<String, Object> metadata = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            // Find first available sheet to extract metadata
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                if (TARGET_SHEETS.contains(sheetName)) {
                    // Parse periode from D4
                    PeriodeInfo periodeInfo = parsePeriode(sheet);
                    metadata.put("tahun", periodeInfo.tahun);
                    metadata.put("bulan", periodeInfo.bulan);

                    // Get unit from specific cell per sheet
                    String unitRemun = getUnitRemun(sheet, sheetName);
                    metadata.put("unitRemunId", unitRemun);

                    break; // Only need one sheet for metadata
                }
            }
        }

        return metadata;
    }

    private List<RekapRemunPegawai> readRemunExcelFile(MultipartFile file, String rekapId, String createdBy, LocalDateTime now) throws IOException {
        List<RekapRemunPegawai> allData = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                if (TARGET_SHEETS.contains(sheetName)) {
                    // Parse periode from D4
                    PeriodeInfo periodeInfo = parsePeriode(sheet);

                    // Get unit from specific cell per sheet
                    String unitRemun = getUnitRemun(sheet, sheetName);

                    List<RekapRemunPegawai> sheetData = readSheetByType(
                            sheet, sheetName, rekapId, periodeInfo.tahun,
                            periodeInfo.bulan, unitRemun, createdBy, now
                    );
                    allData.addAll(sheetData);
                }
            }
        }

        return allData;
    }

    private List<SelisihRekapRemunPegawai> readSelisihRemunExcelFile(MultipartFile file, String rekapId, Integer tahun, Integer bulan, String createdBy,
                                                                     LocalDateTime now) throws IOException {
        List<SelisihRekapRemunPegawai> allData = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();

                if (TARGET_SHEETS.contains(sheetName)) {
                    // Parse periode from D4
                    String periodeInfo = parsePeriodeSelisih(sheet);

                    // Get unit from specific cell per sheet
                    String unitRemun = getUnitRemun(sheet, sheetName);

                    List<SelisihRekapRemunPegawai> sheetData = readSelisihSheetByType(
                            sheet, sheetName, rekapId, tahun, bulan, unitRemun, createdBy, periodeInfo, now
                    );
                    allData.addAll(sheetData);
                }
            }
        }

        return allData;
    }

    private String getUnitRemun(Sheet sheet, String sheetName) {
        String cellAddress = UNIT_CELL_MAP.get(sheetName);
        if (cellAddress == null) return null;

        // Parse cell address (e.g., "W4" -> column W, row 4)
        int col = cellAddress.charAt(0) - 'A';
        int row = Integer.parseInt(cellAddress.substring(1)) - 1;

        Row rowObj = sheet.getRow(row);
        if (rowObj == null) return null;

        Cell cell = rowObj.getCell(col);
        String unitKerja = getCellValueAsString(cell);

        if (unitKerja == null) return null;

        // Convert to uppercase and trim
        unitKerja = unitKerja.toUpperCase().trim();

        // Map to abbreviation
        return UNIT_KERJA_MAP.getOrDefault(unitKerja, unitKerja);
    }

    private PeriodeInfo parsePeriode(Sheet sheet) {
        // Read from D4 (column D = index 3, row 4 = index 3)
        Row row = sheet.getRow(3);
        if (row == null) return new PeriodeInfo(null, null);

        Cell cell = row.getCell(3);
        String periodeText = getCellValueAsString(cell);

        if (periodeText == null) return new PeriodeInfo(null, null);

        // Parse "Periode Bulan : September 2025"
        Pattern pattern = Pattern.compile("Periode Bulan\\s*:\\s*(\\w+)\\s+(\\d{4})");
        Matcher matcher = pattern.matcher(periodeText);

        if (matcher.find()) {
            String bulanStr = matcher.group(1).toUpperCase();
            Integer bulan = BULAN_MAP.get(bulanStr);
            Integer tahun = Integer.parseInt(matcher.group(2));
            return new PeriodeInfo(tahun, bulan);
        }

        return new PeriodeInfo(null, null);
    }

    private String parsePeriodeSelisih(Sheet sheet) {
        // Read from D4 (column D = index 3, row 4 = index 3)
        Row row = sheet.getRow(3);
        if (row == null) return null;

        Cell cell = row.getCell(3);
        String periodeText = getCellValueAsString(cell);

        if (periodeText == null) return null;

        // Parse "Periode Bulan : Juli sd September 2025"
        return periodeText.split(":")[1].trim();
    }

    private static class PeriodeInfo {
        Integer tahun;
        Integer bulan;

        PeriodeInfo(Integer tahun, Integer bulan) {
            this.tahun = tahun;
            this.bulan = bulan;
        }
    }

    private List<RekapRemunPegawai> readSheetByType(Sheet sheet, String sheetName,
                                                    String rekapId, Integer tahun, Integer bulan,
                                                    String unitRemun, String createdBy, LocalDateTime now) {
        return switch (sheetName) {
            case "DS" -> readDSSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, now);
            case "DT" -> readDTSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, now);
            case "Manejerial" -> readManejerialSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, now);
            case "Pelaksana" -> readPelaksanaSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, now);
            case "JF" -> readJFSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, now);
            default -> new ArrayList<>();
        };
    }

    // DS Sheet Parser
    private List<RekapRemunPegawai> readDSSheet(Sheet sheet, String rekapId, Integer tahun,
                                                Integer bulan, String unitRemun, String createdBy, LocalDateTime now) {
        List<RekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            RekapRemunPegawai rekap = new RekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("DS");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum - skip column A (unitRemun) and B (bulan) from row data
            rekap.setNo(getCellValueAsInteger(row.getCell(3)));
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - DS specific (L-Q)
            rekap.setBkr(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setBkd(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(14)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(16)));

            // Selisih P2 periode sebelumnya (R-V)
            rekap.setVolume(getCellValueAsInteger(row.getCell(17)));
            rekap.setCapaianBkr(getCellValueAsFloat(row.getCell(18)));
            rekap.setPembayaranBkr(getCellValueAsBigDecimal(row.getCell(19)));
            rekap.setCapaianBkd(getCellValueAsFloat(row.getCell(20)));
            rekap.setPembayaranBkdSelisih(getCellValueAsBigDecimal(row.getCell(21)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(24)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(29))); // Kolom AH

            data.add(rekap);
        }

        return data;
    }

    // DT Sheet Parser
    private List<RekapRemunPegawai> readDTSheet(Sheet sheet, String rekapId, Integer tahun,
                                                Integer bulan, String unitRemun, String createdBy, LocalDateTime now) {
        List<RekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            RekapRemunPegawai rekap = new RekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("DT");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNo(getCellValueAsInteger(row.getCell(3)));
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - DT specific
            rekap.setIku(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranIku(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setBkd(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setPembayaranBkd(getCellValueAsBigDecimal(row.getCell(14)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(16)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(17)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(18)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(19)));
            rekap.setCapaianIku(getCellValueAsFloat(row.getCell(20)));
            rekap.setKelebihanKekuranganIku(getCellValueAsFloat(row.getCell(21)));
            rekap.setPembayaranKelebihanKekuranganIku(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setCapaianBkd(getCellValueAsFloat(row.getCell(23)));
            rekap.setPembayaranBkdSelisih(getCellValueAsBigDecimal(row.getCell(24)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(25)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(26)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(27)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(36)));

            data.add(rekap);
        }

        return data;
    }

    // Manejerial Sheet Parser
    private List<RekapRemunPegawai> readManejerialSheet(Sheet sheet, String rekapId, Integer tahun,
                                                        Integer bulan, String unitRemun, String createdBy, LocalDateTime now) {
        List<RekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            RekapRemunPegawai rekap = new RekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("Manejerial");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNo(getCellValueAsInteger(row.getCell(3)));
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - Manejerial specific
            rekap.setIku(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranIku(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(14)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(16)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(17)));
            rekap.setCapaianIku(getCellValueAsFloat(row.getCell(18)));
            rekap.setKelebihanKekuranganIku(getCellValueAsFloat(row.getCell(19)));
            rekap.setPembayaranKelebihanKekuranganIku(getCellValueAsBigDecimal(row.getCell(20)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(21)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(32)));

            data.add(rekap);
        }

        return data;
    }

    // Pelaksana Sheet Parser
    private List<RekapRemunPegawai> readPelaksanaSheet(Sheet sheet, String rekapId, Integer tahun,
                                                       Integer bulan, String unitRemun, String createdBy, LocalDateTime now) {
        List<RekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            RekapRemunPegawai rekap = new RekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("Pelaksana");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNo(getCellValueAsInteger(row.getCell(3)));
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - Pelaksana specific
            rekap.setLkh(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranLkh(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(14)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(16)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(17)));
            rekap.setCapaianLkh(getCellValueAsFloat(row.getCell(18)));
            rekap.setKelebihanKekuranganLkh(getCellValueAsFloat(row.getCell(19)));
            rekap.setPembayaranKelebihanKekuranganLkh(getCellValueAsBigDecimal(row.getCell(20)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(21)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(32)));

            data.add(rekap);
        }

        return data;
    }

    // JF Sheet Parser
    private List<RekapRemunPegawai> readJFSheet(Sheet sheet, String rekapId, Integer tahun,
                                                Integer bulan, String unitRemun, String createdBy, LocalDateTime now) {
        List<RekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            RekapRemunPegawai rekap = new RekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("JF");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNo(getCellValueAsInteger(row.getCell(3)));
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - JF specific
            rekap.setProgress(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranProgress(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setNilaiProgressMax(getCellValueAsFloat(row.getCell(13)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(14)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(15)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(16)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(17)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(18)));
            rekap.setCapaianProgress(getCellValueAsFloat(row.getCell(19)));
            rekap.setKelebihanKekuranganProgress(getCellValueAsFloat(row.getCell(20)));
            rekap.setPembayaranKelebihanKekuranganProgress(getCellValueAsBigDecimal(row.getCell(21)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(24)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(33)));

            data.add(rekap);
        }

        return data;
    }

    private List<SelisihRekapRemunPegawai> readSelisihSheetByType(Sheet sheet, String sheetName,
                                                                  String rekapId, Integer tahun, Integer bulan,
                                                                  String unitRemun, String createdBy, String periode, LocalDateTime now) {
        return switch (sheetName) {
            case "DS" -> readSelisihDSSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, periode, now);
            case "DT" -> readSelisihDTSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, periode, now);
            case "Manejerial" -> readSelisihManejerialSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, periode, now);
            case "Pelaksana" -> readSelisihPelaksanaSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, periode, now);
            case "JF" -> readSelisihJFSheet(sheet, rekapId, tahun, bulan, unitRemun, createdBy, periode, now);
            default -> new ArrayList<>();
        };
    }

    // DS Sheet Parser
    private List<SelisihRekapRemunPegawai> readSelisihDSSheet(Sheet sheet, String rekapId, Integer tahun,
                                                              Integer bulan, String unitRemun, String createdBy, String periode, LocalDateTime now) {
        List<SelisihRekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            SelisihRekapRemunPegawai rekap = new SelisihRekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("DS");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setPeriode(periode);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum - skip column A (unitRemun) and B (bulan) from row data
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - DS specific (L-Q)
            rekap.setBkr(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setBkd(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(14)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(16)));

            // Selisih P2 periode sebelumnya (R-V)
            rekap.setVolume(getCellValueAsInteger(row.getCell(17)));
            rekap.setCapaianBkr(getCellValueAsFloat(row.getCell(18)));
            rekap.setPembayaranBkr(getCellValueAsBigDecimal(row.getCell(19)));
            rekap.setCapaianBkd(getCellValueAsFloat(row.getCell(20)));
            rekap.setPembayaranBkdSelisih(getCellValueAsBigDecimal(row.getCell(21)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(24)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(29))); // Kolom AH

            data.add(rekap);
        }

        return data;
    }

    // DT Sheet Parser
    private List<SelisihRekapRemunPegawai> readSelisihDTSheet(Sheet sheet, String rekapId, Integer tahun,
                                                              Integer bulan, String unitRemun, String createdBy, String periode, LocalDateTime now) {
        List<SelisihRekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            SelisihRekapRemunPegawai rekap = new SelisihRekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("DT");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setPeriode(periode);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - DT specific
            rekap.setIku(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranIku(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setBkd(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setPembayaranBkd(getCellValueAsBigDecimal(row.getCell(14)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(16)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(17)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(18)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(19)));
            rekap.setCapaianIku(getCellValueAsFloat(row.getCell(20)));
            rekap.setKelebihanKekuranganIku(getCellValueAsFloat(row.getCell(21)));
            rekap.setPembayaranKelebihanKekuranganIku(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setCapaianBkd(getCellValueAsFloat(row.getCell(23)));
            rekap.setPembayaranBkdSelisih(getCellValueAsBigDecimal(row.getCell(24)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(25)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(26)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(27)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(36)));

            data.add(rekap);
        }

        return data;
    }

    // Manejerial Sheet Parser
    private List<SelisihRekapRemunPegawai> readSelisihManejerialSheet(Sheet sheet, String rekapId, Integer tahun,
                                                                      Integer bulan, String unitRemun, String createdBy, String periode, LocalDateTime now) {
        List<SelisihRekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            SelisihRekapRemunPegawai rekap = new SelisihRekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("Manejerial");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setPeriode(periode);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - Manejerial specific
            rekap.setIku(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranIku(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(14)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(16)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(17)));
            rekap.setCapaianIku(getCellValueAsFloat(row.getCell(18)));
            rekap.setKelebihanKekuranganIku(getCellValueAsFloat(row.getCell(19)));
            rekap.setPembayaranKelebihanKekuranganIku(getCellValueAsBigDecimal(row.getCell(20)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(21)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(32)));

            data.add(rekap);
        }

        return data;
    }

    // Pelaksana Sheet Parser
    private List<SelisihRekapRemunPegawai> readSelisihPelaksanaSheet(Sheet sheet, String rekapId, Integer tahun,
                                                                     Integer bulan, String unitRemun, String createdBy, String periode, LocalDateTime now) {
        List<SelisihRekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            SelisihRekapRemunPegawai rekap = new SelisihRekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("Pelaksana");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setPeriode(periode);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - Pelaksana specific
            rekap.setLkh(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranLkh(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(13)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(14)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(15)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(16)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(17)));
            rekap.setCapaianLkh(getCellValueAsFloat(row.getCell(18)));
            rekap.setKelebihanKekuranganLkh(getCellValueAsFloat(row.getCell(19)));
            rekap.setPembayaranKelebihanKekuranganLkh(getCellValueAsBigDecimal(row.getCell(20)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(21)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(32)));

            data.add(rekap);
        }

        return data;
    }

    // JF Sheet Parser
    private List<SelisihRekapRemunPegawai> readSelisihJFSheet(Sheet sheet, String rekapId, Integer tahun,
                                                              Integer bulan, String unitRemun, String createdBy, String periode, LocalDateTime now) {
        List<SelisihRekapRemunPegawai> data = new ArrayList<>();

        for (int i = 8; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Check if column D (NO) is empty - stop if empty
            Cell noCell = row.getCell(4);
            if (noCell == null || getCellValueAsString(noCell) == null) {
                break;
            }

            SelisihRekapRemunPegawai rekap = new SelisihRekapRemunPegawai();
            rekap.setId(UUID.randomUUID().toString());
            rekap.setJenisJabatan("JF");
            rekap.setRekapId(rekapId);
            rekap.setTahun(tahun);
            rekap.setBulan(bulan);
            rekap.setUnitRemun(unitRemun);
            rekap.setPeriode(periode);
            rekap.setCreatedBy(createdBy);
            rekap.setCreatedDate(now);

            // Info umum
            rekap.setNip(getCellValueAsString(row.getCell(4)));
            rekap.setNama(getCellValueAsString(row.getCell(5)));
            rekap.setJabatan(getCellValueAsString(row.getCell(6)));
            rekap.setKelasJabatan(getCellValueAsString(row.getCell(7)));
            rekap.setKategoriAsn(getCellValueAsString(row.getCell(8)));
            rekap.setP1(getCellValueAsBigDecimal(row.getCell(9)));
            rekap.setP2(getCellValueAsBigDecimal(row.getCell(10)));

            // Tarif P2 - JF specific
            rekap.setProgress(getCellValueAsBigDecimal(row.getCell(11)));
            rekap.setPembayaranProgress(getCellValueAsBigDecimal(row.getCell(12)));
            rekap.setNilaiProgressMax(getCellValueAsFloat(row.getCell(13)));
            rekap.setSkp(getCellValueAsBigDecimal(row.getCell(14)));
            rekap.setCapaianSkp(getCellValueAsInteger(row.getCell(15)));
            rekap.setPembayaranSkp(getCellValueAsBigDecimal(row.getCell(16)));
            rekap.setJumlahTunjangan(getCellValueAsBigDecimal(row.getCell(17)));

            // Selisih P2 periode sebelumnya
            rekap.setVolume(getCellValueAsInteger(row.getCell(18)));
            rekap.setCapaianProgress(getCellValueAsFloat(row.getCell(19)));
            rekap.setKelebihanKekuranganProgress(getCellValueAsFloat(row.getCell(20)));
            rekap.setPembayaranKelebihanKekuranganProgress(getCellValueAsBigDecimal(row.getCell(21)));

            // Total & Pajak
            rekap.setBruto(getCellValueAsBigDecimal(row.getCell(22)));
            rekap.setPphRupiah(getCellValueAsBigDecimal(row.getCell(23)));
            rekap.setNetto(getCellValueAsBigDecimal(row.getCell(24)));
            rekap.setPphPersen(getCellValueAsFloat(row.getCell(33)));

            data.add(rekap);
        }

        return data;
    }

    // Helper methods
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                // Cek apakah bilangan bulat
                if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                    return String.valueOf((long) numericValue);
                }
                return String.valueOf(numericValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    // Coba ambil sebagai string dulu
                    return cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    // Jika gagal, berarti hasilnya numeric
                    double formulaValue = cell.getNumericCellValue();
                    // Cek apakah bilangan bulat
                    if (formulaValue == Math.floor(formulaValue) && !Double.isInfinite(formulaValue)) {
                        return String.valueOf((long) formulaValue);
                    }
                    return String.valueOf(formulaValue);
                }
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    double numericValue = cell.getNumericCellValue();
                    // Cek apakah cell diformat sebagai persen
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return null; // Skip date cells
                    }
                    String format = cell.getCellStyle().getDataFormatString();
                    if (format.contains("%")) {
                        // Excel menyimpan persen sebagai desimal, jadi kalikan 100
                        return (int) Math.round(numericValue * 100);
                    }
                    return (int) Math.round(numericValue);
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty() || value.equalsIgnoreCase("#REF!")) return null;
                    boolean isPercent = value.contains("%");
                    value = value.replace(",", "").replace("%", "");
                    int intValue = Integer.parseInt(value);
                    // Jika sudah ada simbol %, berarti sudah dalam bentuk 50 bukan 0.5
                    return intValue;
                case FORMULA:
                    double formulaValue = cell.getNumericCellValue();
                    String formulaFormat = cell.getCellStyle().getDataFormatString();
                    if (formulaFormat.contains("%")) {
                        return (int) Math.round(formulaValue * 100);
                    }
                    return (int) Math.round(formulaValue);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    double numericValue = cell.getNumericCellValue();
                    // Cek apakah angka bulat
                    if (numericValue == Math.floor(numericValue)) {
                        return new BigDecimal(String.valueOf((long) numericValue));
                    }
                    return new BigDecimal(String.valueOf(numericValue));
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty() || value.equalsIgnoreCase("#REF!")) return null;
                    value = value.replace(",", "").replace("%", "");
                    return new BigDecimal(value).stripTrailingZeros();
                case FORMULA:
                    double formulaValue = cell.getNumericCellValue();
                    if (formulaValue == Math.floor(formulaValue)) {
                        return new BigDecimal(String.valueOf((long) formulaValue));
                    }
                    return new BigDecimal(String.valueOf(formulaValue));
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private Float getCellValueAsFloat(Cell cell) {
        if (cell == null) return null;

        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    double numericValue = cell.getNumericCellValue();

                    // Cek apakah cell diformat sebagai persen
                    if (!DateUtil.isCellDateFormatted(cell)) {
                        String format = cell.getCellStyle().getDataFormatString();
                        if (format.contains("%")) {
                            // Excel menyimpan persen sebagai desimal, jadi kalikan 100
                            numericValue = numericValue * 100;
                        }
                    }

                    return (float) numericValue;
                case STRING:
                    String value = cell.getStringCellValue().trim();
                    if (value.isEmpty() || value.equalsIgnoreCase("#REF!")) return null;
                    value = value.replace(",", "").replace("%", "");
                    return Float.parseFloat(value);
                case FORMULA:
                    double formulaValue = cell.getNumericCellValue();

                    String formulaFormat = cell.getCellStyle().getDataFormatString();
                    if (formulaFormat.contains("%")) {
                        formulaValue = formulaValue * 100;
                    }

                    return (float) formulaValue;
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
