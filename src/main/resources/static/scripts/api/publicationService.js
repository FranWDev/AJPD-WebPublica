import { CacheService } from "./cacheService.js";
export async function normalizeTitle(title) {
    if (!title) return "";
    
    return title.toLowerCase()
        .replace(/[áàäâ]/g, "a")
        .replace(/[éèëê]/g, "e")
        .replace(/[íìïî]/g, "i")
        .replace(/[óòöô]/g, "o")
        .replace(/[úùüû]/g, "u")
        .replace(/ñ/g, "n")
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "")
        .trim();
}

let titleCacheMap = null;
let lastCachedNewsSource = null;

async function getTitleCacheMap(cachedNews) {
    if (titleCacheMap && lastCachedNewsSource === cachedNews) {
        return titleCacheMap;
    }
    const map = new Map();
    for (const news of cachedNews) {
        if (news.title) {
            const normalized = await normalizeTitle(news.title);
            map.set(normalized, news);
        }
    }
    titleCacheMap = map;
    lastCachedNewsSource = cachedNews;
    return map;
}

export async function fetchNewsByTitle(urlTitle) {
    const cachedNews = CacheService.getNewsFromCache();
    
    if (cachedNews && Array.isArray(cachedNews)) {
        const normalizedUrlTitle = await normalizeTitle(urlTitle);
        const map = await getTitleCacheMap(cachedNews);
        const found = map.get(normalizedUrlTitle);
        if (found) {
            return found;
        }
    }
    
    try {
        const response = await fetch(`/api/news/title/${encodeURIComponent(urlTitle)}`);
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('Noticia no encontrada');
            }
            throw new Error('Error al cargar la noticia');
        }
        
        const news = await response.json();
        return news;
    } catch (error) {
        console.error('Error fetching news by title:', error);
        throw error;
    }
}

export async function fetchNewsSummary() {
  try {
    const cachedNews = CacheService.getNewsFromCache();
    const cacheEtag = CacheService.getNewsEtag();

    if (cachedNews) {
      updateNewsCache(cacheEtag).catch((err) =>
        console.warn("Error updating cache:", err)
      );
      return cachedNews;
    }
  } catch (error) {
    console.warn("Cache error, proceeding to fetch:", error);
  }

  try {
    const response = await fetch("/api/news");
    if (!response.ok) {
      throw new Error("Network response was not ok");
    }
    const news = await response.json();
    const etag = response.headers.get("ETag");
    
    try {
      CacheService.saveNewsToCache(news, etag);
    } catch (cacheError) {
      console.warn("Failed to save to cache:", cacheError);
    }
    
    return news;
  } catch (error) {
    console.error("Error fetching news:", error);
    throw error;
  }
}

async function updateNewsCache(cacheEtag) {
  try {
    const lastModifiedResponse = await fetch("/api/news/last", {
      method: "HEAD",
      headers: {
        "If-None-Match": cacheEtag,
      },
      cache: "no-store",
    });

    if (lastModifiedResponse.status === 304) {
      return;
    }

    if (lastModifiedResponse.ok) {
      const response = await fetch("/api/news");
      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      const news = await response.json();
      const etag = response.headers.get("ETag");
      CacheService.saveNewsToCache(news, etag);
    }
  } catch (error) {
    console.warn("Failed to update news cache:", error);
  }
}

export async function fetchActivitiesSummary() {
  const response = await fetch("/api/activities");
  if (!response.ok) {
    throw new Error("Network response was not ok");
  }
  return await response.json();
}

export async function fetchFeaturedSummary() {
  const response = await fetch("/api/featured");
  if (!response.ok) {
    throw new Error("Network response was not ok");
  }
  return await response.json();
}
