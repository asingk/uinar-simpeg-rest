package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Hijriah;
import id.ac.arraniry.simpegapi.repo.HijriahRepo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class HijriahServiceImpl implements HijriahService {

    private final HijriahRepo hijriahRepo;

    public HijriahServiceImpl(HijriahRepo repo) {
        this.hijriahRepo = repo;
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
        return hijriahRepo.findByTahun(tahun).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tahun hijriah tidak ditemukan"));
    }

    @Override
    public List<Hijriah> findAll() {
        return hijriahRepo.findAll(Sort.by(Sort.Direction.DESC, "tahun"));
    }

    @Override
    public Hijriah findById(String id) {
        return hijriahRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "tahun hijriah tidak ditemukan"));
    }

    @Override
    public String create(Integer tahun, LocalDate awalRamadhan, LocalDate awalSyawal) {
        try {
            return hijriahRepo.save(new Hijriah(tahun, awalRamadhan, awalSyawal)).getId();
        } catch (DuplicateKeyException mwe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "tahun sudah ada!");
        }
    }

    @Override
    public void update(String id, Integer tahun, LocalDate awalRamadhan, LocalDate awalSyawal) {
        Hijriah hijriah = findById(id);
        hijriah.setTahun(tahun);
        hijriah.setAwalRamadhan(awalRamadhan);
        hijriah.setAwalSyawal(awalSyawal);
        hijriahRepo.save(hijriah);
    }

    @Override
    public void delete(String id) {
        hijriahRepo.deleteById(id);
    }

}
