import i18next from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import en from "./i18n/en.json";
import fr from "./i18n/fr.json";

const strings = {
	en: {
		translation: en,
	},
	fr: {
		translation: fr,
	},
};

i18next.use(LanguageDetector).init({ fallbackLng: "en", resources: strings });

export default i18next;
