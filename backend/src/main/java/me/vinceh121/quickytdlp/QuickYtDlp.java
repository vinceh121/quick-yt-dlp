package me.vinceh121.quickytdlp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;

public class QuickYtDlp {
	private static final ObjectMapper MAPPER = new ObjectMapper();
	private final Vertx vertx;
	private final HttpServer server;
	private final Router rootRouter, apiRouter;
	private final WorkerExecutor workerPool;
	private final Map<UUID, DownloadWorker> workers = new HashMap<>();
	private final Config config;

	public static void main(final String[] args) throws IOException {
		final QuickYtDlp qytdlp = new QuickYtDlp();
		qytdlp.start();
	}

	public QuickYtDlp() throws IOException {
		this.vertx = Vertx.vertx();
		this.server = this.vertx.createHttpServer();

		this.rootRouter = Router.router(this.vertx);
		this.apiRouter = Router.router(this.vertx);

		this.server.requestHandler(this.rootRouter);
		this.rootRouter.route("/api/v1/*").subRouter(this.apiRouter);

		this.config = MAPPER.readValue(new File("/etc/quick-yt-dlp.json"), Config.class);

		this.workerPool = this.vertx.createSharedWorkerExecutor("yt-dlp-instances", this.config.getWorkerInstances(),
				10, TimeUnit.MINUTES);

		new Routes(this);
	}

	public void start() {
		this.server.listen(this.config.getPort(), this.config.getListenAddress());
	}

	public Config getConfig() {
		return this.config;
	}

	public WorkerExecutor getWorkerPool() {
		return this.workerPool;
	}

	public Router getApiRouter() {
		return this.apiRouter;
	}

	public Map<UUID, DownloadWorker> getWorkers() {
		return this.workers;
	}

	public Vertx getVertx() {
		return this.vertx;
	}
}
