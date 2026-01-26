package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Izin;
import id.ac.arraniry.simpegapi.repo.IzinRepo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
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
//        LocalDate startDate = LocalDate.of(tahun, bulan, 1);
//        YearMonth yearMonth = YearMonth.of(tahun, bulan);
//        LocalDate endDate = LocalDate.of(tahun, bulan, yearMonth.lengthOfMonth());
        String pattern = String.format("^%d-%02d-", tahun, bulan);
        return izinRepo.findBulananByNip(idPegawai, pattern, Sort.by(Sort.Direction.ASC, "dateString"));
    }

    @Override
    public Optional<Izin> findByNipAndTanggal(String nip, LocalDate tanggal) {
        return izinRepo.findByNipAndTanggal(nip, tanggal);
    }

    @Override
    public void deleteById(String id) {
        izinRepo.deleteById(id);
    }

    @Override
    public String create(Izin izin) {
        try {
            return izinRepo.save(izin).getId();
        } catch (DuplicateKeyException dke) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "rekaman sudah ada!");
        }
    }

}
