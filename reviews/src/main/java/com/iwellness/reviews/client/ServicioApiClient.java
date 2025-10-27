package com.iwellness.reviews.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.iwellness.reviews.dto.ServicioDTO;

@FeignClient(name = "servicio-ms", url = "${feign.client.servicio.url:http://localhost:8080}/api/servicios")
public interface ServicioApiClient {

    @GetMapping("/search/{id}")
    ServicioDTO getServicioById(@PathVariable("id") Long id);

    @GetMapping("/{idProveedor}/servicios")
    List<ServicioDTO> getServiciosByProveedorId(@PathVariable("idProveedor") Long idProveedor);

}
