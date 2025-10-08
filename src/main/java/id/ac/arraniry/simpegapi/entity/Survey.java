package id.ac.arraniry.simpegapi.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.util.List;

@Data
public class Survey {
    @Id
    private String id;
    private LocalDate tanggal;
    private String question;
    private List<SurveyOptions> options;
}
