package org.dubini.frontend_api.dto;

import org.springframework.web.multipart.MultipartFile;

public class InscripcionRequest {
    private String nombre;
    private String email;
    private String prefijo;
    private String telefono;
    private String documento;
    private String pais;
    private String direccion_calle;
    private String direccion_numero;
    private String direccion_puerta;
    private String direccion_resto;
    private String direccion_codigo_postal;
    private String direccion_provincia;
    private String direccion_ciudad;
    private String ocupacion;
    private String fecha_nac;
    private String auth_whatsapp;
    private String auth_imagen;
    private String auth_datos;
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

    public String getDireccion_calle() { return direccion_calle; }
    public void setDireccion_calle(String direccion_calle) { this.direccion_calle = direccion_calle; }

    public String getDireccion_numero() { return direccion_numero; }
    public void setDireccion_numero(String direccion_numero) { this.direccion_numero = direccion_numero; }

    public String getDireccion_puerta() { return direccion_puerta; }
    public void setDireccion_puerta(String direccion_puerta) { this.direccion_puerta = direccion_puerta; }

    public String getDireccion_resto() { return direccion_resto; }
    public void setDireccion_resto(String direccion_resto) { this.direccion_resto = direccion_resto; }

    public String getDireccion_codigo_postal() { return direccion_codigo_postal; }
    public void setDireccion_codigo_postal(String direccion_codigo_postal) { this.direccion_codigo_postal = direccion_codigo_postal; }

    public String getDireccion_provincia() { return direccion_provincia; }
    public void setDireccion_provincia(String direccion_provincia) { this.direccion_provincia = direccion_provincia; }

    public String getDireccion_ciudad() { return direccion_ciudad; }
    public void setDireccion_ciudad(String direccion_ciudad) { this.direccion_ciudad = direccion_ciudad; }

    public String getOcupacion() { return ocupacion; }
    public void setOcupacion(String ocupacion) { this.ocupacion = ocupacion; }

    public String getFecha_nac() { return fecha_nac; }
    public void setFecha_nac(String fecha_nac) { this.fecha_nac = fecha_nac; }

    public String getAuth_whatsapp() { return auth_whatsapp; }
    public void setAuth_whatsapp(String auth_whatsapp) { this.auth_whatsapp = auth_whatsapp; }

    public String getAuth_imagen() { return auth_imagen; }
    public void setAuth_imagen(String auth_imagen) { this.auth_imagen = auth_imagen; }

    public String getAuth_datos() { return auth_datos; }
    public void setAuth_datos(String auth_datos) { this.auth_datos = auth_datos; }

    public String getComentarios() { return comentarios; }
    public void setComentarios(String comentarios) { this.comentarios = comentarios; }

    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }

    public MultipartFile getFoto_carnet() { return foto_carnet; }
    public void setFoto_carnet(MultipartFile foto_carnet) { this.foto_carnet = foto_carnet; }
}
