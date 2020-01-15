# dialogflowbot [![Release](https://img.shields.io/github/release/orhun/dialogflowbot.svg?style=flat-square)](https://github.com/orhun/dialogflowbot/releases)

[![License](https://img.shields.io/github/license/orhun/dialogflowbot.svg?color=blue&style=flat-square)](./LICENSE)

## Google's Dialogflow implementation with Speech Recognition and Text-to-Speech.

It's an Android application that aims to continuously listen to environment for recognizing speech and responding with the Dialogflow results converted to speech.

## Application Flowchart

![Flowchart](https://user-images.githubusercontent.com/24392180/63889729-3e85d800-c9ea-11e9-9752-5932151b6226.png)

* Dialogflow has the automatic TTS feature but it's not used for gaining control over text responses. 

## Build Instructions

* Navigate to [Dialogflow Console](https://console.dialogflow.com). (Create your agent and intents if it doesn't exist.)
* Go to general settings and select your service account mail. It will redirect you to the Google Cloud.

![Service Account](https://user-images.githubusercontent.com/24392180/63890353-7a6d6d00-c9eb-11e9-8419-a29309aa4a52.png)

* Select your project mail with the `Dialogflow Integrations` name and create new JSON key at the below.

![Google Cloud Service Account](https://user-images.githubusercontent.com/24392180/63891074-0c29aa00-c9ed-11e9-92e4-8264e3baa1ee.png)

![Create Private Key](https://user-images.githubusercontent.com/24392180/63890583-fbc4ff80-c9eb-11e9-9c9e-e61f4014b647.png)

* Save JSON key file to `/app/src/main/res/raw/dialogflow_credentials.json`

* Build!

## License

GNU General Public License ([v3](https://www.gnu.org/licenses/gpl.txt))

## Copyright

Copyright (c) 2019-2020, [orhun](https://www.github.com/orhun)