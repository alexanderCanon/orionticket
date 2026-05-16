# Objetivo Final

Implementar una seguridad consistente para OrionTicket donde:

Identity emite JWT firmados con clave privada
Gateway valida JWT con JWKS
Microservicios validan JWT o reciben contexto autenticado confiable
Cada endpoint aplica autorización por rol y ownership

El resultado esperado es que ningún servicio dependa de IDs hardcodeados ni de confianza implícita. Todo request
protegido debe saber:

- quién es el usuario
- qué rol tiene
- a qué organizer pertenece
- qué permisos tiene
- qué datos puede tocar

## Fase 1: Definir Contrato De Seguridad

Objetivo: dejar claro qué claims tendrá el token y cómo los servicios lo interpretan.

### Pasos:

1. Definir claims mínimos del JWT:
      - sub: userId
      - email
      - roleId
      - role
      - permissions
      - organizerId
      - iss
      - iat
      - exp
  2. Definir roles oficiales desde actor-role-map.md:
      - BUYER
      - ORGANIZER
      - DOOR_VALIDATOR
      - VENUE_STAFF
      - SUPPORT
      - FINANCE
      - MARKETING
      - PLATFORM_OPERATOR
      - SUPER_ADMIN
  3. Definir permisos por rol:
      - Ejemplo: events:create, events:approve, tickets:read, validations:create.
  4. Documentar la decisión en un ADR:
      - JWT asimétrico.
      - JWKS expuesto por Identity.
      - Gateway y microservicios como Resource Server.

  Entregable: ADR + contrato de claims.

  Fase 2: Identity Como Emisor De Tokens

  Objetivo: convertir Identity en el emisor oficial de JWT firmados con RSA.

  Pasos:

  1. Agregar configuración:
      - JWT_PRIVATE_KEY
      - JWT_PUBLIC_KEY
      - JWT_KEY_ID
      - JWT_ISSUER
      - JWT_EXPIRATION
  2. Cambiar JwtProviderAdapter:
      - dejar de firmar con JWT_SECRET
      - firmar con clave privada RSA
      - agregar kid en el header
      - agregar iss
      - agregar role, roleId, permissions, organizerId
  3. Crear endpoint:

  GET /.well-known/jwks.json

  4. Hacer que login devuelva:
      - accessToken
      - tokenType
      - expiresIn
      - userId
      - role
      - organizerId
  5. Agregar tests:
      - login genera JWT válido
      - JWKS expone clave pública
      - token puede validarse con JWKS

  Entregable: Identity emite tokens verificables públicamente.

  Fase 3: Gateway Como Primer Filtro De Seguridad

  Objetivo: que el API Gateway valide tokens antes de enrutar.

  Pasos:

  1. Agregar Spring Security OAuth2 Resource Server al gateway.
  2. Configurar:

  spring:
    security:
      oauth2:
        resourceserver:
          jwt:
            jwk-set-uri: ${IDENTITY_JWKS_URI:http://identity-service:8081/.well-known/jwks.json}

  3. Permitir rutas públicas:
      - /v1/auth/**
      - /actuator/health
      - Swagger si aplica
  4. Proteger todo lo demás.
  5. Convertir claims role y permissions en authorities Spring:
      - ROLE_BUYER
      - ROLE_ORGANIZER
      - events:create
      - etc.
  6. Opcional: reenviar headers internos:
      - X-User-Id
      - X-Role
      - X-Organizer-Id

  Entregable: Gateway rechaza requests sin token o con token inválido.

  Fase 4: Microservicios Como Resource Servers

  Objetivo: que cada microservicio pueda validar el JWT y aplicar autorización local.

  Pasos:

  1. Agregar OAuth2 Resource Server en servicios protegidos:
      - Event Management
      - Seating / Inventory
      - Orders
      - Payments
      - Ticket Issuance
      - Access Control
      - Reporting
      - Notifications si expone endpoints REST internos
  2. Configurar jwk-set-uri en cada servicio.
  3. Crear configuración común:
      - converter de JWT claims a authorities
      - extracción de userId, role, organizerId
  4. Reemplazar IDs temporales hardcodeados:
      - TEMPORARY_ORGANIZER_ID
      - TEMPORARY_ADMIN_ID
      - cualquier operatorId fijo
  5. Pasar el usuario autenticado desde SecurityContext.

  Entregable: los servicios ya no confían en IDs falsos ni requests anónimos para operaciones protegidas.

  Fase 5: Autorización Por Rol

  Objetivo: aplicar reglas por actor según actor-role-map.md.

  Pasos:

  1. Activar method security:

  @EnableMethodSecurity

  2. Proteger endpoints con:
      - @PreAuthorize("hasRole('ORGANIZER')")
      - @PreAuthorize("hasAuthority('events:approve')")
  3. Aplicar reglas base:
      - Buyer: comprar, ver propios tickets y orders.
      - Organizer: gestionar sus propios eventos.
      - Door Validator: validar tickets.
      - Platform Operator: aprobar/rechazar eventos.
      - Finance: ver pagos/reportes.
      - Support: reenviar tickets/ver soporte.
      - Super Admin: todo.
  4. Mantener endpoints públicos mínimos:
      - login
      - registro
      - catálogo público
      - health
      - swagger en local

  Entregable: endpoints devuelven 401 sin token y 403 con rol incorrecto.

  Fase 6: Ownership Checks

  Objetivo: evitar que un usuario autenticado acceda a datos de otro usuario u organizer.

  Pasos:

  1. Crear políticas por servicio:
      - Event Management: event.organizerId == token.organizerId
      - Orders: order.buyerId == token.sub
      - Payments: payment.buyerId == token.sub o rol interno.
      - Ticket Issuance: ticket.buyerId == token.sub
      - Reporting: organizer solo ve su scope.
      - Access Control: validator pertenece al organizer/evento.
  2. Implementar checks en application layer, no en controllers.
  3. Agregar errores 403 Forbidden consistentes.
  4. Agregar tests de acceso cruzado.

  Entregable: multi-tenancy seguro por organizerId y userId.

  Fase 7: Contratos Y Diagramas

  Objetivo: alinear documentación con la implementación.

  Pasos:

  1. Actualizar system-flow-diagrams.md con la decisión final JWKS.
  2. Crear o actualizar ADR:
      - ADR-018-jwt-jwks-security.md
  3. Actualizar service-contracts.md:
      - rutas públicas
      - rutas protegidas
      - roles requeridos
      - errores 401/403
  4. Actualizar .env.example:
      - IDENTITY_JWKS_URI
      - JWT_PRIVATE_KEY
      - JWT_PUBLIC_KEY
      - JWT_KEY_ID
      - JWT_ISSUER

  Entregable: documentación y código dicen lo mismo.

  Fase 8: Verificación Local

  Objetivo: probar seguridad de forma incremental antes de levantar todo.

  Pasos:

  1. Levantar RabbitMQ + Identity + Gateway.
  2. Probar login.
  3. Validar token contra JWKS.
  4. Probar request protegido vía Gateway sin token: debe dar 401.
  5. Probar request protegido con token inválido: debe dar 401.
  6. Probar request con rol incorrecto: debe dar 403.
  7. Probar request con ownership incorrecto: debe dar 403.
  8. Repetir servicio por servicio.

  Entregable: matriz de seguridad local por microservicio.

  Orden Recomendado De Implementación

  1. ADR + contrato de claims
  2. Identity firma RSA + JWKS
  3. Gateway valida JWT
  4. Event Management protegido
  5. Payments protegido
  6. Ticket Issuance protegido
  7. Access Control protegido
  8. Seating / Inventory protegido
  9. Reporting protegido
  10. Notifications si expone API REST
  11. Orders cuando entre a develop

  Meta Del MVP

  Para el MVP no hace falta implementar refresh tokens, revocación, sesiones distribuidas o rotación automática de claves
  todavía.

  La meta realista es:

  JWT asimétrico funcional
  Gateway validando tokens
  Microservicios aplicando rol + ownership
  401/403 consistentes
  Documentación alineada