import bz2


def open_recording(file):
    body = file.get()["Body"]
    if file.key.endswith(".rec.bz2"):
        return body, _bz2_iter_lines(body)
    elif file.key.endswith(".rec"):
        return body, map(lambda l: l.decode(), body.iter_lines())
    else:
        raise ValueError("Unknown recording extension")


def _bz2_iter_lines(file):
    bz2_file = bz2.open(file)
    while line := bz2_file.readline():
        line = line.decode()
        if line.endswith("\r\n"):
            yield line[:-2]
        elif line.endswith("\n") or line.endswith("\r"):
            yield line[:-1]
