package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLibur;
import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import id.ac.arraniry.simpegapi.repo.HariLiburRepo;
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

        List<HariLibur> hariLiburList = hariLiburRepo.findAll();

        List<LocalDate> result = new ArrayList<>();
        outerloop:
        for(HariLibur hari : hariLiburList) {
            if(hari.getTanggal().getYear() == tahun && hari.getTanggal().getMonthValue() == bulan) {
                for(HariLiburTapiKerja hariKerja : hariLiburTapiKerjaList) {
                    if(hariKerja.getTanggal().isEqual(hari.getTanggal()))
                        continue outerloop;
                }
                result.add(hari.getTanggal());
            }
        }

        result.addAll(weekendList);
        Collections.sort(result);

        return result;
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
