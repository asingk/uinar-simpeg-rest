package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLibur;
import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import id.ac.arraniry.simpegapi.repo.HariLiburRepo;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class HariLiburServiceImpl implements HariLiburService {

    private final HariLiburRepo hariLiburRepo;
    private final HariLiburTapiKerjaService hariLiburTapiKerjaService;

    public HariLiburServiceImpl(HariLiburRepo hariLiburRepo, HariLiburTapiKerjaService hariLiburTapiKerjaService) {
        this.hariLiburRepo = hariLiburRepo;
        this.hariLiburTapiKerjaService = hariLiburTapiKerjaService;
    }

    @Override
    public List<HariLibur> findAll() {
        return hariLiburRepo.findAll();
    }

    @Override
    public List<LocalDate> hariLiburBulanan(int tahun, int bulan) {
        List<HariLiburTapiKerja> hariLiburTapiKerjaList = hariLiburTapiKerjaService.findByTahun(tahun);
        List<LocalDate> weekendList = weekendDates(tahun, bulan, hariLiburTapiKerjaList);

//        List<HariLibur> hariLiburList = hariLiburRepo.findAll();

//        YearMonth month = YearMonth.of(tahun, bulan);
//        LocalDate startDate = month.atDay(1);
//        LocalDate endDate = month.atEndOfMonth();
//        List<HariLibur> hariLiburList = hariLiburRepo.findByTanggalBetween(startDate, endDate, Sort.by(Sort.Direction.ASC, "tanggal"));
        String pattern = String.format("%d-%02d-", tahun, bulan);
        List<HariLibur> hariLiburList = hariLiburRepo.findBulanan(pattern, Sort.by(Sort.Direction.ASC, "dateString"));

        List<LocalDate> result = new ArrayList<>();
        outerloop:
        for(HariLibur hari : hariLiburList) {
            LocalDate tanggal = LocalDate.parse(hari.getDateString());
            if(tanggal.getYear() == tahun && tanggal.getMonthValue() == bulan) {
                for(HariLiburTapiKerja hariKerja : hariLiburTapiKerjaList) {
                    if(hariKerja.getTanggal().isEqual(tanggal))
                        continue outerloop;
                }
                result.add(tanggal);
            }
        }

        result.addAll(weekendList);
        Collections.sort(result);

        return result;
    }

    private static @NonNull List<LocalDate> weekendDates(int tahun, int bulan, List<HariLiburTapiKerja> hariLiburTapiKerjaList) {
        YearMonth month = YearMonth.of(tahun, bulan);
        List<LocalDate> weekendList = new ArrayList<>();
        weekendloop:
        for(int i=1; i<=month.lengthOfMonth(); i++) {
            LocalDate tgl = LocalDate.of(tahun, bulan, i);
            if(tgl.getDayOfWeek().getValue() > 5) {
                for(HariLiburTapiKerja hariKerja : hariLiburTapiKerjaList) {
                    if(hariKerja.getTanggal().isEqual(tgl))
                        continue weekendloop;
                }
                weekendList.add(tgl);
            }

        }
        return weekendList;
    }

    @Override
    public List<HariLibur> findByTahun(Integer tahun) {
        LocalDate startDate = LocalDate.of(tahun, 1, 1);
        LocalDate endDate = LocalDate.of(tahun, 12, 31);
        return hariLiburRepo.findByTanggalBetween(startDate, endDate, Sort.by(Sort.Direction.ASC, "tanggal"));
    }

    @Override
    public String create(LocalDate tanggal) {
        if(tanggal.getDayOfWeek().getValue() > 5)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hanya boleh menambah hari libur di hari senin-jum'at");
        try {
            return hariLiburRepo.save(new HariLibur(tanggal)).getId();
        } catch (DuplicateKeyException mwe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "tanggal sudah ada!");
        }

    }

    @Override
    public void delete(String id) {
        hariLiburRepo.deleteById(id);
    }

}
