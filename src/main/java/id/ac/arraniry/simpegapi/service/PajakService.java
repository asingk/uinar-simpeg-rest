package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Pajak;

import java.util.List;

public interface PajakService {
    List<Pajak> findAll();
    Pajak findById(String id);
    Pajak save(Pajak pajak);
}
