const VERSION = "v36";
const CACHE_NAME = `ajpd-static-cache-${VERSION}`;
const MUSEO_CACHE_NAME = "ajpd-museo-assets";

const SHELL_KEY = "Application loading";

self.addEventListener("install", (event) => {
  event.waitUntil(caches.open(CACHE_NAME).then((cache) => cache.addAll(["/"])));
  self.skipWaiting();
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) =>
      Promise.all(
        cacheNames.map((cacheName) => {
          if (
            cacheName.startsWith("ajpd-static-cache-") &&
            cacheName !== CACHE_NAME
          ) {
            return caches.delete(cacheName);
          }
          // No borrar el caché del museo (Esta nunca va a cambiar)
        })
      )
    )
  );
  self.clients.claim();
});

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);

  // Imágenes del museo (Supabase o GitHub)
  const isOldSupabase = url.href.startsWith("https://mcybqxqlujczgclidnar.supabase.co/storage/v1/object/public/ajpd-storage/");
  const isNewSupabase = url.href.startsWith("https://qpdgnvibatovhkejeidv.supabase.co/storage/v1/object/public/Museo%20Virtual/");
  const isGitHub = url.href.startsWith("https://franwdev.github.io/ajpd-bucket/");

  if (event.request.destination === "image" && (isOldSupabase || isNewSupabase || isGitHub)) {
    // Si es una imagen del museo (por ruta o por el nuevo dominio de GitHub)
    if (url.href.includes("/museo-virtual/") || url.href.includes("/museo_virtual/") || isGitHub) {
      // USAR CACHÉ PERSISTENTE PARA IMÁGENES DEL MUSEO (Nunca se invalidan)
      event.respondWith(
        caches.open(MUSEO_CACHE_NAME).then((cache) => {
          return cache.match(event.request).then((cached) => {
            if (cached) return cached;
            return fetch(event.request).then((networkResponse) => {
              cache.put(event.request, networkResponse.clone());
              return networkResponse;
            });
          });
        })
      );
      return;
    } else {
      event.respondWith(fetch(event.request));
      return;
    }
  }

  // No cachear imágenes hero y slider específicamente (redundante pero seguro)
  if (
    event.request.destination === "image" &&
    (url.href.includes("/storage/v1/object/public/ajpd-storage/hero/") ||
      url.href.includes("/storage/v1/object/public/ajpd-storage/slider/"))
  ) {
    event.respondWith(fetch(event.request));
    return;
  }

  if (event.request.mode === "navigate") {
    event.respondWith(
      caches.open(CACHE_NAME).then((cache) => {
        return cache.match(event.request).then((cached) => {
          if (cached) return cached;

          return fetch(event.request)
            .then((networkResponse) => {
              const responseToInspect = networkResponse.clone();

              return responseToInspect.text().then((text) => {
                // Inspección: Verificamos si la respuesta HTML contiene la clave del SHELL.
                const isShellContent = text.includes(SHELL_KEY);

                if (!isShellContent) {
                  cache.put(event.request, networkResponse.clone());
                }

                return networkResponse;
              });
            })
            .catch(() => cache.match("/"));
        });
      })
    );
    return;
  }

  // Bloque de cacheo general para estáticos (scripts, styles, font, etc)
  if (
    event.request.destination === "script" ||
    event.request.destination === "style" ||
    event.request.destination === "image" ||
    event.request.destination === "font"
  ) {
    event.respondWith(
      caches.open(CACHE_NAME).then((cache) => {
        return cache.match(event.request).then((cached) => {
          if (cached) return cached;

          return fetch(event.request).then((networkResponse) => {
            cache.put(event.request, networkResponse.clone());
            return networkResponse;
          });
        });
      })
    );
  }
});
