package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.KehadiranArc;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KehadiranArcRepo extends MongoRepository<KehadiranArc, String> {

    @Query("{ 'pegawai.nip' : ?0, 'waktu' : { $gte : ?1, $lte : ?2 } }")
    List<KehadiranArc> findByNipAndTanggalBetween(String nip, LocalDateTime start, LocalDateTime end, Sort sort);

    List<KehadiranArc> findByPegawaiNipAndTanggal(String nip, String tanggal);

    @Query("{ 'waktu' : { $gte : ?0, $lte : ?1 }, isDeleted: { $ne: true }, status: 'DATANG', 'pegawai.nip': { $in: ?2 } }")
    List<KehadiranArc> findLaporanUangMakan(LocalDate start, LocalDate end, List<String> nipList, Sort sort);

    @Query("{ 'pegawai.nip' : ?0, 'waktu' : { $gte : ?1, $lte : ?2 }, isDeleted: { $ne: true } }")
    List<KehadiranArc> findByNipAndTanggalBetweenAndIsDeletedFalse(String nip, LocalDateTime start, LocalDateTime end, Sort sort);

    @Query("{ 'pegawai.nip' : ?0, 'status' : ?1, 'waktu' : { $gte : ?2, $lte : ?3 } }")
    Optional<KehadiranArc> findByNipAndStatusAndWaktuBetween(String nip, String status, LocalDateTime start, LocalDateTime end);

}
