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
