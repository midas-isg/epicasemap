package impl;

import interactors.ConfRule;
import gateways.configuration.ConfReader;

public class Factory {
	private Factory() {
	}

	public static ConfRule makeConfRule() {
		return new ConfRule(makeConfReader());
	}

	private static ConfReader makeConfReader() {
		return new ConfReader();
	}
}

