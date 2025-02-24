package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import id.ac.arraniry.simpegapi.repo.HariLiburTapiKerjaRepo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    @Override
    public String create(LocalDate tanggal) {
        if(tanggal.getDayOfWeek().getValue() < 6)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Hanya boleh menambah untuk hari sabtu-minggu");
        try {
            return hariLiburTapiKerjaRepo.save(new HariLiburTapiKerja(tanggal)).getId();
        } catch (DuplicateKeyException mwe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "tanggal sudah ada!");
        }
    }

    @Override
    public void delete(String id) {
        hariLiburTapiKerjaRepo.deleteById(id);
    }

}
