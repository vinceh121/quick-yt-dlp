import "./currentdownload.css";
import { Loader } from "./Loader";
import { liveDownload } from "./api";
import { useEffect, useState } from "preact/hooks";

function DownloadEntry({ state }) {
	return (
		<div class="downloadCard">
			<img alt={state.videoTitle} src={state.videoThumbnail} />
			<h4>{state.videoTitle} <span class="playlistIndex">#{state.playlistIndex}</span></h4>
		</div>
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

	useEffect(() => {
		liveDownload(downloadId).then(ws => {
			ws.onmessage = ({ data }) => {
				data = JSON.parse(data);

				if (data.eventType === "PROGRESS") {
					setState(prev => Object.assign({}, prev, { [data.videoId]: data }));
				}
			};
		});
	}, [downloadId]);

	console.log(state);

	return <>
		{Object.keys(state).length === 0 ? <Loader /> : undefined}
		{Object.values(state).sort(stateSort).map(s => <DownloadEntry key={s.videoId} state={s} />)}
	</>;
}
