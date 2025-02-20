package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.dto.LaporanKehadiranVO;
import id.ac.arraniry.simpegapi.dto.SaveResponse;
import id.ac.arraniry.simpegapi.entity.*;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import id.ac.arraniry.simpegapi.repo.KehadiranArcRepo;
import id.ac.arraniry.simpegapi.repo.KehadiranRepo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KehadiranServiceImpl implements LaporanService, KehadiranService {

    private final KehadiranRepo kehadiranRepo;
    private final KehadiranArcRepo kehadiranArcRepo;
    private final HariLiburService hariLiburService;
    private final PemutihanService pemutihanService;
    private final IzinService izinService;

    public KehadiranServiceImpl(KehadiranRepo kehadiranRepo, KehadiranArcRepo kehadiranArcRepo, HariLiburService hariLiburService,
                                PemutihanService pemutihanService, IzinService izinService) {
        this.kehadiranRepo = kehadiranRepo;
        this.kehadiranArcRepo = kehadiranArcRepo;
        this.hariLiburService = hariLiburService;
        this.pemutihanService = pemutihanService;
        this.izinService = izinService;
    }

    @Override
    public List<LaporanKehadiranVO> findLaporanBulananByNip(String nip, int bulan, int tahun) {
        try {
            List<KehadiranVO> kehadiranVOList = findByNipAndBulanAndTahun(nip, bulan, tahun);
            return generateLaporanVO(nip, kehadiranVOList, bulan, tahun);
        } catch (ResponseStatusException nfe) {
            return generateLaporanVO(nip, null, bulan, tahun);
        }
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
//						if(kehadiran.getIdPegawai().equals("197404172005012002") && kehadiran.getTanggal().equals("2024-04-23")) {
//							System.out.println(kehadiran.getIdPegawai() + " " + kehadiran.getTanggal());
//						}
                        if ((j == 0 || !kehadiran.getIdPegawai().equals(kehadiranVOList.get(j-1).getIdPegawai()))
                                && value.getTanggal().isBefore(kehadiran.getWaktu().toLocalDate())) {    // record pertama list & record pertama pegawai
                            KehadiranVO kehadiranVO = new KehadiranVO();
                            kehadiranVO.setIdPegawai(kehadiran.getIdPegawai());
                            kehadiranVO.setNamaPegawai(kehadiran.getNamaPegawai());
                            kehadiranVO.setTanggal(value.getTanggal().toString());
                            addPutihVO.add(kehadiranVO);
                        } else if ((j == kehadiranVOList.size()-1 || !kehadiran.getIdPegawai().equals(kehadiranVOList.get(j+1).getIdPegawai()))
                                && value.getTanggal().isAfter(kehadiran.getWaktu().toLocalDate())) {	// record terakhir list & record terakhir pegawai
                            KehadiranVO kehadiranVO = new KehadiranVO();
                            kehadiranVO.setIdPegawai(kehadiran.getIdPegawai());
                            kehadiranVO.setNamaPegawai(kehadiran.getNamaPegawai());
                            kehadiranVO.setTanggal(value.getTanggal().toString());
                            addPutihVO.add(kehadiranVO);
                        } else if (j > 0 && j < kehadiranVOList.size()-1 && kehadiranVOList.get(j+1).getIdPegawai().equals(kehadiran.getIdPegawai())
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

    @Override
    public List<RekapRemunPegawai> findLaporanRemon(Integer bulan, Integer tahun, List<JabatanBulanan> jabatanBulanans) {
        List<RekapRemunPegawai> rekapRemunPegawaiList = new ArrayList<>();
        jabatanBulanans.forEach(row -> {
            RekapRemunPegawai rekapRemunPegawai = findRemonPegawai(row.getNip(), tahun, bulan, row);
            rekapRemunPegawaiList.add(rekapRemunPegawai);
        });
        return rekapRemunPegawaiList;
    }

    @Override
    public List<RekapRemunGrade> findRekapRemonGrade(Integer bulan, Integer tahun, String unitRemun, List<RekapRemunPegawai> rekapRemunPegawaiList) {
        rekapRemunPegawaiList.sort(Comparator.comparing(RekapRemunPegawai::getImplementasiRemun).reversed());
        return generateRekapRemonGrade(bulan, tahun, unitRemun, rekapRemunPegawaiList);
    }

    protected List<RekapRemunGrade> generateRekapRemonGrade(Integer bulan, Integer tahun, String unitRemun, List <RekapRemunPegawai> rekapRemunPegawaiList) {
        List<RekapRemunGrade> rekapRemonGradeVOS = new ArrayList<>();
        int jumlahRemonP1 = 0;
        int jumlahPotongan = 0;
        int jumlahSetelahPotongan = 0;
        int jumlahPajak = 0;
        int jumlahNetto = 0;
        int jumlahPenerima = 0;
        for (int i = 0; i < rekapRemunPegawaiList.size(); i++) {
            RekapRemunPegawai remonVO = rekapRemunPegawaiList.get(i);
            RekapRemunGrade gradeVO = new RekapRemunGrade();
            gradeVO.setTahun(tahun);
            gradeVO.setBulan(bulan);
            gradeVO.setUnitRemun(unitRemun);
            if (i == 0 || Objects.equals(remonVO.getImplementasiRemun(), rekapRemunPegawaiList.get(i - 1).getImplementasiRemun())) {
                jumlahRemonP1 += remonVO.getRemunP1();
                jumlahPotongan += remonVO.getRupiahPotongan();
                jumlahSetelahPotongan += remonVO.getSetelahPotongan();
                jumlahPajak += remonVO.getRupiahPajak();
                jumlahNetto += remonVO.getNetto();
                jumlahPenerima++;
            } else {
                gradeVO.setGrade(rekapRemunPegawaiList.get(i - 1).getGrade());
                gradeVO.setImplementasiRemunPersen(rekapRemunPegawaiList.get(i - 1).getImplementasiRemunPersen());
                gradeVO.setImplementasiRemun(rekapRemunPegawaiList.get(i - 1).getImplementasiRemun());
                gradeVO.setRemunP1(rekapRemunPegawaiList.get(i - 1).getRemunP1());
                gradeVO.setJumlahRemunP1(jumlahRemonP1);
                gradeVO.setJumlahPenerima(jumlahPenerima);
                gradeVO.setJumlahPotongan(jumlahPotongan);
                gradeVO.setJumlahSetelahPotongan(jumlahSetelahPotongan);
                gradeVO.setJumlahPajak(jumlahPajak);
                gradeVO.setJumlahNetto(jumlahNetto);
                rekapRemonGradeVOS.add(gradeVO);
                gradeVO = new RekapRemunGrade();
                jumlahRemonP1 = remonVO.getRemunP1();
                jumlahPotongan = remonVO.getRupiahPotongan();
                jumlahSetelahPotongan = remonVO.getSetelahPotongan();
                jumlahPajak = remonVO.getRupiahPajak();
                jumlahNetto = remonVO.getNetto();
                jumlahPenerima = 1;
            }
            if (i == rekapRemunPegawaiList.size() - 1) {
                gradeVO.setGrade(remonVO.getGrade());
                gradeVO.setImplementasiRemunPersen(remonVO.getImplementasiRemunPersen());
                gradeVO.setImplementasiRemun(remonVO.getImplementasiRemun());
                gradeVO.setRemunP1(remonVO.getRemunP1());
                gradeVO.setJumlahRemunP1(jumlahRemonP1);
                gradeVO.setJumlahPenerima(jumlahPenerima);
                gradeVO.setJumlahPotongan(jumlahPotongan);
                gradeVO.setJumlahSetelahPotongan(jumlahSetelahPotongan);
                gradeVO.setJumlahPajak(jumlahPajak);
                gradeVO.setJumlahNetto(jumlahNetto);
                rekapRemonGradeVOS.add(gradeVO);
            }
        }
        return rekapRemonGradeVOS;
    }

    private RekapRemunPegawai findRemonPegawai(String nip, int tahun, int bulan, JabatanBulanan jabatanBulanan) {
        var hadirList = findByNipAndBulanAndTahunAndIsDeletedFalse(nip, bulan, tahun);
        return generateRemonPegawai(bulan, tahun, hadirList, jabatanBulanan);
    }

    protected RekapRemunPegawai generateRemonPegawai (Integer bulan, Integer tahun, List <KehadiranVO> hadirList, JabatanBulanan jabatanBulanan){
        RekapRemunPegawai rekapRemunPegawai = null;
        int lengthOfMonth = YearMonth.of(tahun, bulan).lengthOfMonth();
        List<LocalDate> hariLibur = hariLiburService.hariLiburBulanan(tahun, bulan);
        int[][] potong = {{0, 0, 0, 0}, {0, 0, 0, 0}};
        double persenPotong = 0;
        if (hadirList.isEmpty()) {
            int hariKerja = lengthOfMonth - hariLibur.size();
            var izinList = izinService.findByNipAndTahunAndBulan(jabatanBulanan.getNip(), tahun, bulan);
            int gakAbsen = hariKerja - izinList.size();
            potong[0][0] = 0;
            potong[0][1] = 0;
            potong[0][2] = 0;
            potong[0][3] = gakAbsen;
            potong[1][0] = 0;
            potong[1][1] = 0;
            potong[1][2] = 0;
            potong[1][3] = gakAbsen;
            persenPotong = gakAbsen * 3;
            rekapRemunPegawai = new RekapRemunPegawai();
            rekapRemunPegawai.setNip(jabatanBulanan.getNip());
            rekapRemunPegawai.setNama(jabatanBulanan.getNama());
            rekapRemunPegawai.setUnitRemun(jabatanBulanan.getUnitRemun());
            rekapRemunPegawai.setGolongan(jabatanBulanan.getGolongan());
            rekapRemunPegawai.setGrade(jabatanBulanan.getGrade());
            rekapRemunPegawai.setRemunGrade(jabatanBulanan.getRemunGrade());
            rekapRemunPegawai.setNamaJabatan(jabatanBulanan.getJabatan());
            rekapRemunPegawai.setImplementasiRemunPersen(jabatanBulanan.getImplementasiRemun());
            rekapRemunPegawai.setImplementasiRemun(jabatanBulanan.getRemunGrade() * jabatanBulanan.getImplementasiRemun() / 100);
            rekapRemunPegawai.setPersenPotongan(persenPotong);
            rekapRemunPegawai.setRemunP1((int) Math.round(0.3 * rekapRemunPegawai.getImplementasiRemun()));
            rekapRemunPegawai.setRupiahPotongan((int) Math.round(rekapRemunPegawai.getRemunP1() * persenPotong / 100));
            rekapRemunPegawai.setSetelahPotongan(rekapRemunPegawai.getRemunP1() - rekapRemunPegawai.getRupiahPotongan());
            rekapRemunPegawai.setPersenPajak(null != jabatanBulanan.getPajak() ? jabatanBulanan.getPajak() : 0);
            rekapRemunPegawai.setRupiahPajak(rekapRemunPegawai.getSetelahPotongan() * rekapRemunPegawai.getPersenPajak() / 100);
            rekapRemunPegawai.setNetto(rekapRemunPegawai.getSetelahPotongan() - rekapRemunPegawai.getRupiahPajak());
            rekapRemunPegawai.setTahun(tahun);
            rekapRemunPegawai.setBulan(bulan);
            insertRemonVO(rekapRemunPegawai, potong);
        } else {
            var pemutihanList = pemutihanService.findByBulanAndTahun(bulan, tahun);
            int tanggal = 1;
            int j = 0;
            KehadiranVO kehadiranVO = null;
            KehadiranVO nextHadirVO = null;
            outerTgl:
            while (tanggal <= lengthOfMonth) {
                if (jabatanBulanan.getNip().equals("198812072018032001")) {
                    System.out.println(jabatanBulanan.getNama());
                }
                if (tanggal == 1) {
                    if (null != rekapRemunPegawai) {
                        if (null != rekapRemunPegawai.getRemunP1()) {
                            rekapRemunPegawai.setRupiahPotongan((int) Math.round(rekapRemunPegawai.getRemunP1() * persenPotong / 100));
                            rekapRemunPegawai.setSetelahPotongan(rekapRemunPegawai.getRemunP1() - rekapRemunPegawai.getRupiahPotongan());
                            if (null != rekapRemunPegawai.getPersenPajak()) {
                                rekapRemunPegawai.setRupiahPajak(rekapRemunPegawai.getSetelahPotongan() * rekapRemunPegawai.getPersenPajak() / 100);
                                rekapRemunPegawai.setNetto(rekapRemunPegawai.getSetelahPotongan() - rekapRemunPegawai.getRupiahPajak());
                            }
                        }
                    }
                    rekapRemunPegawai = new RekapRemunPegawai();
                    potong[0][0] = 0;
                    potong[0][1] = 0;
                    potong[0][2] = 0;
                    potong[0][3] = 0;
                    potong[1][0] = 0;
                    potong[1][1] = 0;
                    potong[1][2] = 0;
                    potong[1][3] = 0;
                    persenPotong = 0;
                }
                LocalDate loopingDate = LocalDate.of(tahun, bulan, tanggal);
                if (hariLibur.contains(loopingDate)) {
                    if (null == kehadiranVO) {
                        tanggal++;
                    } else if (tanggal == lengthOfMonth) {
                        rekapRemunPegawai.setPersenPotongan(persenPotong);
                        if (j == hadirList.size() - 1) {
                            if (null != rekapRemunPegawai.getRemunP1()) {
                                rekapRemunPegawai.setRupiahPotongan((int) Math.round(rekapRemunPegawai.getRemunP1() * persenPotong / 100));
                                rekapRemunPegawai.setSetelahPotongan(rekapRemunPegawai.getRemunP1() - rekapRemunPegawai.getRupiahPotongan());
                                if (null != rekapRemunPegawai.getPersenPajak()) {
                                    rekapRemunPegawai.setRupiahPajak(rekapRemunPegawai.getSetelahPotongan() * rekapRemunPegawai.getPersenPajak() / 100);
                                    rekapRemunPegawai.setNetto(rekapRemunPegawai.getSetelahPotongan() - rekapRemunPegawai.getRupiahPajak());
                                }
                            }
                        }
                        insertRemonVO(rekapRemunPegawai, potong);
                        if (j == hadirList.size() - 1) break;
                        j++;
                        tanggal = 1;
                    } else {
                        if (!isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO)) {
                            assert nextHadirVO != null;
                            if (kehadiranVO.getWaktu().getDayOfMonth() != tanggal + 1 && nextHadirVO.getWaktu().getDayOfMonth() == tanggal + 1) {
                                j++;
                            }
                        }
                        tanggal++;
                    }
                    continue;
                }

                outerStatus:
                for (int k = 0; k < 2; k++) {
                    while (j < hadirList.size()) {
                        kehadiranVO = hadirList.get(j);
//                    if (kehadiranVO.getWaktu().toLocalDate().toString().equals("2023-05-26")) {
//                        System.out.println(kehadiranVO.getNamaPegawai());
//                    }
                        if (j < hadirList.size() - 1) {
                            nextHadirVO = hadirList.get(j + 1);
                        }
                        if (isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO) &&
                                !kehadiranVO.getIdPegawai().equals(rekapRemunPegawai.getNip())) {    // sebelum ke pegawai selanjutnya
//						if (null == kehadiranVO.getGradePegawai()) {
//							System.out.println(kehadiranVO.getNamaPegawai());
//                            System.out.println(kehadiranVO.getWaktu());
//                            System.out.println(kehadiranVO.getStatus());
//						}
                            rekapRemunPegawai.setNip(kehadiranVO.getIdPegawai());
                            rekapRemunPegawai.setNama(kehadiranVO.getNamaPegawai());
                            rekapRemunPegawai.setUnitRemun(jabatanBulanan.getUnitRemun());
                            rekapRemunPegawai.setGolongan(jabatanBulanan.getGolongan());
                            rekapRemunPegawai.setTahun(tahun);
                            rekapRemunPegawai.setBulan(bulan);
//                            remonVO.setJenisJabatan(jabatanBulanan.getJenisJabatan());
                            rekapRemunPegawai.setGrade(jabatanBulanan.getGrade());
                            rekapRemunPegawai.setRemunGrade(jabatanBulanan.getRemunGrade());
                            rekapRemunPegawai.setNamaJabatan(jabatanBulanan.getJabatan());
                            rekapRemunPegawai.setImplementasiRemunPersen(jabatanBulanan.getImplementasiRemun());
                            rekapRemunPegawai.setImplementasiRemun(jabatanBulanan.getRemunGrade() * jabatanBulanan.getImplementasiRemun() / 100);
                            rekapRemunPegawai.setRemunP1((int) Math.round(0.3 * rekapRemunPegawai.getImplementasiRemun()));
//                        }
                            rekapRemunPegawai.setPersenPajak(null != jabatanBulanan.getPajak() ? jabatanBulanan.getPajak() : 0);
                            var izinList = izinService.findByNipAndTahunAndBulan(kehadiranVO.getIdPegawai(), tahun, bulan);
                            int izinSize = izinList.size();
                            potong[0][3] -= izinSize;
                            potong[1][3] -= izinSize;
                            persenPotong -= izinSize * 3;
                            if (izinSize > 0 && !pemutihanList.isEmpty()) {
                                for (Pemutihan putih : pemutihanList) {
                                    for (Izin izin : izinList) {
                                        if (putih.getTanggal().isEqual(izin.getTanggal())) {
                                            if (GlobalConstants.STATUS_DATANG.equals(putih.getStatus())) {
                                                potong[0][3]++;
                                            } else {
                                                potong[1][3]++;
                                            }
                                            persenPotong += 1.5;
                                            break;
                                        }
                                    }
                                }
                            }

                        }
                        if (!loopingDate.isEqual(kehadiranVO.getWaktu().toLocalDate())) {    // gak absen datang, gak absen pulang
                            if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_DATANG, pemutihanList)) {
                                potong[0][3]++;
                                persenPotong += 1.5;
                            }
                            if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_PULANG, pemutihanList)) {
                                potong[1][3]++;
                                persenPotong += 1.5;
                            }
                            if (tanggal == lengthOfMonth) {
                                rekapRemunPegawai.setPersenPotongan(persenPotong);
                                if (j == hadirList.size() - 1) {
                                    if (null != rekapRemunPegawai.getRemunP1()) {
                                        rekapRemunPegawai.setRupiahPotongan((int) Math.round(rekapRemunPegawai.getRemunP1() * persenPotong / 100));
                                        rekapRemunPegawai.setSetelahPotongan(rekapRemunPegawai.getRemunP1() - rekapRemunPegawai.getRupiahPotongan());
                                        if (null != rekapRemunPegawai.getPersenPajak()) {
                                            rekapRemunPegawai.setRupiahPajak(rekapRemunPegawai.getSetelahPotongan() * rekapRemunPegawai.getPersenPajak() / 100);
                                            rekapRemunPegawai.setNetto(rekapRemunPegawai.getSetelahPotongan() - rekapRemunPegawai.getRupiahPajak());
                                        }
                                    }
                                }
                                insertRemonVO(rekapRemunPegawai, potong);
                                if (j == hadirList.size() - 1) break outerTgl;
                                j++;
                                tanggal = 1;
                            } else {
                                if (!isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO)) {
                                    assert nextHadirVO != null;
                                    if (nextHadirVO.getWaktu().getDayOfMonth() == tanggal + 1
                                            && loopingDate.isAfter(kehadiranVO.getWaktu().toLocalDate())) {
                                        j++;
                                    }
                                }
                                tanggal++;
                            }
                            continue outerTgl;
                        } else {
                            if (k == 0) {
                                if (GlobalConstants.STATUS_PULANG.equals(kehadiranVO.getStatus())) {    // gak absen datang
                                    if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_DATANG, pemutihanList)) {
                                        potong[0][3]++;
                                        persenPotong += 1.5;
                                    }
                                    if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_PULANG, pemutihanList)) {
                                        persenPotong += potongRemon(kehadiranVO, potong);
                                    }
                                    if (tanggal == lengthOfMonth) {
                                        rekapRemunPegawai.setPersenPotongan(persenPotong);
                                        if (j == hadirList.size() - 1) {
                                            if (null != rekapRemunPegawai.getRemunP1()) {
                                                rekapRemunPegawai.setRupiahPotongan((int) Math.round(rekapRemunPegawai.getRemunP1() * persenPotong / 100));
                                                rekapRemunPegawai.setSetelahPotongan(rekapRemunPegawai.getRemunP1() - rekapRemunPegawai.getRupiahPotongan());
                                                if (null != rekapRemunPegawai.getPersenPajak()) {
                                                    rekapRemunPegawai.setRupiahPajak(rekapRemunPegawai.getSetelahPotongan() * rekapRemunPegawai.getPersenPajak() / 100);
                                                    rekapRemunPegawai.setNetto(rekapRemunPegawai.getSetelahPotongan() - rekapRemunPegawai.getRupiahPajak());
                                                }
                                            }
                                        }
                                        insertRemonVO(rekapRemunPegawai, potong);
                                        if (j == hadirList.size() - 1) break outerTgl;
                                        j++;
                                        tanggal = 1;
                                    } else {
                                        if (!isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO)) {
                                            assert nextHadirVO != null;
                                            if (nextHadirVO.getWaktu().getDayOfMonth() == tanggal + 1) {
                                                j++;
                                            }
                                        }
                                        tanggal++;
                                    }
                                    continue outerTgl;
                                } else if (j == hadirList.size() - 1
                                        || isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO)
                                        || (!isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO)
                                        && Objects.requireNonNull(nextHadirVO).getWaktu().toLocalDate().isAfter(loopingDate))) {    // gak absen pulang
                                    if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_PULANG, pemutihanList)) {
                                        potong[1][3]++;
                                        persenPotong += 1.5;
                                    }
                                    if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_DATANG, pemutihanList)) {
                                        persenPotong += potongRemon(kehadiranVO, potong);
                                    }
                                    if (tanggal == lengthOfMonth) {
                                        rekapRemunPegawai.setPersenPotongan(persenPotong);
                                        if (null != rekapRemunPegawai.getRemunP1()) {
                                            rekapRemunPegawai.setRupiahPotongan((int) Math.round(rekapRemunPegawai.getRemunP1() * persenPotong / 100));
                                            rekapRemunPegawai.setSetelahPotongan(rekapRemunPegawai.getRemunP1() - rekapRemunPegawai.getRupiahPotongan());
                                            if (null != rekapRemunPegawai.getPersenPajak()) {
                                                rekapRemunPegawai.setRupiahPajak(rekapRemunPegawai.getSetelahPotongan() * rekapRemunPegawai.getPersenPajak() / 100);
                                                rekapRemunPegawai.setNetto(rekapRemunPegawai.getSetelahPotongan() - rekapRemunPegawai.getRupiahPajak());
                                            }
                                        }
                                        insertRemonVO(rekapRemunPegawai, potong);
                                        if (j == hadirList.size() - 1) break outerTgl;
                                        j++;
                                        tanggal = 1;
                                    } else {
                                        if (!isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO)) {
                                            assert nextHadirVO != null;
                                            if (nextHadirVO.getWaktu().getDayOfMonth() == tanggal + 1) {
                                                j++;
                                            }
                                        }
                                        tanggal++;
                                    }
                                    continue outerTgl;
                                } else {
                                    if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_DATANG, pemutihanList)) {
                                        persenPotong += potongRemon(kehadiranVO, potong);
                                    }
                                    j++;
                                    continue outerStatus;
                                }
                            }
                            if (isPemutihanInv(loopingDate, GlobalConstants.STATUS_PULANG, pemutihanList)) {
                                persenPotong += potongRemon(kehadiranVO, potong);
                            }
                            if (tanggal == lengthOfMonth
                                    || (!isNextPegawai(j, hadirList.size(), kehadiranVO.getIdPegawai(), nextHadirVO)
                                    && Objects.requireNonNull(nextHadirVO).getWaktu().getDayOfMonth() == tanggal + 1)) {
                                j++;
                            }
                            continue outerStatus;
                        }
                    }
                }
                if (tanggal == lengthOfMonth) {
//                    if (jabatanBulanan.getNip().equals("198812072018032001")) {
//                        System.out.println(jabatanBulanan.getNama());
//                    }
                    rekapRemunPegawai.setPersenPotongan(persenPotong);
                    insertRemonVO(rekapRemunPegawai, potong);
                    if (j == hadirList.size()) {
                        if (null != rekapRemunPegawai.getRemunP1()) {
                            rekapRemunPegawai.setRupiahPotongan((int) Math.round(rekapRemunPegawai.getRemunP1() * persenPotong / 100));
                            rekapRemunPegawai.setSetelahPotongan(rekapRemunPegawai.getRemunP1() - rekapRemunPegawai.getRupiahPotongan());
                            if (null != rekapRemunPegawai.getPersenPajak()) {
                                rekapRemunPegawai.setRupiahPajak(rekapRemunPegawai.getSetelahPotongan() * rekapRemunPegawai.getPersenPajak() / 100);
                                rekapRemunPegawai.setNetto(rekapRemunPegawai.getSetelahPotongan() - rekapRemunPegawai.getRupiahPajak());
                            }
                        }
                        break;
                    }
                    tanggal = 1;
                } else {
                    tanggal++;
                }
            }
        }

        return rekapRemunPegawai;
    }

    private void insertRemonVO (RekapRemunPegawai rekapRemunPegawai,int[][] potong){
        rekapRemunPegawai.setD1(potong[0][0]);
        rekapRemunPegawai.setD2(potong[0][1]);
        rekapRemunPegawai.setD3(potong[0][2]);
        rekapRemunPegawai.setD4(potong[0][3]);
        rekapRemunPegawai.setP1(potong[1][0]);
        rekapRemunPegawai.setP2(potong[1][1]);
        rekapRemunPegawai.setP3(potong[1][2]);
        rekapRemunPegawai.setP4(potong[1][3]);
    }

    private boolean isNextPegawai ( int j, int hadirListSize, String currentIdPegawai, KehadiranVO nextHehadiranVO){
        boolean result = false;
        if (j == hadirListSize - 1) {
            result = true;
        } else if (!currentIdPegawai.equals(nextHehadiranVO.getIdPegawai())) {
            result = true;
        }
        return result;
    }

    private boolean isPemutihanInv (LocalDate tanggal, String status, List < Pemutihan > pemutihanList){
        boolean result = false;
        if (null != pemutihanList && !pemutihanList.isEmpty()) {
            for (Pemutihan row : pemutihanList) {
                if (tanggal.isEqual(row.getTanggal()) && status.equals(row.getStatus())) {
                    result = true;
                    break;
                }
            }
        }
        return !result;
    }

    private double potongRemon (KehadiranVO kehadiran,int[][] potong){
        double potongPersen = 0;
        if (kehadiran.getStatus().equals(GlobalConstants.STATUS_DATANG)) {
            if (null != kehadiran.getKurangMenit() && kehadiran.getKurangMenit() > 0) {
                if (kehadiran.getKurangMenit() < 31) {
                    potong[0][0]++;
                    potongPersen = 0.5;
                } else if (kehadiran.getKurangMenit() < 61) {
                    potong[0][1]++;
                    potongPersen = 1;
                } else if (kehadiran.getKurangMenit() < 91) {
                    potong[0][2]++;
                    potongPersen = 1.25;
                } else {
                    potong[0][3]++;
                    potongPersen = 1.5;
                }
            }
        } else {
            if (null != kehadiran.getKurangMenit() && kehadiran.getKurangMenit() > 0) {
                if (kehadiran.getKurangMenit() < 31) {
                    potong[1][0]++;
                    potongPersen = 0.5;
                } else if (kehadiran.getKurangMenit() < 61) {
                    potong[1][1]++;
                    potongPersen = 1;
                } else if (kehadiran.getKurangMenit() < 91) {
                    potong[1][2]++;
                    potongPersen = 1.25;
                } else {
                    potong[1][3]++;
                    potongPersen = 1.5;
                }
            }
        }
        return potongPersen;
    }

    private List<KehadiranVO> findByNipAndBulanAndTahunAndIsDeletedFalse(String nip, int bulan, int tahun) {
        LocalTime timeStart = LocalTime.of(0, 0, 1);
        LocalTime timeEnd = LocalTime.of(23, 59, 59);
        LocalDate dayStart = LocalDate.of(tahun, bulan, 1);
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate dayEnd = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        LocalDateTime firstDay = LocalDateTime.of(dayStart, timeStart);
        LocalDateTime lastDay = LocalDateTime.of(dayEnd, timeEnd);
        List<KehadiranVO> kehadirVOList = new ArrayList<>();
        if (LocalDate.now().getYear() == tahun) {
            List<Kehadiran> kehadiranList = kehadiranRepo.findByNipAndTanggalBetweenAndIsDeletedFalse(nip, firstDay, lastDay,
                    Sort.by(Sort.Direction.ASC, "waktu"));
            kehadiranList.forEach(kehadiran -> {
                KehadiranVO kehadiranVO = new KehadiranVO(kehadiran);
                kehadirVOList.add(kehadiranVO);
            });
        } else {
            List<KehadiranArc> kehadiranList = kehadiranArcRepo.findByNipAndTanggalBetweenAndIsDeletedFalse(nip, firstDay, lastDay,
                    Sort.by(Sort.Direction.ASC, "waktu"));
            kehadiranList.forEach(kehadiran -> {
                KehadiranVO kehadiranVO = new KehadiranVO(kehadiran);
                kehadirVOList.add(kehadiranVO);
            });
        }
        return kehadirVOList;
    }

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

    private List<KehadiranVO> findByNipAndBulanAndTahun(String nip, int bulan, int tahun) {
        LocalTime timeStart = LocalTime.of(0, 0, 1);
        LocalTime timeEnd = LocalTime.of(23, 59, 59);
        LocalDate dayStart = LocalDate.of(tahun, bulan, 1);
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate dayEnd = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        LocalDateTime firstDay = LocalDateTime.of(dayStart, timeStart);
        LocalDateTime lastDay = LocalDateTime.of(dayEnd, timeEnd);
        List<KehadiranVO> hadirVOList = new ArrayList<>();
            if (LocalDate.now().getYear() == tahun) {
            List<Kehadiran> kehadiranList = kehadiranRepo.findByNipAndTanggalBetween(nip, firstDay, lastDay, Sort.by(Sort.Direction.ASC, "waktu"));
            if(kehadiranList.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "tidak ditemukan!");
            for(Kehadiran hadir : kehadiranList) {
                KehadiranVO hadirVO = new KehadiranVO(hadir);
                hadirVOList.add(hadirVO);
            }
        } else {
            List<KehadiranArc> kehadiranList = kehadiranArcRepo.findByNipAndTanggalBetween(nip, firstDay, lastDay, Sort.by(Sort.Direction.ASC, "waktu"));
            if(kehadiranList.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "tidak ditemukan!");
            for(KehadiranArc hadir : kehadiranList) {
                KehadiranVO hadirVO = new KehadiranVO(hadir);
                hadirVOList.add(hadirVO);
            }
        }
        return hadirVOList;
    }

    protected List<LaporanKehadiranVO> generateLaporanVO(String nip, List<KehadiranVO> hadirList, int month, int year) {
        List<LaporanKehadiranVO> resultList = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(year, month);
        int lengthOfMonth = yearMonth.equals(YearMonth.of(LocalDate.now().getYear(),
                LocalDate.now().getMonthValue())) ? LocalDate.now().getDayOfMonth() : yearMonth.lengthOfMonth();
        List<LocalDate> hariLibur = hariLiburService.hariLiburBulanan(year, month);
        if(null != hadirList && !hadirList.isEmpty()) {
            int hadirIndex = 0;

            for(int tgl=1; tgl<=lengthOfMonth; tgl++) {
                LocalDate date = LocalDate.of(year, month, tgl);

                if(!hariLibur.contains(date)) {
                    LaporanKehadiranVO lapVO = new LaporanKehadiranVO();
                    // gak absen datang, gak absen pulang, gak ada absen lagi di hari selanjutnya
                    if(hadirIndex == hadirList.size()) {
                        lapVO.setTanggal(date);
                        resultList.add(lapVO);
                    }
                    while(hadirIndex < hadirList.size()) {
                        KehadiranVO hadirVO = hadirList.get(hadirIndex);

                        if(hadirVO.getWaktu().toLocalDate().isAfter(date)) {
                            LaporanKehadiranVO lastLapVO = new LaporanKehadiranVO();
                            if(!resultList.isEmpty()) {
                                lastLapVO = resultList.get(resultList.size()-1);
                            }

                            // gak absen datang, gak absen pulang
                            if(resultList.isEmpty() || lastLapVO.getTanggal().isBefore(date)) {
                                lapVO.setTanggal(date);
                                resultList.add(lapVO);
                                break;
                            }

                            // ada absen datang, gak absen pulang tapi ada absen di hari selanjutnya
                            else if(null != lastLapVO.getJamDatang() && null == lastLapVO.getJamPulang()
                                    && lastLapVO.getTanggal().isEqual(date)) {
                                break;
                            }

                        }

                        // absen datang
                        if(GlobalConstants.STATUS_DATANG.equals(hadirVO.getStatus())) {
                            lapVO.setTanggal(date);
                            lapVO.setJamDatang(hadirVO.getWaktu().toLocalTime());
                            if(null != hadirVO.getIsDeleted() && hadirVO.getIsDeleted()) {
                                lapVO.setKeteranganDatang(GlobalConstants.DIHAPUS_STRING);
                            } else if(null != hadirVO.getIsAdded() && hadirVO.getIsAdded()) {
                                lapVO.setKeteranganDatang(GlobalConstants.DITAMBAHKAN_STRING);
                            }
                            lapVO.setTelatDatang(hadirVO.getKurangMenit());
                            resultList.add(lapVO);
                            hadirIndex++;
                        }

                        // absen pulang tapi gak absen datang
                        else if(GlobalConstants.STATUS_PULANG.equals(hadirVO.getStatus()) && (resultList.isEmpty()
                                || !resultList.get(resultList.size() - 1).getTanggal().isEqual(hadirVO.getWaktu().toLocalDate()))) {
                            lapVO.setTanggal(date);
                            lapVO.setJamPulang(hadirVO.getWaktu().toLocalTime());
                            if(null != hadirVO.getIsDeleted() && hadirVO.getIsDeleted()) {
                                lapVO.setKeteranganPulang(GlobalConstants.DIHAPUS_STRING);
                            } else if(null != hadirVO.getIsAdded() && hadirVO.getIsAdded()) {
                                lapVO.setKeteranganPulang(GlobalConstants.DITAMBAHKAN_STRING);
                            }
                            lapVO.setCepatPulang(hadirVO.getKurangMenit());

                            resultList.add(lapVO);
                            hadirIndex++;
                            break;
                        }

                        // absen pulang, ada absen datang
                        else if(GlobalConstants.STATUS_PULANG.equals(hadirVO.getStatus())
                                && resultList.get(resultList.size()-1).getTanggal().isEqual(hadirVO.getWaktu().toLocalDate())){
                            lapVO = resultList.get(resultList.size()-1);
                            lapVO.setJamPulang(hadirVO.getWaktu().toLocalTime());
                            if(null != hadirVO.getIsDeleted() && hadirVO.getIsDeleted()) {
                                lapVO.setKeteranganPulang(GlobalConstants.DIHAPUS_STRING);
                            } else if(null != hadirVO.getIsAdded() && hadirVO.getIsAdded()) {
                                lapVO.setKeteranganPulang(GlobalConstants.DITAMBAHKAN_STRING);
                            }
                            lapVO.setCepatPulang(hadirVO.getKurangMenit());
                            hadirIndex++;
                            break;
                        }

                    }
                }
            }
        } else {
            for(int tgl=1; tgl<=lengthOfMonth; tgl++) {
                LocalDate date = LocalDate.of(year, month, tgl);
                if(!hariLibur.contains(date)) {
                    LaporanKehadiranVO lapVO = new LaporanKehadiranVO();
                    // gak absen datang, gak absen pulang, gak ada absen lagi di hari selanjutnya
                    lapVO.setTanggal(date);
                    resultList.add(lapVO);
                }
            }
        }

        List<Pemutihan> pemutihanList = pemutihanService.findByBulanAndTahun(month, year);
        if(null != pemutihanList && !pemutihanList.isEmpty()) {
            outer:
            for(Pemutihan row: pemutihanList) {
                for (LaporanKehadiranVO laporanKehadiranVO : resultList) {
                    if (row.getTanggal().isEqual(laporanKehadiranVO.getTanggal())) {
                        if (row.getStatus().equals(GlobalConstants.STATUS_DATANG)) {
                            laporanKehadiranVO.setKeteranganDatang(GlobalConstants.PEMUTIHAN_STRING);
                        } else if (row.getStatus().equals(GlobalConstants.STATUS_PULANG)) {
                            laporanKehadiranVO.setKeteranganPulang(GlobalConstants.PEMUTIHAN_STRING);
                        }
                        continue outer;
                    }
                }
            }
        }

        List<Izin> izinList = izinService.findByNipAndTahunAndBulan(nip, year, month);
        if (null != izinList && !izinList.isEmpty()) {
            outer:
            for (Izin row: izinList) {
                for (LaporanKehadiranVO laporanKehadiranVO : resultList) {
                    if (row.getTanggal().isEqual(laporanKehadiranVO.getTanggal())) {
                        laporanKehadiranVO.setKeteranganDatang(row.getIzinCategoryDesc());
                        laporanKehadiranVO.setKeteranganPulang(row.getIzinCategoryDesc());
                        continue outer;
                    }
                }
            }
        }
        return resultList;
    }

    @Override
    public KehadiranVO findByNipAndStatusAndTanggal(String nip, String status, LocalDate tanggal) {
        LocalTime timeStart = LocalTime.of(0, 0, 1);
        LocalTime timeEnd = LocalTime.of(23, 59, 59);
        LocalDateTime todayStart = LocalDateTime.of(tanggal, timeStart);
        LocalDateTime todayEnd = LocalDateTime.of(tanggal, timeEnd);
        if (tanggal.getYear() == LocalDate.now().getYear()) {
            Optional<Kehadiran> kehadiran = kehadiranRepo.findByNipAndStatusAndWaktuBetween(nip, status, todayStart, todayEnd);
            if (kehadiran.isPresent()) {
                return new KehadiranVO(kehadiran.get());
            }
        } else {
            Optional<KehadiranArc> kehadiranArc = kehadiranArcRepo.findByNipAndStatusAndWaktuBetween(nip, status, todayStart, todayEnd);
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
}
