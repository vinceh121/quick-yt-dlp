import i18next from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";

const strings = {
	en: {
		translation: {
			startDownload: "Start download",
			urlLabel: "Video/Audio URL",
			audioOnlyLabel: "Audio only",
			totalProgress: "Downloading {{index}} / {{count}}...",
			downloadButton: "Download"
		},
	},
	fr: {
		translation: {
			startDownload: "Démarrer téléchargement",
			urlLabel: "URL de Vidéo/Audio",
			audioOnlyLabel: "Audio seulement",
			totalProgress: "Téléchargement de {{index}} / {{count}}...",
			downloadButton: "Télécharger"
		},
	},
};

i18next.use(LanguageDetector).init({ fallbackLng: "en", resources: strings });

export default i18next;
