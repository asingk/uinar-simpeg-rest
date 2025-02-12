package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class HariLibur {
	
	@Id
	private String id;
	private LocalDate tanggal;

	public HariLibur(LocalDate tanggal) {
		this.tanggal = tanggal;
	}

}
