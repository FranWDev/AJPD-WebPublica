import { fetchActivitiesSummary } from "../api/publicationService.js";
import edjsHTML from "../components/EditorJSParser.js";
import { sanitizeTitle, initializePopups } from "../components/popup.js";

document.addEventListener("DOMContentLoaded", () => {
  const container = document.querySelector(".program-cards");
  if (!container) return;

  const createSkeletonCard = () => {
    const article = document.createElement("article");
    article.className = "program-card skeleton";
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
  container.innerHTML = "";
  for (let i = 0; i < 6; i++) {
    container.appendChild(createSkeletonCard());
  }

  const startTime = performance.now();
  const minSkeletonDuration = 400;

  fetchActivitiesSummary().then((data) => {
    const parser = edjsHTML();
    const fragments = document.createDocumentFragment();

    if (!data || data.length === 0) {
      container.innerHTML = "<p>No hay actividades disponibles en este momento.</p>";
      return;
    }

    data.forEach((item) => {
      const sanitizedTitle = sanitizeTitle(item.title);

      const article = document.createElement("article");
      article.classList.add("program-card");
      article.innerHTML = `
        <div class="program-card-image-wrapper">
          <img src="${item.imageUrl}" alt="${item.title}">
        </div>
        <div class="program-card-content">
          <h3>${item.title}</h3>
          <p>${item.description}</p>
          <div class="program-card-footer">
            <a href="#" id="${sanitizedTitle}" class="learn-more read-more">
              Saber más <i class="fas fa-arrow-right"></i>
            </a>
          </div>
        </div>
      `;

      const popupOverlay = document.createElement("div");
      popupOverlay.classList.add("popup-overlay");
      popupOverlay.id = "overlay-" + sanitizedTitle;

      const popupContent = document.createElement("div");
      popupContent.classList.add("popup-content");
      popupContent.id = "popup-content-" + sanitizedTitle;

      const popupHeader = document.createElement("div");
      popupHeader.innerHTML = `
        <h2>${item.title}</h2>
        <button aria-label="Cerrar">&times;</button>
      `;

      const popupBody = document.createElement("div");
      popupBody.innerHTML = parser.parse(item.editorContent);

      popupContent.appendChild(popupHeader);
      popupContent.appendChild(popupBody);
      popupOverlay.appendChild(popupContent);

      fragments.appendChild(popupOverlay);
      fragments.appendChild(article);
    });

    const elapsedTime = performance.now() - startTime;
    const delayNeeded = Math.max(0, minSkeletonDuration - elapsedTime);

    setTimeout(() => {
      container.innerHTML = "";
      container.appendChild(fragments);
      initializePopups();
    }, delayNeeded);
  }).catch((err) => {
    console.error("Error fetching activities:", err);
    container.innerHTML = "<p>Error al cargar las actividades. Por favor, inténtelo de nuevo más tarde.</p>";
  });
});
