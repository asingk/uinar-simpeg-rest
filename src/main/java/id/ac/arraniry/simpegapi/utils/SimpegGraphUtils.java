package id.ac.arraniry.simpegapi.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.arraniry.simpegapi.dto.PegawaiSimpegVO;
import org.slf4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

public class SimpegGraphUtils {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SimpegGraphUtils.class);

    public static boolean isPegawaiExistInSimpeg(String idPegawai, Environment environment) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", environment.getProperty("env.data.secret-key"));

        String query = "{\"query\":\"query Pegawai($id: ID!) {" +
                "  pegawai(id: $id) {" +
                "    id" +
                "}}\",\"variables\":{\"id\":\"" + idPegawai + "\"},\"operationName\":\"Pegawai\"}";

        ResponseEntity<String> response = restTemplate.postForEntity(
                Objects.requireNonNull(environment.getProperty("env.data.simpeg-graphql-url")),
                new HttpEntity<>(query, headers),
                String.class);

        if(200 != response.getStatusCode().value()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan mengakses GraphQL!");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(response.getBody());
            JsonNode pegawai = actualObj.get("data").get("pegawai");

            return pegawai != null && !pegawai.isNull();
        } catch (JsonProcessingException jpe) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error parsing response GraphQL: " + jpe.getMessage());
        }
    }

    public static PegawaiSimpegVO getProfilPegawaiFromSimpegGraphql(String idPegawai, Environment environment) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", environment.getProperty("env.data.secret-key"));
        String query = "{\"query\":\"query Pegawai($id: ID!) {" +
                "  pegawai(id: $id) {" +
                "    id" +
                "    nama" +
                "  statusAktif{" +
                "    id" +
                "  }" +
                "  jenisJabatan" +
                "}}\",\"variables\":{\"id\":\"" + idPegawai + "\"},\"operationName\":\"Pegawai\"}";
        ResponseEntity<String> response = restTemplate.postForEntity(Objects.requireNonNull(environment.getProperty("env.data.simpeg-graphql-url")), new HttpEntity<>(query, headers), String.class);
        if(200 != response.getStatusCode().value()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan!");
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
//            log.debug(response.getBody());
            JsonNode actualObj = mapper.readTree(response.getBody());
            JsonNode pegawai = actualObj.get("data").get("pegawai");
            if(pegawai.isNull())
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pegawai " + idPegawai + " tidak ditemukan!");
            PegawaiSimpegVO pegawaiProfilVO = new ObjectMapper().readValue(pegawai.toString(), PegawaiSimpegVO.class);
//            log.debug(pegawaiProfilVO.toString());
            if(pegawaiProfilVO.getStatusAktif().getId() > 1)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pegawai tidak aktif!");
            if(null == pegawaiProfilVO.getJenisJabatan())
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak punya jenis jabatan!");
            return pegawaiProfilVO;
        } catch (JsonProcessingException jpe) {
            log.error(jpe.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Tidak diizinkan!");
        }
    }

}
