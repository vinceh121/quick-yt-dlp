import { Route } from "wouter";
import "./app.css";
import { StartDownload } from "./StartDownload";
import { CurrentDownload } from "./CurrentDownload";

export function App() {
	return (
		<>
			<header>
				<h3>Quick yt-dlp</h3>
			</header>
			<main>
				<Route path="/" component={StartDownload} />
				<Route path="/download/:downloadId" component={CurrentDownload} />
			</main>
		</>
	);
}
