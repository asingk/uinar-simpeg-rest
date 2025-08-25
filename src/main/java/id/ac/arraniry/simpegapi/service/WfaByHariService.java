package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.WfaByHari;

import java.util.List;
import java.util.Optional;

public interface WfaByHariService {
    Optional<WfaByHari> findByHari(Integer hari);
    List<WfaByHari> findAll();
    void update(String id, Boolean wfa);
}
