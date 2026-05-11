package org.dubini.frontend_api.service;



import org.dubini.frontend_api.config.ResendProperties;
import org.dubini.frontend_api.dto.ContactRequest;
import org.dubini.frontend_api.dto.InscripcionRequest;
import org.dubini.frontend_api.dto.MuseoVisitanteRegistroRequest;
import org.dubini.frontend_api.exception.MuseoRegistroException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResendEmailService {

    private static final String NO_COMMENTS = "Sin comentarios";
    private static final String SENDER_NAME = "Asociación Juvenil Proyecto Dubini";

    private final SpringTemplateEngine templateEngine;
    private final ResendProperties resendProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    public ResendEmailService(SpringTemplateEngine templateEngine,
            ResendProperties resendProperties) {
        this.templateEngine = templateEngine;
        this.resendProperties = resendProperties;
    }
    public void enviarRegistroMuseo(MuseoVisitanteRegistroRequest solicitud) {
        validarConfiguracion();
        String comentarios = normalizarComentarios(solicitud.getComentarios());

        Context context = new Context();
        context.setVariable("nombre", solicitud.getNombre());
        context.setVariable("email", solicitud.getEmail());
        context.setVariable("telefono", solicitud.getTelefono());
        context.setVariable("tipoCaridad", solicitud.getTipoCaridad());
        context.setVariable("numPersonas", solicitud.getNumPersonas());
        context.setVariable("fecha", solicitud.getFecha().toString());
        context.setVariable("horaRango", solicitud.getHoraRango());
        context.setVariable("comentarios", comentarios);

        String htmlNotificacion = templateEngine.process("emails/museo-registro-notificacion", context);
        String htmlConfirmacion = templateEngine.process("emails/museo-registro-confirmacion", context);

        enviarCorreoHtml(resendProperties.getAssociationEmailMuseum(), "Nueva inscripción Museo Escolar", htmlNotificacion);
        if (resendProperties.getAssociationEmailMuseum2() != null && !resendProperties.getAssociationEmailMuseum2().isBlank()) {
            enviarCorreoHtml(resendProperties.getAssociationEmailMuseum2(), "Nueva inscripción Museo Escolar", htmlNotificacion);
        }
        enviarCorreoHtml(solicitud.getEmail(), "Confirmación de inscripción - Museo Escolar", htmlConfirmacion);
    }

    public void enviarEmailContacto(ContactRequest solicitud) {
        validarConfiguracion();

        Context context = new Context();
        context.setVariable("nombre", solicitud.getNombre());
        context.setVariable("email", solicitud.getEmail());
        context.setVariable("telefono", solicitud.getTelefono());
        context.setVariable("asunto", solicitud.getAsunto());
        context.setVariable("mensaje", solicitud.getMensaje());

        String htmlContacto = templateEngine.process("emails/contact-form", context);
        String htmlConfirmacion = templateEngine.process("emails/contact-confirmation", context);

        // Enviar notificación a la asociación al email de contacto configurado
        enviarCorreoHtml(resendProperties.getAssociationEmailContact(), "Nuevo contacto: " + solicitud.getAsunto(), htmlContacto);
        
        // Enviar confirmación al remitente
        enviarCorreoHtml(solicitud.getEmail(), "Hemos recibido tu mensaje - AJPD", htmlConfirmacion);
    }

    public void enviarInscripcion(InscripcionRequest solicitud) {
        validarConfiguracion();

        // Format address from separate fields
        String direccionFormateada = formatearDireccion(
            solicitud.getDireccion_calle(),
            solicitud.getDireccion_numero(),
            solicitud.getDireccion_puerta(),
            solicitud.getDireccion_resto(),
            solicitud.getDireccion_codigo_postal(),
            solicitud.getDireccion_provincia(),
            solicitud.getDireccion_ciudad()
        );

        Context context = new Context();
        context.setVariable("nombre", solicitud.getNombre());
        context.setVariable("email", solicitud.getEmail());
        context.setVariable("prefijo", solicitud.getPrefijo());
        context.setVariable("telefono", solicitud.getTelefono());
        context.setVariable("documento", solicitud.getDocumento());
        context.setVariable("pais", solicitud.getPais());
        context.setVariable("direccion", direccionFormateada);
        context.setVariable("ocupacion", solicitud.getOcupacion());
        context.setVariable("fechaNac", solicitud.getFecha_nac());
        context.setVariable("authWhatsapp", solicitud.getAuth_whatsapp());
        context.setVariable("authImagen", solicitud.getAuth_imagen());
        context.setVariable("authDatos", solicitud.getAuth_datos());
        context.setVariable("comentarios", normalizarComentarios(solicitud.getComentarios()));

        String htmlNotificacion = templateEngine.process("emails/inscripcion-notificacion", context);
        
        // Determinar idioma para el correo de confirmación
        boolean isEnglish = "en".equalsIgnoreCase(solicitud.getLang());
        String templateConfirmacion = isEnglish ? "emails/inscripcion-confirmacion-en" : "emails/inscripcion-confirmacion";
        String asuntoConfirmacion = isEnglish ? "Member Application Confirmation - AJPD" : "Confirmación de solicitud de socio - AJPD";
        
        String htmlConfirmacion = templateEngine.process(templateConfirmacion, context);

        List<AttachmentWrapper> adjuntos = new ArrayList<>();
        if (solicitud.getFoto_carnet() != null && !solicitud.getFoto_carnet().isEmpty()) {
            String extension = getFileExtension(solicitud.getFoto_carnet().getOriginalFilename());
            String nuevoNombre = "foto_carnet_" + solicitud.getNombre().replaceAll("[^a-zA-Z0-8]", "_") + extension;
            adjuntos.add(new AttachmentWrapper(nuevoNombre, solicitud.getFoto_carnet()));
        }

        // Enviar notificación a la asociación
        enviarCorreoHtml(resendProperties.getAssociationEmailInscription(), "Nueva solicitud de inscripción: " + solicitud.getNombre(), htmlNotificacion, adjuntos);
        
        // Enviar confirmación al usuario (sin adjuntos para no saturar su buzón)
        enviarCorreoHtml(solicitud.getEmail(), asuntoConfirmacion, htmlConfirmacion);
    }

    // Helper class to handle custom filenames for attachments
    private record AttachmentWrapper(String filename, MultipartFile file) {}

    private String formatearDireccion(String calle, String numero, String puerta, String resto, 
                                      String codigoPostal, String provincia, String ciudad) {
        StringBuilder sb = new StringBuilder();
        
        if (calle != null && !calle.isBlank()) {
            sb.append(calle);
        }
        if (numero != null && !numero.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(numero);
        }
        if (puerta != null && !puerta.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(puerta);
        }
        if (resto != null && !resto.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(resto);
        }
        if (codigoPostal != null && !codigoPostal.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(codigoPostal);
        }
        if (provincia != null && !provincia.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(provincia);
        }
        if (ciudad != null && !ciudad.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ciudad);
        }
        
        return sb.toString();
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private void enviarCorreoHtml(String destinatario, String asunto, String html) {
        enviarCorreoHtml(destinatario, asunto, html, null);
    }

    private void enviarCorreoHtml(String destinatario, String asunto, String html, List<AttachmentWrapper> attachments) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(resendProperties.getApiKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("from", SENDER_NAME + " <" + resendProperties.getMail() + ">");
            requestBody.put("to", new String[]{destinatario});
            requestBody.put("subject", asunto);
            requestBody.put("html", html);

            if (attachments != null && !attachments.isEmpty()) {
                List<Map<String, String>> attachmentList = new ArrayList<>();
                for (AttachmentWrapper wrapper : attachments) {
                    Map<String, String> att = new HashMap<>();
                    att.put("filename", wrapper.filename());
                    att.put("content", Base64.getEncoder().encodeToString(wrapper.file().getBytes()));
                    attachmentList.add(att);
                }
                requestBody.put("attachments", attachmentList);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity("https://api.resend.com/emails", entity, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MuseoRegistroException("Error en la API de Resend: " + response.getStatusCode());
            }
        } catch (Exception exception) {
            throw new MuseoRegistroException("No se pudo procesar el envío de la solicitud.");
        }
    }

    private String normalizarComentarios(String comentarios) {
        if (comentarios == null || comentarios.isBlank()) {
            return NO_COMMENTS;
        }
        return comentarios.trim();
    }

    private void validarConfiguracion() {
        if (resendProperties.getMail() == null || resendProperties.getMail().isBlank()) {
            throw new MuseoRegistroException("La propiedad resend.mail no está configurada.");
        }
        if (resendProperties.getAssociationEmailContact() == null || resendProperties.getAssociationEmailContact().isBlank()) {
            throw new MuseoRegistroException("La propiedad resend.association-email-contact no está configurada.");
        }
        if (resendProperties.getAssociationEmailMuseum() == null || resendProperties.getAssociationEmailMuseum().isBlank()) {
            throw new MuseoRegistroException("La propiedad resend.association-email-museum no está configurada.");
        }
        if (resendProperties.getAssociationEmailInscription() == null || resendProperties.getAssociationEmailInscription().isBlank()) {
            throw new MuseoRegistroException("La propiedad resend.association-email-inscription no está configurada.");
        }
    }
}