package de.bsvrz.sys.startstopp.process;

public class ManagerStatus {
	public enum State {
		INITIALIZED,
		STARTING,
		RUNNING,
		STOPPING,
		STOPPED
	}
	
	private State state = State.INITIALIZED;

	public State getState() {
		synchronized (state) {
			return state;
		}
	}

	public void setState(State state) {
		synchronized (this.state) {
			this.state = state;
		}
	}
}
