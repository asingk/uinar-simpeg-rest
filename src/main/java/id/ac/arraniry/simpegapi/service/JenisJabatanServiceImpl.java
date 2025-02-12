package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.JenisJabatan;
import id.ac.arraniry.simpegapi.repo.JenisJabatanRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class JenisJabatanServiceImpl implements JenisJabatanService {

    private final JenisJabatanRepo jenisJabatanRepo;

    public JenisJabatanServiceImpl(JenisJabatanRepo jenisJabatanRepo) {
        this.jenisJabatanRepo = jenisJabatanRepo;
    }

    @Override
    public JenisJabatan findById(String id) {
        return jenisJabatanRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "jenis jabatan izin tidak ditemukan!"));
    }

}
