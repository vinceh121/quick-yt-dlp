import { Loader } from "./Loader";
import { fetchDownloadJob, liveDownload } from "./api";
import i18n from "./i18n";
import { useEffect, useState } from "react";
import { Alert, Box, Button, ButtonGroup, Card, CardActions, CardContent, CardMedia, Container, Grid, IconButton, LinearProgress, Paper, Slide, Snackbar, Stack, Typography } from "@mui/material";
import { Download } from "@mui/icons-material";
import { DownloadDone } from "@mui/icons-material";
import { Downloading } from "@mui/icons-material";
import { Speed } from "@mui/icons-material";
import numeral from "numeral";

function DownloadEntry({ state }) {
	return (
		<Slide direction="right" in mountOnEnter unmountOnExit>
			<Card sx={{ display: "flex" }}>
				{state.videoThumbnail ?
					<CardMedia sx={{ width: 151 }} component="img" alt={state.videoTitle} image={state.videoThumbnail} />
					: undefined}
				<Box sx={{ flexDirection: "column", width: "100%" }}>
					<Box sx={{ width: "100%" }}>
						{
							state.progress === "NaN" && state.status !== "finished"
								? <LinearProgress variant="indeterminate" />
								: <LinearProgress
									variant="determinate"
									color={state.status === "finished" ? "success" : "primary"}
									value={state.progress === "NaN" ? 100 : state.progress * 100} />
						}
					</Box>
					<Box sx={{ display: 'flex', flexDirection: 'column' }}>
						<CardContent>
							<Box sx={{ display: "flex", flexDirection: "row", }}>
								<Typography variant="h4" gutterBottom>
									{state.videoTitle}&nbsp;{
										state.playlistIndex !== -1
											? <Typography component="span" variant="caption">#{state.playlistIndex}</Typography>
											: undefined}
								</Typography>
							</Box>
							<Grid container spacing={1}>
								<Grid item>
									<Downloading />
								</Grid>
								<Grid item>
									<Typography variant="caption">{numeral(state.downloadedBytes).format("0.00ib")} / {numeral(state.totalBytes).format("0.00ib")}</Typography>
								</Grid>
							</Grid>
							<Grid container spacing={1}>
								<Grid item>
									<Speed />
								</Grid>
								<Grid item>
									<Typography variant="caption">{numeral(state.speed).format("0.00ib")}/s</Typography>
								</Grid>
							</Grid>
						</CardContent>
						<CardActions>
							{state.downloadPath && state.status === "finished" ?
								<IconButton size="small" color="primary" variant="contained" target="_blank" href={state.downloadPath}>
									<Download />
								</IconButton>
								: undefined}
						</CardActions>
					</Box>
				</Box>
			</Card >
		</Slide >
	);
}

const stateSort = (a, b) => {
	if (a.status === "downloading") {
		return -1;
	} else if (b.status === "downloading") {
		return 1;
	}

	return a.playlistIndex - b.playlistIndex;
}

export function CurrentDownload(params) {
	const [state, setState] = useState({});
	const [lastEvent, setLastEvent] = useState(undefined);
	const [downloadPath, setDownloadPath] = useState(undefined);
	const [error, setError] = useState(undefined);

	const onSnackClose = () => {
		setError(undefined);
	};

	useEffect(() => {
		liveDownload(params.params.downloadId).then(ws => {
			ws.onmessage = ({ data }) => {
				data = JSON.parse(data);

				if (data.eventType === "PROGRESS") {
					setState(prev => Object.assign({}, prev, { [data.videoId]: data }));
					setLastEvent(data);
				} else if (data.eventType === "FINISHED") {
					setDownloadPath(data.downloadPath);
				}
			};

			ws.onerror = setError;
			ws.onclose = setError;
		});

		fetchDownloadJob(params.params.downloadId).then(res => {
			setState(res.state);

			const keys = Object.keys(res.state);

			if (res.downloadPath) {
				setDownloadPath(res.downloadPath);
			} else if (keys.length === 1 && res.state[keys[0]].downloadPath) {
				setDownloadPath(res.state[keys[0]].downloadPath);
			}
		}, setError);
	}, [params.params.downloadId]);

	return <>
		<Stack spacing={2}>
			{
				Object.keys(state).length === 0
					? <Loader />
					: <Paper sx={{ padding: "1rem" }}>
						{!downloadPath
							?
							!lastEvent || lastEvent.playlistIndex === -1 || lastEvent.playlistCount === -1
								? <LinearProgress color="secondary" variant="indeterminate" />
								: <>
									<LinearProgress color="secondary" variant="determinate" value={lastEvent.playlistIndex / lastEvent.playlistCount * 100} />
									<Typography variant="body1">{i18n.t("totalProgress", { index: lastEvent.playlistIndex, count: lastEvent.playlistCount })}</Typography>
								</>
							: <>
								<Typography variant="h3"><DownloadDone color="success" fontSize="large" />{i18n.t("downloadFinishedTitle")}</Typography>
								<Button size="large" variant="contained" component="a" target="_blank" href={downloadPath} startIcon={<Download />}>{i18n.t("downloadButton")}</Button>
							</>
						}
					</Paper>
			}

			{Object.values(state).sort(stateSort).map(s => <DownloadEntry key={s.videoId} state={s} />)}
		</Stack>
		<Snackbar open={error} autoHideDuration={10000} onClose={onSnackClose}>
			<Alert onClose={onSnackClose} severity="error">{String(error)}</Alert>
		</Snackbar>
	</>;
}
