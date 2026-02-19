package id.ac.arraniry.simpegapi.repo;

import id.ac.arraniry.simpegapi.entity.Kehadiran;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KehadiranRepo extends MongoRepository<Kehadiran, String> {

//    @Query("{ 'pegawai.nip' : ?0, 'waktu' : { $gte : ?1, $lte : ?2 } }")
//    List<Kehadiran> findByNipAndTanggalBetween(String nip, LocalDateTime start, LocalDateTime end, Sort sort);

//    @Query("{ 'pegawai.nip' : ?0, 'status' : ?1, 'waktu' : { $gte : ?2, $lte : ?3 } }")
//    Optional<Kehadiran> findByNipAndStatusAndWaktuBetween(String nip, String status, LocalDateTime start, LocalDateTime end);

    Optional<Kehadiran> findByPegawaiNipAndStatusAndTanggal(String nip, String status, String tanggal);

    List<Kehadiran> findByPegawaiNipAndTanggal(String nip, String tanggal);

    @Query("{ 'waktu' : { $gte : ?0, $lte : ?1 }, isDeleted: { $ne: true }, status: 'DATANG', 'pegawai.nip': { $in: ?2 }, 'tanggal': { $nin: ?3 } }")
    List<Kehadiran> findLaporanUangMakan(LocalDate start, LocalDate end, List<String> nipList, List<String> tanggal, Sort sort);

//    @Query("{ 'pegawai.nip' : ?0, 'waktu' : { $gte : ?1, $lte : ?2 }, isDeleted: { $ne: true } }")
//    List<Kehadiran> findByNipAndTanggalBetweenAndIsDeletedFalse(String nip, LocalDateTime start, LocalDateTime end, Sort sort);

    @Query("{ 'pegawai.nip': ?0, 'tanggal': { $regex: ?1 } }")
    List<Kehadiran> findBulananByNip(String nip, String regexTanggal, Sort sort);

}
