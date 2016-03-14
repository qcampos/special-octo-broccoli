README
=======

Organisation des sources
------------------------------

Le projet est composé de deux parties principales: 

* le back-end développé en Django stocké dans le dossier spike
* l'application en elle même stockée dans le dossier android-client


Import du projet dans Android Studio
--------------------------------------------

Afin de pouvoir compiler le projet et le déployer sur votre téléphone, vous aurez besoin du keystore (nommé debug.keystore) situé à la racine du dossier android-client. Remplacez votre propre debug.keystore dans le dossier `~/.android` par celui du dépôt.

Si vous ne le faites pas, vous ne pourrez pas correctement utiliser l'application car vous n'aurez pas les droits nécessaires pour utiliser l'API Google Play (utilisée pour afficher une carte et gérer les positions de l'utilisateur).

Depuis android studio, ouvrez le projet  `./android-client/SecurityNotification`, vous devriez alors être capable de compiler l'application et de la déployer sur votre téléphone.
