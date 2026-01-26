package id.ac.arraniry.simpegapi.dto;

import id.ac.arraniry.simpegapi.entity.Kehadiran;
import id.ac.arraniry.simpegapi.entity.KehadiranArc;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class KehadiranVO {

	private String id;
	private String idPegawai;
	private String namaPegawai;
	private LocalDateTime waktu;
	private String status;
	private Double longitude;
	private Double latitude;
	private String userAgent;
	private String tanggal;
	private Boolean isDeleted;
	private String deletedByNip;
	private String deletedByNama;
	private LocalDateTime deletedDate;
	private Boolean isAdded;
	private String addedByNip;
	private String addedByNama;
	private LocalDateTime addedDate;
//	private Integer kurangMenit;
	private String jam;
	private String jadwal;

	public KehadiranVO(KehadiranArc hadir) {
		this.id = hadir.getId();
		this.status = hadir.getStatus();
		this.waktu = hadir.getWaktu();
		this.idPegawai = hadir.getPegawai().getNip();
		this.namaPegawai = hadir.getPegawai().getNama();
		this.isAdded = hadir.getIsAdded();
		this.isDeleted = hadir.getIsDeleted();
//		this.kurangMenit = hadir.getKurangMenit();
		this.tanggal = hadir.getTanggal();
		this.jam = hadir.getJam();
		this.jadwal = hadir.getJadwal();
	}

	public KehadiranVO(Kehadiran hadir) {
		this.id = hadir.getId();
		this.status = hadir.getStatus();
		this.waktu = hadir.getWaktu();
		this.idPegawai = hadir.getPegawai().getNip();
		this.namaPegawai = hadir.getPegawai().getNama();
		this.isAdded = hadir.getIsAdded();
		this.isDeleted = hadir.getIsDeleted();
//		this.kurangMenit = hadir.getKurangMenit();
		this.tanggal = hadir.getTanggal();
		this.jam = hadir.getJam();
		this.jadwal = hadir.getJadwal();
	}
}
