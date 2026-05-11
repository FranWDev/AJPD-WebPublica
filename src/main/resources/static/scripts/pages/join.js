document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('joinForm');
    const steps = document.querySelectorAll('.wizard-step');
    const stepperItems = document.querySelectorAll('.step');
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const submitBtn = document.getElementById('submitBtn');

    const STORAGE_KEY = 'ajpd_join_submitted';
    let currentLang = 'es'; // default
    let currentStep = 1;
    const totalSteps = steps.length;

    // Verificar si ya envió el formulario
    if (localStorage.getItem(STORAGE_KEY)) {
        document.querySelector('.wizard-form').style.display = 'none';
        document.querySelector('.wizard-header').style.display = 'none';
        document.getElementById('already-submitted-msg').style.display = 'block';
    }

    // Paises y Prefijos para popular los selectores
    const countries = [
        { code: 'ES', name: 'España', nameEn: 'Spain', dial: '+34' },
        { code: 'US', name: 'Estados Unidos', nameEn: 'United States', dial: '+1' },
        { code: 'GB', name: 'Reino Unido', nameEn: 'United Kingdom', dial: '+44' },
        { code: 'AR', name: 'Argentina', nameEn: 'Argentina', dial: '+54' },
        { code: 'CO', name: 'Colombia', nameEn: 'Colombia', dial: '+57' },
        { code: 'MX', name: 'México', nameEn: 'Mexico', dial: '+52' },
        { code: 'CL', name: 'Chile', nameEn: 'Chile', dial: '+56' },
        { code: 'PE', name: 'Perú', nameEn: 'Peru', dial: '+51' },
        { code: 'VE', name: 'Venezuela', nameEn: 'Venezuela', dial: '+58' },
        { code: 'FR', name: 'Francia', nameEn: 'France', dial: '+33' },
        { code: 'IT', name: 'Italia', nameEn: 'Italy', dial: '+39' },
        { code: 'DE', name: 'Alemania', nameEn: 'Germany', dial: '+49' },
        { code: 'PT', name: 'Portugal', nameEn: 'Portugal', dial: '+351' }
        // Se pueden añadir más si se desea, pero esto cubre gran parte del mundo hispano y europeo para no saturar.
    ];

    function populateSelects(lang) {
        const paisSelect = document.getElementById('pais');
        const prefijoSelect = document.getElementById('prefijo');
        
        // Guardar selección actual si existe
        const currPais = paisSelect.value;
        const currPrefijo = prefijoSelect.value;

        paisSelect.innerHTML = '<option value="">' + (lang === 'es' ? 'Selecciona un país' : 'Select a country') + '</option>';
        prefijoSelect.innerHTML = '';

        countries.forEach(c => {
            const countryName = lang === 'es' ? c.name : c.nameEn;
            paisSelect.innerHTML += `<option value="${c.code}">${countryName}</option>`;
            
            // Prefijos (asegurar que no haya duplicados si fuera necesario, aunque aquí están únicos por país base)
            prefijoSelect.innerHTML += `<option value="${c.dial}">${c.dial} (${c.code})</option>`;
        });

        // Restaurar selección si aplica
        if (currPais) paisSelect.value = currPais;
        if (currPrefijo) prefijoSelect.value = currPrefijo;
        else prefijoSelect.value = "+34"; // default
    }

    // --- Textos e Idiomas ---
    const i18n = {
        es: {
            title: "Formulario de Inscripción",
            step1Title: "Seleccione su idioma / Select your language",
            step2Title: "Ser Socio de la Asociación Juvenil Proyecto Dubini (AJPD)",
            step2Content: `Unirse a la Asociación Juvenil Proyecto Dubini (AJPD) es más que simplemente formar parte de un grupo; es comprometerse con un viaje de crecimiento personal y colectivo. Como socio, te integrarás a una comunidad dinámica y apasionada, enfocada en generar un impacto positivo tanto en sus miembros como en la sociedad. Aquí te contamos lo que implica ser socio:

Los Socios Fundadores o Numerarios tendrán los siguientes derechos:
a) Disfrutar de los servicios y ventajas de la Asociación.
b) Asistir a cuantos actos se celebren en la Asociación.
c) Intervenir en las reuniones y actos que se celebren.
d) A elegir y ser elegido para cualquier cargo.
e) Exigir que la actuación de la Asociación se ajuste a lo dispuesto en la legislación vigente ya a las disposiciones estatutarias y reglamentarias específicas.
f) Separarse libremente de la Asociación.
g) Conocer las actividades de la Asociación y examinar la documentación contable.
h) Conocer los Estatutos de la Asociación y los acuerdos adoptados por los Órganos de gobierno y tener voz y voto en las Asambleas Generales, así como participar en los debates.
i) Ser oído con carácter previo a la adopción de medidas disciplinarias contra él y a ser informado de los hechos que den lugar a tales medidas, debiendo ser motivado, en su caso, el acuerdo que imponga la sanción.
j) Impugnar los acuerdos de los órganos de la asociación, cuando los estime contrarios a la Ley o a los Estatutos.

Los Socios Fundadores o Numerarios tendrán los siguientes deberes:
a) Conocer y observar el contenido de los Estatutos y Reglamento de Régimen Interno de la Asociación.
b) Acatar los preceptos de estos Estatutos, así como los acuerdos de la Asamblea General de Alumnos.
c) Contribuir a potenciar la Asociación.

Esto se corresponde con los estatutos sociales de la Asociación.`,
            step3Title: "Datos Personales del Socio",
            lblNombre: "Nombre y Apellidos *",
            lblEmail: "Correo electrónico *",
            lblTelefono: "Número de teléfono con prefijo *",
            lblDocumento: "DNI / NIE / Pasaporte *",
            lblPais: "País *",
            lblDireccionCalle: "Calle/Vía *",
            lblDireccionNumero: "Número *",
            lblDireccionPuerta: "Puerta",
            lblDireccionResto: "Resto de Dirección",
            lblDireccionCP: "Código Postal *",
            lblDireccionProvincia: "Provincia",
            lblDireccionCiudad: "Ciudad/Población *",
            lblOcupacion: "Centro de estudios / Ocupación *",
            lblFechaNac: "Fecha de Nacimiento *",
            step4Title: "Autorizaciones y Consentimiento",
            step4Content: `1. Propósito del Grupo:
Este grupo se crea con el propósito de Organizar y coordinar el trabajo de la Asociación.

2. Consentimiento de los Miembros:
Yo, autorizo mi inclusión en este grupo de WhatsApp y estoy de acuerdo con el propósito descrito.

3. Reglas del Grupo:
Acepto las siguientes reglas del grupo:
- Mantener conversaciones respetuosas y pertinentes al propósito del grupo.
- No compartir información confidencial fuera del grupo sin permiso.
- Seguir las instrucciones de los moderadores del grupo.

4. Privacidad y Confidencialidad:
Me comprometo a respetar la privacidad de los demás miembros y a no divulgar sus números de teléfono ni otra información personal sin su consentimiento.

5. Participación Activa:
Acepto participar de manera activa y respetuosa en las conversaciones del grupo.

6. Moderación:
Reconozco la autoridad de los moderadores designados para supervisar el cumplimiento de las reglas del grupo.
Acepto que los moderadores puedan advertir o eliminar a miembros que no cumplan con las reglas.

7. Horarios de Actividad:
Estoy de acuerdo en limitar la actividad del grupo a horarios razonables, evitando mensajes durante la noche a menos que sea necesario.

8. Actualización de Reglas:
Acepto que las reglas del grupo pueden ser revisadas y actualizadas con el consenso de la mayoría de los miembros.

9. Resolución de Conflictos:
Acepto el proceso establecido para resolver conflictos o malentendidos entre los miembros del grupo.`,
            lblAccept: "Acepto todo lo anterior",
            lblDeny: "Deniego todo lo anterior",
            step5Title: "Autorización sobre los derechos de imagen",
            step5Content: `Por medio del presente formulario otorgo mi autorización expresa y voluntaria a la Asociación Juvenil Proyecto Dubini (AJPD), con domicilio fiscal en Calle Leopoldo de la Rosa Olivera, 1, 38206, San Cristóbal de La Laguna, para el uso de mi imagen y/o voz en fotografías, vídeos, grabaciones y cualquier otro medio audiovisual, conforme a las siguientes condiciones:

1. Finalidad de Uso:
Autorizo a AJPD a utilizar mi imagen y/o voz para los siguientes propósitos:
- Publicidad y promoción de actividades y eventos organizados por AJPD.
- Publicaciones en medios impresos y digitales (páginas web, redes sociales, etc.).
- Material educativo y/o formativo.
- Exhibiciones en eventos, ferias y exposiciones.
- Otros usos relacionados con las actividades de AJPD.

2. Duración y Alcance:
Esta autorización es de carácter indefinido y tiene validez en todo el territorio nacional e internacional, sin restricciones temporales ni geográficas.

3. Derechos de Propiedad:
Entiendo y acepto que las imágenes y vídeos en los que aparezco son propiedad exclusiva de AJPD, y que esta puede editarlos, modificarlos y reproducirlos conforme a sus necesidades, sin que ello implique derecho a retribución económica alguna a mi favor.

4. Confidencialidad:
AJPD se compromete a utilizar mi imagen y/o voz de manera respetuosa y profesional, asegurando que su uso no afectará mi honor, dignidad o reputación.

5. Revocación de la Autorización:
Podré revocar esta autorización en cualquier momento, mediante notificación por escrito a AJPD, entendiendo que dicha revocación no tendrá efectos retroactivos sobre el material ya publicado o distribuido.`,
            authWspTitle: "WhatsApp",
            authImagenTitle: "Protección de Imagen",
            authImagenContent: `Por medio del presente formulario otorgo mi autorización expresa y voluntaria a la Asociación Juvenil Proyecto Dubini (AJPD), con domicilio fiscal en Calle Leopoldo de la Rosa Olivera, 1, 38206, San Cristóbal de La Laguna, para el uso de mi imagen y/o voz en fotografías, vídeos, grabaciones y cualquier otro medio audiovisual, conforme a las siguientes condiciones:

1. Finalidad de Uso:
Autorizo a AJPD a utilizar mi imagen y/o voz para los siguientes propósitos:
- Publicidad y promoción de actividades y eventos organizados por AJPD.
- Publicaciones en medios impresos y digitales (páginas web, redes sociales, etc.).
- Material educativo y/o formativo.
- Exhibiciones en eventos, ferias y exposiciones.
- Otros usos relacionados con las actividades de AJPD.

2. Duración y Alcance:
Esta autorización es de carácter indefinido y tiene validez en todo el territorio nacional e internacional, sin restricciones temporales ni geográficas.

3. Derechos de Propiedad:
Entiendo y acepto que las imágenes y vídeos en los que aparezco son propiedad exclusiva de AJPD, y que esta puede editarlos, modificarlos y reproducirlos conforme a sus necesidades, sin que ello implique derecho a retribución económica alguna a mi favor.

4. Confidencialidad:
AJPD se compromete a utilizar mi imagen y/o voz de manera respetuosa y profesional, asegurando que su uso no afectará mi honor, dignidad o reputación.

5. Revocación de la Autorización:
Podré revocar esta autorización en cualquier momento, mediante notificación por escrito a AJPD, entendiendo que dicha revocación no tendrá efectos retroactivos sobre el material ya publicado o distribuido.`,
            authDatosTitle: "Datos y Plataformas de Gestión",
            authDatosContent: `Por medio del presente, otorgo mi autorización expresa y voluntaria a la Asociación Juvenil Proyecto Dubini (AJPD), con domicilio fiscal en Calle Leopoldo de la Rosa Olivera, 1, 38206, San Cristóbal de La Laguna, para el tratamiento de mis datos personales conforme a las siguientes condiciones y de acuerdo con lo establecido en el Reglamento General de Protección de Datos (RGPD), la Ley Orgánica de Protección de Datos Personales y garantía de los derechos digitales (LOPDGDD) y demás normativa española vigente en materia de protección de datos:

Finalidad del Tratamiento:
Autorizo a AJPD a recopilar, almacenar, organizar y utilizar mis datos personales exclusivamente para los siguientes fines:
- Gestión administrativa y organizativa de actividades, proyectos y eventos desarrollados por AJPD.
- Control interno, coordinación y comunicación con participantes, socios, voluntariado o colaboradores.
- Elaboración de listados, registros y bases de datos internas relacionadas con la actividad de AJPD.
- Gestión documental, contable y de cumplimiento de obligaciones legales y organizativas.
- Uso de plataformas digitales y aplicaciones de gestión necesarias para el correcto funcionamiento interno de AJPD.

Cesión y Acceso a Terceras Aplicaciones:
Autorizo a AJPD a alojar, almacenar o tratar mis datos mediante servicios y aplicaciones de terceros (como plataformas de gestión, almacenamiento en la nube, formularios, correo electrónico o herramientas organizativas), siempre que dicho tratamiento tenga exclusivamente fines internos, administrativos y de gestión relacionados con la actividad de AJPD, garantizando en todo momento el cumplimiento de la normativa vigente en materia de protección de datos.

AJPD se compromete a seleccionar proveedores que ofrezcan garantías adecuadas de seguridad y confidencialidad conforme al RGPD y la legislación española aplicable.

Duración y Conservación de los Datos:
Mis datos personales serán conservados durante el tiempo necesario para cumplir con las finalidades descritas y mientras exista relación con AJPD o una obligación legal que justifique su conservación.

Confidencialidad y Seguridad:
AJPD se compromete a tratar mis datos de forma confidencial, lícita y segura, adoptando las medidas técnicas y organizativas necesarias para evitar su pérdida, alteración, acceso no autorizado o tratamiento indebido.

Derechos de la Persona Interesada:
Entiendo que podré ejercer en cualquier momento mis derechos de acceso, rectificación, supresión, oposición, limitación del tratamiento y portabilidad de mis datos, mediante solicitud escrita dirigida a AJPD, conforme a lo previsto en la normativa vigente.

Revocación del Consentimiento:
Podré revocar esta autorización en cualquier momento mediante notificación escrita a AJPD, sin que ello afecte a la licitud del tratamiento realizado con anterioridad a dicha revocación.`,

            lblAccept: "Acepto todo lo anterior",
            lblDeny: "Deniego todo lo anterior",
            step5Title: "Foto del Asociado",
            helpPhotoFiles: "Sube una foto tipo Carnet, donde seas fácilmente reconocible. (En caso de que hayas rechazado la autorización de derechos de imagen, esta servirá para identificarte y prevenir tu inclusión en nuestras redes sociales) Si tienes cualquier problema, manda la foto por correo a Secretaria@proyectodubini.org. Sube 1 archivo compatible. Tamaño máximo: 10 MB.",
            step6Title: "¡Gracias!",
            lblComentarios: "¿Algo que comentarnos?",
            btnNext: "Siguiente",
            btnPrev: "Atrás",
            btnSubmit: "Enviar Inscripción",
            modalSuccessTitle: "¡Inscripción Completada!",
            modalSuccessText: "Hemos recibido tu solicitud correctamente. Nos pondremos en contacto contigo lo antes posible para indicarte los siguientes pasos.",
            modalBtn: "Volver al Inicio",
            alreadyMsgTitle: "Ya hemos recibido tu solicitud",
            alreadyMsgText: "Tu formulario de inscripción ya ha sido enviado y está en proceso de revisión. Nos pondremos en contacto contigo pronto.",
            btnSending: "Enviando..."
        },
        en: {
            title: "Inscription Form",
            step1Title: "Seleccione su idioma / Select your language",
            step2Title: "Being a Member of the Asociación Juvenil Proyecto Dubini (AJPD)",
            step2Content: `Joining the Asociación Juvenil Proyecto Dubini (AJPD) is more than just becoming part of a group; it is committing to a journey of personal and collective growth. As a member, you will integrate into a dynamic and passionate community focused on making a positive impact both on its members and on society. Here’s what being a member entails:

Founding or Full Members will have the following rights:
a) Enjoy the services and benefits of the Association.
b) Attend all events held by the Association.
c) Participate in meetings and events that are held.
d) Vote and be eligible for any position.
e) Demand that the Association's actions comply with current legislation and specific statutory and regulatory provisions.
f) Freely withdraw from the Association.
g) Be informed about the Association's activities and examine its accounting documentation.
h) Know the Association's Statutes and the agreements adopted by the governing bodies, have a voice and vote in the General Assemblies, and participate in debates.
i) Be heard before the adoption of disciplinary measures against them and be informed of the facts leading to such measures, with the decision to impose the sanction being motivated, if applicable.
j) Challenge the decisions of the association's governing bodies when they are considered contrary to the law or the Statutes.

Founding or Full Members will have the following duties:
a) Know and observe the content of the Association's Statutes and Internal Regulations.
b) Comply with the precepts of these Statutes as well as the agreements of the General Assembly of Members.
c) Contribute to strengthening the Association.

This corresponds to the statutes.`,
            step3Title: "Personal Data of the Member",
            lblNombre: "Name and Surnames *",
            lblEmail: "Email *",
            lblTelefono: "Phone number with prefix *",
            lblDocumento: "ID Document (DNI/NIE/Passport) *",
            lblPais: "Country *",
            lblDireccionCalle: "Street/Road *",
            lblDireccionNumero: "Number *",
            lblDireccionPuerta: "Door",
            lblDireccionResto: "Additional Address",
            lblDireccionCP: "Postal Code *",
            lblDireccionProvincia: "Province",
            lblDireccionCiudad: "City/Town *",
            lblOcupacion: "Study Center / Occupation *",
            lblFechaNac: "Date of Birth *",
            step4Title: "Authorizations and Consent",
            authWspTitle: "WhatsApp",
            authWspContent: `1. Group Purpose:
This group is created for the purpose of Organizing and coordinating the work of the Association.

2. Member Consent:
I authorize my inclusion in this WhatsApp group and agree with the described purpose.

3. Group Rules:
I accept the following group rules:
- Maintain respectful and relevant conversations to the group's purpose.
- Do not share confidential information outside the group without permission.
- Follow the instructions of the group moderators.

4. Privacy and Confidentiality:
I commit to respecting the privacy of other members and not disclosing their phone numbers or other personal information without their consent.

5. Active Participation:
I agree to participate actively and respectfully in the group's conversations.

6. Moderation:
I recognize the authority of the designated moderators to oversee compliance with the group's rules.
I accept that moderators may warn or remove members who do not comply with the rules.

7. Activity Hours:
I agree to limit group activity to reasonable hours, avoiding messages during the night unless necessary.

8. Rule Updates:
I accept that the group rules may be reviewed and updated with the consensus of the majority of the members.

9. Conflict Resolution:
I accept the established process for resolving conflicts or misunderstandings among group members.`,
            authImagenTitle: "Image Protection",
            authImagenContent: `By means of this form I grant my express and voluntary authorization to the Asociación Juvenil Proyecto Dubini (AJPD), with registered office at Calle Leopoldo de la Rosa Olivera, 1, 38206, San Cristóbal de La Laguna, for the use of my image and/or voice in photographs, videos, recordings, and any other audiovisual medium, under the following conditions:

1. Purpose of Use:
I authorize AJPD to use my image and/or voice for the following purposes:
- Advertising and promotion of activities and events organized by AJPD.
- Publications in print and digital media (websites, social networks, etc.).
- Educational and/or training material.
- Exhibitions at events, fairs, and expos.
- Other uses related to AJPD activities.

2. Duration and Scope:
This authorization is indefinite and valid nationally and internationally, without temporal or geographical restrictions.

3. Property Rights:
I understand and accept that the images and videos in which I appear are the exclusive property of AJPD, and that it may edit, modify, and reproduce them according to its needs, without this implying any right to financial compensation in my favor.

4. Confidentiality:
AJPD commits to using my image and/or voice respectfully and professionally, ensuring that its use will not affect my honor, dignity, or reputation.

5. Revocation of Authorization:
I may revoke this authorization at any time by written notification to AJPD, understanding that such revocation will not have retroactive effects on material already published or distributed.`,
            authDatosTitle: "Data and Management Platforms",
            authDatosContent: `By means of the present, I expressly and voluntarily grant my authorization to the Asociación Juvenil Proyecto Dubini (AJPD), with registered office at Calle Leopoldo de la Rosa Olivera, 1, 38206, San Cristóbal de La Laguna, for the processing of my personal data in accordance with the following conditions and in accordance with the provisions of the General Data Protection Regulation (GDPR), the Organic Law for the Protection of Personal Data and guarantee of digital rights (LOPDGDD) and other Spanish regulations in force on data protection:

Purpose of Processing:
I authorize AJPD to collect, store, organize and use my personal data exclusively for the following purposes:
- Administrative and organizational management of activities, projects and events developed by AJPD.
- Internal control, coordination and communication with participants, partners, volunteers or collaborators.
- Preparation of lists, records and internal databases related to AJPD's activity.
- Document management, accounting and compliance with legal and organizational obligations.
- Use of digital platforms and management applications necessary for the proper internal functioning of AJPD.

Transfer and Access to Third-Party Applications:
I authorize AJPD to host, store or process my data through third-party services and applications (such as management platforms, cloud storage, forms, email or organizational tools), provided that such processing is exclusively for internal, administrative and management purposes related to AJPD's activity, guaranteeing at all times compliance with current data protection regulations.

AJPD commits to selecting providers that offer adequate guarantees of security and confidentiality in accordance with the GDPR and applicable Spanish legislation.

Duration and Data Conservation:
My personal data will be kept for the time necessary to fulfill the purposes described and while there is a relationship with AJPD or a legal obligation that justifies its conservation.

Confidentiality and Security:
AJPD commits to processing my data in a confidential, lawful and secure manner, adopting the necessary technical and organizational measures to prevent its loss, alteration, unauthorized access or misuse.

Rights of the Data Subject:
I understand that I may exercise at any time my rights of access, rectification, erasure, opposition, limitation of processing and portability of my data, through a written request addressed to AJPD, in accordance with current regulations.

Revocation of Consent:
I may revoke this authorization at any time by written notification to AJPD, without affecting the legality of the processing carried out prior to such revocation.`,

            lblAccept: "I accept all the above",
            lblDeny: "I deny all the above",
            step5Title: "Member's Photo",
            helpPhotoFiles: "Upload a passport-style photo where you are easily recognizable. (If you denied the image rights authorization, this will serve to identify you and prevent your inclusion in our social networks). If you have any problems, send the photo by email to Secretaria@proyectodubini.org. Upload 1 compatible file. Maximum size: 10 MB.",
            step6Title: "Thank you!",
            lblComentarios: "Anything to tell us?",
            btnNext: "Next",
            btnPrev: "Back",
            btnSubmit: "Submit Inscription",
            modalSuccessTitle: "Inscription Completed!",
            modalSuccessText: "We have successfully received your request. We will contact you as soon as possible to indicate the next steps.",
            modalBtn: "Return to Home",
            alreadyMsgTitle: "We have already received your request",
            alreadyMsgText: "Your inscription form has already been submitted and is under review. We will contact you soon.",
            btnSending: "Sending..."
        }
    };

    function setLanguage(lang) {
        currentLang = lang;
        const t = i18n[lang];

        document.getElementById('title-main').innerText = t.title;
        document.getElementById('step1-title').innerText = t.step1Title;
        
        document.getElementById('step2-title').innerText = t.step2Title;
        document.getElementById('step2-content').innerText = t.step2Content;

        document.getElementById('step3-title').innerText = t.step3Title;
        document.getElementById('lbl-nombre').innerText = t.lblNombre;
        document.getElementById('lbl-email').innerText = t.lblEmail;
        document.getElementById('lbl-telefono').innerText = t.lblTelefono;
        document.getElementById('lbl-documento').innerText = t.lblDocumento;
        document.getElementById('lbl-pais').innerText = t.lblPais;
        document.getElementById('lbl-direccion-calle').innerText = t.lblDireccionCalle;
        document.getElementById('lbl-direccion-numero').innerText = t.lblDireccionNumero;
        document.getElementById('lbl-direccion-puerta').innerText = t.lblDireccionPuerta;
        document.getElementById('lbl-direccion-resto').innerText = t.lblDireccionResto;
        document.getElementById('lbl-direccion-cp').innerText = t.lblDireccionCP;
        document.getElementById('lbl-direccion-provincia').innerText = t.lblDireccionProvincia;
        document.getElementById('lbl-direccion-ciudad').innerText = t.lblDireccionCiudad;
        document.getElementById('lbl-ocupacion').innerText = t.lblOcupacion;
        document.getElementById('lbl-fecha-nac').innerText = t.lblFechaNac;

        document.getElementById('step4-title').innerText = t.step4Title;
        document.getElementById('auth-wsp-title').innerText = t.authWspTitle;
        document.getElementById('auth-wsp-text').innerText = t.authWspContent || t.step4Content;
        document.getElementById('auth-imagen-title').innerText = t.authImagenTitle;
        document.getElementById('auth-imagen-text').innerText = t.authImagenContent;
        document.getElementById('auth-datos-title').innerText = t.authDatosTitle;
        document.getElementById('auth-datos-text').innerText = t.authDatosContent;
        
        const scrollWarningText = document.getElementById('scroll-warning-text');
        if (scrollWarningText) scrollWarningText.innerText = t.scrollWarning;

        document.getElementById('step5-title').innerText = t.step5Title;
        document.getElementById('help-photo-files').innerText = t.helpPhotoFiles;

        document.getElementById('step6-title').innerText = t.step6Title;
        document.getElementById('lbl-comentarios').innerText = t.lblComentarios;

        document.getElementById('btn-next-text').innerText = t.btnNext;
        document.getElementById('btn-prev-text').innerText = t.btnPrev;
        document.getElementById('btn-submit-text').innerText = t.btnSubmit;

        // Modal y mensajes
        const modalTitle = document.getElementById('modal-success-title');
        const modalText = document.getElementById('modal-success-text');
        const modalBtn = document.getElementById('modal-success-btn');
        if (modalTitle) modalTitle.innerText = t.modalSuccessTitle;
        if (modalText) modalText.innerText = t.modalSuccessText;
        if (modalBtn) modalBtn.innerText = t.modalBtn;

        const alreadyTitle = document.querySelector('#already-submitted-msg h2');
        const alreadyText = document.querySelector('#already-submitted-msg p');
        const alreadyBtn = document.querySelector('#already-submitted-msg .btn');
        if (alreadyTitle) alreadyTitle.innerText = t.alreadyMsgTitle;
        if (alreadyText) alreadyText.innerText = t.alreadyMsgText;
        if (alreadyBtn) alreadyBtn.innerText = t.modalBtn;

        populateSelects(lang);
    }

    // Inicializar idioma por defecto
    setLanguage(currentLang);
    initializeAuthorizationSections();

    // Escuchar cambio de idioma
    const langRadios = document.querySelectorAll('input[name="language"]');
    langRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            setLanguage(e.target.value);
            initializeAuthorizationSections();
        });
    });

    // --- Navegación ---
    function updateNavigation() {
        // Steps visibility
        steps.forEach((step, index) => {
            if (index + 1 === currentStep) {
                step.classList.add('active-step');
            } else {
                step.classList.remove('active-step');
            }
        });

        // Stepper UI
        stepperItems.forEach((item, index) => {
            const stepNum = index + 1;
            item.classList.remove('active', 'completed');
            if (stepNum < currentStep) {
                item.classList.add('completed');
            } else if (stepNum === currentStep) {
                item.classList.add('active');
            }
        });

        // Buttons
        if (currentStep === 1) {
            prevBtn.style.display = 'none';
        } else {
            prevBtn.style.display = 'flex';
        }

        if (currentStep === totalSteps) {
            nextBtn.style.display = 'none';
            submitBtn.style.display = 'flex';
        } else {
            nextBtn.style.display = 'flex';
            submitBtn.style.display = 'none';
        }
        
        // Reinitialize authorization sections when step 4 is shown
        if (currentStep === 4) {
            initializeAuthorizationSections();
        }
        
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function validateCurrentStep() {
        const currentStepEl = document.getElementById(`step-${currentStep}`);
        const inputs = currentStepEl.querySelectorAll('input[required], select[required], textarea[required]');
        
        let isValid = true;
        
        // Standard HTML5 validation for required fields
        inputs.forEach(input => {
            if (!input.checkValidity()) {
                input.reportValidity();
                isValid = false;
            }
        });
        
        // Custom validation for Step 3 (Address)
        if (currentStep === 3) {
            const calleinput = document.getElementById('direccion_calle');
            const numeroInput = document.getElementById('direccion_numero');
            const cpInput = document.getElementById('direccion_codigo_postal');
            const ciudadInput = document.getElementById('direccion_ciudad');
            
            // Validate postal code (basic pattern: at least 3 characters, alphanumeric)
            const cpValue = cpInput ? cpInput.value.trim() : '';
            if (cpValue && !/^[a-zA-Z0-9]{3,}$/.test(cpValue)) {
                cpInput.reportValidity();
                isValid = false;
            }
        }
        
        // Custom validation for Step 4 (Authorizations)
        if (currentStep === 4) {
            const wspCheckbox = document.getElementById('auth_whatsapp_checkbox');
            const imagenCheckbox = document.getElementById('auth_imagen_checkbox');
            const datosCheckbox = document.getElementById('auth_datos_checkbox');
            
            // Check if all authorizations are accepted
            if (!wspCheckbox || !wspCheckbox.checked) {
                alert(currentLang === 'es' ? 'Debe aceptar la autorización de WhatsApp' : 'You must accept the WhatsApp authorization');
                isValid = false;
            }
            if (!imagenCheckbox || !imagenCheckbox.checked) {
                alert(currentLang === 'es' ? 'Debe aceptar la protección de imagen' : 'You must accept the image protection');
                isValid = false;
            }
            if (!datosCheckbox || !datosCheckbox.checked) {
                alert(currentLang === 'es' ? 'Debe aceptar el tratamiento de datos' : 'You must accept the data processing');
                isValid = false;
            }
        }
        
        return isValid;
    }

    // Authorization sections toggling
    function initializeAuthorizationSections() {
        const authSections = document.querySelectorAll('.authorization-section');
        
        authSections.forEach(section => {
            const header = section.querySelector('.authorization-header');
            const content = section.querySelector('.authorization-content');
            const checkbox = section.querySelector('input[type="checkbox"]');
            const label = section.querySelector('.section-title');
            
            // Allow checkbox to be clicked independently to check/uncheck
            if (checkbox) {
                checkbox.disabled = false;
                checkbox.style.cursor = 'pointer';
                // Add click listener to checkbox to prevent toggle when clicking the checkbox itself
                checkbox.addEventListener('click', (e) => {
                    e.stopPropagation();
                });
            }
            
            // Toggle section expansion on label/icon click only (not checkbox)
            header.addEventListener('click', (e) => {
                // Only toggle if clicking on label or icon, not the checkbox
                if (e.target.closest('.section-title') || e.target.closest('.toggle-icon')) {
                    content.classList.toggle('collapsed');
                    header.classList.toggle('expanded');
                }
            });
        });
    }

    nextBtn.addEventListener('click', () => {
        if (validateCurrentStep()) {
            if (currentStep < totalSteps) {
                currentStep++;
                updateNavigation();
            }
        }
    });

    prevBtn.addEventListener('click', () => {
        if (currentStep > 1) {
            currentStep--;
            updateNavigation();
        }
    });

    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (validateCurrentStep()) {
            // Mostrar estado de carga en el botón
            const originalText = submitBtn.innerHTML;
            const t = i18n[currentLang];
            submitBtn.disabled = true;
            submitBtn.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${t.btnSending}`;

            const formData = new FormData(form);
            formData.append('lang', currentLang);

            // Convertir el código de país (ES, US...) al nombre completo para el correo
            const countryCode = formData.get('pais');
            const countryObj = countries.find(c => c.code === countryCode);
            if (countryObj) {
                formData.set('pais', currentLang === 'es' ? countryObj.name : countryObj.nameEn);
            }

            try {
                const response = await fetch('/api/inscripcion', {
                    method: 'POST',
                    body: formData
                });

                const result = await response.json();

                if (response.ok) {
                    // Guardar en caché
                    localStorage.setItem(STORAGE_KEY, Date.now().toString());
                    // Mostrar modal
                    document.getElementById('success-modal').style.display = 'flex';
                } else {
                    // Mostrar error (ej: Rate limit)
                    alert(result.message || (currentLang === 'es' ? 'Error al enviar la inscripción' : 'Error submitting inscription'));
                }
            } catch (error) {
                console.error('Error:', error);
                alert(currentLang === 'es' ? 'Error de conexión. Inténtalo de nuevo más tarde.' : 'Connection error. Try again later.');
            } finally {
                // Restaurar botón
                submitBtn.disabled = false;
                submitBtn.innerHTML = originalText;
            }
        }
    });

    // --- Compresión de Imágenes ---
    async function compressImage(file) {
        // Solo comprime imágenes mayores de 10MB
        if (!file.type.startsWith('image/') || file.size <= 10 * 1024 * 1024) return file;
        
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = event => {
                const img = new Image();
                img.src = event.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    // Reducir la resolución a la mitad para ahorrar peso
                    const scaleFactor = 0.5;
                    canvas.width = img.width * scaleFactor;
                    canvas.height = img.height * scaleFactor;
                    
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                    
                    canvas.toBlob((blob) => {
                        const newFile = new File([blob], file.name, {
                            type: 'image/jpeg',
                            lastModified: Date.now()
                        });
                        resolve(newFile);
                    }, 'image/jpeg', 0.8); // 80% de calidad
                };
            };
        });
    }

    async function handleFileCompression(event, previewContainerId) {
        const input = event.target;
        if (!input.files || input.files.length === 0) {
            renderPreviews(input, previewContainerId);
            return;
        }
        
        const dataTransfer = new DataTransfer();
        input.style.opacity = '0.5';
        
        for (let i = 0; i < input.files.length; i++) {
            const file = input.files[i];
            const compressedFile = await compressImage(file);
            dataTransfer.items.add(compressedFile);
        }
        
        input.files = dataTransfer.files;
        input.style.opacity = '1';
        renderPreviews(input, previewContainerId);
    }

    function removeFile(input, index, previewContainerId) {
        const dt = new DataTransfer();
        const { files } = input;
        for (let i = 0; i < files.length; i++) {
            if (i !== index) dt.items.add(files[i]);
        }
        input.files = dt.files;
        renderPreviews(input, previewContainerId);
    }

    function renderPreviews(input, previewContainerId) {
        const previewContainer = document.getElementById(previewContainerId);
        if (!previewContainer) return;
        previewContainer.innerHTML = '';
        
        Array.from(input.files).forEach((file, index) => {
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = (e) => {
                    const div = document.createElement('div');
                    div.className = 'preview-item';
                    
                    const img = document.createElement('img');
                    img.src = e.target.result;
                    img.className = 'preview-img';
                    
                    const btn = document.createElement('button');
                    btn.className = 'remove-btn';
                    btn.innerHTML = '&times;';
                    btn.type = 'button';
                    btn.onclick = () => removeFile(input, index, previewContainerId);
                    
                    div.appendChild(img);
                    div.appendChild(btn);
                    previewContainer.appendChild(div);
                };
                reader.readAsDataURL(file);
            }
        });
    }

    document.getElementById('foto_carnet').addEventListener('change', (e) => handleFileCompression(e, 'preview-foto'));
});
