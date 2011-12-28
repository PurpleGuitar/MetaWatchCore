package org.metawatch.manager.core.packets;

public enum WatchButtonMeaning {
	DISMISS_NOTIFICATION((byte) 0x01);
	public final byte	value;

	WatchButtonMeaning(byte value) {
		this.value = value;
	}

	public static WatchButtonMeaning getByValue(byte value) {
		for (WatchButtonMeaning mode : WatchButtonMeaning.values()) {
			if (mode.value == value) {
				return mode;
			}
		}
		return null;
	}
}
