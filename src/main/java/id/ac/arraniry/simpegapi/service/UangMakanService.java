package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.UangMakan;

import java.util.List;

public interface UangMakanService {
    List<UangMakan> findAll();
    UangMakan findById(String id);
    UangMakan save(UangMakan uangMakan);
}
