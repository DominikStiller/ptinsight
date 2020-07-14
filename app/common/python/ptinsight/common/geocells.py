from typing import Tuple, Iterable

import h3.api.basic_int as h3


class SpiralingGeocellGenerator:
    """
    A generator for H3 geocells spiraling outwards from an origin

    The order of geocells within a ring of distance k from the origin is arbitrary.
    The generation blocks without warning at some point, depending on the resolution. For a resolution of 9,
    multiple hundred million cells can be generated.
    """

    def __init__(self, origin_cell: int, max_k: int):
        self.origin = origin_cell
        self.max_k = max_k

        self._k = 0
        self._current_ring = set()

    def cells(self) -> Iterable[int]:
        while True:
            if not self._current_ring:
                if self._k >= self.max_k:
                    return
                self._k += 1
                self._current_ring = h3.hex_ring(self.origin, self._k)
            yield self._current_ring.pop()

    def coordinates(self) -> Iterable[Tuple[float, float]]:
        for cell in self.cells():
            yield h3.h3_to_geo(cell)
