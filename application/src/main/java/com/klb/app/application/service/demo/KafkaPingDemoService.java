package com.klb.app.application.service.demo;

public interface KafkaPingDemoService {

	KafkaPingResult sendPing();

	/** Gui len topic {@code demo.echo} — de test listener #2. */
	KafkaPingResult sendEcho();

	/** Payload co dlqDemo — consumer #1 throw → retry → topic {@code demo.ping.DLT}. */
	KafkaPingResult sendPingDlqDemo();
}
