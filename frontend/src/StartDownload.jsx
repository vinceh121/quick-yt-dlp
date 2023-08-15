import { useLocation } from "wouter";
import { startDownload } from "./api";
import i18n from "./i18n";
import { Button, FormControlLabel, FormGroup, TextField, Checkbox, Paper, Snackbar, Alert } from "@mui/material";
import { Download } from "@mui/icons-material";
import { useState } from "react";

export function StartDownload() {
	const [_, navigate] = useLocation();
	const [error, setError] = useState(undefined);

	const onSnackClose = () => {
		setError(undefined);
	};

	const onSubmit = (e) => {
		e.preventDefault();

		const form = e.target;
		const data = Object.fromEntries(new FormData(form).entries());

		startDownload(data.url, Boolean(data.audioOnly)).then((res) => {
			navigate("/download/" + res.downloadId);
		}, setError);
	};

	return (
		<>
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
			<Snackbar open={error} autoHideDuration={10000} onClose={onSnackClose}>
				<Alert onClose={onSnackClose} severity="error">{String(error)}</Alert>
			</Snackbar>
		</>
	);
}
