package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.KategoriIzin;
import id.ac.arraniry.simpegapi.repo.KategoriIzinRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class KategoriIzinServiceImpl implements KategoriIzinService {

    private final KategoriIzinRepo kategoriIzinRepo;

    public KategoriIzinServiceImpl(KategoriIzinRepo kategoriIzinRepo) {
        this.kategoriIzinRepo = kategoriIzinRepo;
    }

    @Override
    public List<KategoriIzin> findAll() {
        return kategoriIzinRepo.findAll();
    }

    @Override
    public KategoriIzin findById(String id) {
        return kategoriIzinRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "kategori izin tidak ditemukan!"));
    }
}
