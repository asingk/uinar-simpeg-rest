package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.UsulIzin;
import id.ac.arraniry.simpegapi.repo.UsulIzinRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Service
public class UsulIzinServiceImpl implements UsulIzinService {

    private final UsulIzinRepo usulIzinRepo;

    public UsulIzinServiceImpl(UsulIzinRepo usulIzinRepo) {
        this.usulIzinRepo = usulIzinRepo;
    }

    @Override
    public Page<UsulIzin> findByNipAndStatusIn(String nip, Set<Integer> status, Pageable pageable) {
        return usulIzinRepo.findByNipAndStatusIn(nip, status, pageable);
    }

    @Override
    public String save(UsulIzin usulIzin) {
        return usulIzinRepo.save(usulIzin).getId();
    }

    @Override
    public UsulIzin findById(String id) {
        return usulIzinRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "usulan izin tidak ditemukan!"));
    }
}
