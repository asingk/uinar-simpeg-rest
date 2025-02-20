package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.UsulIzin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsulIzinService {
    Page<UsulIzin> findByNipAndStatusIn(String nip, List<Integer> status, Pageable pageable);
    String save(UsulIzin usulIzin);
    UsulIzin findById(String id);
    Page<UsulIzin> findByStatusIn(List<Integer> status, Pageable pageable);
}
