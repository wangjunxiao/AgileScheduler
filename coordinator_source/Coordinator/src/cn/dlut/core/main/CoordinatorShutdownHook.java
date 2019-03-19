package cn.dlut.core.main;

public class CoordinatorShutdownHook extends Thread {

    private final Coordinator coordinator;

    public CoordinatorShutdownHook(final Coordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        this.coordinator.terminate();
    }
}
