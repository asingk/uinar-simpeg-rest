package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.BesaranRemun;

import java.util.List;

public interface BesaranRemunService {
    List<BesaranRemun> findAll();
    BesaranRemun findById(String id);
    BesaranRemun save(BesaranRemun besaranRemun);
}
