package id.ac.arraniry.simpegapi.assembler;

import id.ac.arraniry.simpegapi.dto.UsulIzinModel;
import id.ac.arraniry.simpegapi.entity.UsulIzin;
import id.ac.arraniry.simpegapi.rest.UsulIzinRest;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

@Component
public class UsulIzinModelAssembler extends RepresentationModelAssemblerSupport<UsulIzin, UsulIzinModel> {
    public UsulIzinModelAssembler() {
        super(UsulIzinRest.class, UsulIzinModel.class);
    }

    @Override
    public UsulIzinModel toModel(UsulIzin entity) {
        UsulIzinModel model = new UsulIzinModel();
        model.setFile("https://cdn.ar-raniry.ac.id/kehadiran/usul_izin/" + entity.getFileName());
        BeanUtils.copyProperties(entity, model);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UsulIzinRest.class).getById(entity.getId())).withSelfRel();
        model.add(selfLink);
        return model;
    }
}
