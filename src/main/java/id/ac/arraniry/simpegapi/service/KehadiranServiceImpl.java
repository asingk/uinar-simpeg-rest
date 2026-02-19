package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.*;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import id.ac.arraniry.simpegapi.repo.KehadiranArcRepo;
import id.ac.arraniry.simpegapi.repo.KehadiranRepo;
import id.ac.arraniry.simpegapi.utils.SimpegGraphUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class KehadiranServiceImpl implements LaporanService, KehadiranService {

    private static final Logger logger = LoggerFactory.getLogger(KehadiranServiceImpl.class);

    private final KehadiranRepo kehadiranRepo;
    private final KehadiranArcRepo kehadiranArcRepo;
    private final HariLiburService hariLiburService;
    private final PemutihanService pemutihanService;
    private final IzinService izinService;
    private final HariLiburTapiKerjaService hariLiburTapiKerjaService;
    private final Environment environment;
    private final JamKerjaHarianService jamKerjaHarianService;
    private final HijriahService hijriahService;
    private final WfaByHariService wfaByHariService;
    private final WfaByTanggalService wfaByTanggalService;
    private final JenisJabatanService jenisJabatanService;

    public KehadiranServiceImpl(KehadiranRepo kehadiranRepo, KehadiranArcRepo kehadiranArcRepo, HariLiburService hariLiburService,
                                PemutihanService pemutihanService, IzinService izinService, HariLiburTapiKerjaService hariLiburTapiKerjaService,
                                Environment environment, JamKerjaHarianService jamKerjaHarianService,
                                HijriahService hijriahService, WfaByHariService wfaByHariService, WfaByTanggalService wfaByTanggalService,
                                JenisJabatanService jenisJabatanService) {
        this.kehadiranRepo = kehadiranRepo;
        this.kehadiranArcRepo = kehadiranArcRepo;
        this.hariLiburService = hariLiburService;
        this.pemutihanService = pemutihanService;
        this.izinService = izinService;
        this.hariLiburTapiKerjaService = hariLiburTapiKerjaService;
        this.environment = environment;
        this.jamKerjaHarianService = jamKerjaHarianService;
        this.hijriahService = hijriahService;
        this.wfaByHariService = wfaByHariService;
        this.wfaByTanggalService = wfaByTanggalService;
        this.jenisJabatanService = jenisJabatanService;
    }

    @Override
    public List<LaporanKehadiranVO> findLaporanBulananByNip(String nip, int bulan, int tahun) {
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate today = LocalDate.now();
        int endDay = yearMonth.lengthOfMonth();
        if (yearMonth.equals(YearMonth.from(today))) {
            endDay = today.getDayOfMonth();
        }
        List<KehadiranVO> kehadiranVOList = findKehadiranByNipAndBulanAndTahun(nip, bulan, tahun);
        Map<LocalDate, List<KehadiranVO>> kehadiranMap = kehadiranVOList.stream()
                .collect(Collectors.groupingBy(k -> k.getWaktu().toLocalDate()));

        List<LocalDate> liburDates = hariLiburService.hariLiburBulanan(tahun, bulan);
        List<Izin> izinData = izinService.findByNipAndTahunAndBulan(nip, tahun, bulan);
        List<Pemutihan> pemutihanData = pemutihanService.findByBulanAndTahun(bulan, tahun);

        List<LaporanKehadiranVO> fullRecap = new ArrayList<>();

        for (int day = 1; day <= endDay; day++) {
            LocalDate date = LocalDate.of(tahun, bulan, day);
            LaporanKehadiranVO lapVO = new LaporanKehadiranVO();
            lapVO.setTanggal(date);
            lapVO.setHari(getNamaHari(date.getDayOfWeek().getValue()));

            String keterangan = null;
            int dayOfWeek = date.getDayOfWeek().getValue();

            // Logika Keterangan Hari Libur
            if (dayOfWeek == 6 || dayOfWeek == 7 || liburDates.contains(date)) {
                keterangan = "Libur";
            }

            // Logika Izin
            Optional<Izin> izinEntry = izinData.stream().filter(i -> LocalDate.parse(i.getDateString()).equals(date)).findFirst();
            boolean hasIzinOrCuti = izinEntry.isPresent();
            if (keterangan == null && hasIzinOrCuti) {
                keterangan = izinEntry.get().getIzinCategoryDesc();
            }

            // Logika Pemutihan
            List<Pemutihan> tglPemutihan = pemutihanData.stream().filter(p -> LocalDate.parse(p.getDateString()).equals(date)).toList();
            boolean isPemutihan = false;

            // Default Jadwal (Akan di-override jika pemutihan)
            String jadwalDatang = (keterangan == null) ? "08:00" : null;
            String jadwalPulang = (keterangan == null) ? (dayOfWeek == 5 ? "17:00" : "16:30") : null;

            String absenDatang = null;
            String absenPulang = null;

            if (!hasIzinOrCuti && !tglPemutihan.isEmpty()) {
                for (Pemutihan p : tglPemutihan) {
                    if (GlobalConstants.STATUS_DATANG.equals(p.getStatus())) {
                        jadwalDatang = "08:00";
                        absenDatang = "08:00";
                        isPemutihan = true;
                    } else if (GlobalConstants.STATUS_PULANG.equals(p.getStatus())) {
                        jadwalPulang = (dayOfWeek == 5) ? "17:00" : "16:30";
                        absenPulang = jadwalPulang;
                        isPemutihan = true;
                    }
                }
            }

            // Jika bukan pemutihan, ambil dari data kehadiran asli
            if (!isPemutihan && kehadiranMap.containsKey(date)) {
                List<KehadiranVO> dayAbsen = kehadiranMap.get(date);
                KehadiranVO datangEntry = dayAbsen.stream()
                        .filter(a -> GlobalConstants.STATUS_DATANG.equals(a.getStatus()))
                        .findFirst().orElse(null);
                absenDatang = (datangEntry != null) ? datangEntry.getJam() : null;
                if (datangEntry != null && datangEntry.getJadwal() != null) {
                    jadwalDatang = datangEntry.getJadwal();
                }
                KehadiranVO pulangEntry = dayAbsen.stream()
                        .filter(a -> GlobalConstants.STATUS_PULANG.equals(a.getStatus()))
                        .reduce((first, second) -> second).orElse(null);
                absenPulang = (pulangEntry != null) ? pulangEntry.getJam() : null;
                if (pulangEntry != null && pulangEntry.getJadwal() != null) {
                    jadwalPulang = pulangEntry.getJadwal();
                }
            }

            // Update keterangan jika tidak ada absen dan bukan hari libur/izin
            if (isPemutihan) {
                keterangan = null;
            } else if (keterangan == null && (absenDatang == null && absenPulang == null)) {
                keterangan = "Tanpa Keterangan";
            }

            lapVO.setJadwalDatang(jadwalDatang);
            lapVO.setAbsenDatang(absenDatang);
            lapVO.setJadwalPulang(jadwalPulang);
            lapVO.setAbsenPulang(absenPulang);
            lapVO.setKeterangan(keterangan);

            // Hitung Cepat/Telat (Menit) - Logic sesuai query: jika pemutihan 0, jika absen null tapi jadwal ada, hitung selisih default (simulasi telat)
            lapVO.setCepatTelatDatang(calculateMinutesDiff(jadwalDatang, absenDatang, true, isPemutihan));
            lapVO.setCepatTelatPulang(calculateMinutesDiff(jadwalPulang, absenPulang, false, isPemutihan));

            fullRecap.add(lapVO);
        }
        return fullRecap;
    }

    private String getNamaHari(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Senin"; case 2 -> "Selasa"; case 3 -> "Rabu";
            case 4 -> "Kamis"; case 5 -> "Jumat"; case 6 -> "Sabtu";
            case 7 -> "Minggu"; default -> null;
        };
    }

    private Integer calculateMinutesDiff(String jadwal, String absen, boolean isDatang, boolean isPemutihan) {
        if (isPemutihan) return 0;
        if (jadwal == null || absen == null) return null;

        int jadwalMenit = Integer.parseInt(jadwal.split(":")[0]) * 60 + Integer.parseInt(jadwal.split(":")[1]);
        int absenMenit = Integer.parseInt(absen.split(":")[0]) * 60 + Integer.parseInt(absen.split(":")[1]);

        return isDatang ? (jadwalMenit - absenMenit) : (absenMenit - jadwalMenit);
    }

    @Override
    public List<KehadiranVO> findLaporanUangMakanBaru(Integer bulan, Integer tahun, List<JabatanBulanan> jabatanBulanans) {
        LocalDate startDate = LocalDate.of(tahun, bulan, 1);
        List<String> nipList = jabatanBulanans.stream().map(JabatanBulanan::getNip).toList();
        List<Pemutihan> pemutihanList = pemutihanService.findByBulanAndTahunAndStatus(bulan, tahun, GlobalConstants.STATUS_DATANG);
        List<String> tanggalPemutihan = new ArrayList<>();
        pemutihanList.forEach(row -> tanggalPemutihan.add(row.getTanggal().toString()));
        List<KehadiranVO> kehadiranVOList = new ArrayList<>();
        if (LocalDate.now().getYear() == tahun) {
            List<Kehadiran> kehadiranList = kehadiranRepo.findLaporanUangMakan(startDate, endDate(bulan, tahun), nipList, tanggalPemutihan,
                    Sort.by(Sort.Direction.ASC, "pegawai.nip").and(Sort.by(Sort.Direction.ASC, "waktu")));
            kehadiranList.forEach(row -> {
                if(row.getWaktu().toLocalTime().isBefore(LocalTime.of(12, 0))) {
                    kehadiranVOList.add(new KehadiranVO(row));
                }
            });
        } else {
            List<KehadiranArc> kehadiranList = kehadiranArcRepo.findLaporanUangMakan(startDate, endDate(bulan, tahun), nipList,
                    Sort.by(Sort.Direction.ASC, "pegawai.nip").and(Sort.by(Sort.Direction.ASC, "waktu")));
            kehadiranList.forEach(row -> {
                if(row.getWaktu().toLocalTime().isBefore(LocalTime.of(12, 0))) {
                    kehadiranVOList.add(new KehadiranVO(row));
                }
            });
        }

        if (!pemutihanList.isEmpty()) {
            List<KehadiranVO> addPutihVO = new ArrayList<>();
            for (Pemutihan value : pemutihanList) {
                for (int j = 0; j < kehadiranVOList.size(); j++) {
                    KehadiranVO kehadiran = kehadiranVOList.get(j);
                    if(kehadiran.getWaktu().toLocalTime().isBefore(LocalTime.of(12, 0)) &&
                            izinService.findByNipAndTanggal(kehadiran.getIdPegawai(), value.getTanggal()).isEmpty()) {
						if(kehadiran.getIdPegawai().equals("196601131994021002") && kehadiran.getTanggal().equals("2025-07-31")) {
							System.out.println(kehadiran.getIdPegawai() + " " + kehadiran.getTanggal());
						}
                        if ((j == 0 || !kehadiran.getIdPegawai().equals(kehadiranVOList.get(j-1).getIdPegawai()))
                                && value.getTanggal().isBefore(kehadiran.getWaktu().toLocalDate())) {    // record pertama list atau record pertama pegawai
                            KehadiranVO kehadiranVO = new KehadiranVO();
                            kehadiranVO.setIdPegawai(kehadiran.getIdPegawai());
                            kehadiranVO.setNamaPegawai(kehadiran.getNamaPegawai());
                            kehadiranVO.setTanggal(value.getTanggal().toString());
                            addPutihVO.add(kehadiranVO);
                        } else if ((j == kehadiranVOList.size()-1 || !kehadiran.getIdPegawai().equals(kehadiranVOList.get(j+1).getIdPegawai()))
                                && value.getTanggal().isAfter(kehadiran.getWaktu().toLocalDate())) {	// record terakhir list atau record terakhir pegawai
                            KehadiranVO kehadiranVO = new KehadiranVO();
                            kehadiranVO.setIdPegawai(kehadiran.getIdPegawai());
                            kehadiranVO.setNamaPegawai(kehadiran.getNamaPegawai());
                            kehadiranVO.setTanggal(value.getTanggal().toString());
                            addPutihVO.add(kehadiranVO);
                        } else if (j < kehadiranVOList.size()-1
                                && kehadiranVOList.get(j+1).getIdPegawai().equals(kehadiran.getIdPegawai())
                                && value.getTanggal().isAfter(kehadiran.getWaktu().toLocalDate())
                                && value.getTanggal().isBefore(kehadiranVOList.get(j+1).getWaktu().toLocalDate())) {	// di antara tanggal, pegawai yg sama
                            KehadiranVO kehadiranVO = new KehadiranVO();
                            kehadiranVO.setIdPegawai(kehadiran.getIdPegawai());
                            kehadiranVO.setNamaPegawai(kehadiran.getNamaPegawai());
                            kehadiranVO.setTanggal(value.getTanggal().toString());
                            addPutihVO.add(kehadiranVO);
                        }
                    }
                }
            }
            kehadiranVOList.addAll(addPutihVO);
            kehadiranVOList.sort(Comparator.comparing(KehadiranVO::getIdPegawai).thenComparing(KehadiranVO::getTanggal));
        }
        return kehadiranVOList;
    }

    @Override
    public List<RekapUMPegawai> generateUangMakanFormatLama(List<KehadiranVO> kehadiranVOList, List<JabatanBulanan> jabatanBulananList) {
        LocalDateTime now = LocalDateTime.now();
        return generateUangMakanDataSourceFromBaru(kehadiranVOList).stream().peek(row -> {
            JabatanBulanan jabatanBulanan = jabatanBulananList.stream().filter(jabnul -> row.getNip().equals(jabnul.getNip())).findAny()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "jabatan bulanan pegawai " + row.getNip() + " tidak ditemukan"));
            assert jabatanBulanan != null;
            row.setGolongan(jabatanBulanan.getGolongan());
            row.setUnitGaji(jabatanBulanan.getUnitGaji());
            if (jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_PNS)
                    || jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_CPNS)
                    || jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_PNSDPB)) {
                row.setStatusPegawai("PNS");
            } else if (jabatanBulanan.getIdStatusPegawai().equals(GlobalConstants.ID_STATUS_PEGAWAI_PPPK)) {
                row.setStatusPegawai("PPPK");
            }
            row.setTahun(jabatanBulanan.getTahun());
            row.setBulan(jabatanBulanan.getBulan());
            row.setCreatedDate(now);
        }).collect(Collectors.toList());
    }

