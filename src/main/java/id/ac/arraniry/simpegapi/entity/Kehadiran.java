package id.ac.arraniry.simpegapi.entity;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@NoArgsConstructor
public class Kehadiran extends Absensi {
	
	public Kehadiran(KehadiranVO hadirVO) {
		super(hadirVO);
	}
	
}
