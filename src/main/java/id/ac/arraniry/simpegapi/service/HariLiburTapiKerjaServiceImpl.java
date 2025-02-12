package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import id.ac.arraniry.simpegapi.repo.HariLiburTapiKerjaRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HariLiburTapiKerjaServiceImpl implements HariLiburTapiKerjaService {

    private final HariLiburTapiKerjaRepo hariLiburTapiKerjaRepo;

    public HariLiburTapiKerjaServiceImpl(HariLiburTapiKerjaRepo hariLiburTapiKerjaRepo) {
        this.hariLiburTapiKerjaRepo = hariLiburTapiKerjaRepo;
    }

    @Override
    public List<HariLiburTapiKerja> findAll() {
        return hariLiburTapiKerjaRepo.findAll();
    }

    @Override
    public List<HariLiburTapiKerja> findByTahun(Integer tahun) {
        LocalDate startDate = LocalDate.of(tahun, 1, 1);
        LocalDate endDate = LocalDate.of(tahun, 12, 31);
        return hariLiburTapiKerjaRepo.findByTanggalBetween(startDate, endDate, Sort.by(Sort.Direction.ASC, "tanggal"));
    }
}
