package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.entity.Pengumuman;

import java.util.List;
import java.util.Optional;

public interface PengumumanService {
    List<Pengumuman> findAll();
    Optional<Pengumuman> findById(String id);
    Optional<Pengumuman> findByIsActive(Boolean isActive);
    CreateResponse create(Pengumuman pengumunan);
    void update(Pengumuman pengumunan);
    void deleteById(String id);
}
