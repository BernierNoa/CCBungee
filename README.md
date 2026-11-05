# CCBungee

Plugin Java pour le proxy BungeeCord du réseau La Garderie. Il enregistre le canal `CCChannel`, suit les connexions et le statut des serveurs, et fournit les commandes de messages privés.

## Développement

- `src/fr/nozkay/Main.java` : initialisation du plugin et enregistrement des listeners et commandes.
- `src/fr/nozkay/listeners/` : connexions et suivi des serveurs.
- `src/plugin.yml` : métadonnées du plugin.
- `CCBungee.iml` : module IntelliJ avec une bibliothèque `BungeeCord` externe.

Aucun build Maven/Gradle ni JAR de dépendance n'est fourni. Importer les sources dans l'IDE avec l'API BungeeCord avant de préparer un JAR pour le proxy. La compilation et le chargement sur un proxy n'ont pas été exécutés ; aucun artefact prêt à installer n'est garanti.