//    private List<KehadiranVO> findByNipAndBulanAndTahunAndIsDeletedFalse(String nip, int bulan, int tahun) {
//        LocalTime timeStart = LocalTime.of(0, 0, 1);
//        LocalTime timeEnd = LocalTime.of(23, 59, 59);
//        LocalDate dayStart = LocalDate.of(tahun, bulan, 1);
//        YearMonth yearMonth = YearMonth.of(tahun, bulan);
//        LocalDate dayEnd = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
//        LocalDateTime firstDay = LocalDateTime.of(dayStart, timeStart);
//        LocalDateTime lastDay = LocalDateTime.of(dayEnd, timeEnd);
//        List<KehadiranVO> kehadirVOList = new ArrayList<>();
//        if (LocalDate.now().getYear() == tahun) {
//            List<Kehadiran> kehadiranList = kehadiranRepo.findByNipAndTanggalBetweenAndIsDeletedFalse(nip, firstDay, lastDay,
//                    Sort.by(Sort.Direction.ASC, "waktu"));
//            kehadiranList.forEach(kehadiran -> {
//                KehadiranVO kehadiranVO = new KehadiranVO(kehadiran);
//                kehadirVOList.add(kehadiranVO);
//            });
//        } else {
//            List<KehadiranArc> kehadiranList = kehadiranArcRepo.findByNipAndTanggalBetweenAndIsDeletedFalse(nip, firstDay, lastDay,
//                    Sort.by(Sort.Direction.ASC, "waktu"));
//            kehadiranList.forEach(kehadiran -> {
//                KehadiranVO kehadiranVO = new KehadiranVO(kehadiran);
//                kehadirVOList.add(kehadiranVO);
//            });
//        }
//        return kehadirVOList;
//    }

    private List<RekapUMPegawai> generateUangMakanDataSourceFromBaru(List<KehadiranVO> hadirList) {
        List<RekapUMPegawai> rekapUMPegawaiList = new ArrayList<>();
        String prevNip = "";
        String prevNama = "";
        int jumlahDatang = 0;
        for(int i=0; i<hadirList.size(); i++) {
            KehadiranVO kehadiranVO = hadirList.get(i);
//            if (kehadiranVO.getIdPegawai().equals("199704152020122022")) {
//                System.out.println(kehadiranVO.getNamaPegawai());
//            }
            RekapUMPegawai rekapUMPegawai = new RekapUMPegawai();
            if(i==0) {
                jumlahDatang++;
            } else if(i==hadirList.size()-1) {
                if(prevNip.equals(kehadiranVO.getIdPegawai())) {
                    jumlahDatang++;
                    rekapUMPegawai.setNip(kehadiranVO.getIdPegawai());
                    rekapUMPegawai.setNama(kehadiranVO.getNamaPegawai());
                    rekapUMPegawai.setJumlahHari(jumlahDatang);
                    rekapUMPegawaiList.add(rekapUMPegawai);
                } else {
                    rekapUMPegawai.setNip(prevNip);
                    rekapUMPegawai.setNama(prevNama);
                    rekapUMPegawai.setJumlahHari(jumlahDatang);
                    rekapUMPegawaiList.add(rekapUMPegawai);
                    RekapUMPegawai rekapUMPegawai2 = new RekapUMPegawai();
                    rekapUMPegawai2.setNip(kehadiranVO.getIdPegawai());
                    rekapUMPegawai2.setNama(kehadiranVO.getNamaPegawai());
                    rekapUMPegawai2.setJumlahHari(1);
                    rekapUMPegawaiList.add(rekapUMPegawai2);
                }
            } else if(prevNip.equals(kehadiranVO.getIdPegawai())) {
                jumlahDatang++;
            } else {
                rekapUMPegawai.setNip(prevNip);
                rekapUMPegawai.setNama(prevNama);
                rekapUMPegawai.setJumlahHari(jumlahDatang);
                rekapUMPegawaiList.add(rekapUMPegawai);
                jumlahDatang = 1;
            }
            prevNip = kehadiranVO.getIdPegawai();
            prevNama = kehadiranVO.getNamaPegawai();
        }
        return rekapUMPegawaiList;
    }

    private LocalDate endDate ( int bulan, int tahun){
        int bulanDepan;
        int tahunDepan;
        if (bulan < 12) {
            bulanDepan = bulan + 1;
            tahunDepan = tahun;
        } else {
            bulanDepan = 1;
            tahunDepan = tahun + 1;
        }
        return LocalDate.of(tahunDepan, bulanDepan, 1);
    }

    private List<KehadiranVO> findKehadiranByNipAndBulanAndTahun(String nip, int bulan, int tahun) {
        List<KehadiranVO> hadirVOList = new ArrayList<>();
        String pattern = String.format("^%d-%02d-", tahun, bulan);
        if (LocalDate.now().getYear() == tahun) {
            List<Kehadiran> kehadiranList = kehadiranRepo.findBulananByNip(nip, pattern, Sort.by(Sort.Direction.ASC, "waktu"));
            for(Kehadiran hadir : kehadiranList) {
                KehadiranVO hadirVO = new KehadiranVO(hadir);
                hadirVOList.add(hadirVO);
            }
        } else {
            List<KehadiranArc> kehadiranList = kehadiranArcRepo.findBulananByNip(nip, pattern, Sort.by(Sort.Direction.ASC, "waktu"));
            for(KehadiranArc hadir : kehadiranList) {
                KehadiranVO hadirVO = new KehadiranVO(hadir);
                hadirVOList.add(hadirVO);
            }
        }
        return hadirVOList;
    }

    @Override
    public KehadiranVO findByPegawaiNipAndStatusAndTanggal(String nip, String status, LocalDate tanggal) {
        if (tanggal.getYear() == LocalDate.now().getYear()) {
            Optional<Kehadiran> kehadiran = kehadiranRepo.findByPegawaiNipAndStatusAndTanggal(nip, status, tanggal.toString());
            if (kehadiran.isPresent()) {
                return new KehadiranVO(kehadiran.get());
            }
        } else {
            Optional<KehadiranArc> kehadiranArc = kehadiranArcRepo.findByPegawaiNipAndStatusAndTanggal(nip, status, tanggal.toString());
            if (kehadiranArc.isPresent()) {
                return new KehadiranVO(kehadiranArc.get());
            }
        }
        return new KehadiranVO();
    }

    @Override
    public SaveResponse save(KehadiranVO kehadiranVO) {
        try {
            return new SaveResponse(kehadiranRepo.save(new Kehadiran(kehadiranVO)));
        } catch (DuplicateKeyException dke) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "rekaman sudah ada!");
        }
    }

    @Override
    public List<KehadiranVO> findByNipAndTanggal(String nip, LocalDate tanggal) {
        var hadirVO = new ArrayList<KehadiranVO>();
        if (tanggal.getYear() == LocalDate.now().getYear()) {
            var result = kehadiranRepo.findByPegawaiNipAndTanggal(nip, tanggal.toString());
            result.forEach(row -> hadirVO.add(new KehadiranVO(row)));
        } else {
            var result = kehadiranArcRepo.findByPegawaiNipAndTanggal(nip, tanggal.toString());
            result.forEach(row -> hadirVO.add(new KehadiranVO(row)));
        }
        return hadirVO;
    }

    @Override
    public KehadiranVO findById(String id) {
        Optional<Kehadiran> kehadiran = kehadiranRepo.findById(id);
        if (kehadiran.isPresent()) {
            return new KehadiranVO(kehadiran.get());
        } else {
            Optional<KehadiranArc> kehadiranArc = kehadiranArcRepo.findById(id);
            if (kehadiranArc.isPresent()) {
                return new KehadiranVO(kehadiranArc.get());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kehadiran tidak ditemukan!");
            }
        }
    }

    @Override
    public void delete(KehadiranVO kehadiranVO) {
        if (kehadiranVO.getWaktu().getYear() == LocalDate.now().getYear()) {
            kehadiranRepo.deleteById(kehadiranVO.getId());
        } else {
            kehadiranArcRepo.deleteById(kehadiranVO.getId());
        }
    }

    @Override
    public SaveResponse add(KehadiranAddRequest request) {
        LocalDate tglHariIni = LocalDate.now();
        if(request.getTanggal().equals(tglHariIni) || request.getTanggal().isAfter(tglHariIni)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "hanya boleh menambahkan hari yang sudah lalu");
        }
        if(isLibur(request.getTanggal())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "ini hari libur!");
        Optional<Izin> izinOptional = izinService.findByNipAndTanggal(request.getIdPegawai(), request.getTanggal());
        if (izinOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hari ini pegawai ini " + izinOptional.get().getIzinCategoryDesc());
        }
        PegawaiSimpegVO pegawaiProfilVO = SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(request.getIdPegawai(), environment);
        PegawaiSimpegVO pegawaiSimpegVO = SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(request.getCreatedBy(), environment);
        LocalTime time = switch (request.getStatus()) {
            case GlobalConstants.STATUS_DATANG -> LocalTime.of(7, 15, 1, 123000000);
            case GlobalConstants.STATUS_PULANG -> LocalTime.of(17, 1, 1, 123000000);
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "status salah!");
        };
        LocalTime jadwal = switch (request.getStatus()) {
            case GlobalConstants.STATUS_DATANG -> LocalTime.of(8, 0, 0, 123000000);
            case GlobalConstants.STATUS_PULANG -> LocalTime.of(14, 30, 0, 123000000);
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN, "status salah!");
        };
        KehadiranVO kehadiranVO = new KehadiranVO();
        kehadiranVO.setIdPegawai(pegawaiProfilVO.getId());
        kehadiranVO.setNamaPegawai(pegawaiProfilVO.getNama());
        kehadiranVO.setStatus(request.getStatus());
        kehadiranVO.setWaktu(LocalDateTime.of(request.getTanggal(), time));
        kehadiranVO.setTanggal(request.getTanggal().toString());
        kehadiranVO.setIsAdded(true);
        kehadiranVO.setAddedDate(LocalDateTime.now());
        kehadiranVO.setAddedByNip(request.getCreatedBy());
        kehadiranVO.setAddedByNama(pegawaiSimpegVO.getNama());
        DateTimeFormatter formatterJam = DateTimeFormatter.ofPattern("HH:mm");
        kehadiranVO.setJam(time.format(formatterJam));
        kehadiranVO.setJadwal(jadwal.format(formatterJam));
        try {
            if (tglHariIni.getYear() == request.getTanggal().getYear()) {
                return new SaveResponse(kehadiranRepo.save(new Kehadiran(kehadiranVO)));
            } else {
                return new SaveResponse(kehadiranArcRepo.save(new KehadiranArc(kehadiranVO)));
            }
        } catch (DuplicateKeyException dke) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "rekaman sudah ada!");
        }
    }

    @Override
    public Boolean isLibur(LocalDate tanggal) {
        List<HariLiburTapiKerja> hariLiburTapiKerjaList = hariLiburTapiKerjaService.findAll();
        for(HariLiburTapiKerja hari : hariLiburTapiKerjaList) {
            if(tanggal.isEqual(hari.getTanggal())) {
                return false;
            }
        }
        if(tanggal.getDayOfWeek().getValue() > 5)
            return true;
        List<HariLibur> hariLiburList = hariLiburService.findAll();
        for(HariLibur hari : hariLiburList) {
            if(tanggal.isEqual(hari.getTanggal())) {
                return true;
            }
        }
        return false;
    }

    private Kehadiran paramToSave(LocalDateTime now, Double longitude, Double latitude, String userAgent, String statusJamKerja) {
//        JamKerja jamKerja = jamKerjaService.findByHariAndIsRamadhan(now.getDayOfWeek().getValue(),
//                hijriahService.isRamadhan(HijrahDate.now().get(ChronoField.YEAR), now.toLocalDate()));
        JamKerjaHarian jamKerjaHarian = jamKerjaHarianService.findByTanggal(now.toLocalDate().toString());
//        LocalTime jamMasukAwal = LocalTime.parse(jamKerjaHarian.getDatangStart());
//        LocalTime jamMasukAkhir = LocalTime.parse(jamKerjaHarian.getDatangEnd());
//        LocalTime jamPulangAwal = LocalTime.parse(jamKerjaHarian.getPulangStart());
//        LocalTime jamPulangAkhir = LocalTime.parse(jamKerjaHarian.getPulangEnd());
        LocalTime jadwalDatang = LocalTime.parse(jamKerjaHarian.getJadwalDatang());
        LocalTime jadwalPulang = LocalTime.parse(jamKerjaHarian.getJadwalPulang());
        LocalTime nowTime = now.toLocalTime();
        LocalDate today = now.toLocalDate();
        Kehadiran kehadiran = new Kehadiran();
        kehadiran.setWaktu(now);
        DateTimeFormatter formatterJam = DateTimeFormatter.ofPattern("HH:mm");
        kehadiran.setJam(nowTime.format(formatterJam));
        kehadiran.setStatus(statusJamKerja);

        if(GlobalConstants.STATUS_DATANG.equals(statusJamKerja)) {
            kehadiran.setJadwal(jadwalDatang.format(formatterJam));
        }else if(GlobalConstants.STATUS_PULANG.equals(statusJamKerja)) {
            kehadiran.setStatus(GlobalConstants.STATUS_PULANG);
            kehadiran.setJadwal(jadwalPulang.format(formatterJam));
        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Saat ini di luar jam absen!");
        }
        if(null != longitude && null != latitude) {
            Location location = new Location();
            location.setType(GlobalConstants.LOCATION_TYPE_POINT);
            List<Double> coordinates = new ArrayList<>();
            coordinates.add(longitude);
            coordinates.add(latitude);
            location.setCoordinates(coordinates);
            kehadiran.setLocation(location);
        }
        kehadiran.setUserAgent(userAgent);
        kehadiran.setTanggal(today.toString());
        return kehadiran;
    }

    @Override
    public StatusSaatIniResponse getStatusSaatIni() {
        //		log.debug("getStatusSaatIni start " + System.currentTimeMillis());
        StatusSaatIniResponse status = new StatusSaatIniResponse();
        //TODO dev purpose only
//        LocalDateTime now = LocalDateTime.of(LocalDate.of(2026, 2, 19), LocalTime.of(13, 0, 0));
        LocalDateTime now = LocalDateTime.now();
        status.setWaktu(now);
        status.setStatus(statusJamKerja(now));
        status.setJamLembur(getStatusLembur(now));
        boolean isRamadhan = hijriahService.isRamadhan(HijrahDate.now().get(ChronoField.YEAR), now.toLocalDate());
        status.setRamadhan(isRamadhan);
//		log.debug("getStatusSaatIni stop " + System.currentTimeMillis());
        return status;
    }

    @Override
    public SaveResponse create(HttpServletRequest request, String userAgent, KehadiranCreateRequest createRequest) {
        //		log.debug("--- save start ---");
//		log.debug("nip: " + nip);

        //TODO dev purpose only
//		String nip = "198703222019031010";
//        LocalDateTime now = LocalDateTime.of(LocalDate.of(2026, 2, 19), LocalTime.of(13, 0, 0));
        LocalDateTime now = LocalDateTime.now();
//        var statusSaatIni = getStatusSaatIni();
        String statusJamKerja = statusJamKerja(now);
        if (!(statusJamKerja.equals(GlobalConstants.STATUS_DATANG) || statusJamKerja.equals(GlobalConstants.STATUS_PULANG))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, statusJamKerja);
        }
        Optional<Izin> izinOptional = izinService.findByNipAndTanggal(createRequest.getIdPegawai(), now.toLocalDate());
        if (izinOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hari ini Anda " + izinOptional.get().getIzinCategoryDesc());
        }
        PegawaiSimpegVO pegawaiProfilVO = SimpegGraphUtils.getProfilPegawaiFromSimpegGraphql(createRequest.getIdPegawai(), environment);
        Kehadiran kehadiran = paramToSave(now, createRequest.getLongitude(), createRequest.getLatitude(), userAgent, statusJamKerja);
        if (null == createRequest.getLongitude() && null == createRequest.getLatitude() && isHarusDiKampus(pegawaiProfilVO)) {
            validasiIpPrivate(request.getRemoteAddr());
        }
        kehadiran.setPegawai(new Pegawai(pegawaiProfilVO.getId(), pegawaiProfilVO.getNama()));
        KehadiranVO existingVO = findByPegawaiNipAndStatusAndTanggal(createRequest.getIdPegawai(), kehadiran.getStatus(), now.toLocalDate());
        if (null != existingVO.getId()) {
            if(kehadiran.getStatus().equals(GlobalConstants.STATUS_DATANG)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anda sudah datang!");
            }
            kehadiran.setId(existingVO.getId());
        }

