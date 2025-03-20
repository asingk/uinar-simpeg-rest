package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.WfaByTanggal;
import id.ac.arraniry.simpegapi.repo.WfaByTanggalRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
}
