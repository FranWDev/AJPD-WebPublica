package org.dubini.frontend_api.dto;

import org.springframework.web.multipart.MultipartFile;

public class InscripcionRequest {
    private String nombre;
    private String email;
    private String prefijo;
    private String telefono;
    private String documento;
    private String pais;
    private String direccion;
    private String ocupacion;
    private String fecha_nac;
    private String auth_whatsapp;
    private String auth_imagen;
    private String comentarios;
    private String lang;
    private MultipartFile foto_carnet;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPrefijo() { return prefijo; }
    public void setPrefijo(String prefijo) { this.prefijo = prefijo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }

    public String getFecha_nac() { return fecha_nac; }
    public void setFecha_nac(String fecha_nac) { this.fecha_nac = fecha_nac; }

    public String getAuth_whatsapp() { return auth_whatsapp; }
    public void setAuth_whatsapp(String auth_whatsapp) { this.auth_whatsapp = auth_whatsapp; }

    public String getAuth_imagen() { return auth_imagen; }
    public void setAuth_imagen(String auth_imagen) { this.auth_imagen = auth_imagen; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }

    public MultipartFile getFoto_carnet() { return foto_carnet; }
    public void setFoto_carnet(MultipartFile foto_carnet) { this.foto_carnet = foto_carnet; }
}
