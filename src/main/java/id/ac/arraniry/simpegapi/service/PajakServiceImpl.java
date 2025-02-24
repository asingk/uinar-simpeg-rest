package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.entity.Pajak;
import id.ac.arraniry.simpegapi.repo.PajakRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PajakServiceImpl implements PajakService {
    private final PajakRepo pajakRepo;

    @Autowired
    public PajakServiceImpl(PajakRepo pajakRepo) {
        this.pajakRepo = pajakRepo;
    }

    @Override
    public List<Pajak> findAll() {
        return pajakRepo.findAll();
    }

    @Override
    public Pajak findById(String id) {
        return pajakRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Override
    public Pajak save(Pajak pajak) {
        return pajakRepo.save(pajak);
    }

}
