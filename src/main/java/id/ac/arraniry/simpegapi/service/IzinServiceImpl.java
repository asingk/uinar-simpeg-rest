package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Izin;
import id.ac.arraniry.simpegapi.repo.IzinRepo;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class IzinServiceImpl implements IzinService {

    private final IzinRepo izinRepo;

    public IzinServiceImpl(IzinRepo izinRepo) {
        this.izinRepo = izinRepo;
    }

    @Override
    public List<Izin> findByNipAndTahunAndBulan(String idPegawai, Integer tahun, Integer bulan) {
        LocalDate startDate = LocalDate.of(tahun, bulan, 1);
        YearMonth yearMonth = YearMonth.of(tahun, bulan);
        LocalDate endDate = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        return izinRepo.findByNipAndTanggalBetween(idPegawai, startDate, endDate, Sort.by(Sort.Direction.ASC, "tanggal"));
    }

    @Override
    public Optional<Izin> findByNipAndTanggal(String nip, LocalDate tanggal) {
        return izinRepo.findByNipAndTanggal(nip, tanggal);
    }

    @Override
    public void deleteById(String id) {
        izinRepo.deleteById(id);
    }

}
