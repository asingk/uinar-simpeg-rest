package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.JamKerja;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JamKerjaRepo extends MongoRepository<JamKerja, String> {

    Optional<JamKerja> findByHariAndIsRamadhan(Integer hari, Boolean isRamadhan);
    List<JamKerja> findByIsRamadhan(Boolean isRamadhan, Sort sort);

}
