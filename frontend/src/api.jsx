export const startDownload = async (url, audioOnly) => {
	const res = await fetch("/api/v1/download", {
		method: "POST",
		body: JSON.stringify({ url, audioOnly }),
	});

	if (res.status !== 200) {
		throw new Error("Unexpected error: " + res.status);
	}

	return await res.json();
};

export const liveDownload = async (downloadId) => {
	const ws = new WebSocket(
		"ws://" + window.location.host + "/api/v1/download/" + downloadId + "/live"
	);
	return ws;
};
