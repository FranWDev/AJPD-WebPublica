import { fetchNewsSummary, normalizeTitle } from "../api/publicationService.js";

document.addEventListener("DOMContentLoaded", () => {
  const programCards = document.querySelector(".featured-section .program-cards");

  if (!programCards) {
    console.error("No se encontró el contenedor .featured-section .program-cards");
    return;
  }

  const createSkeletonCard = () => {
    const article = document.createElement("article");
    article.classList.add("program-card", "skeleton");
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

  programCards.innerHTML = "";
  for (let index = 0; index < 3; index += 1) {
    programCards.appendChild(createSkeletonCard());
  }

  const startTime = performance.now();
  const minSkeletonDuration = 400;

  fetchNewsSummary()
    .then(async (data) => {
      const newsItems = data.slice(0, 3);
      const articles = await Promise.all(
        newsItems.map(async (item) => {
          const normalizedTitle = await normalizeTitle(item.title);
          return `
            <article class="program-card">
              <div class="program-card-image-wrapper">
                <img src="${item.imageUrl}" alt="${item.title}">
              </div>
              <div class="program-card-content">
                <h3>${item.title}</h3>
                <p>${item.description}</p>
                <div class="program-card-footer">
                  <a href="/noticias-y-actividades/${normalizedTitle}" class="learn-more read-more">
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
        programCards.innerHTML = articles.join("");
      }, delayNeeded);
    })
    .catch((err) => {
      console.error("Error al cargar las noticias:", err);
      programCards.innerHTML = "";
    });
});
