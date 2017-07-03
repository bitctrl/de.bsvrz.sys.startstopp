package de.bsvrz.sys.startstopp.process;

public class ManagerStatus {
	public enum State {
		INITIALIZED,
		RUNNING,
		STARTING,
		STARTED,
		STOPPING,
		STOPPED
	}
	
	private State state = State.INITIALIZED;

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}
	
}
