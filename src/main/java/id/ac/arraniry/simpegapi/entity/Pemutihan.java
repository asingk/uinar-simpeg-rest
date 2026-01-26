package id.ac.arraniry.simpegapi.entity;

import id.ac.arraniry.simpegapi.dto.PemutihanCreateRequest;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Document
public class Pemutihan {
	
	private String id;
	private LocalDate tanggal;
	private String status;
	private String dateString;

	public Pemutihan(PemutihanCreateRequest request) {
		this.tanggal = request.getTanggal();
		this.status = request.getStatus();
	}

}
