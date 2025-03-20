package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.WfaByHari;
import id.ac.arraniry.simpegapi.repo.WfaByHariRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WfaByHariServiceImpl implements WfaByHariService {
    private final WfaByHariRepo wfaByHariRepo;

    public WfaByHariServiceImpl(WfaByHariRepo wfaByHariRepo) {
        this.wfaByHariRepo = wfaByHariRepo;
    }

    @Override
    public Optional<WfaByHari> findByHari(Integer hari) {
        return wfaByHariRepo.findByHari(hari);
    }
}
