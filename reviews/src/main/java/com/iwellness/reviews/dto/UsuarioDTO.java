package com.iwellness.reviews.dto;

import com.iwellness.reviews.model.Reviewable;

import lombok.Data;

@Data
public class UsuarioDTO implements Reviewable {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String foto;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getType() {
        return "proveedor";
    }

    @Override
    public String getName() {
        return nombre + " " + apellido;
    }

    @Override
    public Long getProviderId() {
        return id; // For providers, they are their own provider
    }
}