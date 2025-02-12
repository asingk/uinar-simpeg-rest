package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.JamKerja;

import java.util.List;

public interface JamKerjaService {
    JamKerja findByHariAndIsRamadhan(Integer hari, Boolean isRamadhan);
    List<JamKerja> findAll();
    List<JamKerja> findByIsRamadhan(Boolean isRamadhan);
}
