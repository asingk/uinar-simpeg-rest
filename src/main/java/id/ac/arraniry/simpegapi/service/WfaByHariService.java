package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.WfaByHari;

import java.util.Optional;

public interface WfaByHariService {
    Optional<WfaByHari> findByHari(Integer hari);
}
