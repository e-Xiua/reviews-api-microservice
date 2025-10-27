package com.iwellness.reviews.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.iwellness.reviews.config.FeignClientInterceptor;
import com.iwellness.reviews.dto.UsuarioDTO;

// 'admin-users-service' es el nombre de la aplicación de destino.
// La URL es para desarrollo local. En producción, esto se resolvería con un Discovery Server.
@FeignClient(name = "admin-users-service", url = "${feign.client.turista.url:http://localhost:8082}/usuarios", configuration = FeignClientInterceptor.class)
public interface UserApiClient {

    // Este método llamará a GET http://localhost:8082/usuarios/{id}
    @GetMapping("/{id}")
    UsuarioDTO findById(@PathVariable("id") Long id);
}
