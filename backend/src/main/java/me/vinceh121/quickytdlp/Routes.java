package me.vinceh121.quickytdlp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import me.vinceh121.quickytdlp.event.ProgressEvent;

public class Routes {
	private final QuickYtDlp main;

	public Routes(QuickYtDlp main) {
		this.main = main;

		this.main.getApiRouter()
				.post("/download")
				.handler(BodyHandler.create(false))
				.handler(this::handleDownloadStart);
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

		this.main.getWorkers().put(worker.getId(), worker);
		this.main.getWorkerPool().executeBlocking(worker, false).onFailure(Throwable::printStackTrace);

		ctx.json(new JsonObject().put("downloadId", worker.getId()));
	}

	private void handleDownloadLive(final RoutingContext ctx) {
		final UUID downloadId = UUID.fromString(ctx.pathParam("downloadId"));

		ctx.request().toWebSocket().onSuccess(ws -> {
			this.main.getVertx()
					.eventBus()
					.<ProgressEvent>consumer(DownloadWorker.EVENT_BUS_PREFIX + downloadId)
					.handler(msg -> {
						final ProgressEvent evt = msg.body();
						ws.writeTextMessage(JsonObject.mapFrom(evt).encode());
					});
		}).onFailure(ctx::fail);
	}
}
