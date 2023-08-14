import { useLocation } from "wouter";
import { startDownload } from "./api";
import i18n from "./i18n";
import { Button, FormControlLabel, FormGroup, TextField, Checkbox, Paper } from "@mui/material";
import { Download } from "@mui/icons-material";

export function StartDownload() {
	const [_, navigate] = useLocation();

	const onSubmit = (e) => {
		e.preventDefault();

		const form = e.target;
		const data = Object.fromEntries(new FormData(form).entries());
		console.log(data);

		startDownload(data.url, Boolean(data.audioOnly)).then((res) => {
			navigate("/download/" + res.downloadId);
		});
	};

	return (
		<Paper sx={{ padding: "1rem" }}>
			<form id="startDownloadForm" onSubmit={onSubmit}>
				<div>
					<TextField fullWidth required type="url" name="url" label={i18n.t("urlLabel")} />
				</div>

				<div>
					<FormGroup>
						<FormControlLabel control={<Checkbox name="audioOnly" />} label={i18n.t("audioOnlyLabel")} />
					</FormGroup>
				</div>

				<div>
					<Button variant="contained" type="submit" startIcon={<Download />}>{i18n.t("startDownload")}</Button>
				</div>
			</form>
		</Paper>
	);
}
