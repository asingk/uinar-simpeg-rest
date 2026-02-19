package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.JamKerjaHarian;

public interface JamKerjaHarianService {
    JamKerjaHarian findByTanggal(String tanggal);
}
