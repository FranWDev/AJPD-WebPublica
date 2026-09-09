document.addEventListener("DOMContentLoaded", () => {
  const REGISTRATION_ENABLED = true;
  const form = document.getElementById("form-registro-jardín");
  const statusDiv = document.getElementById("form-status");
  const disabledNotice = document.getElementById("form-disabled-notice");
  const registroSection = form?.closest(".registro-visitante");
  const CONTACT_EMAIL = "contacto@proyectodubini.org";
  const FORM_LOCK_KEY = "museoRegistroEnviadoAt";
  const LOCK_DURATION_MS = 24 * 60 * 60 * 1000;
  const SUCCESS_HIDE_DELAY_MS = 900;
  const HIDE_ANIMATION_MS = 320;

  if (!form) {
    return;
  }

  const submitBtn = form.querySelector(".btn-submit");
  const fechaInput = document.getElementById("fecha");
  const horaRangoSelect = document.getElementById("hora-rango");

  const applyFrontendDisabledState = () => {
    if (REGISTRATION_ENABLED) {
      return;
    }

    if (disabledNotice) {
      disabledNotice.hidden = false;
    }

    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = "Envío temporalmente deshabilitado";
    }
  };

  const storageGet = (key) => {
    try {
      return window.localStorage.getItem(key);
    } catch (error) {
      return null;
    }
  };

  const storageSet = (key, value) => {
    try {
      window.localStorage.setItem(key, value);
      return true;
    } catch (error) {
      return false;
    }
  };

  const storageRemove = (key) => {
    try {
      window.localStorage.removeItem(key);
    } catch (error) {
      // Ignorar: storage no disponible
    }
  };

  const renderLockedMessage = () => {
    statusDiv.innerHTML =
      "Solicitud enviada correctamente. Ya tenemos tus datos y nos pondremos en contacto contigo con la mayor brevedad posible.";
    statusDiv.className = "form-status success";
  };

  const hideFormAsSent = ({ animate = false } = {}) => {
    if (!animate) {
      form.style.display = "none";
      if (registroSection) {
        registroSection.classList.add("registro-visitante--sent");
      }
      renderLockedMessage();
      return;
    }

    form.classList.add("form-registro--closing");
    window.setTimeout(() => {
      form.style.display = "none";
      form.classList.remove("form-registro--closing");
      if (registroSection) {
        registroSection.classList.add("registro-visitante--sent");
      }
      renderLockedMessage();
    }, HIDE_ANIMATION_MS);
  };

  const getLastSentAt = () => {
    const raw = storageGet(FORM_LOCK_KEY);
    if (!raw) return null;
    const timestamp = Number(raw);
    return Number.isFinite(timestamp) ? timestamp : null;
  };

  const applyClientRateLimit = () => {
    if (!REGISTRATION_ENABLED) {
      return;
    }

    const lastSentAt = getLastSentAt();
    if (lastSentAt === null) {
      return;
    }

    const elapsed = Date.now() - lastSentAt;
    if (elapsed < LOCK_DURATION_MS) {
      hideFormAsSent({ animate: false });
      return;
    }

    storageRemove(FORM_LOCK_KEY);
  };

  applyClientRateLimit();
  applyFrontendDisabledState();

  if (form.style.display === "none") {
    return;
  }

  // Validación de días martes y miércoles
  const isDayAllowed = (dateString) => {
    const date = new Date(dateString + 'T12:00:00');
    const dayOfWeek = date.getDay(); // 0=domingo, 2=martes, 3=miércoles
    return dayOfWeek === 2 || dayOfWeek === 3;
  };

  const getDayOfWeek = (dateString) => {
    const date = new Date(dateString + 'T12:00:00');
    return date.getDay();
  };

  const updateHoraRangoOptions = (dateString) => {
    if (!dateString) {
      horaRangoSelect.disabled = true;
      horaRangoSelect.innerHTML = '<option value="">Primero selecciona una fecha</option>';
      return;
    }

    if (!isDayAllowed(dateString)) {
      horaRangoSelect.disabled = true;
      horaRangoSelect.innerHTML = '<option value="">Solo se permiten martes y miércoles</option>';
      statusDiv.textContent = "Solo se permiten visitas los martes y miércoles.";
      statusDiv.className = "form-status error";
      return;
    }

    const dayOfWeek = getDayOfWeek(dateString);
    horaRangoSelect.disabled = false;
    statusDiv.textContent = "";
    statusDiv.className = "form-status";

    if (dayOfWeek === 2) {
      // Martes: 8:15 a 10:00
      horaRangoSelect.innerHTML = '<option value="08:15-10:00">08:15 - 10:00</option>';
    } else if (dayOfWeek === 3) {
      // Miércoles: 9:15 a 11:00
      horaRangoSelect.innerHTML = '<option value="09:15-11:00">09:15 - 11:00</option>';
    }
  };

  // Función para validar y rechazar fecha inválida
  const validateAndRejectInvalidDate = (inputElement) => {
    const value = inputElement.value;
    if (!value) return;

    if (!isDayAllowed(value)) {
      statusDiv.textContent = "Solo están disponibles los martes y miércoles. Por favor, selecciona uno de estos días.";
      statusDiv.className = "form-status error";
      inputElement.value = '';
      updateHoraRangoOptions('');
      
      // Highlight temporal del campo
      inputElement.style.borderColor = '#d32f2f';
      inputElement.style.backgroundColor = 'rgba(211, 47, 47, 0.05)';
      setTimeout(() => {
        inputElement.style.borderColor = '';
        inputElement.style.backgroundColor = '';
      }, 2000);
    }
  };

  // Event listener para cambio de fecha con validación inmediata
  fechaInput?.addEventListener('change', (e) => {
    validateAndRejectInvalidDate(e.target);
    if (e.target.value) {
      updateHoraRangoOptions(e.target.value);
    }
  });

  // Validación también en input (mientras el usuario escribe/selecciona)
  fechaInput?.addEventListener('input', (e) => {
    if (e.target.value) {
      validateAndRejectInvalidDate(e.target);
    }
  });

  // Validación en blur para asegurar que no se deje un valor inválido
  fechaInput?.addEventListener('blur', (e) => {
    if (e.target.value) {
      validateAndRejectInvalidDate(e.target);
    }
  });

  // Prevenir submit del formulario con fecha inválida mediante Enter
  fechaInput?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      if (e.target.value) {
        validateAndRejectInvalidDate(e.target);
      }
    }
  });

  const buildBackendErrorMessage = (payload) => {
    if (!payload) return "Error al enviar la inscripción. Por favor, intenta nuevamente.";

    const baseMessage = payload.message || "La solicitud no pudo procesarse.";
    if (!payload.data || typeof payload.data !== "object") {
      return baseMessage;
    }

    const fieldErrors = Object.entries(payload.data)
      .map(([field, message]) => `• ${field}: ${message}`)
      .join("\n");

    return fieldErrors ? `${baseMessage}\n${fieldErrors}` : baseMessage;
  };

  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    if (!REGISTRATION_ENABLED) {
      statusDiv.innerHTML = `El envío de solicitudes está temporalmente deshabilitado.<br><br>Para más información, escríbenos a <a href="mailto:${CONTACT_EMAIL}">${CONTACT_EMAIL}</a>.`;
      statusDiv.className = "form-status error";
      return;
    }

    const nombre = document.getElementById("nombre").value.trim();
    const email = document.getElementById("email").value.trim();
    let telefono = document.getElementById("telefono").value.trim();
    const tipoCaridad = document.getElementById("tipo-caridad").value;
    const numPersonas = document.getElementById("num-personas").value;
    const fecha = document.getElementById("fecha").value;
    const horaRango = document.getElementById("hora-rango").value;
    const comentarios = document.getElementById("comentarios").value.trim();
    const privacyConsent = document.getElementById("privacy-consent").checked;
    const horarioConsent = document.getElementById("horario-consent").checked;

    // Procesar teléfono: agregar +34 si no comienza con +
    if (!telefono.startsWith("+")) {
      telefono = "+34" + telefono.replace(/\s+/g, "");
    }

    if (!nombre || !email || !telefono || !tipoCaridad || !numPersonas || !fecha || !horaRango) {
      statusDiv.textContent = "Por favor, completa todos los campos requeridos.";
      statusDiv.className = "form-status error";
      return;
    }

    // Validar que la fecha sea martes o miércoles
    if (!isDayAllowed(fecha)) {
      statusDiv.textContent = "Solo se permiten visitas los martes y miércoles.";
      statusDiv.className = "form-status error";
      return;
    }

    if (!privacyConsent) {
      statusDiv.textContent = "Debes aceptar la política de privacidad para continuar.";
      statusDiv.className = "form-status error";
      return;
    }

    if (!horarioConsent) {
      statusDiv.textContent = "Debes confirmar que comprendes que el horario puede ser flexible.";
      statusDiv.className = "form-status error";
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = "Enviando...";
    statusDiv.textContent = "";
    statusDiv.className = "form-status";

    try {
      const response = await fetch("/api/museo/visitantes", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          nombre,
          email,
          telefono,
          tipoCaridad,
          numPersonas: parseInt(numPersonas),
          fecha,
          horaRango,
          comentarios,
        }),
      });

      let payload = null;
      try {
        payload = await response.json();
      } catch (jsonError) {
        payload = null;
      }

      if (response.ok) {
        storageSet(FORM_LOCK_KEY, String(Date.now()));
        statusDiv.textContent =
          payload?.message ||
          "Solicitud enviada correctamente. Hemos recibido tu petición y la revisaremos en breve.";
        statusDiv.className = "form-status success";

        await new Promise((resolve) => {
          window.setTimeout(resolve, SUCCESS_HIDE_DELAY_MS);
        });

        form.reset();
        hideFormAsSent({ animate: true });
      } else {
        throw new Error(buildBackendErrorMessage(payload));
      }
    } catch (error) {
      console.error("Error:", error);
      statusDiv.innerHTML = `${error.message}<br><br>Si el problema continúa, escríbenos a <a href="mailto:${CONTACT_EMAIL}">${CONTACT_EMAIL}</a>.`;
      statusDiv.className = "form-status error";
    } finally {
      if (form.style.display !== "none") {
        submitBtn.disabled = false;
        submitBtn.textContent = "Enviar inscripción";
      }
    }
  });
});
