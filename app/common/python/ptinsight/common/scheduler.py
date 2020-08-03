import multiprocessing
import threading
import time
from multiprocessing import Process, Queue
from sched import scheduler


class MultithreadingScheduler:
    def __init__(self):
        self._scheduler = scheduler(time.perf_counter, time.sleep)
        self._done = threading.Event()
        self.worker = threading.Thread(target=self._run)
        self.t_start = time.perf_counter()

    def start(self):
        self.worker.start()

    def _run(self) -> None:
        """Custom run method which prevents scheduler from exiting early because no events are scheduled yet"""
        while not self._done.is_set():
            self._scheduler.run()
        # Clear queue after done is set
        self._scheduler.run()

    def schedule(self, t_offset, action):
        self._scheduler.enterabs(self.t_start + t_offset, 1, action)

    def get_queue_length(self):
        return len(self._scheduler.queue)

    def is_queue_full(self):
        return self.get_queue_length() >= 1000

    def stop(self):
        self._done.set()
        self.worker.join()


class MultiprocessingScheduler:
    def __init__(self):
        self.queue = Queue(1000)
        self.worker = _SchedulerWorker(self.queue)

    def start(self):
        self.worker.start()

    def schedule(self, t_offset, action):
        self.queue.put((t_offset, action))

    def get_queue_length(self):
        return self.queue.qsize()

    def is_queue_full(self):
        return self.queue.full()

    def stop(self):
        self.worker._done.set()
        self.worker.join()


class _SchedulerWorker(Process):
    def __init__(self, queue):
        self.queue = queue
        super(_SchedulerWorker, self).__init__()

        self._scheduler = scheduler(time.perf_counter, time.sleep)
        self._scheduler._lock = multiprocessing.RLock()

        self._done = multiprocessing.Event()

    def run(self) -> None:
        """Custom run method which prevents scheduler from exiting early because no events are scheduled yet"""
        t_start = time.perf_counter()

        while not self._done.is_set():
            while not self.queue.empty():
                t_offset, action = self.queue.get()
                self._scheduler.enterabs(t_start + t_offset, 1, action)
                print(f"add={t_start + t_offset}   now={time.perf_counter()}")
            self._scheduler.run()
        # Clear queue after done is set
        self._scheduler.run()
