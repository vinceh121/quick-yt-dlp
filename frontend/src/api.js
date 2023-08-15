export const startDownload = async (url, audioOnly) => {
	const res = await fetch("/api/v1/download", {
		method: "POST",
		body: JSON.stringify({ url, audioOnly }),
	});

	if (res.status !== 200) {
		throw new Error("Unexpected status code: " + res.status);
	}

	return await res.json();
};

export const liveDownload = async (downloadId) => {
	let proto;
	switch (window.location.protocol) {
		case "http:": proto = "ws://"; break;
		case "https:": proto = "wss://"; break;
	}

	const ws = new WebSocket(
		proto + window.location.host + "/api/v1/download/" + downloadId + "/live"
	);
	return ws;
};

export const fetchDownloadJob = async (downloadId) => {
	const res = await fetch("/api/v1/download/" + downloadId);

	if (res.status !== 200 && res.status !== 206) {
		throw new Error("Unexpeted status code: " + res.status);
	}

	return await res.json();
};
