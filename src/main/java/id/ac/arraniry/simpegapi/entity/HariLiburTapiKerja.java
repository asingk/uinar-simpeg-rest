package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class HariLiburTapiKerja {
	
	@Id
	private String id;
	private LocalDate tanggal;

	public HariLiburTapiKerja(LocalDate tanggal) {
		this.tanggal = tanggal;
	}

}