//		log.debug("--- save end ---");
        return save(new KehadiranVO(kehadiran));
    }

    private boolean isHarusDiKampus(PegawaiSimpegVO pegawaiProfilVO) {
        var now = LocalDate.now();
        var opt = wfaByHariService.findByHari(now.getDayOfWeek().getValue());
        if (opt.isPresent() && opt.get().getWfa()) return false;
        if (wfaByTanggalService.findByTanggal(now).isPresent()) return false;
        return jenisJabatanService.findById(pegawaiProfilVO.getJenisJabatan()).getIsHarusDiKampus();
    }

    private void validasiIpPrivate(String ip) {
        Pattern pattern = Pattern.compile("(^10\\.)|(^172\\.1[6-9]\\.)|(^172\\.2[0-9]\\.)|(^172\\.3[0-1]\\.)|(^192\\.168\\.)");
        Matcher matcher = pattern.matcher(ip);
        if(!matcher.find()){
            logger.trace("{} harus menggunakan jaringan kampus!", ip);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Harus menggunakan jaringan kampus!");
        }
        logger.trace("{} sukses menggukanan jaringan kampus", ip);
    }

    private String statusJamKerja(LocalDateTime now) {
        String status;
        JamKerjaHarian jamKerjaHarian = jamKerjaHarianService.findByTanggal(now.toLocalDate().toString());
//        JamKerja jamKerja = jamKerjaService.findByHariAndIsRamadhan(now.getDayOfWeek().getValue(),
//                isRamadhan);
        LocalTime jamMasukAwal = LocalTime.parse(jamKerjaHarian.getDatangStart());
        LocalTime jamMasukAkhir = LocalTime.parse(jamKerjaHarian.getDatangEnd());
        LocalTime jamPulangAwal = LocalTime.parse(jamKerjaHarian.getPulangStart());
        LocalTime jamPulangAkhir = LocalTime.parse(jamKerjaHarian.getPulangEnd());
        LocalTime nowTime = now.toLocalTime();
        if(isLibur(now.toLocalDate())) {
            status = GlobalConstants.STATUS_LIBUR;
        } else {
            if(!nowTime.isBefore(jamMasukAwal) && nowTime.isBefore(jamMasukAkhir)) {	// absen datang
                if (null != pemutihanService.findByDateStringAndStatus(now.toLocalDate(), GlobalConstants.STATUS_DATANG)) {
                    status = GlobalConstants.STATUS_PEMUTIHAN;
                } else {
                    status = GlobalConstants.STATUS_DATANG;
                }
            } else if(!nowTime.isBefore(jamPulangAwal) && nowTime.isBefore(jamPulangAkhir)) {	// absen pulang
                if (null != pemutihanService.findByDateStringAndStatus(now.toLocalDate(), GlobalConstants.STATUS_PULANG)) {
                    status = GlobalConstants.STATUS_PEMUTIHAN;
                } else {
                    status = GlobalConstants.STATUS_PULANG;
                }
            }
            else {	// di luar jam absen
                status = GlobalConstants.STATUS_TUTUP;
            }
        }
//		log.debug("getstatusJamKerja stop " + System.currentTimeMillis());
        return status;
    }

    private boolean getStatusLembur(LocalDateTime now) {
//        log.debug("getStatusLembur start " + System.currentTimeMillis());
        LocalTime nowTime = now.toLocalTime();
        JamKerjaHarian jamKerjaHarian = jamKerjaHarianService.findByTanggal(now.toLocalDate().toString());
//        JamKerja jamKerja = jamKerjaService.findByHariAndIsRamadhan(now.getDayOfWeek().getValue(),
//                hijriahService.isRamadhan(HijrahDate.now().get(ChronoField.YEAR), now.toLocalDate()));
        LocalTime jamLemburAwal = LocalTime.parse(jamKerjaHarian.getLemburStart());
        LocalTime jamLemburAkhir = LocalTime.parse(jamKerjaHarian.getLemburEnd());
        LocalTime jamMasukAwal = LocalTime.parse(jamKerjaHarian.getDatangStart());
        LocalTime jamPulangAkhir = LocalTime.parse(jamKerjaHarian.getPulangEnd());
        boolean isLembur = false;
        // jam lembur hari libur
        if(isLibur(now.toLocalDate()) && nowTime.isAfter(jamMasukAwal.minusSeconds(1)) && nowTime.isBefore(jamPulangAkhir.plusSeconds(1))) {
            isLembur = true;
        }
        // jam lembur hari kerja
        else if(!isLibur(now.toLocalDate()) && nowTime.isAfter(jamLemburAwal.minusSeconds(1)) && nowTime.isBefore(jamLemburAkhir.plusSeconds(1))) {
            isLembur = true;
        }

//		log.debug("getStatusLembur stop " + System.currentTimeMillis());

        return isLembur;
    }

}
