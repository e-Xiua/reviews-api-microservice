package com.iwellness.reviews.dto;

import com.iwellness.reviews.model.Reviewable;

import lombok.Data;

@Data
public class ServicioDTO implements Reviewable {
    private Long idServicio;
    private Long idProveedor;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String imagen;
    private String horario;
    private boolean estado;

    @Override
    public Long getId() {
        return idServicio;
    }

    @Override
    public String getType() {
        return "servicio";
    }

    @Override
    public String getName() {
        return nombre;
    }

    @Override
    public Long getProviderId() {
        return idProveedor;
    }
}
