package com.klb.app.application.service.demo;

public interface MultiThreadCounterDemoService {

	MultiThreadCounterDemoResult runCounterRaceDemo(int threads, int incrementsPerThread);
}
