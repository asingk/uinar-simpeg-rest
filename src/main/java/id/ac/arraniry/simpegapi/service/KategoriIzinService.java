package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.KategoriIzin;

import java.util.List;

public interface KategoriIzinService {
    List<KategoriIzin> findAll();
    KategoriIzin findById(String id);
}
