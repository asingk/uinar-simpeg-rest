package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.WfaByHari;
import id.ac.arraniry.simpegapi.repo.WfaByHariRepo;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

    @Override
    public List<WfaByHari> findAll() {
        return wfaByHariRepo.findAll(Sort.by(Sort.Direction.ASC, "hari"));
    }

    @Override
    public void update(String id, Boolean wfa) {
        WfaByHari  wfaByHari = wfaByHariRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hari tidak ditemukan"));
        wfaByHari.setWfa(wfa);
        wfaByHariRepo.save(wfaByHari);
    }
}
