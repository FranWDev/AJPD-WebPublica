import { fetchNewsSummary, normalizeTitle } from "../api/publicationService.js";
import { sanitizeTitle } from "../components/popup.js";

document.addEventListener("DOMContentLoaded", () => {
  const container = document.querySelector(".editorial-grid");
  if (!container) return;

  const createSkeletonCard = (isFeatured = false) => {
    const cardClass = isFeatured ? "program-card featured-card skeleton" : "program-card skeleton";
    const article = document.createElement("article");
    article.className = cardClass;
    article.setAttribute("aria-hidden", "true");
    article.innerHTML = `
      <div class="skeleton-image"></div>
      <div class="skeleton-content">
        <div class="skeleton-line skeleton-title"></div>
        <div class="skeleton-line skeleton-text-1"></div>
        <div class="skeleton-line skeleton-text-2"></div>
        <div class="skeleton-line skeleton-text-3"></div>
        <div class="skeleton-line skeleton-button"></div>
      </div>
    `;
    return article;
  };

  // Render skeletons first
  container.innerHTML = `
    <div class="featured-article-container"></div>
    <div class="secondary-articles-grid"></div>
  `;
  const featuredContainer = container.querySelector(".featured-article-container");
  const secondaryContainer = container.querySelector(".secondary-articles-grid");

  featuredContainer.appendChild(createSkeletonCard(true));
  for (let i = 0; i < 6; i++) {
    secondaryContainer.appendChild(createSkeletonCard(false));
  }

  const startTime = performance.now();
  const minSkeletonDuration = 400;

  fetchNewsSummary().then(async (data) => {
    if (!data || data.length === 0) {
      container.innerHTML = "<p>No hay noticias disponibles en este momento.</p>";
      return;
    }

    const featuredItem = data[0];
    const secondaryItems = data.slice(1);

    const featuredSlug = await normalizeTitle(featuredItem.title);
    const featuredHTML = `
      <article class="program-card featured-card">
        <div class="program-card-image-wrapper">
          <img src="${featuredItem.imageUrl}" alt="${featuredItem.title}">
        </div>
        <div class="program-card-content">
          <h3>${featuredItem.title}</h3>
          <p>${featuredItem.description}</p>
          <div class="program-card-footer">
            <a href="/noticias-y-actividades/${featuredSlug}" id="${sanitizeTitle(featuredItem.title)}" class="learn-more read-more">
              Saber más <i class="fas fa-arrow-right"></i>
            </a>
          </div>
        </div>
      </article>
    `;

    const secondaryArticlesHTML = await Promise.all(
      secondaryItems.map(async (item) => {
        const slug = await normalizeTitle(item.title);
        return `
          <article class="program-card">
            <div class="program-card-image-wrapper">
              <img src="${item.imageUrl}" alt="${item.title}">
            </div>
            <div class="program-card-content">
              <h3>${item.title}</h3>
              <p>${item.description}</p>
              <div class="program-card-footer">
                <a href="/noticias-y-actividades/${slug}" id="${sanitizeTitle(item.title)}" class="learn-more read-more">
                  Saber más <i class="fas fa-arrow-right"></i>
                </a>
              </div>
            </div>
          </article>
        `;
      })
    );

    const elapsedTime = performance.now() - startTime;
    const delayNeeded = Math.max(0, minSkeletonDuration - elapsedTime);

    setTimeout(() => {
      featuredContainer.innerHTML = featuredHTML;
      secondaryContainer.innerHTML = secondaryArticlesHTML.join("");
    }, delayNeeded);
  }).catch((err) => {
    console.error("Error fetching news:", err);
    container.innerHTML = "<p>Error al cargar las noticias. Por favor, inténtelo de nuevo más tarde.</p>";
  });
});
