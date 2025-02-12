package id.ac.arraniry.simpegapi.entity;

import id.ac.arraniry.simpegapi.dto.KehadiranVO;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@NoArgsConstructor
public class KehadiranArc extends Absensi {

	public KehadiranArc(KehadiranVO hadirVO) {
		super(hadirVO);
	}
	
}
