package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.HariLiburTapiKerja;
import id.ac.arraniry.simpegapi.entity.WfaByTanggal;
import id.ac.arraniry.simpegapi.repo.WfaByTanggalRepo;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WfaByTanggalServiceImpl implements WfaByTanggalService {
    private final WfaByTanggalRepo wfaByTanggalRepo;

    public WfaByTanggalServiceImpl(WfaByTanggalRepo wfaByTanggalRepo) {
        this.wfaByTanggalRepo = wfaByTanggalRepo;
    }

    @Override
    public Optional<WfaByTanggal> findByTanggal(LocalDate tanggal) {
        return wfaByTanggalRepo.findByTanggal(tanggal);
    }

    @Override
    public List<WfaByTanggal> findByTahun(Integer tahun) {
        LocalDate startDate = LocalDate.of(tahun, 1, 1);
        LocalDate endDate = LocalDate.of(tahun, 12, 31);
        return wfaByTanggalRepo.findByTanggalBetween(startDate, endDate, Sort.by(Sort.Direction.ASC, "tanggal"));
    }

    @Override
    public String create(LocalDate tanggal) {
        try {
            return wfaByTanggalRepo.save(new WfaByTanggal(tanggal)).getId();
        } catch (DuplicateKeyException mwe) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "tanggal sudah ada!");
        }
    }

    @Override
    public void deleteById(String id) {
        wfaByTanggalRepo.deleteById(id);
    }
}
