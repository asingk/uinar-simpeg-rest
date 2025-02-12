package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Pemutihan;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PemutihanRepository extends MongoRepository<Pemutihan, String> {
	
	@Query("{ 'tanggal' : { $gte : ?0, $lte : ?1 } }")
	List<Pemutihan> findByTanggalBetween(LocalDate start, LocalDate end, Sort sort);
	
	Pemutihan findByTanggalAndStatus(LocalDate tanggal, String status);
	
	@Query("{ 'tanggal' : { $gte : ?0, $lte : ?1 }, status: ?2 }")
	List<Pemutihan> findByTanggalBetweenAndStatus(LocalDate start, LocalDate end, String status, Sort sort);

}
