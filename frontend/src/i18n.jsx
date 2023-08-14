import i18next from "i18next";

const strings = {
	en: {
		translation: {
			startDownload: "Start download",
			urlLabel: "Video/Audio URL",
			audioOnlyLabel: "Audio only",
		},
	},
	fr: {
		translation: {
			startDownload: "Démarrer téléchargement",
			urlLabel: "URL de Vidéo/Audio",
			audioOnlyLabel: "Audio seulement",
		},
	},
};

i18next.init({ fallbackLng: "en", resources: strings });

export default i18next;
