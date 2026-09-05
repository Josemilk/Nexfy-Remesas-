# Instrucciones para el Agente (NexFy App)

**Regla 1: Cero datos de prueba (mocks).**
Todo debe provenir 100% de Firebase/Firestore y APIs reales. Está terminantemente prohibido utilizar listas estáticas (`listOf(...)`), generadores de datos aleatorios, o información "dummy" para poblar la interfaz gráfica o la base de datos local Room. Todo flujo de datos debe estar conectado a la nube y reflejar la información real del servidor.

**Regla 2: Cualquier nueva funcionalidad debe conectarse directamente a la nube.**
Al desarrollar nuevas vistas, módulos o características, no construyas soluciones exclusivamente locales de forma aislada a menos que sean estrictamente temporales (cache). Cualquier nueva tabla, historial, estadística, trabajador o envío debe sincronizarse con Firebase.

**Regla 3: Integración Limpia.**
Asegurar que todas las llamadas asíncronas y operaciones de red manejen adecuadamente los estados (Loading, Success, Error) mediante Flujos (`Flow` / `StateFlow`) y no enmascaren los errores con datos falsos por defecto.
