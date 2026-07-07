if ("serviceWorker" in navigator) {
  window.addEventListener("load", () => {
    navigator.serviceWorker
      .register("/sw.js")
      .then((reg) => {
        console.log("Service Worker registrado con éxito:", reg);
        
        reg.update();

        reg.onupdatefound = () => {
          const installingWorker = reg.installing;
          installingWorker.onstatechange = () => {
            if (installingWorker.state === 'activated') {
              if (navigator.serviceWorker.controller) {
                console.log("Nueva versión del Service Worker detectada. Recargando...");
                window.location.reload();
              }
            }
          };
        };
      })
      .catch((err) => {
        console.error("Error al registrar el Service Worker:", err);
      });
  });
}

document.addEventListener("DOMContentLoaded", () => {
  const mobileMenuBtn = document.querySelector(".mobile-menu-btn");
  const mobileNav = document.querySelector(".mobile-nav");
  const mobileDropdownBtns = document.querySelectorAll(".mobile-dropdown-btn");
  const body = document.body;
8
  const overlay = document.createElement("div");
  overlay.className = "mobile-nav-overlay";
  document.body.appendChild(overlay);

  mobileMenuBtn.addEventListener("click", () => {
    const isExpanded = mobileMenuBtn.getAttribute("aria-expanded") === "true";
    mobileMenuBtn.setAttribute("aria-expanded", !isExpanded);
    mobileMenuBtn.classList.toggle("active");
    mobileNav.classList.toggle("active");
    overlay.classList.toggle("active");
    body.style.overflow = isExpanded ? "" : "hidden";
  });

  overlay.addEventListener("click", () => {
    mobileNav.classList.remove("active");
    mobileMenuBtn.classList.remove("active");
    overlay.classList.remove("active");
    mobileMenuBtn.setAttribute("aria-expanded", "false");
    body.style.overflow = "";
  });

  mobileDropdownBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
      const isExpanded = btn.getAttribute("aria-expanded") === "true";
      btn.setAttribute("aria-expanded", !isExpanded);
      const dropdownContent = btn.nextElementSibling;

      if (!isExpanded) {
        dropdownContent.style.display = "block";
        dropdownContent.style.maxHeight = "0";
        dropdownContent.classList.add("active");
        requestAnimationFrame(() => {
          dropdownContent.style.maxHeight = dropdownContent.scrollHeight + "px";
        });
      } else {
        dropdownContent.style.maxHeight = "0";
        dropdownContent.addEventListener("transitionend", function handler() {
          if (dropdownContent.style.maxHeight === "0px") {
            dropdownContent.classList.remove("active");
            dropdownContent.style.display = "";
            dropdownContent.style.maxHeight = "";
            dropdownContent.removeEventListener("transitionend", handler);
          }
        });
      }

      const icon = btn.querySelector(".fa-chevron-down");
      icon.style.transform = isExpanded ? "rotate(0)" : "rotate(180deg)";
      icon.style.transition =
        "transform 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55)";
    });
  });

  const mobileLinks = document.querySelectorAll(".mobile-nav a");
  mobileLinks.forEach((link) => {
    link.addEventListener("click", () => {
      mobileNav.classList.remove("active");
      mobileMenuBtn.classList.remove("active");
      mobileMenuBtn.setAttribute("aria-expanded", "false");
      body.style.overflow = "";
    });
  });

  // FIN MENU MOVIL

  // GOOGLE MAPS
  /* function initMap() {
    const sede = { lat: 40.416775, lng: -3.70379 };
    const map = new google.maps.Map(document.getElementById("map"), {
      zoom: 15,
      center: sede,
      styles: [
        {
          featureType: "all",
          elementType: "geometry",
          stylers: [{ saturation: "-30" }],
        },
      ],
    });

    const marker = new google.maps.Marker({
      position: sede,
      map: map,
      title: "Sede Proyecto Dubini",
    });
  }
  function loadGoogleMaps() {
    const script = document.createElement("script");
    script.src = `https://maps.googleapis.com/maps/api/js?key=TU_API_KEY&callback=initMap`;
    script.async = true;
    script.defer = true;
    window.initMap = initMap;
    document.head.appendChild(script);
  }

  loadGoogleMaps();
*/

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add("animate-fadeIn");
          observer.unobserve(entry.target);
        }
      });
    },
    {
      threshold: 0.1,
    }
  );

  document.querySelectorAll(".footer-section").forEach((section) => {
    observer.observe(section);
  });

  function initHeroSlider() {
    const slides = document.querySelectorAll(".hero-slide");
    const dots = document.querySelectorAll(".hero-dot");
    const prevBtn = document.querySelector(".hero-prev");
    const nextBtn = document.querySelector(".hero-next");
    
    if (slides.length === 0) return;

    let currentSlide = 0;
    let slideInterval;

    function goToSlide(n) {
      slides[currentSlide].classList.remove("active");
      if(dots.length) dots[currentSlide].classList.remove("active");
      
      currentSlide = (n + slides.length) % slides.length;
      
      slides[currentSlide].classList.add("active");
      if(dots.length) dots[currentSlide].classList.add("active");
    }

    function nextSlide() {
      goToSlide(currentSlide + 1);
    }

    function prevSlide() {
      goToSlide(currentSlide - 1);
    }

    function startSlideShow() {
      slideInterval = setInterval(nextSlide, 5000);
    }

    function resetSlideShow() {
      clearInterval(slideInterval);
      startSlideShow();
    }

    if (prevBtn) {
      prevBtn.addEventListener("click", () => {
        prevSlide();
        resetSlideShow();
      });
    }

    if (nextBtn) {
      nextBtn.addEventListener("click", () => {
        nextSlide();
        resetSlideShow();
      });
    }

    dots.forEach((dot, index) => {
      dot.addEventListener("click", () => {
        goToSlide(index);
        resetSlideShow();
      });
    });

    slides[0].classList.add("active");
    startSlideShow();
  }

  initHeroSlider();

  async function initActivitySlider() {
    const sliderTrack = document.querySelector(".slider-track");
    const slides = document.querySelectorAll(".slide");
    const dots = document.querySelectorAll(".activity-dot");
    const prevBtn = document.querySelector(".activity-prev");
    const nextBtn = document.querySelector(".activity-next");
    let currentSlide = 0;
    const totalSlides = slides.length;

    if (!sliderTrack) return;

    // Cargar captions desde JSONs
    for (let i = 1; i <= totalSlides; i++) {
        try {
            const jsonUrl = `https://mcybqxqlujczgclidnar.supabase.co/storage/v1/object/public/ajpd-storage/slider/slide${i}.json`;
            const response = await fetch(jsonUrl);
            const data = await response.json();
            const slide = document.querySelector(`.slide[data-slide-id="${i}"]`);
            if (slide && data.caption) {
                const captionParagraph = slide.querySelector(".slide-caption p");
                if (captionParagraph) {
                    captionParagraph.textContent = data.caption;
                }
            }
        } catch (error) {
            console.warn(`Error cargando caption para slide ${i}:`, error);
        }
    }

    function goToSlide(index) {
        currentSlide = (index + totalSlides) % totalSlides;
        const offset = -currentSlide * 100;
        sliderTrack.style.transform = `translateX(${offset}%)`;
        
        dots.forEach((dot) => dot.classList.remove("active"));
        if(dots[currentSlide]) dots[currentSlide].classList.add("active");
    }

    if (prevBtn) {
        prevBtn.addEventListener("click", () => {
            stopAutoSlide();
            goToSlide(currentSlide - 1);
            startAutoSlide();
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener("click", () => {
            stopAutoSlide();
            goToSlide(currentSlide + 1);
            startAutoSlide();
        });
    }

    dots.forEach((dot, index) => {
        dot.addEventListener("click", () => {
            stopAutoSlide();
            goToSlide(index);
            startAutoSlide();
        });
    } );

    let autoSlideInterval;
    function startAutoSlide() {
        autoSlideInterval = setInterval(() => {
            goToSlide(currentSlide + 1);
        }, 8000);
    }

    function stopAutoSlide() {
        clearInterval(autoSlideInterval);
    }

    startAutoSlide();

    // Touch events
    let startX;
    sliderTrack.addEventListener("touchstart", (e) => {
        startX = e.touches[0].clientX;
        stopAutoSlide();
    });

    sliderTrack.addEventListener("touchend", (e) => {
        const endX = e.changedTouches[0].clientX;
        if (startX - endX > 50) goToSlide(currentSlide + 1);
        else if (endX - startX > 50) goToSlide(currentSlide - 1);
        startAutoSlide();
    });
  }
  initActivitySlider();

  // ── Auto-hide header en móvil (estilo barra de buscador) ──────────────────
  const header = document.querySelector('.main-header');
  if (header) {
    let lastScrollY = window.scrollY;
    let ticking = false;

    function handleHeaderScroll() {
      const currentScrollY = window.scrollY;
      const isMobile = window.innerWidth < 768;
      const menuIsOpen = mobileNav && mobileNav.classList.contains('active');

      if (isMobile && !menuIsOpen) {
        if (currentScrollY > lastScrollY && currentScrollY > 80) {
          // Scrolling DOWN → ocultar header
          header.classList.add('header-hidden');
        } else {
          // Scrolling UP → mostrar header
          header.classList.remove('header-hidden');
        }
      } else {
        // Desktop o menú abierto: siempre visible
        header.classList.remove('header-hidden');
      }

      lastScrollY = currentScrollY;
      ticking = false;
    }

    window.addEventListener('scroll', () => {
      if (!ticking) {
        requestAnimationFrame(handleHeaderScroll);
        ticking = true;
      }
    }, { passive: true });
  }

  
});

document.addEventListener('DOMContentLoaded', function() {
  const fadeElements = document.querySelectorAll('.fade-in-element');

  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };
  
  const observerCallback = (entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  };
  
  const observer = new IntersectionObserver(observerCallback, observerOptions);

  fadeElements.forEach(element => {
    observer.observe(element);
  });
});