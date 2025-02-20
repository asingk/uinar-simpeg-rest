package id.ac.arraniry.simpegapi.entity;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import id.ac.arraniry.simpegapi.utils.GlobalConstants;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Absensi {

	@Id
	private String id;
	private Pegawai pegawai;
	private LocalDateTime waktu;
	private String status;
	private Location location;
	private String userAgent;
	private String tanggal;
	private Boolean isDeleted;
	private Pegawai deletedBy;
	private LocalDateTime deletedDate;
	private Boolean isAdded;
	private Pegawai addedBy;
	private LocalDateTime addedDate;
	private Integer kurangMenit;

	public Absensi(KehadiranVO hadirVO) {
		this.id = hadirVO.getId();
		this.status = hadirVO.getStatus();
		this.waktu = hadirVO.getWaktu();
		Pegawai pegawai = new Pegawai();
		pegawai.setNip(hadirVO.getIdPegawai());
		pegawai.setNama(hadirVO.getNamaPegawai());
		this.pegawai = pegawai;
		if(null != hadirVO.getLongitude() && null != hadirVO.getLatitude()) {
			Location location = new Location();
			location.setType(GlobalConstants.LOCATION_TYPE_POINT);
			List<Double> coordinates = new ArrayList<>();
			coordinates.add(hadirVO.getLongitude());
			coordinates.add(hadirVO.getLatitude());
			location.setCoordinates(coordinates);
			this.location = location;
		}
		this.userAgent = hadirVO.getUserAgent();
		this.tanggal = hadirVO.getTanggal();
		if(null != hadirVO.getIsDeleted()) {
			this.isDeleted = hadirVO.getIsDeleted();
			if(this.isDeleted) {
				Pegawai deletedBy = new Pegawai();
				deletedBy.setNip(hadirVO.getDeletedByNip());
				deletedBy.setNama(hadirVO.getDeletedByNama());
				this.deletedBy = deletedBy;
				this.deletedDate = hadirVO.getDeletedDate();
			}
		}
		if(null != hadirVO.getIsAdded()) {
			this.isAdded = hadirVO.getIsAdded();
			if(this.isAdded) {
				Pegawai addedBy = new Pegawai();
				addedBy.setNip(hadirVO.getAddedByNip());
				addedBy.setNama(hadirVO.getAddedByNama());
				this.addedBy = addedBy;
				this.addedDate = hadirVO.getAddedDate();
			}
		}
		if (null != hadirVO.getKurangMenit()) {
			this.kurangMenit = hadirVO.getKurangMenit();
		}
	}

}
