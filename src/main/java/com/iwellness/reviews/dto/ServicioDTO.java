package com.iwellness.reviews.dto;

import lombok.Data;

@Data
public class ServicioDTO {
    private Long idServicio;
    private Long idProveedor;
    private String nombre;
    private String descripcion;
    private Double precio;
    private String imagen;
    private String horario;
    private boolean estado;
}
