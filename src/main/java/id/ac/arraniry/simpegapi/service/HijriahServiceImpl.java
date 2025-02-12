package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Hijriah;
import id.ac.arraniry.simpegapi.repo.HijriahRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class HijriahServiceImpl implements HijriahService {

    private final HijriahRepo repo;

    public HijriahServiceImpl(HijriahRepo repo) {
        this.repo = repo;
    }

    @Override
    public boolean isRamadhan(Integer tahun, LocalDate tanggal) {
        try {
            var hijriah = findByTahun(tahun);
            if (!tanggal.isBefore(hijriah.getAwalRamadhan()) && tanggal.isBefore(hijriah.getAwalSyawal()))
                return true;
        } catch (ResponseStatusException rse) {
            return false;
        }
        return false;
    }

    private Hijriah findByTahun(Integer tahun) {
        return repo.findByTahun(tahun).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tahun hijriah tidak ditemukan"));
    }

}
