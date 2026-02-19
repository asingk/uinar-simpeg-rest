package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.JamKerjaHarian;
import id.ac.arraniry.simpegapi.repo.JamKerjaHarianRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JamKerjaHarianServiceImpl implements JamKerjaHarianService {

    private final JamKerjaHarianRepo jamKerjaHarianRepo;

    public JamKerjaHarianServiceImpl(JamKerjaHarianRepo jamKerjaHarianRepo) {
        this.jamKerjaHarianRepo = jamKerjaHarianRepo;
    }

    @Override
    public JamKerjaHarian findByTanggal(String tanggal) {
        return jamKerjaHarianRepo.findByDateString(tanggal).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "jam kerja tidak ditemukan!"));
    }

}
