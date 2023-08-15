package me.vinceh121.quickytdlp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import me.vinceh121.quickytdlp.event.IEvent;

public class Routes {
	private static final Logger LOG = LogManager.getLogger(Routes.class);
	private final QuickYtDlp main;

	public Routes(QuickYtDlp main) {
		this.main = main;

		this.main.getApiRouter()
				.post("/download")
				.handler(BodyHandler.create(false))
				.handler(this::handleDownloadStart);
		this.main.getApiRouter().get("/download/:downloadId").handler(this::handleGetJob);
		this.main.getApiRouter().get("/download/:downloadId/live").handler(this::handleDownloadLive);
	}

	private void handleDownloadStart(final RoutingContext ctx) {
		final JsonObject body = ctx.body().asJsonObject();

		final URL url;
		try {
			url = new URL(body.getString("url"));
		} catch (MalformedURLException e) {
			ctx.fail(400, e);
			return;
		}

		final boolean audioOnly = body.getBoolean("audioOnly", false);

		final DownloadWorker worker = new DownloadWorker(this.main);
		worker.setUrl(url);
		worker.setAudioOnly(audioOnly);

		LOG.info("{} started download of {} as job {}", ctx.request().getHeader("X-Forwarded-For"), url,
				worker.getId());

		this.main.getWorkers().put(worker.getId(), worker);
		this.main.getWorkerPool().executeBlocking(worker, false).onFailure(Throwable::printStackTrace);

		ctx.json(new JsonObject().put("downloadId", worker.getId()));
	}

	private void handleGetJob(final RoutingContext ctx) {
		final UUID downloadId = UUID.fromString(ctx.pathParam("downloadId"));

		final DownloadWorker worker = this.main.getWorkers().get(downloadId);

		final JsonObject res = new JsonObject();

		res.put("state", worker.getState());

		if (worker.getDownloadPath() == null) { // download is in progress
			ctx.response().setStatusCode(206);
		} else { // download is finished
			ctx.response().setStatusCode(200);
			res.put("downloadPath", worker.getDownloadPath());
		}

		ctx.json(res);
	}

	private void handleDownloadLive(final RoutingContext ctx) {
		final UUID downloadId = UUID.fromString(ctx.pathParam("downloadId"));

		ctx.request().toWebSocket().onSuccess(ws -> {
			this.main.getVertx()
					.eventBus()
					.<IEvent>consumer(DownloadWorker.EVENT_BUS_PREFIX + downloadId)
					.handler(msg -> {
						final IEvent evt = msg.body();
						ws.writeTextMessage(JsonObject.mapFrom(evt).encode());
					});
		}).onFailure(ctx::fail);
	}
}
