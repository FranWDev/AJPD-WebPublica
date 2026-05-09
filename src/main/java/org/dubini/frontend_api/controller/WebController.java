package org.dubini.frontend_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    @GetMapping("/")
    public String main() {
        return "main";
    }

    @GetMapping("/documentos-institucionales")
    public String documents() {
        return "documents";
    }

    @GetMapping("/contacto")
    public String contact() {
        return "contact";
    }

    @GetMapping("/alexandra-dubini")
    public String alexandra() {
        return "alexandra";
    }

    @GetMapping("/privacidad")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terminos-y-condiciones")
    public String terms() {
        return "terms";
    }

    @GetMapping("/junta-directiva")
    public String directorate() {
        return "directorate";
    }

    @GetMapping("/premios")
    public String awards() {
        return "awards";
    }

    @GetMapping("/noticias-y-actividades")
    public String news() {
        return "news";
    }

    @GetMapping("/noticias-y-actividades/{title}")
    public String newsDetail(@PathVariable String title) {
        return "news-detail";
    }

    @GetMapping("/museo-escolar-jardin-botanico-ies-san-benito")
    public String museoEscolar() {
        return "museo-escolar";
    }

    @GetMapping("/museo-escolar-jardin-botanico-ies-san-benito/museo-virtual")
    public String museoVirtual() {
        return "museo-virtual";
    }

    @GetMapping("/historia-y-mision-jardin-botanico")
    public String historiaJardin() {
        return "historia-y-mision-jardin-botanico";
    }

    @GetMapping("/se-socio")
    public String join() {
        return "join";
    }
}
