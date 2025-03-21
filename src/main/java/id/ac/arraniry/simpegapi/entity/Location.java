package id.ac.arraniry.simpegapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
	
	private String type;
	private List<Double> coordinates;

}
