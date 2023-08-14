import { useLocation } from "wouter";
import { startDownload } from "./api";
import i18n from "./i18n";

export function StartDownload() {
	const [_, navigate] = useLocation();

	const onSubmit = (e) => {
		e.preventDefault();

		const form = e.target;
		const data = Object.fromEntries(new FormData(form).entries());

		startDownload(data.url, Boolean(data.audioOnly)).then((res) => {
			navigate("/download/" + res.downloadId);
		});
	};

	return (
		<form id="startDownloadForm" onSubmit={onSubmit}>
			<div>
				<label for="url">{i18n.t("urlLabel")}</label>
				<input id="url" name="url" type="url" />
			</div>

			<div>
				<label for="audioOnly">{i18n.t("audioOnlyLabel")}</label>
				<input id="audioOnly" name="audioOnly" type="checkbox" />
			</div>

			<div>
				<input class="button" type="submit" value={i18n.t("startDownload")} />
			</div>
		</form>
	);
}
