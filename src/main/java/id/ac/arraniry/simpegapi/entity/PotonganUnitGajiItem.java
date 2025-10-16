package id.ac.arraniry.simpegapi.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PotonganUnitGajiItem {
    private String nama;
    private BigDecimal nilai;
}
