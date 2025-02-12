package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.UsulIzin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UsulIzinService {
    Page<UsulIzin> findByNipAndStatusIn(String nip, Set<Integer> status, Pageable pageable);
    String save(UsulIzin usulIzin);
    UsulIzin findById(String id);
}
