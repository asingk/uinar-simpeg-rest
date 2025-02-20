package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Pemutihan;
import id.ac.arraniry.simpegapi.repo.PemutihanRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PemutihanServiceImpl implements PemutihanService {

    private final PemutihanRepository pemutihanRepo;

    public PemutihanServiceImpl(PemutihanRepository pemutihanRepo) {
        this.pemutihanRepo = pemutihanRepo;
    }

    @Override
    public Pemutihan findByTanggalAndStatus(LocalDate tanggal, String status) {
        return pemutihanRepo.findByTanggalAndStatus(tanggal, status);
    }

    @Override
    public List<Pemutihan> findByBulanAndTahun(Integer bulan, Integer tahun) {
        LocalDate startDate = LocalDate.of(tahun, bulan, 1);
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate endDate = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        return pemutihanRepo.findByTanggalBetween(startDate, endDate, Sort.by(Sort.Direction.ASC, "tanggal"));
    }

    @Override
    public List<Pemutihan> findByBulanAndTahunAndStatus(Integer bulan, Integer tahun, String status) {
        LocalDate startDate = LocalDate.of(tahun, bulan, 1);
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate endDate = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        return pemutihanRepo.findByTanggalBetweenAndStatus(startDate, endDate, status, Sort.by(Sort.Direction.ASC, "tanggal"));
    }
}
