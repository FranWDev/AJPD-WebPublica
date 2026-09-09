package org.dubini.frontend_api.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEtagService {

    private final ObjectMapper objectMapper;

    private final Map<Integer, String> etagCache = new ConcurrentHashMap<>();

    /**
     * Calcula el ETag basado en el contenido del objeto
     * Usa SHA-256 para generar un hash del contenido serializado
     * El resultado se cachea en memoria para evitar recalcular constantemente
     */
    public String calculateEtag(Object content) {
        int contentHash = content.hashCode();

        String cachedEtag = etagCache.get(contentHash);
        if (cachedEtag != null) {
            log.debug("[CACHE] [ETAG] ETag recuperado de memoria: {}", cachedEtag);
            return cachedEtag;
        }

        try {
            String jsonContent = objectMapper.writeValueAsString(content);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(jsonContent.getBytes(StandardCharsets.UTF_8));

            String fullHash = HexFormat.of().formatHex(hash);
            String etag = "\"" + fullHash.substring(0, 16) + "\"";

            etagCache.put(contentHash, etag);
            log.debug("[CACHE] [ETAG] Nuevo ETag calculado y cacheado: {}", etag);
            return etag;

        } catch (JsonProcessingException e) {
            log.error("[CACHE] [ETAG] Error al serializar contenido para cálculo de ETag: {}", e.getMessage());
            return "\"" + System.currentTimeMillis() + "\"";
        } catch (NoSuchAlgorithmException e) {
            log.error("[CACHE] [ETAG] Algoritmo SHA-256 no disponible en el entorno: {}", e.getMessage());
            return "\"" + System.currentTimeMillis() + "\"";
        }
    }

    public boolean hasChanged(String clientEtag, String serverEtag) {
        if (clientEtag == null || serverEtag == null) {
            return true;
        }

        String normalizedClient = normalizeEtag(clientEtag);
        String normalizedServer = normalizeEtag(serverEtag);

        return !normalizedClient.equals(normalizedServer);
    }

    private String normalizeEtag(String etag) {
        if (etag == null) {
            return "";
        }
        return etag.trim().replaceAll("^\"|\"$", "");
    }

    public void clearEtagCache() {
        etagCache.clear();
        log.info("[CACHE] [ETAG] Caché de ETags limpiada");
    }
}