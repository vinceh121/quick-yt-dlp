import "./app.css";
import { Route } from "wouter";
import { StartDownload } from "./StartDownload";
import { CurrentDownload } from "./CurrentDownload";
import { AppBar, Container, Toolbar, Typography } from "@mui/material";

export function App() {
	return (
		<>
			<AppBar position="static">
				<Container>
					<Toolbar>
						<Typography variant="h6" noWrap component="a" href="/" sx={{
							mr: 2,
							display: { xs: 'none', md: 'flex' },
							fontFamily: 'monospace',
							color: 'inherit',
							textDecoration: 'none',
						}}>Quick yt-dlp</Typography>
					</Toolbar>
				</Container>
			</AppBar>
			<main>
				<Route path="/" component={StartDownload} />
				<Route path="/download/:downloadId" component={CurrentDownload} />
			</main>
		</>
	);
}
