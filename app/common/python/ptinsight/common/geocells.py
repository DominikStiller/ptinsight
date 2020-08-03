from typing import Tuple

import h3.api.basic_int as h3


class SpiralingGeocellGenerator:
    """
    A generator for H3 geocells spiraling outwards from an origin up to the max_k-th ring. After that, the generation
    starts again from the beginning. Multiple, mutually exclusive generators are supported by skipping rings.

    The order of geocells within a ring of distance k from the origin is arbitrary.

    The generation blocks without warning at some point, depending on the resolution. For a resolution of 9,
    multiple hundred million cells can be generated, so a max_k of 10000 is a good choice.

    This class uses iterators instead of generators to be compatible with multiprocessing.
    """

    def __init__(
        self, origin_cell: int, max_k: int, k_offset: int = 0, k_step: int = 1
    ):
        self.origin = origin_cell
        self.k_offset = k_offset
        self.k_step = k_step
        self.max_k = max_k

        self._k = k_offset
        self._current_ring = set()

    def __iter__(self):
        return self

    def __next__(self) -> int:
        if not self._current_ring:
            if self._k + self.k_step >= self.max_k:
                self._k = self.k_offset
            else:
                self._k += self.k_step
            self._current_ring = h3.hex_ring(self.origin, self._k)
        return self._current_ring.pop()


class SpiralingCoordinateGenerator(SpiralingGeocellGenerator):
    def __next__(self) -> Tuple[float, float]:
        return h3.h3_to_geo(super().__next__())
