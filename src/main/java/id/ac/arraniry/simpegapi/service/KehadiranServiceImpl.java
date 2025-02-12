package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.dto.LaporanKehadiranVO;
import id.ac.arraniry.simpegapi.dto.SaveResponse;
import id.ac.arraniry.simpegapi.entity.Izin;
import id.ac.arraniry.simpegapi.entity.Kehadiran;
import id.ac.arraniry.simpegapi.entity.KehadiranArc;
import id.ac.arraniry.simpegapi.entity.Pemutihan;
import id.ac.arraniry.simpegapi.helper.GlobalConstants;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    public Optional<Kehadiran> findByNipAndStatusAndTanggal(String nip, String status, LocalDate tanggal) {
        LocalTime timeStart = LocalTime.of(0, 0, 1);
        LocalTime timeEnd = LocalTime.of(23, 59, 59);
        LocalDateTime todayStart = LocalDateTime.of(tanggal, timeStart);
        LocalDateTime todayEnd = LocalDateTime.of(tanggal, timeEnd);
        return kehadiranRepo.findByNipAndStatusAndWaktuBetween(nip, status, todayStart, todayEnd);
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
            result.forEach(row -> {
                hadirVO.add(new KehadiranVO(row));
            });
        } else {
            var result = kehadiranArcRepo.findByPegawaiNipAndTanggal(nip, tanggal.toString());
            result.forEach(row -> {
                hadirVO.add(new KehadiranVO(row));
            });
        }
        return hadirVO;
    }
}
