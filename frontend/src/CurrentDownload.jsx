import { Loader } from "./Loader";
import { liveDownload } from "./api";
import i18n from "./i18n";
import { useEffect, useState } from "react";
import { Box, Button, Card, CardContent, CardMedia, LinearProgress, Paper, Slide, Stack, Typography } from "@mui/material";
import { Download } from "@mui/icons-material";
import { DownloadDone } from "@mui/icons-material";

function DownloadEntry({ state }) {
	return (
		<Slide direction="right" in mountOnEnter unmountOnExit>
			<Card sx={{ display: "flex" }}>
				<CardMedia sx={{ width: 151 }} component="img" alt={state.videoTitle} image={state.videoThumbnail} />
				<Box sx={{ flexDirection: "column", width: "100%" }}>
					<Box sx={{ width: "100%" }}>
						{
							state.progress === "NaN" && state.status !== "finished"
								? <LinearProgress variant="indeterminate" />
								: <LinearProgress
									variant="determinate"
									color={state.status === "finished" ? "success" : "primary"}
									value={state.progress === "NaN" ? 100 : 100 - state.progress} />
						}
					</Box>
					<Box sx={{ display: 'flex', flexDirection: 'column' }}>
						<CardContent>
							<Typography variant="h4" gutterBottom>
								{state.videoTitle}&nbsp;<Typography component="span" variant="caption">#{state.playlistIndex}</Typography>
							</Typography>
						</CardContent>
					</Box>
				</Box>
			</Card>
		</Slide>
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

export function CurrentDownload({ params: { downloadId } }) {
	const [state, setState] = useState({});
	const [lastEvent, setLastEvent] = useState(undefined);
	const [downloadPath, setDownloadPath] = useState(undefined);

	useEffect(() => {
		liveDownload(downloadId).then(ws => {
			ws.onmessage = ({ data }) => {
				data = JSON.parse(data);
				console.log(data);

				if (data.eventType === "PROGRESS") {
					setState(prev => Object.assign({}, prev, { [data.videoId]: data }));
					setLastEvent(data);
				} else if (data.eventType === "FINISHED") {
					setDownloadPath(data.downloadPath);
				}
			};
		});
	}, [downloadId]);

	return <>
		<Stack spacing={2}>
			{
				Object.keys(state).length === 0
					? <Loader />
					: <Paper sx={{ padding: "1rem" }}>
						{!downloadPath ? <>
							<LinearProgress color="secondary" variant="determinate" value={lastEvent.playlistIndex / lastEvent.playlistCount * 100} />
							<Typography variant="body1">{i18n.t("totalProgress", { index: lastEvent.playlistIndex, count: lastEvent.playlistCount })}</Typography>
						</>
							: <>
								<Typography variant="h3"><DownloadDone color="success" fontSize="large" />{i18n.t("downloadFinishedTitle")}</Typography>
								<Button variant="contained" component="a" target="_blank" href={downloadPath} startIcon={<Download />}>{i18n.t("downloadButton")}</Button>
							</>
						}
					</Paper>
			}

			{Object.values(state).sort(stateSort).map(s => <DownloadEntry key={s.videoId} state={s} />)}
		</Stack>
	</>;
}
