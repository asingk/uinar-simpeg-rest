package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.JamKerja;
import id.ac.arraniry.simpegapi.repo.JamKerjaRepo;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JamKerjaServiceImpl implements JamKerjaService {

    private final JamKerjaRepo jamKerjaRepo;

    public JamKerjaServiceImpl(JamKerjaRepo jamKerjaRepo) {
        this.jamKerjaRepo = jamKerjaRepo;
    }

    @Override
    public JamKerja findByHariAndIsRamadhan(Integer hari, Boolean isRamadhan) {
        return jamKerjaRepo.findByHariAndIsRamadhan(hari, isRamadhan).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "jam kerja tidak ditemukan!"));
    }

    @Override
    public List<JamKerja> findAll() {
        return jamKerjaRepo.findAll(Sort.by(Sort.Direction.ASC, "isRamadhan").and(Sort.by(Sort.Direction.ASC, "hari")));
    }

    @Override
    public List<JamKerja> findByIsRamadhan(Boolean isRamadhan) {
        return jamKerjaRepo.findByIsRamadhan(isRamadhan, Sort.by(Sort.Direction.ASC, "hari"));
    }
}
