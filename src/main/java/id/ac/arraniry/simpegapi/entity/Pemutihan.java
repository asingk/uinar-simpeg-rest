package id.ac.arraniry.simpegapi.entity;

import id.ac.arraniry.simpegapi.dto.PemutihanCreateRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document
public class Pemutihan {
	
	private String id;
	private LocalDate tanggal;
	private String status;
	private String dateString;
	private LocalDateTime createdDate;
	private Pegawai createdBy;

	public Pemutihan(PemutihanCreateRequest request, String namaPegawai) {
		Pegawai pegawai = new Pegawai();
		pegawai.setNip(request.getCreatedBy());
		pegawai.setNama(namaPegawai);
		this.createdBy = pegawai;
		this.tanggal = request.getTanggal();
		this.status = request.getStatus();
		this.dateString = request.getTanggal().toString();
	}

}
