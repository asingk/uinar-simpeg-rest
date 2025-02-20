package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.entity.Pengumuman;
import id.ac.arraniry.simpegapi.repo.PengumumanRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PengumumanServiceImpl implements PengumumanService {

    private final PengumumanRepo pengumumanRepo;

    public PengumumanServiceImpl(PengumumanRepo pengumumanRepo) {
        this.pengumumanRepo = pengumumanRepo;
    }

    @Override
    public List<Pengumuman> findAll() {
        return pengumumanRepo.findAll();
    }

    @Override
    public Optional<Pengumuman> findById(String id) {
        return pengumumanRepo.findById(id);
    }

    @Override
    public Optional<Pengumuman> findByIsActive(Boolean isActive) {
        return pengumumanRepo.findByIsActive(isActive);
    }

    @Override
    public CreateResponse create(Pengumuman pengumunan) {
        return new CreateResponse(pengumumanRepo.save(pengumunan).getId());
    }

    @Override
    public void update(Pengumuman pengumunan) {
        pengumumanRepo.save(pengumunan);
    }

    @Override
    public void deleteById(String id) {
        pengumumanRepo.deleteById(id);
    }
}
